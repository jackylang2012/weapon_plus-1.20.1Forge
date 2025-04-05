package net.lclmod.weapon_plus.item.custom;

import net.lclmod.weapon_plus.WeaponPlus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nullable;
import java.util.List;

public class BlazeSword extends SwordItem {

    // 冷却时间常量（单位：tick）
    private static final int FIREBALL_COOLDOWN = 100; // 5秒

    public BlazeSword() {
        super(Tiers.NETHERITE, 6, -2.4F, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.literal("§6特殊能力:"));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("§7- 使目标着火§e5秒"));
            tooltip.add(Component.literal("§7- 右键发射火球（冷却§e5秒§7）"));
            tooltip.add(Component.literal("§7- 持有者获得火焰抗性"));
        } else {
            tooltip.add(Component.literal("§7按下 §eShift §7查看详细信息"));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player && target != attacker) {
            target.setSecondsOnFire(5);  // 使目标着火5秒
            if (attacker instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable("item.weapon_plus.blaze_sword.set_on_fire")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // 检查冷却
            if (player.getCooldowns().isOnCooldown(this)) {
                player.displayClientMessage(
                        Component.translatable("item.weapon_plus.blaze_sword.fireball_cooldown",
                                        String.format("%.1f", player.getCooldowns().getCooldownPercent(this, 0) * FIREBALL_COOLDOWN / 20.0f))
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }

            // 发射火球
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            double offsetDistance = 2.0;  // 火球与玩家的距离
            Vec3 startPos = eyePos.add(lookVec.x * offsetDistance,
                    lookVec.y * offsetDistance,
                    lookVec.z * offsetDistance);

            Vec3 motionVec = new Vec3(lookVec.x * 2.0,
                    lookVec.y * 2.0,
                    lookVec.z * 2.0);

            LargeFireball fireball = new LargeFireball(level, player,
                    lookVec.x * offsetDistance,
                    lookVec.y * offsetDistance,
                    lookVec.z * offsetDistance,
                    3);  // 设置爆炸威力

            fireball.setPos(startPos.x, startPos.y, startPos.z);
            fireball.setDeltaMovement(motionVec);
            fireball.setOwner(player);

            level.addFreshEntity(fireball);  // 将火球添加到世界中

            // 设置冷却
            player.getCooldowns().addCooldown(this, FIREBALL_COOLDOWN);

            // 提示信息
            player.displayClientMessage(
                    Component.translatable("item.weapon_plus.blaze_sword.fireball_launch")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );

            // 音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Mod.EventBusSubscriber(modid = WeaponPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof Player player) {
                if (hasBlazeSwordInInventory(player)) {
                    MobEffectInstance fireResistanceEffect = player.getEffect(MobEffects.FIRE_RESISTANCE);

                    if (fireResistanceEffect == null || fireResistanceEffect.getDuration() <= 100) {
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false));

                        // 首次获得效果时提示
                        if (fireResistanceEffect == null) {
                            player.displayClientMessage(
                                    Component.translatable("item.weapon_plus.blaze_sword.fire_resistance")
                                            .withStyle(ChatFormatting.GOLD),
                                    true
                            );
                        }
                    }
                }
            }
        }

        private static boolean hasBlazeSwordInInventory(Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlazeSword) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(BlazeSword::registerEvents);
    }

    private static void registerEvents(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(Events.class);
    }
}