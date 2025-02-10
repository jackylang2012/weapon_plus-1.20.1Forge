package net.lclmod.weapon_plus.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IceBallEntity extends ThrowableProjectile {

    private static final int EFFECT_RADIUS = 8;               // 冰霜效果范围
    private static final int EFFECT_DURATION = 300;           // 效果持续时间（15秒）
    private static final int PARTICLE_COUNT = 700;            // 每次生成的粒子数量
    private static final double PARTICLE_OFFSET = 5;        // 半球半径
    private static final int PARTICLE_DURATION = 15 * 20;     // 粒子效果持续15秒（300 ticks）
    private static final int PARTICLE_INTERVAL = 40;          // 每隔2秒生成一次粒子（40 ticks）


    private int particleTick = 0;
    private boolean hasHit = false; // 标志位，表示冰球是否已经击中目标

    public IceBallEntity(EntityType<? extends IceBallEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.5F, 0.8F);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.0F, 1.0F);

            // 施加效果
            applyEffects();
//            freezeGround(); // 冰冻地面
            hasHit = true; // 设置标志位
            this.discard(); // 移除实体
        }
    }


    private void applyEffects() {
        AABB area = new AABB(this.blockPosition()).inflate(EFFECT_RADIUS);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (!(entity instanceof Player)) { // 排除玩家
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION, 5)); // 霜冻效果
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION, 3)); // 虚弱效果
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION, 1)); // 凋零效果
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, EFFECT_DURATION, 255)); // 凋零效果
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        if (hasHit) {
            // 每隔一定 tick 生成粒子
            if (particleTick % PARTICLE_INTERVAL == 0) {
                spawnParticles((ServerLevel) this.level(), new Vec3(this.getX(), this.getY(), this.getZ()));
            }

            particleTick++;

            // 粒子效果持续 15 秒后停止
            if (particleTick >= PARTICLE_DURATION) {
                this.discard();
            }
        }
    }

    private void freezeGround() {
        AABB area = new AABB(this.blockPosition()).inflate(EFFECT_RADIUS);
        BlockPos.betweenClosedStream(area).forEach(pos -> {
            if (this.level().getBlockState(pos).canBeReplaced()) { // 修正：使用 canBeReplaced()
                this.level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            }
        });
    }


    private void spawnParticles(ServerLevel level, Vec3 center) {
        double minHeight = 0.5; // 最小高度

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double radius = Math.random() * PARTICLE_OFFSET;
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.random() * (Math.PI / 2);

            double xOffset = radius * Math.cos(theta) * Math.sin(phi);
            double yOffset = radius * Math.cos(phi) + minHeight;
            double zOffset = radius * Math.sin(theta) * Math.sin(phi);

            // 随机运动
            double motionX = (Math.random() - 0.5) * 0.2;
            double motionY = Math.random() * 0.2;
            double motionZ = (Math.random() - 0.5) * 0.2;

            // 生成多种粒子
            level.sendParticles(ParticleTypes.SNOWFLAKE, center.x + xOffset, center.y + yOffset, center.z + zOffset, 1, motionX, motionY, motionZ, 0.0);
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL, center.x + xOffset, center.y + yOffset, center.z + zOffset, 1, motionX, motionY, motionZ, 0.0);
            level.sendParticles(ParticleTypes.ENCHANT, center.x + xOffset, center.y + yOffset, center.z + zOffset, 1, motionX, motionY, motionZ, 0.0);
        }

        // 光效
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 10, 0.5, 0.5, 0.5, 0.0);
    }
}