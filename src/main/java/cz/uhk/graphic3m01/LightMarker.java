package cz.uhk.graphic3m01;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class LightMarker {

    private final int vaoId;
    private final int vboId;

    public LightMarker() {
        float[] vertices = {
                0.0f, 0.0f, 0.0f
        };

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawArrays(GL_POINTS, 0, 1);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}