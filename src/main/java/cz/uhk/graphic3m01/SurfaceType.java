package cz.uhk.graphic3m01;

// Enum określający typ powierzchni generowanej w shaderze
public enum SurfaceType {

    // funkcja w układzie kartezjańskim – fala sinus
    CARTESIAN_WAVE,

    // funkcja kartezjańska – fale okrągłe (ripple)
    CARTESIAN_RIPPLE,

    // kula (układ sferyczny)
    SPHERE,

    // kula z modyfikacją (np. "kwiat")
    SPHERE_FLOWER,

    // cylinder z falą
    CYLINDER_WAVE,

    // cylinder skręcony (twist)
    CYLINDER_TWIST
}