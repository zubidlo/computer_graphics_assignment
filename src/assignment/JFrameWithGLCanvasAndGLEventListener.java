package assignment;

import static java.lang.System.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.GLUT;

import java.awt.*;
import java.lang.Math;
import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.fixedfunc.*;
import javax.swing.*;
import javax.media.opengl.glu.GLU;

/**
 * Created by martin on 07/02/2015.
 * requires java 1.7 and jogl 2.0
 */
public class JFrameWithGLCanvasAndGLEventListener extends JFrame
    implements GLEventListener, Runnable{

    //title
    protected String title;

    //height and width of JFrame
    protected int height, width;

    //interface to OpenGL 2.0
    protected GL2 gl2;

    //for the GL Utility
    protected GLU glu;

    protected GLUT glut;

    //drawable in a frame
    protected GLCanvas canvas;

    //OpenGL capabilities
    protected GLCapabilities capabilities;

    //drive display() in loop
    protected boolean startAnimator ;
    protected Animator animator;

    //last frame time stamp in nanoseconds
    protected long lastFrameTime;

    /**
     * Creates Frame with OpenGL 2.0 capabilities and settings
     * @param width         width of the window in pixels
     * @param height        height of the window in pixels
     * @param title         a title of the window
     * @param startAnimator if you want animator thread 60fps be running set this to true
     */
    public JFrameWithGLCanvasAndGLEventListener(int width, int height, String title, boolean startAnimator) {


        if(height <= 0 || width <= 0) throw new RuntimeException("bad frame size!");

        //JFrame title, height and width
        this.title = title;
        this.height = height;
        this.width = width;
        this.startAnimator = startAnimator;

        //OpenGL capabilities
        capabilities = new GLCapabilities(GLProfile.getDefault());

        //single buffer only
        capabilities.setDoubleBuffered(false);
        out.println(capabilities.toString());

        //canvas
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        //initialize JFrame
        setTitle(title);
        setSize(width + 16, height + 38);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(canvas, BorderLayout.CENTER);
        canvas.requestFocus();
        setVisible(true);
    }

    @Override
    public void run() {}

    private void setLastFrameTime() {
        lastFrameTime = nanoTime();
    }

    /**
     * Puts frame per second into frame title
     */
    protected final void calculateFPS(){

        if(startAnimator) {
            setTitle(title + " fps: [" + 1000000000 / (nanoTime() - lastFrameTime) + "]");
            setLastFrameTime();
        }
    }

    // Called once for OpenGL initialization
    @Override
    public void init(GLAutoDrawable glAutoDrawable) {

        // start animator thread (if requested) which calls display() 60 times per seconds
        if(startAnimator) {
            animator = new FPSAnimator(canvas, 60);
            animator.start();
            setLastFrameTime();
        }

        //OpenGL 2.0 interface
        gl2 = canvas.getGL().getGL2();
    }

    // Called for handling reshaped drawing area
    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {

        //define the drawing area coordinates
        gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl2.glLoadIdentity();

        //if window were re-sized set new width and height
        gl2.glOrtho(0, width, 0, height, -1.0, 1.0);
        gl2.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;

        out.println("reshape() canvas width:" + width);
        out.println("reshape() canvas height:" + height);
    }

    // Called for OpenGL rendering every reshape
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {

        calculateFPS();
        //clear buffer
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Returns random int from [0..range]. If range is negative,
     * then returns random number from [range..0].
     * @param range upper range limit
     * @return float
     */
    protected float random(float range) {

        return (float) Math.round((range) * Math.random());
    }

    /**
     * Returns random float from [0..range]. If range is negative,
     * then returns random number from [range..0].
     * @param range upper range limit
     * @return integer
     */
    protected int random(int range) {

        return (int) Math.round((range) * Math.random());
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }
}
