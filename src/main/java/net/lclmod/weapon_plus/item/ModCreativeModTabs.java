package net.lclmod.weapon_plus.item;

import net.lclmod.weapon_plus.WeaponPlus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WeaponPlus.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WEAPON_PLUS_TAB = CREATIVE_MODE_TABS.register("weapon_plus_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BLAZE_SWORD.get()))
                    .title(Component.translatable("creativetab.weapon_plus_tab"))
                    .displayItems((pParameters, pOutput) -> {


                        pOutput.accept(ModItems.BLAZE_SWORD.get());
                        pOutput.accept(ModItems.ICE_BALL.get());
                        pOutput.accept(ModItems.ICE_SWORD.get());
                        pOutput.accept(ModItems.GOLDEN_LUCK_SWORD.get());
                        pOutput.accept(ModItems.BLOOD_SWORD.get());
                        pOutput.accept(ModItems.EARTH_SWORD.get());


                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
