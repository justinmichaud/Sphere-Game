package me.justin.culminating;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import java.util.ArrayList;

public class Player {

    private Body body;
    private CulminatingGame game;

    public Vector2 position = new Vector2(20,60);

    //Maintain a list of all collisions with the floor so we know when they can jump
    private ArrayList<Fixture> floorCollisions = new ArrayList<Fixture>();
    //The current terrain they are sticking to (so they don't get pulled off a planet)
    private TerrainSection currentTerrain = null;

    private static enum PlayerState {
        WALKING, JUMPING, FALLING;
    }

    private PlayerState state = PlayerState.FALLING;

    public Player(CulminatingGame game) {
        this.game = game;

        BodyDef playerDef = new BodyDef();
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.position.set(position);
        body = game.world.createBody(playerDef);

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

    public void update() {

        Vector2 gravity = calculateGravity();
        Vector2 perpendicularGravity = new Vector2(-gravity.y, gravity.x).nor();

        float forceX, forceY;

        if (state == PlayerState.WALKING) forceY = 1000; //Stick to the ground when they move across the planet
        else forceY = 80;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) forceX = -500;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) forceX = 500;
        else forceX = 0;

        if (state == PlayerState.WALKING && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            forceY = -3000;
            state = PlayerState.JUMPING;
        }

        //Bring them back sooner if they stop holding the button for variable jump heights
        if (state == PlayerState.JUMPING && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            forceY += 100;
        }

        //realistic friction would cause them to stick to walls, so we handle friction ourselves
        float horizontalVelocity = body.getLinearVelocity().dot(perpendicularGravity) * 0.8f;
        float verticalVelocity = Math.max(-1000, Math.min(1000, body.getLinearVelocity().dot(gravity)));
        body.setLinearVelocity(new Vector2(gravity.cpy().scl(verticalVelocity).add(perpendicularGravity.cpy().scl(horizontalVelocity))));

        //Move this frame
        body.applyForceToCenter(gravity.cpy().scl(forceY).add(perpendicularGravity.cpy().scl(forceX)), true);
        position = body.getPosition();

        game.camera.position.set(position.x, position.y, 0);
        //interpolate between the current camera position and the desired one to make movement more smooth
        game.camera.up.scl(0.9f).add(-gravity.x * 0.1f, -gravity.y * 0.1f, 0).nor();
        body.setTransform(body.getPosition(), (float)Math.toRadians(gravity.angle() + 180));
    }

    //Claculate the current influence of gravity on the player
    private Vector2 calculateGravity() {
        if (currentTerrain == null) {
            //If they are in the air, we find he weighted average of the gravitational influences
            Vector2 gravity = new Vector2();
            TerrainSection closest = null;

            for (TerrainSection s : game.terrain) {
                float dist = s.getDistance(position);
                if (dist < 500)
                    //Weighted average
                    gravity.add(s.getGravityDirection(position).scl(1f/(dist*dist) * s.mass));
            }

            //We want constant gravity everywhere (so they don't get stuck)
            return gravity.nor();
        }

        if (currentTerrain == null) return Vector2.Zero;
        else return currentTerrain.getGravityDirection(position);
    }

    //Handle state changes when walking, falling and jumping
    public void onCollideGround(Contact contact) {
        state = PlayerState.WALKING;

        if (contact.getFixtureA().getBody() == body) {
            floorCollisions.add(contact.getFixtureB());
            currentTerrain = (TerrainSection) contact.getFixtureB().getBody().getUserData();
        }
        else {
            floorCollisions.add(contact.getFixtureA());
            currentTerrain = (TerrainSection) contact.getFixtureA().getBody().getUserData();
        }
    }

    public void onLeaveGround(Contact contact) {

        if (contact.getFixtureA().getBody() == body) {
            floorCollisions.remove(contact.getFixtureB());
        }
        else {
            floorCollisions.remove(contact.getFixtureA());
        }

        if (floorCollisions.isEmpty()) {
            currentTerrain = null;
            if (state == PlayerState.WALKING) state = PlayerState.FALLING;
        }
    }
}
