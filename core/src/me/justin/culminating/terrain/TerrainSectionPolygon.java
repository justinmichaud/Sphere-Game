package me.justin.culminating.terrain;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import me.justin.culminating.World;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainSectionPolygon extends TerrainSection {

    protected PolygonShape shape;

    public TerrainSectionPolygon(World world, float x, float y, float hw, float hh, float mass) {
        this(world, x, y, new Vector2[]{new Vector2(-hw, -hh),new Vector2(-hw, hh),
                new Vector2(hw, hh),new Vector2(hw, -hh)}, mass);
    }

    public TerrainSectionPolygon(World world, float x, float y, Vector2[] vertices, float mass) {
        super(world,x,y,mass);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(position);
        body = world.physicsWorld.createBody(def);

        shape = new PolygonShape();
        shape.set(vertices);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1;
        fixture.friction = 0;

        body.createFixture(fixture);
        body.setUserData(this);
    }

    protected Vector2[] getVertices() {
        Vector2[] vertices = new Vector2[shape.getVertexCount()];
        Vector2 vertex = new Vector2();
        for (int i=0; i<vertices.length; i++) {
            shape.getVertex(i, vertex);
            vertices[i] = vertex.cpy().add(body.getPosition());
        }

        return vertices;
    }

    //We know the closest point is either a vertex, or an altitude from /from/ to an edge
    //This solution is a bit naive, as it checks every single possible point, but it is still
    //linear, so it should be good enough (tm)
    protected Vector2 getClosestSurfacePoint(Vector2 from) {

        Vector2[] vertices = getVertices();
        Vector2 closestPoint = vertices[0];

        for (int i=0; i<vertices.length; i++) {
            Vector2 v1 = vertices[i];
            Vector2 v2 = vertices[(i+1)%vertices.length];
            Vector2 v2v1 = v1.cpy().sub(v2);
            Vector2 v2From = from.cpy().sub(v2);

            //Find altitude intersection with v2v1:
            //leg of the right angle triangle /from altitudeIntersection v2/
            // = |from - v2| * cos angle between from, v2, v1
            float hyp = (float) Math.abs(v2From.len() * Math.cos(v2v1.angleRad(v2From)));
            Vector2 altitudePoint =  v2.cpy().add(v2v1.cpy().nor().scl(hyp));

            if (from.dst2(v1) < from.dst2(closestPoint)) closestPoint = v1;

            //We make sure we aren't outside the line
            if (hyp <= v2v1.len() && from.dst2(altitudePoint) < from.dst2(closestPoint)) closestPoint = altitudePoint;
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
        Vector2[] verts = getVertices();
        float[] vertsFloat = new float[verts.length*2];

        for (int i=0; i<verts.length; i++) {
            vertsFloat[i*2] = verts[i].x;
            vertsFloat[i*2+1] = verts[i].y;
        }

        renderer.polygon(vertsFloat);
    }
}
