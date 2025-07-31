package mods.flammpfeil.slashblade.client.core.obj;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mods.flammpfeil.slashblade.client.core.obj.event.ColorDynamicUpdater;
import mods.flammpfeil.slashblade.client.core.obj.event.ModelRenderer;
import mods.flammpfeil.slashblade.client.core.obj.event.UVDynamicUpdater;
import mods.flammpfeil.slashblade.client.core.obj.util.IrisUtils;
import mods.flammpfeil.slashblade.client.core.obj.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class GroupObject {
    public String name;
    public ArrayList<Face> faces = new ArrayList<Face>();
    public int glDrawingMode;
    public int vertexCount;

    public GroupObject() {
        this("");
    }

    public GroupObject(String name) {
        this(name, -1);
    }

    public GroupObject(String name, int glDrawingMode) {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

    protected int VAO;
    protected int VBO;
    protected int EBO;
    protected int indexCount;
    protected boolean initialized = false;
    protected ColorDynamicUpdater colorDynamicUpdater = new ColorDynamicUpdater();
    private FloatBuffer reusableColorBuffer;
    protected UVDynamicUpdater uvDynamicUpdater = new UVDynamicUpdater();
    private FloatBuffer reusableUVBuffer;

    public void init(){
        BufferBuilder bufferBuilder = new BufferBuilder(256);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
        // 向bufferBuilder输入顶点数据
        tessellate(bufferBuilder);

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        BufferBuilder.DrawState drawState = renderedBuffer.drawState();
        ByteBuffer vertexBuffer = renderedBuffer.vertexBuffer();

        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);

        VBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_STATIC_DRAW);
        drawState.format().setupBufferState();

        colorDynamicUpdater.initColorBuffer(vertexCount);
        reusableColorBuffer = ByteBuffer.allocateDirect(vertexCount * 4 * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        uvDynamicUpdater.initUVBuffer(vertexCount);
        reusableUVBuffer = ByteBuffer.allocateDirect(vertexCount * 2 * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        indexCount = drawState.indexCount();
        int newCapacity = Math.max(indexCount * 2, 65536);
        IntBuffer intBuffer = ByteBuffer.allocateDirect(newCapacity * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        for (int i = 0; i < indexCount; i++) {
            intBuffer.put(i, i);
        }

        intBuffer.position(0);
        intBuffer.limit(indexCount);
        EBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL30.GL_STATIC_DRAW);

        //初始化结束
        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
        initialized = true;
    }

    public void cleanup() {
        if (VAO != 0) GL30.glDeleteVertexArrays(VAO);
        if (VBO != 0) GL30.glDeleteBuffers(VBO);
        if (EBO != 0) GL30.glDeleteBuffers(EBO);
    }

    public static int lightmap = LightTexture.FULL_BRIGHT;
    public static void setLightMap(int value) {
        GroupObject.lightmap = value;
    }
    public static void resetLightMap() {
        GroupObject.lightmap = LightTexture.FULL_BRIGHT;
    }

    public static int overlaymap = OverlayTexture.NO_OVERLAY;
    public static void setOverlayMap(int value) {
        overlaymap = value;
    }
    public static void resetOverlayMap() {
        overlaymap = OverlayTexture.NO_OVERLAY;
    }

    public static Color col = Color.white;
    public static void setCol(Color col) {
        GroupObject.col = col;
    }
    public static void resetCol() {
        GroupObject.col = Color.white;
    }
    public static final BiFunction<Vector4f, Integer, Integer> alphaNoOverride = (v, a) -> a;
    public static final BiFunction<Vector4f, Integer, Integer> alphaOverrideYZZ = (v, a) -> v.y() == 0 ? 0 : a;
    public static BiFunction<Vector4f, Integer, Integer> alphaOverride = alphaNoOverride;
    public static void setAlphaOverride(BiFunction<Vector4f, Integer, Integer> alphaOverride) {
        GroupObject.alphaOverride = alphaOverride;
    }
    public static void resetAlphaOverride() {
        GroupObject.alphaOverride = alphaNoOverride;
    }

    public static final Vector4f uvDefaultOperator = new Vector4f(1, 1, 0, 0);
    public static Vector4f uvOperator = uvDefaultOperator;
    public static void setUvOperator(float uScale, float vScale, float uOffset, float vOffset) {
        GroupObject.uvOperator = new Vector4f(uScale, vScale, uOffset, vOffset);
    }
    public static void resetUvOperator() {
        GroupObject.uvOperator = uvDefaultOperator;
    }

    public static PoseStack matrix = null;
    public static void setMatrix(PoseStack ms) {
        GroupObject.matrix = ms;
    }
    public static void resetMatrix() {
        GroupObject.matrix = null;
    }

    public void render(RenderStateShard renderType) {
        if (!initialized) {
            init();
        }
        PoseStack poseStack = RenderUtils.copyPoseStack(matrix);
        int overlay = overlaymap;
        int light = lightmap;
        Color color = GroupObject.col;
        BiFunction<Vector4f, Integer, Integer> alphaOverride = GroupObject.alphaOverride;
        Vector4f nowUvOperator = uvOperator;
        ModelRenderer.addRenderCommand(renderType, () -> {
            if (poseStack == null) return;
            ShaderInstance shader = RenderSystem.getShader();
            int currentProgram = shader.getId();
            if (shader.MODEL_VIEW_MATRIX != null) {
                shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
            }
            int nm = GL20.glGetUniformLocation(currentProgram, "iris_NormalMat");
            if (nm >= 0) {
                Matrix3f normalMatrix = new Matrix3f(poseStack.last().normal());
                FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
                normalMatrix.get(buffer);
                GL20.glUniformMatrix3fv(nm, false, buffer);
            }
            shader.apply();
            GL30.glBindVertexArray(VAO);
            // 颜色
            reusableColorBuffer.clear();
            if (faces.size() > 0) {
                for (Face face : faces) {
                    face.calculateColor(reusableColorBuffer, color, alphaOverride);
                }
            }
            reusableColorBuffer.flip();
            colorDynamicUpdater.updateColors(reusableColorBuffer, 0, reusableColorBuffer.remaining()/4);
            colorDynamicUpdater.beforeRender();
            // uv
            reusableUVBuffer.clear();
            if (faces.size() > 0) {
                for (Face face : faces) {
                    face.calculateUV(reusableUVBuffer, nowUvOperator, 0.0005F);
                }
            }
            reusableUVBuffer.flip();
            uvDynamicUpdater.updateUVs(reusableUVBuffer, 0, reusableUVBuffer.remaining()/2);
            uvDynamicUpdater.beforeRender();
            // 光照和覆盖层
            GL30.glDisableVertexAttribArray(IrisUtils.vaUV1);
            GL30.glVertexAttribI2i(IrisUtils.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
            GL30.glDisableVertexAttribArray(IrisUtils.vaUV2);
            GL30.glVertexAttribI2i(IrisUtils.vaUV2, light & '\uffff', light >> 16 & '\uffff');

            //绘制
            GL30.glDrawElements(
                    GL30.GL_TRIANGLES,
                    indexCount,
                    GL30.GL_UNSIGNED_INT,
                    0
            );

            GL30.glBindVertexArray(0);
            shader.clear();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void tessellate(VertexConsumer tessellator) {
        if (faces.size() > 0) {
            for (Face face : faces) {
                face.tessellate(tessellator);
            }
        }
    }
}