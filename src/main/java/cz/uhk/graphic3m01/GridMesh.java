package cz.uhk.graphic3m01;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

// Klasa tworzy siatkę punktów 2D (grid)
// i łączy je w trójkąty za pomocą indeksów.
// Ta geometria jest wejściem do vertex shadera.
public class GridMesh {

    // VAO - przechowuje konfigurację atrybutów wierzchołków
    private final int vaoId;

    // VBO - przechowuje dane wierzchołków (pozycje punktów)
    private final int vboId;

    // EBO - przechowuje indeksy trójkątów
    private final int eboId;

    // liczba indeksów do narysowania
    private final int indexCount;

    public GridMesh(int cols, int rows) {

        // liczba punktów siatki
        // np. dla 2x2 komórek mamy 3x3 punkty
        int vertexCount = (cols + 1) * (rows + 1);

        // każdy punkt ma 2 współrzędne: x i y
        float[] vertices = new float[vertexCount * 2];

        // ==================
        // TWORZENIE PUNKTÓW GRIDU
        // ==
        int v = 0;
        for (int y = 0; y <= rows; y++) {
            for (int x = 0; x <= cols; x++) {

                // przeskalowanie punktów do zakresu <-1, 1>
                // dzięki temu grid pokrywa cały obszar wejściowy
                float px = -1.0f + 2.0f * x / cols;
                float py = -1.0f + 2.0f * y / rows;

                vertices[v++] = px;
                vertices[v++] = py;
            }
        }

        // ======
        // TWORZENIE INDEKSÓW
        // =========
        // Każda komórka siatki = 2 trójkąty
        // Każdy trójkąt = 3 indeksy
        // Czyli 1 komórka = 6 indeksów
        int[] indices = new int[cols * rows * 6];

        int i = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                // indeksy 4 narożników pojedynczej komórki
                int topLeft = y * (cols + 1) + x;
                int topRight = topLeft + 1;
                int bottomLeft = (y + 1) * (cols + 1) + x;
                int bottomRight = bottomLeft + 1;

                // pierwszy trójkąt
                indices[i++] = topLeft;
                indices[i++] = bottomLeft;
                indices[i++] = topRight;

                // drugi trójkąt
                indices[i++] = topRight;
                indices[i++] = bottomLeft;
                indices[i++] = bottomRight;
            }
        }

        // zapamiętanie liczby indeksów
        indexCount = indices.length;

        // ======
        // KONWERSJA DO BUFFERÓW
        // ========
        // OpenGL nie czyta zwykłych tablic Java,
        // dlatego trzeba przygotować FloatBuffer i IntBuffer

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        // ===========
        // TWORZENIE OBIEKTÓW OPENGL
        // ===
        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        eboId = glGenBuffers();

        // aktywacja VAO
        glBindVertexArray(vaoId);

        // przesłanie punktów do GPU
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // przesłanie indeksów do GPU
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // informacja dla OpenGL:
        // atrybut 0 = pozycja
        // 2 liczby float na wierzchołek (x, y)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // odpięcie VAO
        glBindVertexArray(0);
    }

    // rysowanie siatki jako listy trójkątów
    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // usuwanie danych z GPU
    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}