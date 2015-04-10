package assignment;

import com.sun.opengl.util.texture.Texture;

/**
 * Created by martin on 09/04/2015.
 */
public class Quad extends Triangle {

    protected final float[] D;

    public Quad(Texture tex, float[]... vertices) {
        super(tex, vertices[0], vertices[1], vertices[2]);
        if(vertices.length != 4) throw new RuntimeException("quad must have 4 vertices");
        D = vertices[3];
    }
}
