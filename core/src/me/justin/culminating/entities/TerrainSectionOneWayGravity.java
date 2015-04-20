package me.justin.culminating.entities;

import com.badlogic.gdx.math.Vector2;

import me.justin.culminating.World;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainSectionOneWayGravity extends TerrainSectionPolygon {

    private Vector2 cog;

    public TerrainSectionOneWayGravity(World world, float x, float y, Vector2[] vertices, Vector2 cog, float mass) {
        super(world, x, y, vertices, mass);
        this.cog = cog.cpy().add(position);
    }

    //http://stackoverflow.com/questions/7050186/find-if-point-lays-on-line-segment
    private boolean isOnLine(Vector2 point, Vector2 a, Vector2 b) {
        float x = point.x, y = point.y, x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
        double AB = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double AP = Math.sqrt((x-x1)*(x-x1)+(y-y1)*(y-y1));
        double PB = Math.sqrt((x2-x)*(x2-x)+(y2-y)*(y2-y));
        return Math.abs(AB - (AP + PB)) <=0.1;
    }

    @Override
    public float getDistance(Vector2 playerFeet) {
        return playerFeet.dst(cog);
    }

    @Override
    public Vector2 getGravityDirection(Vector2 playerFeet) {
        return new Vector2(0, -1).rotateRad(body.getAngle());
    }
}
