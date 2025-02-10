package net.lclmod.weapon_plus.client.render;

import com.mojang.math.Axis;
import net.lclmod.weapon_plus.entity.custom.IceBallEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class IceBallRenderer extends EntityRenderer<IceBallEntity> {

    // 自定义的冰球纹理路径
    private static final ResourceLocation ICEBALL_TEXTURE = new ResourceLocation("src/main/resources/essets/auperets/auperty/textures/entity/ece_ball.png");

    public IceBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(IceBallEntity entity) {
        // 返回自定义的冰球纹理路径
        return ICEBALL_TEXTURE;
    }

    @Override
    public void render(IceBallEntity entity, float entityYaw, float partial_ticks,
                       PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();

        // 设置实体的缩放大小
        matrixStack.scale(0.5F, 0.5F, 0.5F);

        // 旋转实体（使其看起来更自然）
        matrixStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        // 使用 Minecraft 的物品渲染器渲染雪球，使用自定义纹理
        Minecraft.getInstance().getItemRenderer().renderStatic(
                new ItemStack(Items.SNOWBALL), // 使用雪球物品作为占位符
                ItemDisplayContext.GROUND,     // 设置显示上下文为地面
                packedLight,                   // 光照强度
                OverlayTexture.NO_OVERLAY,     // 不使用覆盖纹理
                matrixStack,                   // 当前的姿态栈
                buffer,                        // 渲染缓冲区
                entity.level(),                // 实体所在的世界
                0                             // 随机种子（通常为0）
        );

        matrixStack.popPose();

        // 注意：不需要调用 super.render，否则可能会导致渲染问题
    }
}