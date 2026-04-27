package cz.uhk.graphic3m01;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    // punkt, na który patrzy kamera (środek sceny / obiektu)
    private final Vector3f target = new Vector3f(0.0f, 0.0f, 0.0f);

    // aktualna pozycja kamery w przestrzeni 3D
    private final Vector3f position = new Vector3f();

    // obrót wokół osi Y (lewo/prawo)
    private float yaw = -35.0f;

    // obrót góra/dół
    private float pitch = 20.0f;

    // odległość kamery od obiektu (zoom dla perspektywy)
    private float distance = 6.5f;

    // skala dla projekcji ortogonalnej (zoom ortho)
    private float orthoScale = 3.5f;

    //
    // MACIERZ WIDOKU (najważniejsze)
    // ======
    public Matrix4f getViewMatrix() {

        // obliczamy aktualną pozycję kamery
        updatePositionFromOrbit();

        // tworzymy macierz widoku (kamera patrzy na target)
        return new Matrix4f().lookAt(
                position,                      // skąd patrzy kamera
                target,                        // na co patrzy
                new Vector3f(0.0f, 1.0f, 0.0f) // > kierunek "góry"
        );
    }

    // zwraca aktualną pozycję kamery
    public Vector3f getPosition() {
        updatePositionFromOrbit();
        return new Vector3f(position);
    }

    //
    // RUCH KAMERY (WSAD)
    // ===

    // ruch do przodu / tyłu
    public void moveForward(float amount) {

        // kierunek od kamery do targetu
        Vector3f forward = new Vector3f(target)
                .sub(position)
                .normalize()
                .mul(amount);

        // przesuwamy target
        target.add(forward);
    }

    // ruch w lewo / prawo
    public void moveRight(float amount) {

        // kierunek patrzenia
        Vector3f forward = new Vector3f(target)
                .sub(position)
                .normalize();

        // wektor prostopadły (prawo/lewo)
        Vector3f right = forward
                .cross(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f())
                .normalize()
                .mul(amount);

        // przesuwamy target
        target.add(right);
    }

    // zwraca skalę ortho (do projekcji ortogonalnej)
    public float getOrthoScale() {
        return orthoScale;
    }

    // ruch góra/dół
    public void moveUp(float amount) {
        target.y += amount;
    }

    // ==========
    // ROTACJA KAMERY (mysz)
    // =========
    public void addRotation(float deltaX, float deltaY) {

        float sensitivity = 0.18f;

        // zmiana kąta poziomego
        yaw += deltaX * sensitivity;

        // zmiana kąta pionowego
        pitch += deltaY * sensitivity;

        // ograniczenie żeby kamera się nie „przewróciła”
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    // ===
    // ZOOM (scroll)
    // ==========
    public void addZoom(float offset) {

        // zmiana odległości (perspective)
        distance -= offset * 0.4f;

        // ograniczenia zoomu
        if (distance < 1.5f) distance = 1.5f;
        if (distance > 30.0f) distance = 30.0f;

        // zmiana skali ortho (dla projekcji ortogonalnej)
        orthoScale -= offset * 0.2f;

        if (orthoScale < 0.5f) orthoScale = 0.5f;
        if (orthoScale > 20.0f) orthoScale = 20.0f;
    }

    // ============
    // OBLICZENIE POZYCJI KAMERY
    // ===
    private void updatePositionFromOrbit() {

        // zamiana stopni na radiany
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        // obliczenie pozycji kamery na podstawie obrotu i odległości
        float x = target.x + distance * (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        float y = target.y + distance * (float) Math.sin(pitchRad);
        float z = target.z + distance * (float) (Math.cos(pitchRad) * Math.cos(yawRad));

        // ustawienie pozycji
        position.set(x, y, z);
    }
}