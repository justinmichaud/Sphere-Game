package me.justin.culminating;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainSectionSphere extends TerrainSection {

    public float radius = 1;

    public TerrainSectionSphere(World world, float x, float y, float radius, float mass) {
        super(world,x,y,mass);
        this.radius = radius;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(position);
        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1;
        fixture.friction = 0;

        body.createFixture(fixture);
        body.setUserData(this);
    }

    @Override
    public float getDistance(Vector2 playerFeet) {
        return position.dst(playerFeet) - radius;
    }

    @Override
    public Vector2 getGravityDirection(Vector2 playerFeet) {
        return position.cpy().sub(playerFeet).nor();
    }
}
