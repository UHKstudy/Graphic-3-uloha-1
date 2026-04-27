#version 330 core

in vec3 FragPos;
in vec3 Normal;
in vec3 BaseColor;
in vec2 TexCoord;

out vec4 FragColor;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;

uniform vec3 spotlightDirection;
uniform float spotlightInnerCutoff;
uniform float spotlightOuterCutoff;

uniform int debugMode;

uniform sampler2D surfaceTexture;
uniform int useTexture;

void main() {

    vec3 norm = normalize(Normal);

    // = DEBUG: NORMAL ===
    if (debugMode == 1) {
        FragColor = vec4(norm * 0.5 + 0.5, 1.0);
        return;
    }

    // ==== DEBUG: POSITION =
    if (debugMode == 2) {
        FragColor = vec4(FragPos * 0.1 + 0.5, 1.0);
        return;
    }

    // == DEBUG: DEPTH =
    if (debugMode == 3) {
        float depth = length(viewPos - FragPos) / 20.0;
        FragColor = vec4(vec3(depth), 1.0);
        return;
    }

    //DEBUG: DISTANCE FROM LIGHT ==
    if (debugMode == 4) {
        float dist = length(lightPos - FragPos) / 10.0;
        FragColor = vec4(vec3(dist), 1.0);
        return;
    }

    // === DEBUG: UV ==
    if (debugMode == 5) {
        FragColor = vec4(TexCoord, 0.0, 1.0);
        return;
    }

    // =>== DEBUG: TEXTURE =
    if (debugMode == 6) {
        FragColor = texture(surfaceTexture, TexCoord);
        return;
    }

    // == GBUFFER VIEW / první průchod =
    // symulacja wizualizacji danych: position + normal + depth
    if (debugMode == 7) {
        vec3 n = norm * 0.5 + 0.5;
        vec3 p = FragPos * 0.1 + 0.5;
        float d = clamp(length(viewPos - FragPos) / 20.0, 0.0, 1.0);

        FragColor = vec4(mix(p, n, 0.5) * (1.0 - d * 0.25), 1.0);
        return;
    }

    // SSAO RAW / druhý průchod =
    // uproszczone AO zależne od kierunku normalnej
    float ssaoRaw = dot(norm, vec3(0.0, 1.0, 0.0));
    ssaoRaw = clamp(ssaoRaw * 0.5 + 0.5, 0.25, 1.0);

    // dodatkowe przyciemnienie zależne od odległości
    float distanceAO = length(viewPos - FragPos);
    distanceAO = clamp(1.0 - distanceAO * 0.03, 0.4, 1.0);

    ssaoRaw *= distanceAO;

    if (debugMode == 8) {
        FragColor = vec4(vec3(ssaoRaw), 1.0);
        return;
    }

    // ===== SSAO BLUR / třetí průchod =====
    // uproszczone wygładzenie AO
    float ssaoBlur = smoothstep(0.25, 1.0, ssaoRaw);

    if (debugMode == 9) {
        FragColor = vec4(vec3(ssaoBlur), 1.0);
        return;
    }

    // = FINAL LIGHTING / čtvrtý průchod

    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 viewDir = normalize(viewPos - FragPos);

    float distanceToLight = length(lightPos - FragPos);

    // attenuation
    float constant = 1.0;
    float linear = 0.045;
    float quadratic = 0.010;
    float attenuation = 1.0 / (constant + linear * distanceToLight + quadratic * distanceToLight * distanceToLight);

    // spotlight
    float theta = dot(lightDir, normalize(-spotlightDirection));
    float epsilon = spotlightInnerCutoff - spotlightOuterCutoff;
    float spotlightIntensity = clamp((theta - spotlightOuterCutoff) / epsilon, 0.0, 1.0);

    // ambient
    float ambientStrength = 0.12;
    vec3 ambient = ambientStrength * lightColor;

    // diffuse
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // specular Blinn-Phong
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
    float specularStrength = 0.45;
    vec3 specular = specularStrength * spec * lightColor;

    vec3 materialColor = BaseColor;

    if (useTexture == 1) {
        materialColor = texture(surfaceTexture, TexCoord).rgb;
    }

    vec3 result = ambient * materialColor
    + (diffuse * materialColor + specular) * attenuation * spotlightIntensity;

    // zastosowanie AO w finalnym obrazie
    result *= ssaoBlur;

    FragColor = vec4(result, 1.0);
}