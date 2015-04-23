package assignment;

import com.sun.opengl.util.texture.Texture;

/**
 * quad
 * Created by martin on 09/04/2015.
 */
public class Quad extends Triangle {

    protected final float[] D;

    public Quad(Texture tex, float[]... vertices) {
        super(tex, vertices[0], vertices[1], vertices[2]);
        D = vertices[3];
    }
}
