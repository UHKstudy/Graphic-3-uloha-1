#version 330 core

// wejście: pojedynczy punkt z gridu 2D , wstęp do vertex shaderu
layout (location = 0) in vec2 aPos;

// uniformy przekazywane z Javy
uniform mat4 model;       // transformacja obiektu
uniform mat4 view;        // kamera
uniform mat4 projection;  // projekcja
uniform float time;       // czas do animacji
uniform int surfaceType;  // typ powierzchni

// dane wyjściowe do fragment shadera
out vec3 FragPos;    // pozycja punktu w świecie 3D
out vec3 Normal;     // normalna powierzchni
out vec3 BaseColor;  // bazowy kolor powierzchni
out vec2 TexCoord;


const float PI = 3.14159265359;

// ====
// FUNKCJA: zamiana punktu 2D (uv) na punkt 3D (x,y,z)
// ===================================
// vertex shader (z 2D → 3D ; liczy normalne ;  ustawia pozycję)
vec3 evaluateSurface(int type, vec2 uv, float t) {

    // 0 = fala kartezjańska
    if (type == 0) {
        float x = uv.x * 3.0;
        float z = uv.y * 3.0;
        float y = 0.35 * sin(2.0 * x + t) * cos(2.0 * z + t * 0.7);
        return vec3(x, y, z);
    }

    // 1 = ripple / fale okrągłe
    else if (type == 1) {
        float x = uv.x * 3.0;
        float z = uv.y * 3.0;
        float r = sqrt(x * x + z * z);
        float y = 0.30 * cos(4.0 * r - t * 2.0);
        return vec3(x, y, z);
    }

    // 2 = zwykła kula
    else if (type == 2) {
        // przeskalowanie uv z <-1,1> do <0,1>
        float u = (uv.x + 1.0) * 0.5;
        float v = (uv.y + 1.0) * 0.5;

        // współrzędne sferyczne
        float theta = v * PI;
        float phi = u * 2.0 * PI;
        float r = 1.4;

        float x = r * sin(theta) * cos(phi);
        float y = r * cos(theta);
        float z = r * sin(theta) * sin(phi);
        return vec3(x, y, z);
    }

    // 3 = kula z falowaniem / "flower sphere"
    else if (type == 3) {
        float u = (uv.x + 1.0) * 0.5;
        float v = (uv.y + 1.0) * 0.5;

        float theta = v * PI;
        float phi = u * 2.0 * PI;

        // promień zmienia się w zależności od kąta i czasu
        float r = 1.2 + 0.25 * cos(6.0 * phi + t) * sin(3.0 * theta);

        float x = r * sin(theta) * cos(phi);
        float y = r * cos(theta);
        float z = r * sin(theta) * sin(phi);
        return vec3(x, y, z);
    }

    // 4 = falujący cylinder
    else if (type == 4) {
        float u = (uv.x + 1.0) * 0.5;
        float v = (uv.y + 1.0) * 0.5;

        float phi = u * 2.0 * PI;
        float h = (v - 0.5) * 3.0;

        float r = 1.0 + 0.18 * sin(6.0 * h + t * 2.0);

        float x = r * cos(phi);
        float y = h;
        float z = r * sin(phi);
        return vec3(x, y, z);
    }

    // 5 = skręcony cylinder
    else {
        float u = (uv.x + 1.0) * 0.5;
        float v = (uv.y + 1.0) * 0.5;

        float h = (v - 0.5) * 3.0;
        float phi = u * 2.0 * PI + h * 2.0 + t * 0.6;
        float r = 0.7 + 0.25 * v;

        float x = r * cos(phi);
        float y = h;
        float z = r * sin(phi);
        return vec3(x, y, z);
    }
}

// ========
// Funkcja zwraca kolor bazowy zależny od typu powierzchni, zmiana koloru
//
vec3 getSurfaceColor(int type) {
    if (type == 0) return vec3(0.2, 0.7, 1.0);
    if (type == 1) return vec3(0.9, 0.7, 0.2);
    if (type == 2) return vec3(0.2, 0.9, 0.4);
    if (type == 3) return vec3(1.0, 0.4, 0.8);
    if (type == 4) return vec3(0.9, 0.4, 0.2);
    return vec3(0.7, 0.3, 1.0);
}

void main() {

    // wejściowy punkt z gridu traktujemy jako uv
    vec2 uv = aPos;
    TexCoord = vec2(1.0 - (uv.x + 1.0) * 0.5, (uv.y + 1.0) * 0.5);

    // obliczenie punktu 3D na powierzchni
    vec3 position = evaluateSurface(surfaceType, uv, time);

    // =====
    // OBLICZENIE NORMALNEJ
    // ==========================

    // małe przesunięcie w kierunku u i v
    float eps = 0.01;
    vec3 px = evaluateSurface(surfaceType, uv + vec2(eps, 0.0), time);
    vec3 py = evaluateSurface(surfaceType, uv + vec2(0.0, eps), time);

    // dwa wektory styczne do powierzchni
    vec3 tangentU = px - position;
    vec3 tangentV = py - position;

    // iloczyn wektorowy daje wektor prostopadły = normalną
    vec3 normalObj = normalize(cross(tangentU, tangentV));

    if (surfaceType == 0 || surfaceType == 1 || surfaceType == 4 || surfaceType == 5) {
        normalObj = -normalObj;
    }

    // =================================
    //  TRANSFORMACJA POZYCJI
    // =====================

    // pozycja punktu po transformacji modelu
    vec4 worldPos4 = model * vec4(position, 1.0);
    FragPos = worldPos4.xyz;

    // =====================================================
    // TRANSFORMACJA NORMALNEJ
    // =====================================================

    // macierz normalnych
    mat3 normalMatrix = transpose(inverse(mat3(model)));

    // normalna po transformacji , smooth shading
    Normal = normalize(normalMatrix * normalObj);

    // kolor bazowy powierzchni
    BaseColor = getSurfaceColor(surfaceType);

    // finalna pozycja na ekranie
    gl_Position = projection * view * worldPos4;
}