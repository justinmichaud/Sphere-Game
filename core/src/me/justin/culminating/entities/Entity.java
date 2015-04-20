package me.justin.culminating.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import java.util.ArrayList;

import me.justin.culminating.World;

/**
 * Created by justin on 20/04/15.
 *
 * A gameobject with a bounding box and collision logic
 */
public abstract class Entity extends GameObject {

    //Maintain a list of all collisions with other objects so we know what state they are in
    protected ArrayList<GameObject> currentCollisions = new ArrayList<GameObject>();
    //The current terrain they are sticking to (for use with gravity, so they don't get pulled off a planet)
    protected ArrayList<TerrainSection> currentTerrain = new ArrayList<TerrainSection>();

    protected Body body;

    public Entity(World world, Vector2 position) {
        super(world);

        BodyDef playerDef = new BodyDef();
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.position.set(position);
        body = world.physicsWorld.createBody(playerDef);

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

        body.setUserData(this);
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

    //Claculate the current influence of gravity on the player
    protected Vector2 calculateGravity() {
        if (currentTerrain.isEmpty()) {
            return world.calculateGravityDirection(body.getPosition(), world.terrain);
        }
        else {
            return world.calculateGravityDirection(body.getPosition(), currentTerrain);
        }
    }

    //Callbacks for subclasses to implement logic
    //Called whenever we touch another game object
    protected abstract void onBeginContactWith(GameObject other);
    protected abstract void onEndContactWith(GameObject other);

    //Handle state changes when walking, falling and jumping
    public final void onBeginContact(Contact contact) {
        GameObject other = (GameObject) (contact.getFixtureA().getBody() == body ?
                contact.getFixtureB().getBody().getUserData() :
                contact.getFixtureA().getBody().getUserData());

        currentCollisions.add(other);
        if (other instanceof TerrainSection) currentTerrain.add((TerrainSection) other);

        onBeginContactWith(other);
    }

    public final void onEndContact(Contact contact) {
        GameObject other = (GameObject) (contact.getFixtureA().getBody() == body ?
                contact.getFixtureB().getBody().getUserData() :
                contact.getFixtureA().getBody().getUserData());

        currentCollisions.remove(other);
        if (other instanceof TerrainSection) currentTerrain.remove((TerrainSection) other);

        onEndContactWith(other);
    }
}
