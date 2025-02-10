package net.lclmod.weapon_plus.item;

import net.lclmod.weapon_plus.WeaponPlus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class BlazeSword extends SwordItem {

    public BlazeSword() {
        super(Tiers.NETHERITE, 6, -2.4F, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 添加基础提示
        tooltip.add(Component.literal("§6特殊能力:"));

        // 检查是否按下 Shift 键
        if (Screen.hasShiftDown()) {
            // 按下 Shift 时显示的详细提示
            tooltip.add(Component.literal("§7- 使目标着火5秒"));
            tooltip.add(Component.literal("§7- 右键发射火球"));
            tooltip.add(Component.literal("§7- 持有者获得火焰抗性"));
        } else {
            // 未按下 Shift 时显示的提示
            tooltip.add(Component.literal("§7按下 §eShift §7查看详细信息"));
        }
    }
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
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
            // 冷却时间（20 ticks = 1秒）
            player.getCooldowns().addCooldown(this, 100);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // 静态内部类用于处理所有与 BlazeSword 相关的事件监听
    @Mod.EventBusSubscriber(modid = WeaponPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof Player player) {
                // 检查玩家的背包是否含有 BlazeSword
                if (hasBlazeSwordInInventory(player)) {
                    // 检查玩家是否已经有 FIRE_RESISTANCE 效果
                    MobEffectInstance fireResistanceEffect = player.getEffect(MobEffects.FIRE_RESISTANCE);

                    // 如果玩家没有 FIRE_RESISTANCE 效果，或者效果剩余时间小于等于5秒
                    if (fireResistanceEffect == null || fireResistanceEffect.getDuration() <= 100) {
                        // 添加火焰抗性，持续时间为 100 ticks（5秒）
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false));
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

    // 在模组初始化时注册事件处理器
    public static void register(IEventBus eventBus) {
        eventBus.addListener(BlazeSword::registerEvents);
    }

    private static void registerEvents(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(Events.class);
    }
}