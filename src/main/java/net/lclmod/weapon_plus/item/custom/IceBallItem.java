package net.lclmod.weapon_plus.item.custom;

import net.lclmod.weapon_plus.entity.custom.IceBallEntity;
import net.lclmod.weapon_plus.entity.ModEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IceBallItem extends Item {
    public IceBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 创建 IceBall 实体
            IceBallEntity iceBall = new IceBallEntity(ModEntities.ICE_BALL.get(), level);
            iceBall.setOwner(player);
            iceBall.setPos(player.getX(), player.getEyeY() - 0.1F, player.getZ());
            iceBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);

            // 添加实体到世界
            level.addFreshEntity(iceBall);

            // 如果玩家不是创造模式，消耗物品
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
