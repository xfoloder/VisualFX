package com.mojang.blaze3d.vertex;
import org.joml.Matrix4f;
public interface VertexConsumer {
 VertexConsumer addVertex(Matrix4f m,float x,float y,float z);
 VertexConsumer setColor(int color);
}
