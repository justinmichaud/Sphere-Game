package me.justin.culminating;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainSectionOneWayGravity extends TerrainSectionPolygon {

    public TerrainSectionOneWayGravity(World world, float x, float y, float hw, float hh, float mass) {
        super(world, x, y, hw, hh, mass);
    }

    public TerrainSectionOneWayGravity(World world, float x, float y, Vector2[] vertices, float mass) {
        super(world, x, y, vertices, mass);
    }

    @Override
    public float getDistance(Vector2 playerFeet) {
        return super.getDistance(playerFeet);
    }

    @Override
    public Vector2 getGravityDirection(Vector2 playerFeet) {
        return new Vector2(0, -1);
    }
}
