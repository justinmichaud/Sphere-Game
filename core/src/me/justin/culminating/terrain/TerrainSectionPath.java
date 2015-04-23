package me.justin.culminating.terrain;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import java.util.ArrayList;

import me.justin.culminating.World;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainSectionPath extends TerrainSection {

    //I know I could have used fixtures instead, but this seemed like it would be fine
    public ArrayList<TerrainSectionPolygon> children = new ArrayList<TerrainSectionPolygon>();
    protected Vector2[] points;

    public TerrainSectionPath(World world, float x, float y, Vector2[] points, float thickness, float mass) {
        super(world,x,y,mass);

        this.points = points;

        for (int i=0; i<points.length-1; i++) {
            Vector2 topLeft = points[i];
            Vector2 topRight = points[i+1];

            Vector2 topEdge = topRight.cpy().sub(topLeft);
            Vector2 normal = new Vector2(-topEdge.y, topEdge.x).nor();

            Vector2 bottomRight = topRight.cpy().sub(normal.cpy().scl(thickness));
            Vector2 bottomLeft = topLeft.cpy().sub(normal.cpy().scl(thickness));

            children.add(new TerrainSectionPolygon(world, x, y, new Vector2[] {bottomLeft, bottomRight, topRight, topLeft}, mass));
        }
    }

    protected Vector2 getClosestSurfacePoint(Vector2 from) {

        Vector2 closestPoint = null;

        for (TerrainSectionPolygon ts : children) {
            Vector2 v = ts.getClosestSurfacePoint(from);
            if (closestPoint == null || v.dst2(from) < closestPoint.dst2(from))
                closestPoint = v;
        }

        return closestPoint;
    }

    @Override
    public float getDistance(Vector2 playerFeet) {
        return getClosestSurfacePoint(playerFeet).dst(playerFeet);
    }

    @Override
    public Vector2 getGravityDirection(Vector2 playerFeet) {
        return getClosestSurfacePoint(playerFeet).sub(playerFeet).nor();
    }

    @Override
    public void update() {

    }

    @Override
    public void renderShapes(ShapeRenderer renderer) {
        float[] vertsFloat = new float[points.length*2];

        for (int i=0; i<points.length; i++) {
            vertsFloat[i*2] = points[i].x;
            vertsFloat[i*2+1] = points[i].y;
        }

        renderer.polygon(vertsFloat);
    }
}
