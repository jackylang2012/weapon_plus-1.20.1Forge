package net.lclmod.weapon_plus.item;

import net.lclmod.weapon_plus.WeaponPlus;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WeaponPlus.MOD_ID);

    public static final RegistryObject<Item> BlAZE_SWORD = ITEMS.register("blaze_sword",
            () -> new BlazeSword());



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
