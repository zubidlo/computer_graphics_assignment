package assignment;

import java.awt.event.*;
import java.util.*;

/**
 * multi-key listener
 */
public class MultiKeyListener extends KeyAdapter {

    private final List<Integer> pressed;
    private volatile Integer lastPressed, lastReleased;

    public MultiKeyListener() {
        pressed = new ArrayList<Integer>();
        lastPressed = 0;
        lastReleased = 0;
    }

    /**
     * Returns list of pressed key codes. Thread safe.
     * @return list of pressed key codes.
     */
    public List<Integer> getPressed() {

        return new ArrayList<Integer>(pressed);
    }

    public int getLastPressed() {
        return lastPressed;
    }

    public void setLastPressed(int code) {
        lastPressed = code;
    }

    public int getLastReleased() {
        return lastReleased;
    }

    public void setLastReleased(int code) {
        lastReleased = code;
    }

    @Override
    public void keyPressed(KeyEvent e) {

        lastPressed = new Integer(e.getKeyCode());
        if(!pressed.contains(lastPressed)) pressed.add(lastPressed);

    }

    @Override
    public void keyReleased(KeyEvent e) {

        lastReleased = new Integer(e.getKeyCode());
        if(pressed.contains(lastReleased)) pressed.remove(lastReleased);
    }
}
