package net.lclmod.weapon_plus.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class BlazeSword extends SwordItem {

    public BlazeSword() {
        super(Tiers.NETHERITE, 4, -2.4F, new Item.Properties());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 被动火焰攻击：当敌人攻击玩家时，使敌人着火
        if (attacker instanceof Player && target != attacker) {
            target.setSecondsOnFire(5);  // 使目标着火5秒
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {  // 确保火球只在服务器端发射
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            // 增加偏移距离，确保火球从玩家眼前较远的位置发射
            double offsetDistance = 2.0;  // 火球与玩家的距离

            // 计算火球的起始位置
            Vec3 startPos = eyePos.add(lookVec.x * offsetDistance,
                    lookVec.y * offsetDistance,
                    lookVec.z * offsetDistance);

            // 定义一个较大的运动向量，确保火球能够迅速远离玩家
            Vec3 motionVec = lookVec.scale(2.0);

            // 创建一颗烈焰弹
            LargeFireball fireball = new LargeFireball(level, player, motionVec.x, motionVec.y, motionVec.z, 3);  // 设置爆炸威力

            // 设置火球的起始位置
            fireball.setPos(startPos.x, startPos.y, startPos.z);

            // 设置火球的运动向量
            fireball.setDeltaMovement(motionVec);

            // 确保火球知道它的拥有者，这样它不会伤害到玩家
            fireball.setOwner(player);

            level.addFreshEntity(fireball);  // 将火球添加到世界中
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // 监听玩家是否持有BlazeSword并添加火焰免疫效果
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            // 检查玩家是否持有 BlazeSword
            if (player.getMainHandItem().getItem() instanceof BlazeSword || player.getOffhandItem().getItem() instanceof BlazeSword) {
                // 检查玩家是否已经有 FIRE_RESISTANCE 效果
                MobEffectInstance fireResistanceEffect = player.getEffect(MobEffects.FIRE_RESISTANCE);

                // 如果玩家没有 FIRE_RESISTANCE 效果，或者效果剩余时间小于等于5秒
                if (fireResistanceEffect == null || fireResistanceEffect.getDuration() <= 100) {
                    // 添加火焰抗性，持续时间为 100 ticks（5秒）
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, true));
                }
            }
        }
    }

    // 注册事件
    public static void register(IEventBus eventBus) {
        eventBus.addListener(BlazeSword::onLivingTick);
    }
}