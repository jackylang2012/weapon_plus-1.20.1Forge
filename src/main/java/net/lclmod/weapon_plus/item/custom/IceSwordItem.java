package net.lclmod.weapon_plus.item.custom;

import net.lclmod.weapon_plus.entity.custom.IceBallEntity;
import net.lclmod.weapon_plus.entity.ModEntities;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public class IceSwordItem extends SwordItem {

    public IceSwordItem() {
        super(Tiers.NETHERITE, 7, -2.4F, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 添加基础提示
        tooltip.add(Component.literal("§6特殊能力:"));

        // 检查是否按下 Shift 键
        if (Screen.hasShiftDown()) {
            // 按下 Shift 时显示的详细提示
            tooltip.add(Component.literal("§7- 右键发射一个超级冰球"));
            tooltip.add(Component.literal("§7- 持有者获得水下呼吸，抗性提升，海豚的恩惠"));
        } else {
            // 未按下 Shift 时显示的提示
            tooltip.add(Component.literal("§7按下 §eShift §7查看详细信息"));
        }
    }

    // 右键发射 IceBall
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            IceBallEntity iceBall = new IceBallEntity(ModEntities.ICE_BALL.get(), level);
            iceBall.setOwner(player);
            iceBall.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ()); // 设定生成位置
            iceBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.5F); // 设定方向与速度
            level.addFreshEntity(iceBall);

            // 播放音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 冷却时间（20 ticks = 1秒）
            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.success(stack);
    }

    // 当剑在背包中时给予玩家效果
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {

            // 检查玩家是否拥有 IceSword
            boolean hasItem = player.getInventory().items.stream().anyMatch(s -> s.getItem() instanceof IceSwordItem);
            if (hasItem) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
            }
        }
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

}