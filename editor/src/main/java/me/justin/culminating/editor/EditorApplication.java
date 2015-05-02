package me.justin.culminating.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import me.justin.culminating.Input;
import me.justin.culminating.World;
import me.justin.culminating.entities.GameObject;

/**
 * Created by justin on 25/04/15.
 */
public class EditorApplication extends ApplicationAdapter {
    //The possible editor states (editing terrain or editing entities)
    public static enum State {
        TERRAIN, ENTITY
    }

    public static interface StateChangeListener {
        public void onStateChange(EditorApplication app, State current, State next);
    }

    public static interface SelectedChangeListener {
        public void onStateChange(EditorApplication app, Object current, Object next);
    }

    public Input input;

    private State state = State.TERRAIN;
    private ArrayList<StateChangeListener> stateChangeListeners = new ArrayList<>();

    private Object currentlySelected = null;
    private ArrayList<SelectedChangeListener> selectedChangeListeners = new ArrayList<>();

    public EditorApplication(Input input) {
        this.input = input;
    }

    private float totalFrameTime = 0;
    private int frames = 0;

    private Level level;

    @Override
    public void create () {
        level = new Level(this);
    }

    @Override
    public void render () {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state == State.TERRAIN) {
            level.renderMetaballs();
        }
        else if (state == State.ENTITY) {

        }

        frames++;
        totalFrameTime += Gdx.graphics.getDeltaTime();
        if (totalFrameTime > 2) {
            System.out.println("Current time per frame (60fps is 16ms) " + totalFrameTime/frames*1000 + "ms");
            frames = 0;
            totalFrameTime = 0;
        }
    }

    public void addStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.add(listener);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        for (StateChangeListener l : stateChangeListeners)
            l.onStateChange(this, this.state, state);
        this.state = state;
    }

    public void addSelectedChangeListener(SelectedChangeListener listener) {
        selectedChangeListeners.add(listener);
    }

    public Object getCurrentlySelected() {
        return currentlySelected;
    }

    public void setCurrentlySelected(Object currentlySelected) {
        for (SelectedChangeListener l : selectedChangeListeners)
            l.onStateChange(this, this.currentlySelected, currentlySelected);
        this.currentlySelected = currentlySelected;
    }

    public void onAddEntityClicked(String type) {
        if (type == null || type.isEmpty()) return;
        System.out.println("Not Implemented");
    }

    public void onAddMetaballClicked() {
        Metaball newBall = new Metaball();
        level.metaballs.add(newBall);
        setCurrentlySelected(newBall);
    }

}