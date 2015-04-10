package assignment;

import com.sun.opengl.util.texture.*;
import com.sun.opengl.util.texture.awt.*;

import javax.imageio.*;
import javax.media.opengl.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

import static java.lang.System.*;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;
import static java.awt.event.KeyEvent.*;

/**
 * Created by martin on 09/04/2015.
 */
public class House extends JFrameWithGLCanvasAndGLEventListener {

    public House(int width, int height, String title, boolean startAnimator) {
        super(width, height, title, startAnimator);
        menuBar.add(optionsMenu());
        initializeVariables();
    }

    private volatile float[] housePosition = {0, 0, 0};
    private volatile float[] cameraPos, origin;
    private volatile double cameraRotationRadius, cameraRotationAngle;
    private volatile float scaleFactor, rotationAngleX, rotationAngleY, doorRotationAngleY;
    private volatile boolean lightIsOn, texturesAreOn, cameraRotationIsOn;

    private JMenu optionsMenu() {
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.add(new MenuItem("Toggle Light", e -> { lightIsOn = !lightIsOn; }));
        optionsMenu.add(new MenuItem("Toggle Textures", e -> { texturesAreOn = !texturesAreOn; }));
        optionsMenu.add(new MenuItem("Toggle Camera Rotation", e -> { cameraRotationIsOn = !cameraRotationIsOn; }));
        return optionsMenu;
    }

    private void initializeVariables() {
        housePosition = new float[]{0, 0, 0};
        cameraPos = new float[]{0, 0, 5};
        origin = new float[]{0, 0, 0};
        cameraRotationRadius = cameraPos[2];
        cameraRotationAngle = 1;
        cameraPos[0] = (float) (cameraRotationRadius * cos(cameraRotationAngle));
        cameraPos[2] = (float) (cameraRotationRadius * sin(cameraRotationAngle));
        scaleFactor = 1;
        rotationAngleX = 0;
        rotationAngleY = 0;
        doorRotationAngleY = 0;
        lightIsOn = true;
        texturesAreOn = true;
        cameraRotationIsOn = false;
    }

    private final float[] lightSourcePosition = {5, 5, 5, 1};
    private final float[] ambientLight = {0.01f, 0.01f, 0.01f, 1};
    private final float[] diffuseLight = {1, 1, 1, 1};
    private final float[] blackLight = {0, 0, 0, 1};

    private void setLight() {
        gl2.glEnable(GL_LIGHTING);
        gl2.glEnable(GL_NORMALIZE);
        gl2.glEnable(GL_LIGHT0);
        gl2.glLightfv(GL_LIGHT0, GL_POSITION, lightSourcePosition, 0);
        gl2.glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight, 0);
        gl2.glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuseLight, 0);
        gl2.glLightfv(GL_LIGHT0, GL_SPECULAR, diffuseLight, 0);
        gl2.glEnable(GL_COLOR_MATERIAL);
        gl2.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        gl2.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, diffuseLight, 0);
        gl2.glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, ambientLight, 0);
        gl2.glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, blackLight, 0);
    }

    private List<Texture> loadTextures() {
        List<Texture> textures = new ArrayList<>();
        Arrays.asList("door1.png", "roof1.png", "walls1.png", "walls2.png", "wood_tex1.png").forEach(t -> {
            try {
                BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource("assignment/images/" + t));
                textures.add(AWTTextureIO.newTexture(image, false));
                gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return textures;
    }

    //house coordinate constants
    private final float[] frontRightUp = {1, 0.625f, 0.5f};
    private final float[] frontLeftUp = {-1, 0.625f, 0.5f};
    private final float[] frontLeftDown = {-1, -0.625f, 0.5f};
    private final float[] frontRightDown = {1, -0.625f, 0.5f};
    private final float[] backRightUp = {1, 0.625f, -0.5f};
    private final float[] backLeftUp = {-1, 0.625f, -0.5f};
    private final float[] backLeftDown = {-1, -0.625f, -0.5f};
    private final float[] backRightDown = {1, -0.625f, -0.5f};
    private final float[] roofRight = {1, 1, 0};
    private final float[] roofLeft = {-1, 1, 0};
    private final float[] aboveDoorRight = {0.3f, 0.625f, 0.5f};
    private final float[] aboveDoorLeft = {-0.3f, 0.625f, 0.5f};

    //door coordinate constants
    private final float[] doorRightDown = {0.3f, -0.625f, 0.5f};
    private final float[] doorLeftDown = {-0.3f, -0.625f, 0.5f};
    private final float[] doorLeftUp = {-0.3f, 0.425f, 0.5f};
    private final float[] doorRightUp = {0.3f, 0.425f, 0.5f};

    private Triangle roofRightSideTriangle, roofLeftSideTriangle;
    private Quad frontFaceQuadRIGHT, frontFaceQuadLEFT, frontFaceQuadTOP, leftSideFaceQuad, backFaceQuad, rightSideFaceQuad, bottomFaceQuad,
            roofFrontFaceQuad, roofBackFaceQuad, doorFaceQuad;

    private void createPolygons() {
        List<Texture> textures = loadTextures();
        roofFrontFaceQuad = new Quad(textures.get(1), frontRightUp, frontLeftUp, roofLeft, roofRight);
        roofBackFaceQuad = new Quad(textures.get(1), backLeftUp, backRightUp, roofRight, roofLeft);
        roofRightSideTriangle = new Triangle(textures.get(4), backRightUp, frontRightUp, roofRight);
        roofLeftSideTriangle = new Triangle(textures.get(4), frontLeftUp, backLeftUp, roofLeft);
        leftSideFaceQuad = new Quad(textures.get(2), frontLeftDown, backLeftDown, backLeftUp, frontLeftUp);
        backFaceQuad = new Quad(textures.get(2), backLeftDown, backRightDown, backRightUp, backLeftUp);
        rightSideFaceQuad = new Quad(textures.get(2), backRightDown, frontRightDown, frontRightUp, backRightUp);
        bottomFaceQuad = new Quad(textures.get(3), backRightDown, backLeftDown, frontLeftDown, frontRightDown);
        doorFaceQuad = new Quad(textures.get(0), doorRightDown, doorLeftDown, doorLeftUp, doorRightUp);
        frontFaceQuadRIGHT = new Quad(textures.get(2), frontRightDown, doorRightDown, aboveDoorRight, frontRightUp);
        frontFaceQuadLEFT = new Quad(textures.get(2), doorLeftDown, frontLeftDown, frontLeftUp, aboveDoorLeft);
        frontFaceQuadTOP = new Quad(textures.get(2), doorRightUp, doorLeftUp, aboveDoorLeft, aboveDoorRight);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        super.init(glAutoDrawable);
        gl2.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl2.glClearDepth(1.0f);
        gl2.glEnable(GL_DEPTH_TEST);
        gl2.glDepthFunc(GL_LEQUAL);
        gl2.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl2.glShadeModel(GL_SMOOTH);
        setLight();
        createPolygons();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        if (height == 0) height = 1;
        float aspect = (float) width / height;
        gl2.glViewport(0, 0, width, height);
        gl2.glMatrixMode(GL_PROJECTION);
        gl2.glLoadIdentity();
        glu.gluPerspective(45.0, aspect, 0.1, 100.0);
        gl2.glMatrixMode(GL_MODELVIEW);
        gl2.glLoadIdentity(); // reset
        //out.printf("canvas [width:%d height:%d]%n", width, height);
    }

    private void render(Triangle triangle) {
        if(texturesAreOn) {
            gl2.glEnable(GL.GL_TEXTURE_2D);
            triangle.texture.bind();
            gl2.glTexEnvf(GL_TEXTURE_2D, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        }

        gl2.glBegin(GL_TRIANGLES);
        gl2.glNormal3fv(triangle.normal, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(triangle.texture.getImageTexCoords().top(), triangle.texture.getImageTexCoords().left());
        gl2.glVertex3fv(triangle.A, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(triangle.texture.getImageTexCoords().left(), triangle.texture.getImageTexCoords().bottom());
        gl2.glVertex3fv(triangle.B, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(triangle.texture.getImageTexCoords().bottom(), triangle.texture.getImageTexCoords().right());
        gl2.glVertex3fv(triangle.C, 0);
        gl2.glEnd();

        if(texturesAreOn) {
            gl2.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
            gl2.glDisable(GL_TEXTURE_2D);
        }
    }

    private void render(Quad quad) {
        if(texturesAreOn) {
            gl2.glEnable(GL.GL_TEXTURE_2D);
            quad.texture.bind();
            gl2.glTexEnvf(GL_TEXTURE_2D, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        }

        gl2.glBegin(GL_QUADS);
        gl2.glNormal3fv(quad.normal, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(quad.texture.getImageTexCoords().top(), quad.texture.getImageTexCoords().left());
        gl2.glVertex3fv(quad.A, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(quad.texture.getImageTexCoords().left(), quad.texture.getImageTexCoords().bottom());
        gl2.glVertex3fv(quad.B, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(quad.texture.getImageTexCoords().bottom(), quad.texture.getImageTexCoords().right());
        gl2.glVertex3fv(quad.C, 0);
        if(texturesAreOn)
            gl2.glTexCoord2f(quad.texture.getImageTexCoords().right(), quad.texture.getImageTexCoords().top());
        gl2.glVertex3fv(quad.D, 0);
        gl2.glEnd();

        if(texturesAreOn) {
            gl2.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
            gl2.glDisable(GL_TEXTURE_2D);
        }
    }

    private void renderDoor() {
        gl2.glPushMatrix();
        gl2.glTranslatef(doorLeftDown[0], doorLeftDown[1], doorLeftDown[2]);
        gl2.glRotatef(doorRotationAngleY, 0.0f, 1.0f, 0.0f);
        gl2.glTranslatef(-doorLeftDown[0], -doorLeftDown[1], -doorLeftDown[2]);
        render(doorFaceQuad);
        gl2.glPopMatrix();
    }

    private void renderHouse(float[] position) {
        render(roofFrontFaceQuad);
        render(roofBackFaceQuad);
        render(roofRightSideTriangle);
        render(roofLeftSideTriangle);
        render(frontFaceQuadRIGHT);
        render(frontFaceQuadLEFT);
        render(frontFaceQuadTOP);
        renderDoor();
        render(leftSideFaceQuad);
        render(backFaceQuad);
        render(rightSideFaceQuad);
        render(bottomFaceQuad);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        super.display(glAutoDrawable);
        gl2.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl2.glLoadIdentity();
        glu.gluLookAt(cameraPos[0], cameraPos[1], cameraPos[2], origin[0], origin[1], origin[2], 0, 1, 0);
        gl2.glTranslatef(housePosition[0], housePosition[1], housePosition[2]);
        gl2.glScalef(scaleFactor, scaleFactor, scaleFactor);
        gl2.glRotatef(rotationAngleX, 1.0f, 0.0f, 0.0f);
        gl2.glRotatef(rotationAngleY, 0.0f, 1.0f, 0.0f);
        renderHouse(housePosition);
        update();
    }

    private void processKey(Integer code) {
        switch (code) {
            case VK_PAGE_UP: if(scaleFactor < 3) scaleFactor += 0.01f; break;
            case VK_PAGE_DOWN: if(scaleFactor > 0.03f) scaleFactor -= 0.01f; break;
            case VK_UP: rotationAngleX -= 1; break;
            case VK_DOWN: rotationAngleX += 1; break;
            case VK_LEFT: rotationAngleY -= 1; break;
            case VK_RIGHT: rotationAngleY += 1; break;
            case VK_NUMPAD6: housePosition[0] += 0.01f; break;
            case VK_NUMPAD4: housePosition[0] -= 0.01f; break;
            case VK_NUMPAD8: housePosition[1] += 0.01f; break;
            case VK_NUMPAD2: housePosition[1] -= 0.01f; break;
            case VK_NUMPAD9: housePosition[2] -= 0.01f; break;
            case VK_NUMPAD1: housePosition[2] += 0.01f; break;
            case VK_O: if(doorRotationAngleY > -140) doorRotationAngleY -= 4; break;
            case VK_C: if(doorRotationAngleY < 0) doorRotationAngleY += 4; break;
            case VK_ESCAPE: initializeVariables(); break;
        }
    }

    protected void update() {

        if(lightIsOn) gl2.glEnable(GL_LIGHT0);
        else gl2.glDisable(GL_LIGHT0);

        if(cameraRotationIsOn) {
            cameraRotationAngle += 0.01f;
            cameraPos[0] = (float) (cameraRotationRadius * cos(cameraRotationAngle));
            cameraPos[2] = (float) (cameraRotationRadius * sin(cameraRotationAngle));
        }

        listener.getPressed().forEach(this::processKey);
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new House(800, 600, "House", true));
        IntStream.range(1, 7).forEach(i -> out.println());
        Arrays.asList("Use the keys:",
                "PAGE_UP: zoom in, PAGE_DOWN: zoom out ",
                "UP: rotate up, DOWN: rotate down, LEFT: rotate left, RIGHT: rotate right ",
                "NUMPAD 8: move up, NUMPAD 2: move down, NUMPAD 6: move right, NUMPAD 4: move left, NUMPAD 9: move away, NUMPAD 1: move closer ",
                "O: open the door, C: close the door ",
                "ESC: reset to origin").forEach(out::println);
    }
}
