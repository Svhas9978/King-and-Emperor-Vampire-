package ru.darkthrone.gameplay;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.darkthrone.DarkThrone;
import ru.darkthrone.entity.XardrotEntity;
import ru.darkthrone.registry.DTRegistry;

@Mod.EventBusSubscriber(modid = DarkThrone.MOD_ID)
public final class DarkThroneEvents {
    private DarkThroneEvents() {}

    // ---- promotion --------------------------------------------------------

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) checkPromotion((ServerPlayerEntity) e.getPlayer());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        ThroneState.copy(e.getOriginal(), e.getPlayer());
    }

    private static void checkPromotion(ServerPlayerEntity p) {
        if (ThroneState.get(p) != 0) return;
        if (VampireProgress.isVampireLord5(p) && VampireProgress.allVampireAdvancements(p)) {
            ThroneState.set(p, 1);
            p.displayClientMessage(new TranslationTextComponent("darkthrone.msg.king"), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level.isClientSide) return;
        ServerPlayerEntity p = (ServerPlayerEntity) e.player;
        if (p.tickCount % 40 == 0) checkPromotion(p);
        if (ThroneState.emperor(p)) {
            p.fallDistance = 0f;
            if (p.isOnFire()) p.clearFire();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        net.minecraft.server.MinecraftServer s = net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer();
        if (s != null && s.getTickCount() % 20 == 0) Abilities.tickSummons(s.overworld().getGameTime());
    }

    // ---- Emperor passives -------------------------------------------------

    /** Hostile mobs lose interest in an Emperor (bosses are exempt). */
    @SubscribeEvent
    public static void onSetTarget(LivingSetAttackTargetEvent e) {
        if (!(e.getTarget() instanceof ServerPlayerEntity) || !(e.getEntityLiving() instanceof MobEntity)) return;
        if (!ThroneState.emperor((ServerPlayerEntity) e.getTarget())) return;
        LivingEntity attacker = e.getEntityLiving();
        if (!(attacker instanceof IMob)) return;
        if (attacker instanceof XardrotEntity || attacker instanceof WitherEntity || attacker instanceof EnderDragonEntity) return;
        ((MobEntity) attacker).setTarget(null);
    }

    /**
     * Sun protection for the Emperor. Vampirism's exact sun damage / sun effects ids are not
     * verified against 1.9.10, so this matches by name ("sun") and is meant to be adjusted
     * once you see the real ids in game (/effect give @s vampirism:<TAB>).
     */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent e) {
        if (!(e.getEntityLiving() instanceof ServerPlayerEntity)) return;
        if (!ThroneState.emperor((ServerPlayerEntity) e.getEntityLiving())) return;
        String id = e.getSource().getMsgId();
        if (id != null && id.toLowerCase().contains("sun")) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEffectApplicable(PotionEvent.PotionApplicableEvent e) {
        if (!(e.getEntityLiving() instanceof ServerPlayerEntity)) return;
        if (!ThroneState.emperor((ServerPlayerEntity) e.getEntityLiving())) return;
        net.minecraft.potion.Effect effect = e.getPotionEffect().getEffect();
        net.minecraft.util.ResourceLocation key = effect.getRegistryName();
        if (key != null && "vampirism".equals(key.getNamespace()) && key.getPath().contains("sun")
                && effect.getCategory() == EffectType.HARMFUL) {
            e.setResult(Event.Result.DENY);
        }
    }

    /** Emperor: kills restore health and blood. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity p = (ServerPlayerEntity) e.getSource().getEntity();
        if (!ThroneState.emperor(p)) return;
        p.heal(e.getEntityLiving().getMaxHealth() * 0.2f);
        VampirismBridge.fillBlood(p, 4);
    }

    // ---- test / admin commands -------------------------------------------

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        CommandDispatcher<CommandSource> d = e.getDispatcher();
        d.register(Commands.literal("darkthrone").requires(s -> s.hasPermission(2))
                .then(Commands.literal("title")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 3))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "player");
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            ThroneState.set(target, level);
                                            ctx.getSource().sendSuccess(new StringTextComponent(
                                                    "Dark Throne: " + target.getName().getString() + " -> title " + level), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("boss").executes(ctx -> {
                    ServerWorld w = ctx.getSource().getLevel();
                    XardrotEntity boss = DTRegistry.XARDROT.get().create(w);
                    if (boss == null) return 0;
                    boss.moveTo(ctx.getSource().getPosition().x, ctx.getSource().getPosition().y, ctx.getSource().getPosition().z, 0f, 0f);
                    w.addFreshEntity(boss);
                    return 1;
                })));
    }
}
