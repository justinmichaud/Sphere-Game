package me.justin.culminating;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainUtils {

    //Pairs of vertices will be extruded downwards if they are in order of left to right
    public static TerrainSection[] generatePath(World world, Vector2[] points, float thickness, float x, float y, float mass) {
        TerrainSection[] path = new TerrainSection[points.length-1];

        for (int i=0; i<points.length-1; i++) {
            Vector2 topLeft = points[i];
            Vector2 topRight = points[i+1];

            Vector2 topEdge = topRight.cpy().sub(topLeft);
            Vector2 normal = new Vector2(-topEdge.y, topEdge.x).nor();

            Vector2 bottomRight = topRight.cpy().sub(normal.cpy().scl(thickness));
            Vector2 bottomLeft = topLeft.cpy().sub(normal.cpy().scl(thickness));

            path[i] = new TerrainSectionPolygon(world, x, y, new Vector2[] {topLeft, topRight, bottomRight, bottomLeft}, mass);
        }

        return path;
    }
}
