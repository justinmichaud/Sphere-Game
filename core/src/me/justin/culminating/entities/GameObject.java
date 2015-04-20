package me.justin.culminating.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import me.justin.culminating.World;

/**
 * Created by justin on 16/04/15.
 */
public abstract class GameObject {

    public World world;

    public GameObject(World world) {
        this.world = world;
    }

    public abstract void update();
    public abstract void renderShapes(ShapeRenderer renderer);
}
