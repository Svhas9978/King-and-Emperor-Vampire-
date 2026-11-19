package ru.darkthrone.gameplay;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import ru.darkthrone.registry.DTRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Server-side implementation of the three keybound abilities. */
public final class Abilities {
    private Abilities() {}

    public static final int PACK = 0;
    public static final int ROAR = 1;
    public static final int ECLIPSE = 2;

    private static final long PACK_COOLDOWN = 20L * 60;
    private static final long ROAR_COOLDOWN = 20L * 30;
    private static final long ECLIPSE_COOLDOWN = 20L * 120;

    private static final int MAX_WOLVES = 4;
    private static final int BATS = 6;
    private static final long SUMMON_LIFETIME = 20L * 60;

    private static final class Summon {
        final MobEntity entity;
        final UUID owner;
        final long expires;
        Summon(MobEntity entity, UUID owner, long expires) {
            this.entity = entity;
            this.owner = owner;
            this.expires = expires;
        }
    }

    private static final List<Summon> SUMMONS = new ArrayList<Summon>();

    public static void use(ServerPlayerEntity p, int id) {
        if (id == PACK) {
            if (!ThroneState.king(p)) { deny(p); return; }
            if (!ready(p, "pack", PACK_COOLDOWN)) return;
            bloodPack(p);
        } else if (id == ROAR) {
            if (!ThroneState.emperor(p)) { deny(p); return; }
            if (!ready(p, "roar", ROAR_COOLDOWN)) return;
            roar(p);
        } else if (id == ECLIPSE) {
            if (!ThroneState.emperor(p)) { deny(p); return; }
            if (!ready(p, "eclipse", ECLIPSE_COOLDOWN)) return;
            eclipse(p);
        }
    }

    private static void deny(ServerPlayerEntity p) {
        p.displayClientMessage(new TranslationTextComponent("darkthrone.msg.locked"), true);
    }

    private static boolean ready(ServerPlayerEntity p, String ability, long cooldown) {
        long now = p.level.getGameTime();
        long until = ThroneState.cooldownUntil(p, ability);
        if (now < until) {
            p.displayClientMessage(new TranslationTextComponent("darkthrone.msg.cooldown", (until - now) / 20 + 1), true);
            return false;
        }
        ThroneState.setCooldown(p, ability, now + cooldown);
        return true;
    }

    // ---- Blood pack -------------------------------------------------------

    private static void bloodPack(ServerPlayerEntity p) {
        ServerWorld w = p.getLevel();
        long now = w.getGameTime();

        // Dismiss the previous pack so summons never pile up.
        Iterator<Summon> it = SUMMONS.iterator();
        while (it.hasNext()) {
            Summon s = it.next();
            if (s.owner.equals(p.getUUID())) {
                s.entity.remove();
                it.remove();
            }
        }

        for (int i = 0; i < MAX_WOLVES; i++) {
            WolfEntity wolf = EntityType.WOLF.create(w);
            if (wolf == null) continue;
            wolf.moveTo(p.getX() + w.random.nextGaussian() * 2.5, p.getY(), p.getZ() + w.random.nextGaussian() * 2.5, p.yRot, 0f);
            wolf.tame(p);
            wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D);
            wolf.setHealth(40f);
            wolf.addEffect(new EffectInstance(Effects.GLOWING, (int) SUMMON_LIFETIME, 0, false, false));
            wolf.setPersistenceRequired();
            w.addFreshEntity(wolf);
            SUMMONS.add(new Summon(wolf, p.getUUID(), now + SUMMON_LIFETIME));
        }
        for (int i = 0; i < BATS; i++) {
            BatEntity bat = EntityType.BAT.create(w);
            if (bat == null) continue;
            bat.moveTo(p.getX() + w.random.nextGaussian() * 3, p.getY() + 2 + w.random.nextDouble() * 2, p.getZ() + w.random.nextGaussian() * 3, p.yRot, 0f);
            bat.setPersistenceRequired();
            w.addFreshEntity(bat);
            SUMMONS.add(new Summon(bat, p.getUUID(), now + SUMMON_LIFETIME));
        }
        w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WOLF_HOWL, SoundCategory.PLAYERS, 1.5f, 0.6f);
        w.sendParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.02);
    }

    /** Called once a second from the server tick. */
    public static void tickSummons(long gameTime) {
        Iterator<Summon> it = SUMMONS.iterator();
        while (it.hasNext()) {
            Summon s = it.next();
            if (!s.entity.isAlive()) {
                it.remove();
            } else if (gameTime >= s.expires) {
                s.entity.remove();
                it.remove();
            }
        }
    }

    // ---- Primordial roar --------------------------------------------------

    private static boolean isHostileTarget(PlayerEntity caster, LivingEntity e) {
        if (e == caster || !e.isAlive()) return false;
        if (e instanceof PlayerEntity || e instanceof ArmorStandEntity) return false;
        if (e instanceof TameableEntity && ((TameableEntity) e).isOwnedBy(caster)) return false;
        return true;
    }

    private static void roar(ServerPlayerEntity p) {
        ServerWorld w = p.getLevel();
        AxisAlignedBB box = p.getBoundingBox().inflate(15);
        float healed = 0f;
        for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, box, x -> isHostileTarget(p, x))) {
            float before = e.getHealth();
            e.hurt(DamageSource.playerAttack(p), 30f);
            healed += Math.max(0f, before - e.getHealth());
            double dx = e.getX() - p.getX();
            double dz = e.getZ() - p.getZ();
            double len = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            e.push(dx / len * 1.2D, 0.6D, dz / len * 1.2D);
            e.hurtMarked = true;
        }
        p.heal(Math.min(healed, 40f));
        p.addEffect(new EffectInstance(DTRegistry.PRIMORDIAL_FURY.get(), 20 * 10, 0));
        w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 2f, 0.7f);
        w.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1, p.getZ(), 80, 4, 1, 4, 0.05);
    }

    // ---- Blood eclipse ----------------------------------------------------

    private static void eclipse(ServerPlayerEntity p) {
        ServerWorld w = p.getLevel();
        if (w.dimension() == World.OVERWORLD) {
            long t = w.getDayTime();
            long target = t - Math.floorMod(t, 24000L) + 18000L;
            if (target < t) target += 24000L;
            w.setDayTime(target);
        }
        AxisAlignedBB box = p.getBoundingBox().inflate(25);
        for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, box, x -> x instanceof MobEntity && isHostileTarget(p, x))) {
            e.addEffect(new EffectInstance(Effects.BLINDNESS, 200, 0));
            e.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200, 4));
            e.addEffect(new EffectInstance(DTRegistry.BLOOD_ECLIPSE.get(), 200, 0));
        }
        w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WITHER_SPAWN, SoundCategory.PLAYERS, 1.2f, 0.5f);
        w.sendParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getY() + 2, p.getZ(), 120, 8, 3, 8, 0.02);
    }
}
