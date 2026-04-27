package cz.uhk.graphic3m01;

// Enum określający typ projekcji (sposób rzutowania 3D na ekran 2D)
public enum ProjectionType {

    // projekcja perspektywiczna (realistyczna)
    // dalsze obiekty są mniejsze
    PERSPECTIVE,

    // projekcja ortogonalna (techniczna)
    // brak perspektywy – obiekty mają stały rozmiar
    ORTHOGRAPHIC
}