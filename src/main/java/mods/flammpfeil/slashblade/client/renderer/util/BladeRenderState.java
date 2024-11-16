package mods.flammpfeil.slashblade.client.renderer.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.function.Function;

import net.minecraft.client.renderer.entity.ItemRenderer;
import org.lwjgl.opengl.GL20;

public class BladeRenderState extends RenderStateShard {

    private static final Color defaultColor = Color.white;
    private static Color col = defaultColor;

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

    public BladeRenderState(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture,
                                       PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn,
                packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlend), true);
    }

    static public void renderOverridedColorWrite(ItemStack stack, WavefrontObject model, String target,
                                                 ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendColorWrite), true);
    }

    static public void renderChargeEffect(ItemStack stack, float f, WavefrontObject model, String target,
                                          ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                (loc) -> BladeRenderState.getChargeEffect(loc, f * 0.1F % 1.0F, f * 0.01F % 1.0F), false);
    }

    static public void renderOverridedLuminous(ItemStack stack, WavefrontObject model, String target,
                                               ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendLuminous), false);
    }

    static public void renderOverridedLuminousDepthWrite(ItemStack stack, WavefrontObject model, String target,
                                                         ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendLuminousDepthWrite), false);
    }

    static public void renderOverridedReverseLuminous(ItemStack stack, WavefrontObject model, String target,
                                                      ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
            renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                    Util.memoize(BladeRenderState::getSlashBladeBlendReverseLuminous), false);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture,
                                       PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
                                       Function<ResourceLocation, RenderType> getRenderType, boolean enableEffect) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderOverrideEvent event = RenderOverrideEvent.onRenderOverride(stack, model, target, texture, matrixStackIn,
                bufferSource);

        if (event.isCanceled())
            return;

        ResourceLocation loc = event.getTexture();

        RenderType rt = getRenderType.apply(loc);// getSlashBladeBlendLuminous(event.getTexture());
        VertexConsumer vb = bufferSource.getBuffer(rt);


        Face.setCol(col);
        Face.setLightMap(packedLightIn);
        //Face.setMatrix(matrixStackIn);
        //添加顶点数据
        event.getModel().tessellateOnly(vb, event.getTarget());


        //获取当前modelViewMatrix对象
        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
        //获取变换矩阵
        Matrix4f poseMatrix = matrixStackIn.last().pose();
        //记录当前矩阵
        Matrix4f oldMatrix = new Matrix4f(modelViewMatrix);
        //将变换矩阵应用到模型视图矩阵中
        Matrix4f newMatrix = new Matrix4f(modelViewMatrix);
        newMatrix.mul(poseMatrix);
        modelViewMatrix.set(newMatrix);
        //添加完顶点数据后,直接结束批处理并开始渲染（主要是为了矩阵能够上传使用）
        bufferSource.endBatch(rt);


        //附魔效果的渲染
        if (stack.hasFoil() && enableEffect) {
            rt = BladeRenderState.getSlashBladeGlint();
            vb = bufferSource.getBuffer(rt);
            //event.getModel().tessellateOnly(vb, event.getTarget());
            //添加完顶点数据后,直接结束批处理并开始渲染（主要是为了矩阵能够上传使用）
            //bufferSource.endBatch(rt);
        }
        //渲染完成后还原矩阵
        modelViewMatrix.set(oldMatrix);

        Face.resetMatrix();
        Face.resetLightMap();
        Face.resetCol();

        Face.resetAlphaOverride();
        Face.resetUvOperator();

        resetCol();
    }

    public static VertexConsumer getBuffer(MultiBufferSource bufferIn, RenderType renderTypeIn, boolean glintIn) {
        return null;
    }

    public static final VertexFormat POSITION_TEX = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION).put("UV0", DefaultVertexFormat.ELEMENT_UV0).build());
    public static final RenderType BLADE_GLINT = RenderType.create("blade_glint", POSITION_TEX,
            VertexFormat.Mode.TRIANGLES, 256, false, false,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
                    .setTextureState(new TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
                    .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING)
                    .createCompositeState(false));

    public static RenderType getSlashBladeBlend(ResourceLocation p_228638_0_) {

        /*
         * RenderType.CompositeState rendertype$compositestate =
         * RenderType.CompositeState.builder()
         * .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER) .setTextureState(new
         * RenderStateShard.TextureStateShard(p_173200_, false, false))
         * .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         * .setOutputState(ITEM_ENTITY_TARGET) .setLightmapState(LIGHTMAP)
         * .setOverlayState(OVERLAY)
         * .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
         * .createCompositeState(true);
         */

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).createCompositeState(true);

        return RenderType.create("slashblade_blend", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeGlint() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ENTITY, true, true))
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .setDepthTestState(EQUAL_DEPTH_TEST)
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setTexturingState(ENTITY_GLINT_TEXTURING)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .createCompositeState(false);
        return RenderType.create("slashblade_glint", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeBlendColorWrite(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                .setOutputState(RenderStateShard.PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, true))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .createCompositeState(true);
        return RenderType.create("slashblade_blend_write_color", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderType getSlashBladeBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                //.setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                //该着色器无法正确处理lightmap，且无法兼容光影
                //.setOutputState(PARTICLES_TARGET)
                //该渲染写入粒子帧缓冲，鉴于帧缓冲主要用于后处理管线，渲染物品使用可能会使部分光影出现问题
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                //RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER监守者发光部分使用的着色器，支持lightmap,overlaymap
                .setOutputState(ITEM_ENTITY_TARGET)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, true))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getChargeEffect(ResourceLocation p_228638_0_, float x, float y) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, true))
                .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(x, y))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .setOverlayState(OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .createCompositeState(false);
        return RenderType.create("slashblade_charge_effect", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeBlendLuminousDepthWrite(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setOutputState(RenderStateShard.PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, true))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous_depth_write", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_REVERSE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
    }, () -> {
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderType getSlashBladeBlendReverseLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setOutputState(RenderStateShard.PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, true))
                .setTransparencyState(LIGHTNING_REVERSE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .createCompositeState(false);
        return RenderType.create("slashblade_blend_reverse_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

}
