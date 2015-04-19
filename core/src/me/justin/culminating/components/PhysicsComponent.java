package me.justin.culminating.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import me.justin.culminating.World;
import me.justin.culminating.entities.Entity;

/**
 * Created by justin on 16/04/15.
 */
public class PhysicsComponent {

    public Body body;
    private Entity parent;

    public Vector2 position = new Vector2(0,0);

    public PhysicsComponent(Entity entity) {

        this.parent = entity;

        BodyDef playerDef = new BodyDef();
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.position.set(position);
        body = entity.world.physicsWorld.createBody(playerDef);

        {
            CircleShape shape = new CircleShape();
            shape.setRadius(1);

            FixtureDef def = new FixtureDef();
            def.shape = shape;
            def.density = 1f;
            def.friction = 0;
            def.restitution = 0;

            body.createFixture(def);
        }

        body.setUserData(parent);
    }


    //Apply a force relative to the current orientation (Gravity direction)
    public void applyLocalForce(float forceX, float forceY, Vector2 gravity) {
        Vector2 perpendicularGravity = new Vector2(-gravity.y, gravity.x).nor();

        //Move this frame
        body.applyForceToCenter(gravity.cpy().scl(forceY).add(perpendicularGravity.cpy().scl(forceX)), true);
    }

    public void applyFriction(Vector2 gravity) {
        Vector2 perpendicularGravity = new Vector2(-gravity.y, gravity.x).nor();

        //realistic friction would cause them to stick to walls, so we handle friction ourselves
        float horizontalVelocity = body.getLinearVelocity().dot(perpendicularGravity) * 0.8f;
        float verticalVelocity = Math.max(-1000, Math.min(1000, body.getLinearVelocity().dot(gravity)));
        body.setLinearVelocity(new Vector2(gravity.cpy().scl(verticalVelocity).add(perpendicularGravity.cpy().scl(horizontalVelocity))));
    }

    public void update() {
        this.position = body.getPosition();
    }

}
