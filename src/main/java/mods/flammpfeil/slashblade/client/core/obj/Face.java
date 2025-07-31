package mods.flammpfeil.slashblade.client.core.obj;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Face {
    public Vertex[] vertices;
    public Vertex[] vertexNormals;
    public Vertex faceNormal;
    public TextureCoordinate[] textureCoordinates;
    public GroupObject parentGroup;

    public void calculateUV(FloatBuffer uv, Vector4f uvOperator, float textureOffset) {
        // 处理纹理坐标缺失情况
        if (textureCoordinates == null || textureCoordinates.length == 0) {
            for (int i = 0; i < vertices.length; i++) {
                uv.put(0).put(0);
            }
            return;
        }

        // 确保顶点与纹理坐标数量匹配
        int vertexCount = Math.min(vertices.length, textureCoordinates.length);

        // 计算平均UV坐标
        float averageU = 0f;
        float averageV = 0f;
        for (int i = 0; i < vertexCount; i++) {
            averageU += textureCoordinates[i].u * uvOperator.x() + uvOperator.z();
            averageV += textureCoordinates[i].v * uvOperator.y() + uvOperator.w();
        }
        averageU /= vertexCount;
        averageV /= vertexCount;

        // 预计算变换后的UV值并确定偏移方向
        float[] transformedU = new float[vertexCount];
        float[] transformedV = new float[vertexCount];
        boolean[] invertU = new boolean[vertexCount];
        boolean[] invertV = new boolean[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            transformedU[i] = textureCoordinates[i].u * uvOperator.x() + uvOperator.z();
            transformedV[i] = textureCoordinates[i].v * uvOperator.y() + uvOperator.w();
            invertU[i] = transformedU[i] > averageU;
            invertV[i] = transformedV[i] > averageV;
        }

        // 创建缓冲区并填充数据
        for (int i = 0; i < vertices.length; i++) {
            if (i < vertexCount) {
                float offsetU = invertU[i] ? -textureOffset : textureOffset;
                float offsetV = invertV[i] ? -textureOffset : textureOffset;
                uv.put(transformedU[i] + offsetU);
                uv.put(transformedV[i] + offsetV);
            } else {
                // 为缺少纹理坐标的顶点提供默认值
                uv.put(0).put(0);
            }
        }
    }

    public void calculateColor(FloatBuffer color, Color col, BiFunction<Vector4f, Integer, Integer> alphaOverride) {
        for (int i = 0; i < vertices.length; ++i) {
            color.put(col.getRed() / 255f);
            color.put(col.getGreen() / 255f);
            color.put(col.getBlue() / 255f);
            color.put( alphaOverride.apply(new Vector4f(vertices[i].x, vertices[i].y, vertices[i].z, 1.0F), col.getAlpha()) / 255f);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tessellate(VertexConsumer tessellator) {
        if (faceNormal == null) {
            faceNormal = this.calculateFaceNormal();
        }

        for (int i = 0; i < vertices.length; ++i) {
            putVertex(tessellator, i);
        }
    }

    void putVertex(VertexConsumer wr, int i) {
        wr.vertex(vertices[i].x, vertices[i].y, vertices[i].z);
        wr.color(255,255,255,255);
        wr.uv(0, 0);
        wr.overlayCoords(OverlayTexture.NO_OVERLAY);
        wr.uv2(LightTexture.FULL_BRIGHT);

        Vector3f vector3f;
        if (vertexNormals != null) {
            
        	Vertex normal = vertexNormals[i];
            
            vector3f = new Vector3f(normal.x, normal.y, normal.z);
        } else {
            vector3f = new Vector3f(faceNormal.x, faceNormal.y, faceNormal.z);
        }
        vector3f.normalize();
        wr.normal(vector3f.x(), vector3f.y(), vector3f.z());

        wr.endVertex();
    }

    public Vertex calculateFaceNormal() {
        Vec3 v1 = new Vec3(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vec3 v2 = new Vec3(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
        Vec3 normalVector = v1.cross(v2).normalize();

        return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
    }
}