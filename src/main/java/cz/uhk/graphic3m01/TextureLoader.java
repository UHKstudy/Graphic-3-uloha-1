package cz.uhk.graphic3m01;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureLoader {

    public static int loadTexture(String resourcePath) {
        try (InputStream is = TextureLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Texture not found: " + resourcePath);
            }

            byte[] bytes = is.readAllBytes();
            ByteBuffer imageBuffer = BufferUtils.createByteBuffer(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();

            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);

            stbi_set_flip_vertically_on_load(false);

            ByteBuffer image = stbi_load_from_memory(imageBuffer, width, height, channels, 4);

            if (image == null) {
                throw new RuntimeException("Failed to load texture: " + stbi_failure_reason());
            }

            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    width.get(0),
                    height.get(0),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    image
            );

            glGenerateMipmap(GL_TEXTURE_2D);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            stbi_image_free(image);

            return textureId;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read texture: " + resourcePath, e);
        }
    }
}