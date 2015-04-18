package me.justin.culminating;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class CulminatingGame extends ApplicationAdapter {

    public World world;

    private float totalFrameTime = 0;
    private int frames = 0;
	
	@Override
	public void create () {
		world = new World();
	}

	@Override
	public void render () {
        world.update();

		Gdx.gl.glClearColor(1, 1, 1, 1);
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
