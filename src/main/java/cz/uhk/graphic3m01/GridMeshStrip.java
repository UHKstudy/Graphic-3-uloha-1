package cz.uhk.graphic3m01;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

// Klasa tworzy grid podobnie jak GridMesh,
// ale używa trybu TRIANGLE_STRIP (ciąg trójkątów)
// zamiast pojedynczych trójkątów.
public class GridMeshStrip {

    private final int vaoId; // konfiguracja wierzchołków
    private final int vboId; // dane punktów (vertexy)
    private final int eboId; // indeksy (kolejność rysowania)
    private final int indexCount; // liczba indeksów

    public GridMeshStrip(int cols, int rows) {

        // =====
        // TWORZENIE PUNKTÓW (tak samo jak GridMesh)
        // ============
        int vertexCount = (cols + 1) * (rows + 1);
        float[] vertices = new float[vertexCount * 2];

        int v = 0;
        for (int y = 0; y <= rows; y++) {
            for (int x = 0; x <= cols; x++) {

                float px = -1.0f + 2.0f * x / cols;
                float py = -1.0f + 2.0f * y / rows;

                vertices[v++] = px;
                vertices[v++] = py;
            }
        }

        // ===
        // TWORZENIE INDEKSÓW (TRIANGLE STRIP)
        // ====

        // maksymalna liczba indeksów (trochę zapasu)
        int estimatedMax = rows * (2 * (cols + 1) + 2);
        int[] temp = new int[estimatedMax];

        int idx = 0;

        for (int y = 0; y < rows; y++) {

            // "połączenie" między paskami (degenerate triangle)
            if (y > 0) {
                temp[idx++] = y * (cols + 1);
            }

            for (int x = 0; x <= cols; x++) {

                // górny punkt
                temp[idx++] = y * (cols + 1) + x;

                // dolny punkt
                temp[idx++] = (y + 1) * (cols + 1) + x;
            }

            // zakończenie paska
            if (y < rows - 1) {
                temp[idx++] = (y + 1) * (cols + 1) + cols;
            }
        }

        // przepisanie do dokładnej tablicy
        int[] indices = new int[idx];
        System.arraycopy(temp, 0, indices, 0, idx);

        indexCount = indices.length;

        // =========
        // BUFFERY (jak wcześniej)
        // ===============
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        eboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

    // ===
    // RENDER (STRIP!)
    // ====
    public void render() {
        glBindVertexArray(vaoId);

        // UWAGA: tutaj różnica!
        glDrawElements(GL_TRIANGLE_STRIP, indexCount, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}