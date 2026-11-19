package ru.darkthrone.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.UUID;

/** Xard'Rot: 5000 HP, three phases (>60% / 20-60% / <20% of max health). */
public class XardrotEntity extends MonsterEntity {
    private static final DataParameter<Integer> PHASE = EntityDataManager.defineId(XardrotEntity.class, DataSerializers.INT);
    private static final UUID RAGE_MODIFIER_ID = UUID.fromString("c3b1a6e2-84d5-4f0a-9d7e-61a2b4c8e533");

    private final ServerBossInfo bossEvent = new ServerBossInfo(getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);

    public XardrotEntity(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
        this.xpReward = 500;
    }

    public static net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5000.0D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    private int computePhase() {
        float ratio = getHealth() / getMaxHealth();
        return ratio > 0.6f ? 1 : ratio > 0.2f ? 2 : 3;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayerEntity player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int phase = computePhase();
        if (phase != getPhase()) {
            this.entityData.set(PHASE, phase);
            if (phase == 3) applyRage();
        }
        this.bossEvent.setPercent(getHealth() / getMaxHealth());

        ServerWorld world = (ServerWorld) this.level;

        // Phase 1+: close the gap by teleporting next to a distant target.
        if (tickCount % 100 == 0 && getTarget() != null && distanceToSqr(getTarget()) > 36.0D) {
            randomTeleport(getTarget().getX() + random.nextGaussian() * 2.0D, getTarget().getY(),
                    getTarget().getZ() + random.nextGaussian() * 2.0D, true);
        }
        // Phase 2+: blinding presence.
        if (phase >= 2 && tickCount % 30 == 0) {
            for (PlayerEntity p : this.level.getEntitiesOfClass(PlayerEntity.class, getBoundingBox().inflate(10.0D))) {
                p.addEffect(new EffectInstance(Effects.BLINDNESS, 80, 0, true, false));
            }
        }
        // Phase 2+: devour a nearby mob to recover health.
        if (phase >= 2 && tickCount % 600 == 0) {
            List<MobEntity> prey = this.level.getEntitiesOfClass(MobEntity.class, getBoundingBox().inflate(12.0D),
                    e -> e != this && e.isAlive() && !(e instanceof XardrotEntity));
            if (!prey.isEmpty()) {
                prey.get(0).remove();
                heal(150.0F);
            }
        }
        if (phase == 3 && tickCount % 10 == 0) {
            world.sendParticles(ParticleTypes.DRAGON_BREATH, getX(), getY() + 1.0D, getZ(), 8, 1.2D, 1.0D, 1.2D, 0.01D);
        }
    }

    private void applyRage() {
        ModifiableAttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null && attack.getModifier(RAGE_MODIFIER_ID) == null) {
            attack.addTransientModifier(new AttributeModifier(RAGE_MODIFIER_ID, "xardrot_rage", 0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
