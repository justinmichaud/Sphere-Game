package me.justin.culminating.editor;

import javax.swing.SwingUtilities;

public class Editor {

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new EditorJFrame();
            }
        });
    }

}
