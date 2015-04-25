package me.justin.culminating.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import me.justin.culminating.CulminatingGame;
import me.justin.culminating.Input;

/**
 * Created by justin on 25/04/15.
 */
public class EditorJFrame extends JFrame {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private CulminatingGame game;
    private LwjglAWTCanvas lwjglAWTCanvas;

// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests-lwjgl/src/com/badlogic/gdx/tests/lwjgl/SwingLwjglTest.java
    public EditorJFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container container = getContentPane();

        game = new CulminatingGame(new Input.DefaultInput());
        lwjglAWTCanvas = new LwjglAWTCanvas(game);
        Canvas canvas = lwjglAWTCanvas.getCanvas();

        container.add(canvas, BorderLayout.CENTER);

        pack();
        setSize(800, 600);
        setVisible(true);

        //Centre on screen: http://stackoverflow.com/questions/2442599/how-to-set-jframe-to-appear-centered-regardless-of-the-monitor-resolution
        setLocationRelativeTo(null);

        canvas.requestFocus();
    }
}
