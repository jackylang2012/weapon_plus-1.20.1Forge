package net.lclmod.weapon_plus.item.custom;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class BloodSword extends SwordItem {

    public BloodSword(Tier tier, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.literal("§6特殊能力:"));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("§7- 吸血技能 (Vampiric Strike) - 20% 概率恢复敌人最大生命值的 20%"));
            tooltip.add(Component.literal("§7- 血之怒 (Blood Rage): 当玩家生命值低于 30% 时，剑的攻击力增加 50%"));
            tooltip.add(Component.literal("§7- 血祭技能 (Blood Sacrifice): 玩家可以主动消耗 10% 的生命值来使剑的攻击力翻倍 10 秒"));
            tooltip.add(Component.literal("§7- 血腥爆炸 (Blood Explosion): 当玩家击杀敌人时，有几率引发一个小范围的血腥爆炸，造成周围敌人伤害并恢复玩家生命值。"));
            tooltip.add(Component.literal("§7- 诅咒效果 (Curse of the Blood): 每次击中敌人时，敌人会受到减速和凋零效果。"));
        } else {
            tooltip.add(Component.literal("§7按下 §eShift §7查看详细信息"));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player) {
            Player player = (Player) attacker;

            // 计算伤害加成
            float damageBonus = stack.getOrCreateTag().getFloat("AttackDamageBonus");
            float baseDamage = getDamage();
            float totalDamage = baseDamage * (1 + damageBonus);

            target.hurt(player.damageSources().playerAttack(player), totalDamage);

            // 吸血技能
            if (player.level().random.nextFloat() < 0.2f) {
                float healAmount = target.getMaxHealth() * 0.2f;
                player.heal(healAmount);
                player.displayClientMessage(Component.translatable("item.weapon_plus.blood_sword.vampiric_strike"), true);
                spawnHealingParticles(player.level(), target.getX(), target.getY(), target.getZ());
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            // 血之怒激活
            if (player.getHealth() < player.getMaxHealth() * 0.3f) {
                boolean wasActive = stack.getOrCreateTag().getBoolean("BloodRageActive");
                if (!wasActive) {
                    player.displayClientMessage(
                            Component.translatable("item.weapon_plus.blood_sword.blood_rage_activate"),
                            true
                    );
                }
                stack.getOrCreateTag().putBoolean("BloodRageActive", true);
                stack.getOrCreateTag().putLong("BloodRageEndTime", player.level().getGameTime() + 200);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            // 诅咒效果
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            spawnCurseParticles(player.level(), target.getX(), target.getY(), target.getZ());

            // 血腥爆炸
            if (target.getHealth() <= 0 && player.getRandom().nextFloat() < 0.3f) {
                explodeBlood(player.level(), target.getX(), target.getY(), target.getZ(), player);
                player.displayClientMessage(
                        Component.translatable("item.weapon_plus.blood_sword.blood_explosion"),
                        true
                );
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private void explodeBlood(Level level, double x, double y, double z, Player player) {
        if (!level.isClientSide) {
            ExplosionDamageCalculator damageCalculator = new ExplosionDamageCalculator() {
                @Override
                public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
                    return false;
                }
            };

            Explosion explosion = level.explode(player, x, y, z, 2.0F, false, Level.ExplosionInteraction.NONE);

            AABB area = new AABB(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                if (entity != player && entity.isAlive()) {
                    entity.hurt(level.damageSources().explosion(explosion), 5.0F);
                }
            }

            player.heal(4.0F);

            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                for (int i = 0; i < 20; i++) {
                    double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
                    double offsetY = (level.random.nextDouble() - 0.5) * 2.0;
                    double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
                    serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                            x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0);
                }
            }

            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }



    private void spawnHealingParticles(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            for (int i = 0; i < 10; i++) {
                double offsetX = (level.random.nextDouble() - 0.5);
                double offsetY = (level.random.nextDouble() - 0.5);
                double offsetZ = (level.random.nextDouble() - 0.5);
                serverLevel.sendParticles(ParticleTypes.HEART, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnCurseParticles(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            for (int i = 0; i < 10; i++) {
                double offsetX = (level.random.nextDouble() - 0.5);
                double offsetY = (level.random.nextDouble() - 0.5);
                double offsetZ = (level.random.nextDouble() - 0.5);
                serverLevel.sendParticles(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected); // 调用父类方法
        if (entity instanceof Player) {
            Player player = (Player) entity;

            // 血祭技能 (Blood Sacrifice)
            if (stack.getOrCreateTag().getBoolean("BloodSacrificeActive")) {
                player.hurt(player.damageSources().magic(), player.getMaxHealth() * 0.3f);
                stack.getOrCreateTag().putBoolean("BloodSacrificeActive", false);
                stack.getOrCreateTag().putLong("BloodSacrificeEndTime", level.getGameTime() + 200); // 10 seconds
            }

            // 血之怒持续时间
            if (stack.getOrCreateTag().getBoolean("BloodRageActive") &&
                    level.getGameTime() > stack.getOrCreateTag().getLong("BloodRageEndTime")) {
                stack.getOrCreateTag().putBoolean("BloodRageActive", false);
            }

            // 血祭技能攻击力提升
            if (level.getGameTime() >= stack.getOrCreateTag().getLong("BloodSacrificeEndTime")) {
                stack.getOrCreateTag().remove("BloodSacrificeEndTime");
            }

            // 血之怒攻击力提升
            if (stack.getOrCreateTag().getBoolean("BloodRageActive")) {
                stack.getOrCreateTag().putFloat("AttackDamageBonus", 0.5f);
            } else {
                stack.getOrCreateTag().putFloat("AttackDamageBonus", 0.0f);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getHealth() > player.getMaxHealth() * 0.1f) {
            stack.getOrCreateTag().putBoolean("BloodSacrificeActive", true);
            stack.getOrCreateTag().putLong("BloodSacrificeEndTime", level.getGameTime() + 200);

            player.displayClientMessage(
                    Component.translatable("item.weapon_plus.blood_sword.blood_sacrifice_activate"),
                    true
            );

            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                for (int i = 0; i < 20; i++) {
                    double x = player.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                    double y = player.getY() + 1.0 + (level.random.nextDouble() - 0.5) * 2.0;
                    double z = player.getZ() + (level.random.nextDouble() - 0.5) * 2.0;
                    serverLevel.sendParticles(ParticleTypes.HEART, x, y, z, 1, 0, 0, 0, 0);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.0F);

            return InteractionResultHolder.success(stack);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        long currentTime = System.currentTimeMillis();
        boolean isBloodRageActive = stack.getOrCreateTag().getBoolean("BloodRageActive");

        // 检查 BloodSacrifice 是否过期
        boolean isBloodSacrificeActive = stack.getOrCreateTag().contains("BloodSacrificeEndTime") &&
                stack.getOrCreateTag().getLong("BloodSacrificeEndTime") > currentTime;

        // 如果 BloodSacrifice 已经过期，则移除该标记
        if (!isBloodSacrificeActive) {
            stack.getOrCreateTag().remove("BloodSacrificeEndTime");
        }

        return isBloodRageActive || isBloodSacrificeActive;
    }
}