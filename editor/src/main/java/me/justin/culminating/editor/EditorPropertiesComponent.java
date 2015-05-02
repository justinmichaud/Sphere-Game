package me.justin.culminating.editor;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import me.justin.culminating.entities.GameObject;

/**
 * Created by justin on 26/04/15.
 */
public class EditorPropertiesComponent extends JPanel {

    private JTable table;
    private JLabel emptyLabel;

    public EditorPropertiesComponent(EditorApplication editor) {
        table = new JTable();
        emptyLabel = new JLabel("No Object Selected");
        add(table);
        add(emptyLabel);

        editor.addSelectedChangeListener(new EditorApplication.SelectedChangeListener() {
            @Override
            public void onStateChange(EditorApplication app, GameObject current, GameObject next) {
                if (next == null) setEmptyObject();
                else changeObject(next);
            }
        });

        setEmptyObject();
    }

    private void setEmptyObject() {
        table.setVisible(false);
        emptyLabel.setVisible(true);
    }

    public void changeObject(final Object obj) {

        if (obj == null) {
            setEmptyObject();
            return;
        }

        final ArrayList<String> properties = new ArrayList<String>();

        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().startsWith("editorGet")) {
                String property = method.getName().replace("editorGet", "");
                properties.add(property);
            }
        }

        table.setModel(new TableModel() {
            @Override
            public int getRowCount() {
                return properties.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int i) {
                return new String[] {"Property", "Value"}[i];
            }

            @Override
            public Class<?> getColumnClass(int i) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int i, int i2) {
                return (i2 == 1);
            }

            @Override
            public Object getValueAt(int i, int i2) {
                if (i2 == 0) {
                    return properties.get(i);
                }
                else {
                    try {
                        return obj.getClass().getMethod("editorGet" + properties.get(i)).invoke(obj).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }

            @Override
            public void setValueAt(Object o, int row, int column) {
                if (column == 0) return;
                try {
                    obj.getClass().getMethod("editorSet" + properties.get(row), String.class).invoke(obj, o.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void addTableModelListener(TableModelListener tableModelListener) {

            }

            @Override
            public void removeTableModelListener(TableModelListener tableModelListener) {

            }
        });

        emptyLabel.setVisible(false);
        table.setVisible(true);
    }

}
