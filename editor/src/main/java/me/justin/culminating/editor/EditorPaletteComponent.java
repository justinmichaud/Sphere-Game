package me.justin.culminating.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Created by justin on 26/04/15.
 */
public class EditorPaletteComponent extends JPanel {

    public static enum State {
        NOTHING, METABALL, ENEMY
    }

    public State state = State.NOTHING;
    private ArrayList<JButton> childButtons = new ArrayList<JButton>();

    public EditorPaletteComponent() {
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (final State state : State.values()) {
            final JButton btn = new JButton(state.name().substring(0,1) + state.name().toLowerCase().substring(1));
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    EditorPaletteComponent.this.state = state;
                    btn.setEnabled(false);
                    for (JButton otherBtn : childButtons) {
                        otherBtn.setEnabled(true);
                    }
                    System.out.println(EditorPaletteComponent.this.state.name());
                }
            });
            add(btn);
            childButtons.add(btn);
        }
    }

}
