package assignment;

import static java.lang.System.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.GLUT;

import java.awt.*;
import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.fixedfunc.*;
import javax.swing.*;
import javax.media.opengl.glu.GLU;
import java.awt.event.*;
import java.util.*;
import java.util.function.Consumer;

import static javax.swing.UIManager.*;


/**
 * Created by martin on 07/02/2015.
 * requires java 1.7 and jogl 2.0
 */
public class JFrameWithGLCanvasAndGLEventListener extends JFrame
    implements GLEventListener, Runnable{

    //title
    protected final String title;
    protected final JMenuBar menuBar;

    //height and width of JFrame
    protected int height, width;

    //interface to OpenGL 2.0
    protected GL2 gl2;

    //the GL Utility
    protected final GLU glu;
    protected final GLUT glut;

    //drawable in a frame
    protected final GLCanvas canvas;

    //OpenGL capabilities
    protected final GLCapabilities capabilities;

    //drive display() in loop
    protected final boolean startAnimator ;
    protected final Animator animator;

    //last frame time stamp in nanoseconds
    protected long lastFrameTime;

    //key listener
    protected final MultiKeyListener listener;

    /**
     * Creates Frame with OpenGL 2.0 capabilities and settings
     * @param width         width of the window in pixels
     * @param height        height of the window in pixels
     * @param title         a title of the window
     * @param startAnimator if you want animator thread 60fps be running set this to true
     */
    public JFrameWithGLCanvasAndGLEventListener(final int width,
                                                final int height,
                                                final String title,
                                                final boolean startAnimator) {


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

        //GL Utilities
        glu = new GLU();
        glut = new GLUT();

        //animator
        animator = new FPSAnimator(canvas, 60);

        //key listener
        listener = new MultiKeyListener();
        canvas.addKeyListener(listener);

        //menu
        menuBar = new JMenuBar();
        menuBar.add(themesMenu());
        setJMenuBar(menuBar);

        //initialize JFrame
        setTitle(title);
        setSize(width + 16, height + 38);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(canvas, BorderLayout.CENTER);
        canvas.requestFocus();

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(JFrameWithGLCanvasAndGLEventListener.this);
        } catch (Exception e) { e.printStackTrace(); }

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

        //OpenGL 2.0 interface
        gl2 = canvas.getGL().getGL2();

        // start animator thread (if requested) which calls display() 60 times per seconds
        if(startAnimator) {
            animator.start();
            setLastFrameTime();
        }
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
        //clear buffer
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
        calculateFPS();
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

    private JMenu themesMenu() {
        JMenu menuLookAndFeel = new JMenu("Themes");
        Arrays.asList(getInstalledLookAndFeels())
                .forEach(i -> menuLookAndFeel.add(new LookAndFeelMenuItem(i)));
        return menuLookAndFeel;
    }

    private class LookAndFeelMenuItem extends JMenuItem implements ActionListener{

        private final LookAndFeelInfo lookAndFeelInfo;

        private LookAndFeelMenuItem(final LookAndFeelInfo lookAndFeelInfo) {
            super(lookAndFeelInfo.getName());
            this.lookAndFeelInfo = lookAndFeelInfo;
            addActionListener(this);
            //System.out.println("theme: " + lookAndFeelInfo.getClassName());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            System.out.println("current theme: " + event.getActionCommand());
            try {
                LookAndFeelMenuItem menuItem = ((LookAndFeelMenuItem) event.getSource());
                String className = menuItem.lookAndFeelInfo.getClassName();
                UIManager.setLookAndFeel(className);
                SwingUtilities.updateComponentTreeUI(JFrameWithGLCanvasAndGLEventListener.this);
                //pack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
