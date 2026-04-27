package cz.uhk.graphic3m01;

// Enum określający sposób rysowania siatki (mesh)
public enum MeshType {

    // każdy trójkąt rysowany osobno
    TRIANGLE_LIST,

    // trójkąty połączone w jeden ciąg (strip)
    TRIANGLE_STRIP
}