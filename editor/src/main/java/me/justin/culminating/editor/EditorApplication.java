package me.justin.culminating.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import me.justin.culminating.Input;
import me.justin.culminating.World;

/**
 * Created by justin on 25/04/15.
 */
public class EditorApplication extends ApplicationAdapter {

    public World world;
    public Input input;

    public EditorApplication(Input input) {
        this.input = input;
    }

    private float totalFrameTime = 0;
    private int frames = 0;

    @Override
    public void create () {
        world = new World(input);
        world.update();
    }

    @Override
    public void render () {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.render();

        frames++;
        totalFrameTime += Gdx.graphics.getDeltaTime();
        if (totalFrameTime > 2) {
            System.out.println("Current time per frame (60fps is 16ms) " + totalFrameTime/frames*1000 + "ms");
            frames = 0;
            totalFrameTime = 0;
        }
    }
}