package net.lclmod.weapon_plus;

import com.mojang.logging.LogUtils;
import net.lclmod.weapon_plus.entity.ModEntities;
import net.lclmod.weapon_plus.item.BlazeSword;
import net.lclmod.weapon_plus.item.ModCreativeModTabs;
import net.lclmod.weapon_plus.item.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.lclmod.weapon_plus.client.render.IceBallRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WeaponPlus.MOD_ID)
public class WeaponPlus {
    public static final String MOD_ID = "weapon_plus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WeaponPlus() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        // 注册 BlazeSword 的事件处理器
        BlazeSword.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 注册 IceBall 实体的渲染器
            EntityRenderers.register(ModEntities.ICE_BALL.get(), IceBallRenderer::new);
            LOGGER.info("Client setup for mod {}", MOD_ID);

        }
    }
}
