package cz.uhk.graphic3m01;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;


// Klasa Renderer odpowiada za rysowanie całej sceny.
// Ustawia shader, kamerę, światło, projekcję
// i rysuje dwa obiekty parametryczne.
public class Renderer {

    // program shaderów (vertex + fragment shader)
    private ShaderProgram shaderProgram;

    // siatka w trybie triangle list
    private GridMesh gridMeshList;

    // siatka w trybie triangle strip
    private GridMeshStrip gridMeshStrip;

    // czas używany do animacji powierzchni
    private float time = 0.0f;

    // kamera sceny
    private final Camera camera = new Camera();

    // typ pierwszego obiektu
    private SurfaceType currentSurfaceA = SurfaceType.SPHERE;

    private SurfaceType currentSurfaceB = SurfaceType.CARTESIAN_WAVE;

    // aktualny tryb rysowania: punkty / linie / wypełnienie
    private RenderMode currentRenderMode = RenderMode.FILL;

    // aktualny typ projekcji
    private ProjectionType currentProjection = ProjectionType.PERSPECTIVE;

    // aktualny typ siatki
    private MeshType currentMeshType = MeshType.TRIANGLE_LIST;

    // pozycja światła w scenie
    private final Vector3f lightPos = new Vector3f(3.0f, 3.0f, 4.0f);

    // kolor światła
    private final Vector3f lightColor = new Vector3f(1.8f, 1.8f, 2.4f);

    // // aktualny tryb debugowania obrazu, przelaczany z klawiatury
    private DebugMode debugMode = DebugMode.FINAL;

    // identyfikatory tekstur uzywanych dla dwoch obiektów sceny
    private int globeTextureId;
    private int moonTextureId;

    private final Vector3f spotlightDirection = new Vector3f(-0.5f, -0.6f, -1.0f).normalize();
    private float spotlightInnerCutoff = (float) Math.cos(Math.toRadians(22.0f));
    private float spotlightOuterCutoff = (float) Math.cos(Math.toRadians(35.0f));

    private ShaderProgram lightMarkerShader;
    private LightMarker lightMarker;


    // ========
    // INICJALIZACJA RENDERERA
    // =
    public void init() {

        // tworzenie programu shaderów
        shaderProgram = new ShaderProgram(
                "shaders/grid.vert",
                "shaders/grid.frag"
        );

        // tworzenie dwóch wersji siatki:
        // list i strip
        gridMeshList = new GridMesh(140, 140);
        gridMeshStrip = new GridMeshStrip(140, 140);

        globeTextureId = TextureLoader.loadTexture("textures/globe.jpg");
        moonTextureId = TextureLoader.loadTexture("textures/moon.jpg");


        lightMarkerShader = new ShaderProgram(
                "shaders/light_marker.vert",
                "shaders/light_marker.frag"
        );

        lightMarker = new LightMarker();

    }



    // zwraca kamerę (potrzebne do sterowania z App.java)
    public Camera getCamera() {
        return camera;
    }

    // ustawienie typu pierwszego obiektu
    public void setSurfaceTypeA(SurfaceType surfaceType) {
        this.currentSurfaceA = surfaceType;
    }

    // ustawienie typu drugiego obiektu
    public void setSurfaceTypeB(SurfaceType surfaceType) {
        this.currentSurfaceB = surfaceType;
    }

    // ustawienie trybu renderowania
    public void setRenderMode(RenderMode renderMode) {
        this.currentRenderMode = renderMode;
    }

    // ustawienie projekcji
    public void setProjectionType(ProjectionType projectionType) {
        this.currentProjection = projectionType;
    }

    // ustawienie typu siatki
    public void setMeshType(MeshType meshType) {
        this.currentMeshType = meshType;
    }

    // przesuwanie światła w scenie
    public void moveLight(float dx, float dy, float dz) {
        lightPos.add(dx, dy, dz);
    }

    // ustawienie trybu debugowania
    public void setDebugMode(DebugMode mode) {
        this.debugMode = mode;
    }

    // ========
    // GŁÓWNE RYSOWANIE SCENY
    // ==
    public void render(int width, int height) {

        // zwiększanie czasu – dzięki temu powierzchnie mogą się animować
        time += 0.01f;

        // pobranie macierzy widoku z kamery
        Matrix4f view = camera.getViewMatrix();

        // proporcje okna
        float aspect = (float) width / height;

        // macierz projekcji
        Matrix4f projection;

        // wybór rodzaju projekcji
        if (currentProjection == ProjectionType.PERSPECTIVE) {
            // projekcja perspektywiczna
            projection = new Matrix4f()
                    .perspective((float) Math.toRadians(60.0f), aspect, 0.1f, 100.0f);
        } else {
            // projekcja ortogonalna
            float orthoScale = camera.getOrthoScale();
            projection = new Matrix4f()
                    .ortho(-orthoScale * aspect, orthoScale * aspect,
                            -orthoScale, orthoScale,
                            0.1f, 100.0f);
        }

        // aktywacja programu shaderów
        shaderProgram.use();

        // przekazanie danych do shaderów przez uniformy
        shaderProgram.setUniformMat4("view", view);
        shaderProgram.setUniformMat4("projection", projection);
        shaderProgram.setUniform1f("time", time);
        shaderProgram.setUniformVec3("lightPos", lightPos);
        shaderProgram.setUniformVec3("lightColor", lightColor);
        shaderProgram.setUniformVec3("viewPos", camera.getPosition());

        shaderProgram.setUniformVec3("spotlightDirection", spotlightDirection);
        shaderProgram.setUniform1f("spotlightInnerCutoff", spotlightInnerCutoff);
        shaderProgram.setUniform1f("spotlightOuterCutoff", spotlightOuterCutoff);




        // ustawienie trybu debugowania w shaderze 7890
        shaderProgram.setUniform1i("debugMode", debugMode.ordinal());

        shaderProgram.setUniform1i("surfaceTexture", 0);


        // wybór trybu rysowania
        switch (currentRenderMode) {
            case POINTS -> {
                GL11.glPointSize(3.0f);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT);
            }
            case WIREFRAME -> GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            case FILL -> GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }



        // =======
        // RYSOWANIE PIERWSZEGO OBIEKTU
        // =========
        renderObject(
                new Matrix4f()
                        .translate(-2.2f, 0.0f, 0.0f)
                        .rotateY((float) Math.toRadians((time * 10.0f) % 360.0f)),
                currentSurfaceA,
                globeTextureId,
                currentSurfaceA == SurfaceType.SPHERE
        );

        // =============
        // RYSOWANIE DRUGIEGO OBIEKTU
        // =========
        renderObject(
                new Matrix4f()
                        .translate(2.2f, 0.0f, 0.0f)
                        .rotateY((float) Math.toRadians((-time * 14.0f) % 360.0f))
                        .scale(0.95f),
                currentSurfaceB,
                moonTextureId,
                currentSurfaceB == SurfaceType.SPHERE
        );

        renderLightMarker(view, projection);
        // po rysowaniu przywracamy fill
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    private void renderLightMarker(Matrix4f view, Matrix4f projection) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glPointSize(14.0f);

        Matrix4f lightModel = new Matrix4f()
                .translate(lightPos)
                .scale(1.0f);

        lightMarkerShader.use();
        lightMarkerShader.setUniformMat4("model", lightModel);
        lightMarkerShader.setUniformMat4("view", view);
        lightMarkerShader.setUniformMat4("projection", projection);

        lightMarker.render();
    }

    // =
    // RYSOWANIE JEDNEGO OBIEKTU
    // ====================
    private void renderObject(Matrix4f model, SurfaceType surfaceType, int textureId, boolean useTexture) {
        shaderProgram.setUniformMat4("model", model);
        shaderProgram.setUniform1i("surfaceType", surfaceType.ordinal());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        shaderProgram.setUniform1i("useTexture", useTexture ? 1 : 0);

        if (currentMeshType == MeshType.TRIANGLE_LIST) {
            gridMeshList.render();
        } else {
            gridMeshStrip.render();
        }
    }

    // ==
    // SPRZĄTANIE
    // ==============
    public void cleanup() {
        if (gridMeshList != null) gridMeshList.cleanup();
        if (gridMeshStrip != null) gridMeshStrip.cleanup();
        if (shaderProgram != null) shaderProgram.cleanup();

        if (globeTextureId != 0) glDeleteTextures(globeTextureId);
        if (moonTextureId != 0) glDeleteTextures(moonTextureId);

        if (lightMarker != null) lightMarker.cleanup();
        if (lightMarkerShader != null) lightMarkerShader.cleanup();
    }
}