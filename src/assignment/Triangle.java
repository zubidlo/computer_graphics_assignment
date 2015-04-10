package assignment;

import com.sun.opengl.util.texture.Texture;

/**
 * Created by martin on 09/04/2015.
 */
public class Triangle {

    public final float[] A, B, C;
    public final float[] normal;
    public final Texture texture;

    public Triangle(final Texture tex, final float[]...vertices) {
        if(vertices.length != 3) throw new RuntimeException("triangle has 3 vertices");
        A = vertices[0];
        B = vertices[1];
        C = vertices[2];
        normal = normalVFromTriangle(A, B, C);
        texture = tex;
    }

    /**
     * Returns normal vector of the triangle defined by given 3 vertices
     * @param v1 vertex 1
     * @param v2 vertex 2
     * @param v3 vertex 3
     * @return vector perpendicular to given triangle
     */
    protected static float[] normalVFromTriangle(final float[] v1, final float[] v2, final float[] v3) {
        if(v1.length != 3 || v2.length != 3 || v3.length != 3)
            throw new IllegalArgumentException("use float[3] for arguments!");

        float[] normal = new float[3];
        float[] vector1 = new float[3];
        float[] vector2 = new float[3];

        //2 vectors from 3 vertices
        for (int i = 0; i < 3; i++) {
            vector1[i] = v2[i] - v1[i];
            vector2[i] = v1[i] - v3[i];
        }

        //cross product of 2 vectors
        normal[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
        normal[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
        normal[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];

        //normalize cross product
        float magnitude = (float) Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        for (int i = 0; i < 3; i++) normal[i] = normal[i] / magnitude;

        //System.out.printf("normal: %.2f %.2f %.2f%n", normal[0], normal[1], normal[2]);
        return normal;
    }
}
