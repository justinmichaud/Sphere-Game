package me.justin.culminating.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * Created by justin on 26/04/15.
 */
public class EditorToolbarComponent extends JToolBar {

    private EditorApplication editor;
    private ArrayList<JButton> stateButtons = new ArrayList<JButton>();
    private JLabel currentStateLabel;

    public EditorToolbarComponent(final EditorJFrame jFrame, final EditorApplication editor) {
        this.editor = editor;
        this.currentStateLabel = new JLabel(editor.getState().toString());

        setFloatable(false);

        int currentKeybinding = 1; //F key to bind current toolbar item to
        for (final EditorApplication.State state : EditorApplication.State.values()) {
            final JButton btn = new JButton(state.name().substring(0,1) + state.name().toLowerCase().substring(1));
            Action action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    EditorToolbarComponent.this.editor.setState(state);
                    btn.setEnabled(false);
                    for (JButton otherBtn : stateButtons) {
                        if (otherBtn != btn) otherBtn.setEnabled(true);
                    }
                    currentStateLabel.setText(EditorToolbarComponent.this.editor.getState().toString());
                }
            };
            if (editor.getState() == state) btn.setEnabled(false);
            btn.addActionListener(action);
            btn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F" + currentKeybinding++), state.toString());
            btn.getActionMap().put(state.toString(), action);
            add(btn);
            stateButtons.add(btn);
        }

        add(new JToolBar.Separator());
        JButton createEntityBtn = new JButton("Add Entity");
        Action createAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (editor.getState() != EditorApplication.State.ENTITY) return;
                EditorToolbarComponent.this.editor.onAddEntityClicked(
                        JOptionPane.showInputDialog(jFrame,"Enter Class Name","Add Entity", JOptionPane.PLAIN_MESSAGE)
                );
            }
        };
        createEntityBtn.addActionListener(createAction);
        createEntityBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F" + currentKeybinding++), "CREATE_ENTITY");
        createEntityBtn.getActionMap().put("CREATE_ENTITY", createAction);
        add(createEntityBtn);

        JButton createMetaballBtn = new JButton("Add Metaball");
        Action createMetaballAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (editor.getState() != EditorApplication.State.TERRAIN) return;
                editor.onAddMetaballClicked();
            }
        };
        createMetaballBtn.addActionListener(createMetaballAction);
        createMetaballBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F" + currentKeybinding++), "CREATE_METABALL");
        createMetaballBtn.getActionMap().put("CREATE_METABALL", createMetaballAction);
        add(createMetaballBtn);

        add(new JToolBar.Separator());
        add(currentStateLabel);
    }

}
