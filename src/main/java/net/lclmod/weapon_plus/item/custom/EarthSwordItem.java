package net.lclmod.weapon_plus.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "weapon_plus", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EarthSwordItem extends SwordItem {
    // 冷却时间
    private static final int BASE_COOLDOWN = 5 * 20;      // 5秒基础冷却
    private static final int SHIELD_COOLDOWN = 5 * 20;    // 5秒护盾冷却
    private static final long SLAM_DELAY_MS = 1000;       // 1秒震击间隔

    // 玩家状态存储
    private static final ConcurrentHashMap<UUID, Long> lastSlamTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> rightClickFlags = new ConcurrentHashMap<>();

    public EarthSwordItem(Tier tier, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            if (player.isCrouching()) {
                activateStoneShield(player);
                player.getCooldowns().addCooldown(this, SHIELD_COOLDOWN);
            } else {
                releaseShockwave(player);
                int cooldown = isOnSolidGround(player) ? BASE_COOLDOWN - (2 * 20) : BASE_COOLDOWN;
                player.getCooldowns().addCooldown(this, cooldown);
            }
        } else {
            if (player.isCrouching()) {
                spawnShieldParticles(world, player.blockPosition());
            } else {
                spawnShockwaveParticles(world, player.blockPosition());
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            triggerEarthEcho(attacker, target);
        } else {
            spawnEchoParticles(attacker.level(), target.blockPosition());
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof EarthSwordItem) {
            rightClickFlags.put(player.getUUID(), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof EarthSwordItem)) return;

        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        boolean canTrigger = event.getDistance() > 2.0f &&
                rightClickFlags.getOrDefault(playerId, false) &&
                (currentTime - lastSlamTimes.getOrDefault(playerId, 0L)) > SLAM_DELAY_MS;

        if (canTrigger) {
            event.setCanceled(true);
            triggerSeismicSlam(player);
            player.getCooldowns().addCooldown(mainHand.getItem(), BASE_COOLDOWN);

            lastSlamTimes.put(playerId, currentTime);
            rightClickFlags.put(playerId, false);
        } else {
            rightClickFlags.put(playerId, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        lastSlamTimes.remove(playerId);
        rightClickFlags.remove(playerId);
    }

    private static void triggerSeismicSlam(Player player) {
        Level world = player.level();
        BlockPos pos = player.blockPosition();

        // 伤害和击退
        world.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(7), e -> e != player)
                .forEach(entity -> {
                    entity.hurt(player.damageSources().playerAttack(player), 12.0f);
                    Vec3 knockback = new Vec3(
                            entity.getX() - player.getX(),
                            0.5,
                            entity.getZ() - player.getZ()
                    ).normalize().scale(3.0);
                    entity.setDeltaMovement(knockback);
                });

        // 音效
        world.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0f, 0.7f);
        world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.6f);

        // 特效
        if (world instanceof ServerLevel serverLevel) {
            // 核心冲击波
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    5, 0, 0, 0, 0);

            // 环形冲击波
            for (int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                double x = pos.getX() + 0.5 + Math.cos(rad) * 7;
                double z = pos.getZ() + 0.5 + Math.sin(rad) * 7;
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x, pos.getY(), z,
                        3, 0, 0.1, 0, 0.1);
            }

            // 岩石突刺效果
            for (int i = 0; i < 50; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2;
                double distance = world.random.nextDouble() * 6;
                double x = pos.getX() + 0.5 + Math.cos(angle) * distance;
                double z = pos.getZ() + 0.5 + Math.sin(angle) * distance;

                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        x, pos.getY(), z,
                        3, 0.2, 0.5, 0.2, 0.1
                );
            }
        }
    }

    private void releaseShockwave(Player player) {
        Level world = player.level();
        BlockPos pos = player.blockPosition();

        // 伤害和击退
        world.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(5), e -> e != player)
                .forEach(entity -> {
                    entity.hurt(player.damageSources().playerAttack(player), 8.0f);
                    entity.setDeltaMovement(
                            new Vec3(
                                    entity.getX() - player.getX(),
                                    1.0,
                                    entity.getZ() - player.getZ()
                            ).normalize().scale(2.0)
                    );
                });

        // 音效
        world.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5f, 0.8f);
        world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, 0.7f);

        if (world instanceof ServerLevel serverLevel) {
            // 冲击波核心
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX(), pos.getY(), pos.getZ(),
                    100, 3, 1, 3, 0.2);

            // 地面裂纹
            for(int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                double x = pos.getX() + 0.5 + Math.cos(rad) * 5;
                double z = pos.getZ() + 0.5 + Math.sin(rad) * 5;
                serverLevel.sendParticles(ParticleTypes.POOF,
                        x, pos.getY(), z,
                        5, 0.1, 0, 0.1, 0.02);
            }

            // 岩石碎片
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    pos.getX(), pos.getY() + 0.5, pos.getZ(),
                    50, 2, 0.5, 2, 0.2
            );
        }
    }

    private void activateStoneShield(Player player) {
        Level world = player.level();

        // 效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 3));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

        // 音效
        world.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.5f, 0.8f);
        world.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.5f);

        if (world instanceof ServerLevel serverLevel) {
            // 护盾旋转效果
            for(int i = 0; i < 360; i += 20) {
                double rad = Math.toRadians(i);
                double x = player.getX() + Math.cos(rad) * 2;
                double z = player.getZ() + Math.sin(rad) * 2;
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()),
                        x, player.getY() + 1.5, z,
                        3, 0, 0.5, 0, 0.1
                );
            }

            // 能量充能
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    50, 1, 1, 1, 0.5);

            // 地面震动
            serverLevel.sendParticles(ParticleTypes.EFFECT,
                    player.getX(), player.getY(), player.getZ(),
                    30, 2, 0.1, 2, 0.1);
        }
    }

    private void triggerEarthEcho(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof Player player)) return;

        Random random = new Random();
        boolean isOnGrassOrSand = isOnGrassOrSand(attacker);
        int chance = isOnGrassOrSand ? 20 : 10;

        if (random.nextInt(100) < chance) {
            target.hurt(attacker.damageSources().playerAttack(player), 5.0f);
            attacker.level().playSound(null, attacker.blockPosition(),
                    SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.2f, 1.5f);

            // 增强的回响特效
            if (attacker.level() instanceof ServerLevel serverLevel) {
                BlockPos pos = target.blockPosition();
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        10, 0.5, 0.5, 0.5, 0.2);

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        15, 0.3, 0.5, 0.3, 0.1);
            }
        }
    }

    // 辅助方法
    private boolean isOnSolidGround(Player player) {
        BlockPos pos = player.blockPosition().below();
        return player.level().getBlockState(pos).is(Blocks.STONE) ||
                player.level().getBlockState(pos).is(Blocks.DIRT);
    }

    private boolean isOnGrassOrSand(LivingEntity entity) {
        BlockPos pos = entity.blockPosition().below();
        return entity.level().getBlockState(pos).is(Blocks.GRASS_BLOCK) ||
                entity.level().getBlockState(pos).is(Blocks.SAND);
    }

    private void spawnShieldParticles(Level world, BlockPos pos) {
        if (world.isClientSide) {
            for (int i = 0; i < 50; i++) {
                world.addParticle(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        (world.random.nextDouble() - 0.5) * 0.5,
                        world.random.nextDouble() * 0.5,
                        (world.random.nextDouble() - 0.5) * 0.5);
            }
            for (int i = 0; i < 20; i++) {
                world.addParticle(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        (world.random.nextDouble() - 0.5) * 0.3,
                        world.random.nextDouble() * 0.3,
                        (world.random.nextDouble() - 0.5) * 0.3);
            }
        }
    }

    private void spawnShockwaveParticles(Level world, BlockPos pos) {
        if (world.isClientSide) {
            for (int i = 0; i < 100; i++) {
                world.addParticle(ParticleTypes.CLOUD,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        (world.random.nextDouble() - 0.5) * 2,
                        0.1,
                        (world.random.nextDouble() - 0.5) * 2);
            }
            for (int i = 0; i < 30; i++) {
                world.addParticle(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                        (world.random.nextDouble() - 0.5) * 1.5,
                        0.05,
                        (world.random.nextDouble() - 0.5) * 1.5);
            }
        }
    }

    private void spawnEchoParticles(Level world, BlockPos pos) {
        if (world.isClientSide) {
            for (int i = 0; i < 15; i++) {
                world.addParticle(ParticleTypes.NOTE,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        (world.random.nextDouble() - 0.5) * 0.2,
                        0.2,
                        (world.random.nextDouble() - 0.5) * 0.2);
            }
            world.addParticle(ParticleTypes.EXPLOSION,
                    pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                    0, 0, 0);
        }
    }
}