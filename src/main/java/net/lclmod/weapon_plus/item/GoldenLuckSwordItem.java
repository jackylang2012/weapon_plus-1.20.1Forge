package net.lclmod.weapon_plus.item;

import net.lclmod.weapon_plus.WeaponPlus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

public class GoldenLuckSwordItem extends SwordItem {

    public GoldenLuckSwordItem() {
        super(Tiers.DIAMOND, 5, -2.4F, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 添加基础提示
        tooltip.add(Component.literal("§6特殊能力:"));

        // 检查是否按下 Shift 键
        if (Screen.hasShiftDown()) {
            // 按下 Shift 时显示的详细提示
            tooltip.add(Component.literal("§7- 打死生物时额外掉落金粒和金锭"));
            tooltip.add(Component.literal("§7- 右键点击实体时消耗一个金锭，换取对方背包里的一个物品"));
            tooltip.add(Component.literal("§7- 持有者获得幸运 III 状态"));
        } else {
            // 未按下 Shift 时显示的提示
            tooltip.add(Component.literal("§7按下 §eShift §7查看详细信息"));
        }
    }

    // 功能 1：打死生物时额外掉落金粒和金锭
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity) {
            // 在生物死亡时触发额外掉落逻辑
            player.getCooldowns().addCooldown(this, 20); // 添加冷却时间
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    // 功能 2：放在背包时获得幸运 III 状态
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            // 检查剑是否在玩家的背包中
            if (player.getInventory().contains(stack)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 40, 2, true, false)); // 幸运 III
            }
        }
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    // 功能 3：右键点击实体时消耗一个金锭，换取对方背包里的一个物品
    @Mod.EventBusSubscriber(modid = WeaponPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity entity = event.getEntity();
            Entity source = event.getSource().getEntity();

            if (source instanceof Player player) {
                ItemStack stack = player.getMainHandItem();

                if (stack.getItem() instanceof GoldenLuckSwordItem) {
                    Level level = entity.level(); // 使用 getLevel() 方法

                    if (!level.isClientSide()) {
                        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                                new ItemStack(Items.GOLD_NUGGET, level.random.nextInt(10) + 1)));

                        if (level.random.nextFloat() < 0.5F) {
                            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                                    new ItemStack(Items.GOLD_INGOT, level.random.nextInt(6) + 1)));
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            ItemStack stack = player.getMainHandItem();

            // 检查主手或副手是否是 GoldenLuckSword
            if (player.getMainHandItem().getItem() instanceof GoldenLuckSwordItem || player.getOffhandItem().getItem() instanceof GoldenLuckSwordItem) {
                Entity target = event.getTarget();

                // 调试信息：输出是否触发了右键点击
                System.out.println("Right-click event triggered!");

                // 检查目标实体是否是玩家
                if (target instanceof Player targetPlayer) {
                    // 目标是玩家
                    System.out.println("Target is a Player!");

                    // 检查玩家背包中是否有金锭
                    if (player.getInventory().contains(new ItemStack(Items.GOLD_INGOT))) {
                        // 消耗一个金锭
                        player.getInventory().clearOrCountMatchingItems(item -> item.getItem() == Items.GOLD_INGOT, 1, player.inventoryMenu.getCraftSlots());

                        // 获取目标玩家手中的物品
                        ItemStack targetItemStack = targetPlayer.getMainHandItem();

                        if (!targetItemStack.isEmpty()) {
                            // 将目标玩家手中的物品转移到玩家背包
                            player.getInventory().add(targetItemStack.copy());
                            targetItemStack.setCount(0); // 清空目标玩家手中的物品
                            // 生成多个绿色粒子效果
                            for (int i = 0; i < 10; i++) { // 生成 10 个粒子
                                double offsetX = (target.level().random.nextDouble() - 0.5) * 0.5; // 左右偏移
                                double offsetY = target.level().random.nextDouble() * 1.5; // 高度偏移
                                double offsetZ = (target.level().random.nextDouble() - 0.5) * 0.5; // 前后偏移

                                target.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                                        target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ,
                                        0.0D, 0.1D, 0.0D);
                            }

                        }
                    }
                }
                // 检查目标是否是村民
                else if (target instanceof Villager targetVillager) {
                    ItemStack villagerItem = targetVillager.getMainHandItem();

                    // 目标是村民
                    System.out.println("Target is a Villager!");

                    // 检查村民是否有物品交换条件
                    // 如果村民手中没有物品，跳过后续操作
                    if (villagerItem.isEmpty()) {
                        return; // 不消耗金锭也不进行物品交换
                    }
                    if (player.getInventory().contains(new ItemStack(Items.GOLD_INGOT))) {
                        // 消耗一个金锭
                        player.getInventory().clearOrCountMatchingItems(item -> item.getItem() == Items.GOLD_INGOT, 1, player.inventoryMenu.getCraftSlots());

                        // 获取村民手中的物品
                        ItemStack targetItemStack = targetVillager.getMainHandItem();

                        if (!targetItemStack.isEmpty()) {
                            // 将村民手中的物品转移到玩家背包
                            player.getInventory().add(targetItemStack.copy());
                            // 生成多个绿色粒子效果
                            for (int i = 0; i < 10; i++) { // 生成 10 个粒子
                                double offsetX = (target.level().random.nextDouble() - 0.5) * 0.5; // 左右偏移
                                double offsetY = target.level().random.nextDouble() * 1.5; // 高度偏移
                                double offsetZ = (target.level().random.nextDouble() - 0.5) * 0.5; // 前后偏移

                                target.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                                        target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ,
                                        0.0D, 0.1D, 0.0D);
                            }

                        }
                    }
                }
                // 检查目标是否是其他生物（LivingEntity）且有手持物品
                else if (target instanceof LivingEntity targetLivingEntity) {
                    System.out.println("Target is a LivingEntity!");

                    // 获取目标生物手中的物品
                    ItemStack targetItemStack = targetLivingEntity.getMainHandItem();

                    if (!targetItemStack.isEmpty()) {
                        // 检查玩家是否有金锭
                        if (player.getInventory().contains(new ItemStack(Items.GOLD_INGOT))) {
                            // 消耗一个金锭
                            player.getInventory().clearOrCountMatchingItems(item -> item.getItem() == Items.GOLD_INGOT, 1, player.inventoryMenu.getCraftSlots());

                            // 将目标生物手中的物品转移到玩家背包
                            player.getInventory().add(targetItemStack.copy());
                            targetItemStack.setCount(0); // 清空目标生物手中的物品
                            // 生成多个绿色粒子效果
                            for (int i = 0; i < 10; i++) { // 生成 10 个粒子
                                double offsetX = (target.level().random.nextDouble() - 0.5) * 0.5; // 左右偏移
                                double offsetY = target.level().random.nextDouble() * 1.5; // 高度偏移
                                double offsetZ = (target.level().random.nextDouble() - 0.5) * 0.5; // 前后偏移

                                target.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                                        target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ,
                                        0.0D, 0.1D, 0.0D);
                            }

                        }
                    }
                } else {
                    // 如果目标不是玩家、村民或其他具有物品的生物
                    System.out.println("Target does not have a usable inventory or item.");
                }
            }
        }
    }
}
