package net.lclmod.weapon_plus.item;

import net.lclmod.weapon_plus.WeaponPlus;
import net.lclmod.weapon_plus.item.custom.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // 注册物品
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WeaponPlus.MOD_ID);

    // 注册实体类型
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WeaponPlus.MOD_ID);

    // 注册 Blaze Sword
    public static final RegistryObject<Item> BLAZE_SWORD = ITEMS.register("blaze_sword",
            BlazeSword::new);

    // 注册 IceBall 实体
    public static final RegistryObject<Item> ICE_BALL = ITEMS.register("ice_ball",
            () -> new IceBallItem(new Item.Properties().stacksTo(16))); // 使用自定义的 IceBallItem
    // 注册 Blaze Sword
    public static final RegistryObject<Item> ICE_SWORD = ITEMS.register("ice_sword",
            IceSwordItem::new);
    public static final RegistryObject<Item> GOLDEN_LUCK_SWORD = ITEMS.register("golden_luck_sword",
            () -> new GoldenLuckSwordItem());
    public static final RegistryObject<Item> BLOOD_SWORD = ITEMS.register("blood_sword",
            () -> new BloodSword(Tiers.NETHERITE, 5, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> EARTH_SWORD = ITEMS.register("earth_sword",
            () -> new EarthSwordItem(Tiers.NETHERITE, 7, -2.4F, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));


    // 注册所有内容
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);       // 注册物品
        ENTITY_TYPES.register(eventBus); // 注册实体类型
    }
}
