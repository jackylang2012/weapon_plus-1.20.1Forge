package net.lclmod.weapon_plus.entity;

import net.lclmod.weapon_plus.WeaponPlus;
import net.lclmod.weapon_plus.entity.custom.IceBallEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    // 注册实体类型
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WeaponPlus.MOD_ID);

    // 注册 IceBall 实体
    public static final RegistryObject<EntityType<IceBallEntity>> ICE_BALL = ENTITY_TYPES.register("ice_ball",
            () -> EntityType.Builder.<IceBallEntity>of(IceBallEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F) // 实体大小
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(":ice_ball"));


    // 注册实体方法
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}