package net.lclmod.weapon_plus.item.custom;

import net.lclmod.weapon_plus.WeaponPlus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

public class GoldenLuckSwordItem extends SwordItem {

    // 冷却时间常量（单位：tick）
    private static final int ATTACK_COOLDOWN = 60;  // 3秒
    private static final int EXCHANGE_COOLDOWN = 100; // 5秒

    public GoldenLuckSwordItem() {
        super(Tiers.DIAMOND, 5, -2.4F, new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.literal("§6特殊能力:"));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("§7- 攻击冷却: §e3秒"));
            tooltip.add(Component.literal("§7- 物品交换冷却: §e5秒"));
            tooltip.add(Component.literal("§7- 击杀生物掉落金粒和金锭"));
            tooltip.add(Component.literal("§7- 右键消耗金锭交换物品"));
            tooltip.add(Component.literal("§7- 永久幸运III效果"));
        } else {
            tooltip.add(Component.literal("§7按住 §eSHIFT §7查看详情"));
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN);
                Events.spawnCooldownParticles(player, ParticleTypes.ANGRY_VILLAGER);
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (player.getInventory().contains(stack) && !player.hasEffect(MobEffects.LUCK)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 40, 2, true, false));
            }
        }
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    @Mod.EventBusSubscriber(modid = WeaponPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity entity = event.getEntity();
            Entity source = event.getSource().getEntity();

            if (source instanceof Player player && player.getMainHandItem().getItem() instanceof GoldenLuckSwordItem) {
                Level level = entity.level();

                if (!level.isClientSide()) {
                    level.addFreshEntity(new ItemEntity(level,
                            entity.getX(), entity.getY(), entity.getZ(),
                            new ItemStack(Items.GOLD_NUGGET, level.random.nextInt(10) + 1)));

                    if (level.random.nextFloat() < 0.5F) {
                        level.addFreshEntity(new ItemEntity(level,
                                entity.getX(), entity.getY(), entity.getZ(),
                                new ItemStack(Items.GOLD_INGOT, level.random.nextInt(6) + 1)));
                    }
                    if (level.random.nextFloat() < 0.1F) {
                        level.addFreshEntity(new ItemEntity(level,
                                entity.getX(), entity.getY(), entity.getZ(),
                                new ItemStack(Items.GOLDEN_APPLE, level.random.nextInt(2) + 1)));
                    }
                    if (level.random.nextFloat() < 0.01F) {
                        level.addFreshEntity(new ItemEntity(level,
                                entity.getX(), entity.getY(), entity.getZ(),
                                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, level.random.nextInt(1) + 1)));
                    }

                    player.displayClientMessage(
                            Component.translatable("item.weapon_plus.golden_luck_sword.gold_drop"),
                            true);
                }
            }
        }

        @SubscribeEvent
        public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof GoldenLuckSwordItem) {
                // 优先检查冷却
                if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                    Events.spawnCooldownParticles(player, ParticleTypes.ELECTRIC_SPARK);
                    event.setCanceled(true);
                    return;
                }

                Entity target = event.getTarget();
                if (target instanceof LivingEntity livingTarget) {
                    handleItemExchange(player, livingTarget, stack);
                }
            }
        }

        private static void handleItemExchange(Player player, LivingEntity target, ItemStack stack) {
            ItemStack targetItem = target.getMainHandItem();

            if (!targetItem.isEmpty()) {
                if (player.getInventory().contains(new ItemStack(Items.GOLD_INGOT))) {
                    // 执行交换逻辑
                    player.getInventory().clearOrCountMatchingItems(
                            item -> item.getItem() == Items.GOLD_INGOT,
                            1,
                            player.inventoryMenu.getCraftSlots()
                    );

                    player.getInventory().add(targetItem.copy());
                    targetItem.setCount(0);

                    // 显示成功消息
                    player.displayClientMessage(
                            Component.translatable("item.weapon_plus.golden_luck_sword.item_exchange"),
                            true);

                    // 设置冷却
                    player.getCooldowns().addCooldown(stack.getItem(), EXCHANGE_COOLDOWN);

                    // 反馈效果
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    spawnParticles(target);
                } else {
                    player.displayClientMessage(
                            Component.translatable("item.weapon_plus.golden_luck_sword.need_gold"),
                            true);
                }
            }
        }

        private static void spawnParticles(LivingEntity entity) {
            Level level = entity.level();
            for (int i = 0; i < 20; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
                double offsetY = level.random.nextDouble() * 2.0;
                double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

                level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        0.0D, 0.2D, 0.0D);
            }
        }

        static void spawnCooldownParticles(Player player, ParticleOptions type) {
            Level level = player.level();
            for (int i = 0; i < 15; i++) {
                double x = player.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                double y = player.getY() + 1.0 + level.random.nextDouble();
                double z = player.getZ() + (level.random.nextDouble() - 0.5) * 2.0;
                level.addParticle(type, x, y, z, 0, 0.1, 0);
            }
        }
    }
}