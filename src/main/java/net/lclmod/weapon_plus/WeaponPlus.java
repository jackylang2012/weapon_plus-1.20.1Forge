package net.lclmod.weapon_plus;

import com.mojang.logging.LogUtils;
import net.lclmod.weapon_plus.entity.ModEntities;
import net.lclmod.weapon_plus.item.custom.BlazeSword;
import net.lclmod.weapon_plus.item.ModCreativeModTabs;
import net.lclmod.weapon_plus.item.ModItems;
import net.lclmod.weapon_plus.network.NetworkHandler;
import net.minecraft.client.renderer.entity.EntityRenderers;
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

        // 注册创造模式标签
        ModCreativeModTabs.register(modEventBus);

        // 注册物品
        ModItems.register(modEventBus);

        // 注册实体
        ModEntities.register(modEventBus);

        // 注册通用设置
        modEventBus.addListener(this::commonSetup);

        // 注册事件总线
        MinecraftForge.EVENT_BUS.register(this);

        // 添加创造模式物品
        modEventBus.addListener(this::addCreative);

        // 注册 BlazeSword 的事件处理器
        BlazeSword.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register(); // 注册网络通信
            System.out.println("Network registered for mod " + MOD_ID); // 调试输出
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // 添加创造模式物品的逻辑
    }

    // 服务器启动时的事件
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting for mod {}", MOD_ID);
    }

    // 客户端设置
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