package cz.uhk.graphic3m01;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

// Klasa ShaderProgram odpowiada za:
// - wczytanie shaderów z plików
// - kompilację shaderów
// - połączenie ich w program
// - ustawianie uniformów (danych dla GPU)
public class ShaderProgram {

    // ID programu shaderów w OpenGL
    private final int programId;

    // =============
    // KONSTRUKTOR
    // =
    public ShaderProgram(String vertexPath, String fragmentPath) {

        // wczytanie kodu shaderów z plików
        String vertexSource = loadResource(vertexPath);
        String fragmentSource = loadResource(fragmentPath);

        // kompilacja shaderów
        int vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);

        // utworzenie programu shaderów
        programId = glCreateProgram();

        // podpięcie shaderów
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);

        // linkowanie programu
        glLinkProgram(programId);

        // sprawdzenie czy linkowanie się udało
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program link failed:\n" + glGetProgramInfoLog(programId));
        }

        // po połączeniu shaderów można je usunąć
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    // =====
    // KOMPILACJA SHADERA
    // =====
    private int compileShader(String source, int type) {

        // tworzenie shadera (vertex lub fragment)
        int shaderId = glCreateShader(type);

        // przekazanie kodu
        glShaderSource(shaderId, source);

        // kompilacja
        glCompileShader(shaderId);

        // sprawdzenie błędów
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile failed:\n" + glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    // ========
    // WCZYTYWANIE PLIKU
    // ============
    private String loadResource(String path) {

        // pobranie pliku z resources
        InputStream is = ShaderProgram.class.getClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new RuntimeException("Resource not found: " + path);
        }

        StringBuilder sb = new StringBuilder();

        // czytanie pliku linia po linii
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }

        return sb.toString();
    }

    // ======
    // AKTYWACJA SHADERA
    // =
    public void use() {

        // ustawienie tego programu jako aktywnego
        glUseProgram(programId);
    }

    // =========
    // UNIFORMY
    // =============

    // macierz 4x4 (model, view, projection)
    public void setUniformMat4(String name, Matrix4f matrix) {

        int location = glGetUniformLocation(programId, name);
        if (location == -1) return;

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        matrix.get(fb);

        glUniformMatrix4fv(location, false, fb);
    }

    // wektor 3D (np. światło, kamera)
    public void setUniformVec3(String name, Vector3f value) {

        int location = glGetUniformLocation(programId, name);
        if (location == -1) return;

        glUniform3f(location, value.x, value.y, value.z);
    }

    // float (np. czas)
    public void setUniform1f(String name, float value) {

        int location = glGetUniformLocation(programId, name);
        if (location == -1) return;

        glUniform1f(location, value);
    }

    // int (np. debugMode, surfaceType)
    public void setUniform1i(String name, int value) {

        int location = glGetUniformLocation(programId, name);
        if (location == -1) return;

        glUniform1i(location, value);
    }

    // ===
    // USUWANIE
    // =========
    public void cleanup() {
        glDeleteProgram(programId);
    }
}