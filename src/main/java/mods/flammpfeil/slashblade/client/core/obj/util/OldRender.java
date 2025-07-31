package mods.flammpfeil.slashblade.client.core.obj.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import mods.flammpfeil.slashblade.client.core.obj.model.GroupObject;
import mods.flammpfeil.slashblade.client.core.obj.model.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// 旧渲染，用于渲染图标顺带改进（加了批处理）
public class OldRender extends RenderStateShard {
    private static final Color defaultColor = Color.white;
    private static Color col = defaultColor;

    public OldRender(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
        super(p_110161_, p_110162_, p_110163_);
    }

    public static void setCol(int rgba) {
        setCol(rgba, true);
    }

    public static void setCol(int rgb, boolean hasAlpha) {
        setCol(new Color(rgb, hasAlpha));
    }

    public static void setCol(Color value) {
        col = value;
    }

    public static final int MAX_LIGHT = 15728864;

    public static void resetCol() {
        col = defaultColor;
    }

    static public void renderOverridedLuminous(ItemStack stack, WavefrontObject model, String target,
                                               ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                OldRender::getSlashBladeBlendLuminous, false);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture,
                                       PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn,
                packedLightIn, OldRender::getSlashBladeBlend, true);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture,
                                       PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
                                       Function<ResourceLocation, RenderType> getRenderType, boolean enableEffect) {
        RenderOverrideEvent event = RenderOverrideEvent.onRenderOverride(stack, model, target, texture, matrixStackIn,
                bufferIn);

        if (event.isCanceled())
            return;

        ResourceLocation loc = event.getTexture();

        RenderType rt = getRenderType.apply(loc);
        VertexConsumer vb = bufferIn.getBuffer(rt);//这行不要去掉，将 renderType 放入 MultiBufferSource 中，确保渲染将在 Iris 批量实体渲染中运行。

        GroupObject.setCol(col);
        GroupObject.setLightMap(packedLightIn);
        GroupObject.setMatrix(matrixStackIn);
        event.getModel().tessellateOnly(vb, event.getTarget());

        if (stack.hasFoil() && enableEffect) {
            rt = target.startsWith("item_") ?BladeRenderState.getSlashBladeItemGlint():BladeRenderState.getSlashBladeGlint();
            vb = bufferIn.getBuffer(rt);
            event.getModel().tessellateOnly(vb, event.getTarget());
        }

        GroupObject.resetMatrix();
        GroupObject.resetLightMap();
        GroupObject.resetCol();

        GroupObject.resetAlphaOverride();
        GroupObject.resetUvOperator();

        resetCol();
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "lightning_additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    // 使用缓存避免重复创建 RenderType 对象（用于批处理）
    private static final ConcurrentHashMap<ResourceLocation, RenderType> LUMINOUS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ResourceLocation, RenderType> BLEND_CACHE = new ConcurrentHashMap<>();

    public static RenderType getSlashBladeBlendLuminous(ResourceLocation texture) {
        return LUMINOUS_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, true, true))
                    .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);

            return RenderType.create(
                    "slashblade_blend_luminous",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.TRIANGLES,
                    256, false, true, state
            );
        });
    }

    public static RenderType getSlashBladeBlend(ResourceLocation texture) {
        return BLEND_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, true))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(true);

            return RenderType.create(
                    "slashblade_blend",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.TRIANGLES,
                    256, true, false, state
            );
        });
    }
}
