package me.justin.culminating;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by justin on 05/04/15.
 */
public abstract class TerrainSection {

    public Vector2 position;
    public float mass = 1;
    protected World world;
    protected Body body;

    public TerrainSection(World world, float x, float y, float mass) {
        this.world = world;
        this.position = new Vector2(x, y);
        this.mass = mass;
    }

    public abstract float getDistance(Vector2 playerFeet);
    public abstract Vector2 getGravityDirection(Vector2 playerFeet);

}
