package assignment;

import javax.swing.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * menu item
 * Created by martin on 09/04/2015.
 */
class MenuItem extends JMenuItem implements ActionListener {

    private final Consumer<ActionEvent> actionToPerform;

    public MenuItem(final String title, final Consumer<ActionEvent> action) {
        super(title);
        actionToPerform = action;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        //System.out.println(event.getActionCommand());
        actionToPerform.accept(event);
    }
}
