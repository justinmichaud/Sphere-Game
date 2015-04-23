package me.justin.culminating.terrain;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import me.justin.culminating.World;
import me.justin.culminating.entities.GameObject;

/**
 * Created by justin on 05/04/15.
 */
public abstract class TerrainSection extends GameObject {

    public Vector2 position;
    public float mass = 1;
    public Body body;

    public TerrainSection(World world, float x, float y, float mass) {
        super(world);

        this.position = new Vector2(x, y);
        this.mass = mass;
    }

    public abstract float getDistance(Vector2 playerFeet);
    public abstract Vector2 getGravityDirection(Vector2 playerFeet);

}
