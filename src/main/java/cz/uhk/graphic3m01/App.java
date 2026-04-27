package cz.uhk.graphic3m01;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class App {

    // uchwyt do okna (GLFW)
    private long window;

    // główny renderer (rysuje scenę)
    private Renderer renderer;

    // gdy TAB jest wcisniety, klawisze 1-6 zmieniaja drugi obiekt
    private boolean secondObjectMode = false;


    // obsługa myszy (czy lewy przycisk jest wciśnięty)
    private boolean leftMousePressed = false;

    // pierwsze kliknięcie (żeby nie było skoku kamery)
    private boolean firstMouseAfterClick = true;

    // poprzednia pozycja myszy
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;

    public static void main(String[] args) {
        new App().run(); // start programu
    }

    public void run() {
        init();     // inicjalizacja
        loop();     // główna pętla renderowania
        cleanup();  // sprzątanie
    }

    private void init() {

        // obsługa błędów GLFW
        GLFWErrorCallback.createPrint(System.err).set();

        // inicjalizacja GLFW (okna)
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // ustawienia okna
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // na początku ukryte
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // można zmieniać rozmiar

        // tworzenie okna
        window = glfwCreateWindow(1000, 700, "graphic3m01 - two objects", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        // ustawienie kontekstu OpenGL
        glfwMakeContextCurrent(window);

        // synchronizacja z odświeżaniem (VSync)
        glfwSwapInterval(1);

        // pokazanie okna
        glfwShowWindow(window);

        // ustawienie kursora (normalny widoczny)
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        // inicjalizacja OpenGL
        GL.createCapabilities();

        // włączenie testu głębi (ważne dla 3D)
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // tworzenie renderera
        renderer = new Renderer();
        renderer.init();

        // ===
        //OBSŁUGA MYSZY (kliknięcie)
        // =
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    leftMousePressed = true;
                    firstMouseAfterClick = true;
                } else if (action == GLFW_RELEASE) {
                    leftMousePressed = false;
                }
            }
        });

        // ===
        // OBSŁUGA RUCHU MYSZY (kamera)
        // ==
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {

            // kamera działa tylko gdy trzymasz LPM
            if (!leftMousePressed) return;

            //   pierwszy ruch po kliknięciu – zapamiętanie pozycji
            if (firstMouseAfterClick) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouseAfterClick = false;
                return;
            }

            // obliczenie różnicy ruchu myszy
            double deltaX = xpos - lastMouseX;
            double deltaY = ypos - lastMouseY;

            lastMouseX = xpos;
            lastMouseY = ypos;

            // przekazanie do kamery (obrót)
            renderer.getCamera().addRotation((float) deltaX, (float) deltaY);
        });

        // =======
        // SCROLL (zoom kamery)
        // =======
        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            if (glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE) {
                renderer.getCamera().addZoom((float) yoffset);
            }
        });

        // ======
        // KLAWIATURA
        // ==
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {

                    if (key == GLFW_KEY_TAB) {
                        if (action == GLFW_PRESS) {
                            secondObjectMode = true;
                        } else if (action == GLFW_RELEASE) {
                            secondObjectMode = false;
                        }
                        return;
                    }

                    if (action == GLFW_PRESS) {

                        switch (key) {


                    case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(win, true);

                    // zmiana powierzchni (obiekty)
                    case GLFW_KEY_1 -> {
                        if (secondObjectMode) renderer.setSurfaceTypeB(SurfaceType.CARTESIAN_WAVE);
                        else renderer.setSurfaceTypeA(SurfaceType.CARTESIAN_WAVE);
                    }

                    case GLFW_KEY_2 -> {
                        if (secondObjectMode) renderer.setSurfaceTypeB(SurfaceType.CARTESIAN_RIPPLE);
                        else renderer.setSurfaceTypeA(SurfaceType.CARTESIAN_RIPPLE);
                    }

                    case GLFW_KEY_3 -> {
                        if (secondObjectMode) renderer.setSurfaceTypeB(SurfaceType.SPHERE);
                        else renderer.setSurfaceTypeA(SurfaceType.SPHERE);
                    }

                    case GLFW_KEY_4 -> {
                        if (secondObjectMode) renderer.setSurfaceTypeB(SurfaceType.SPHERE_FLOWER);
                        else renderer.setSurfaceTypeA(SurfaceType.SPHERE_FLOWER);
                    }

                    case GLFW_KEY_5 -> {
                        if (secondObjectMode) renderer.setSurfaceTypeB(SurfaceType.CYLINDER_WAVE);
                        else renderer.setSurfaceTypeA(SurfaceType.CYLINDER_WAVE);
                    }

                    case GLFW_KEY_6 -> {
                        if (secondObjectMode) renderer.setSurfaceTypeB(SurfaceType.CYLINDER_TWIST);
                        else renderer.setSurfaceTypeA(SurfaceType.CYLINDER_TWIST);
                    }

                    // sterowanie światłem
                    case GLFW_KEY_UP -> renderer.moveLight(0.0f, 0.2f, 0.0f);
                    case GLFW_KEY_DOWN -> renderer.moveLight(0.0f, -0.2f, 0.0f);
                    case GLFW_KEY_LEFT -> renderer.moveLight(-0.2f, 0.0f, 0.0f);
                    case GLFW_KEY_RIGHT -> renderer.moveLight(0.2f, 0.0f, 0.0f);
                    case GLFW_KEY_PAGE_UP -> renderer.moveLight(0.0f, 0.0f, -0.2f);
                    case GLFW_KEY_PAGE_DOWN -> renderer.moveLight(0.0f, 0.0f, 0.2f);

                    // tryb renderowania
                    case GLFW_KEY_F1 -> renderer.setRenderMode(RenderMode.POINTS);
                    case GLFW_KEY_F2 -> renderer.setRenderMode(RenderMode.WIREFRAME);
                    case GLFW_KEY_F3 -> renderer.setRenderMode(RenderMode.FILL);

                    // projekcja
                    case GLFW_KEY_P -> renderer.setProjectionType(ProjectionType.PERSPECTIVE);
                    case GLFW_KEY_O -> renderer.setProjectionType(ProjectionType.ORTHOGRAPHIC);

                    // mesh
                    case GLFW_KEY_M -> renderer.setMeshType(MeshType.TRIANGLE_LIST);
                    case GLFW_KEY_N -> renderer.setMeshType(MeshType.TRIANGLE_STRIP);

                    // debug
                    case GLFW_KEY_0 -> renderer.setDebugMode(DebugMode.FINAL);
                    case GLFW_KEY_7 -> renderer.setDebugMode(DebugMode.NORMAL);
                    case GLFW_KEY_8 -> renderer.setDebugMode(DebugMode.POSITION);
                    case GLFW_KEY_9 -> renderer.setDebugMode(DebugMode.DEPTH);
                    case GLFW_KEY_MINUS -> renderer.setDebugMode(DebugMode.LIGHT_DISTANCE);

                            case GLFW_KEY_F5 -> renderer.setDebugMode(DebugMode.GBUFFER);
                            case GLFW_KEY_F6 -> renderer.setDebugMode(DebugMode.SSAO_RAW);
                            case GLFW_KEY_F7 -> renderer.setDebugMode(DebugMode.SSAO_BLUR);
                            case GLFW_KEY_F8 -> renderer.setDebugMode(DebugMode.FINAL);
                }
            }
        });

        // ustaw focus na okno
        glfwFocusWindow(window);
    }

    //
    // RUCH KAMERY (WSAD)
    // ====
    private void processInput() {
        float moveSpeed = 0.08f;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            renderer.getCamera().moveForward(moveSpeed);

        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            renderer.getCamera().moveForward(-moveSpeed);

        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            renderer.getCamera().moveRight(-moveSpeed);

        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            renderer.getCamera().moveRight(moveSpeed);

        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS)
            renderer.getCamera().moveUp(moveSpeed);

        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)
            renderer.getCamera().moveUp(-moveSpeed);
    }

    // ====
    // GŁÓWNA PĘTLA
    // ========
    private void loop() {
        int[] widthArr = new int[1];
        int[] heightArr = new int[1];

        while (!glfwWindowShouldClose(window)) {

            processInput();

            glfwGetFramebufferSize(window, widthArr, heightArr);

            int width = widthArr[0];
            int height = heightArr[0];

            GL11.glViewport(0, 0, width, height);

            // kolor tła
            GL11.glClearColor(0.08f, 0.10f, 0.14f, 1.0f);

            // czyszczenie ekranu
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // rysowanie sceny
            renderer.render(width, height);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    // ====
    // SPRZĄTANIE
    // ==============
    private void cleanup() {

        if (renderer != null)
            renderer.cleanup();

        glfwDestroyWindow(window);
        glfwTerminate();

        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}