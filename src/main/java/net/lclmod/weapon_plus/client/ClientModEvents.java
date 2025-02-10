package net.lclmod.weapon_plus.client;

import net.lclmod.weapon_plus.WeaponPlus;
import net.lclmod.weapon_plus.entity.ModEntities;
import net.lclmod.weapon_plus.client.render.IceBallRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WeaponPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 直接注册渲染器事件
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册实体渲染器
        modEventBus.addListener(ClientModEvents::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册 IceBall 实体的渲染器
        event.registerEntityRenderer(ModEntities.ICE_BALL.get(), IceBallRenderer::new);
    }
}
