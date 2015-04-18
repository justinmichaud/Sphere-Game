package me.justin.culminating.entities;

import me.justin.culminating.World;

/**
 * Created by justin on 16/04/15.
 */
public abstract class Entity {

    public World world;

    public Entity(World world) {
        this.world = world;
    }

    public abstract void update();
    //public abstract void render();
}
