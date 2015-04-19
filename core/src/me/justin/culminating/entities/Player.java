package me.justin.culminating.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import java.util.ArrayList;

import me.justin.culminating.TerrainSection;
import me.justin.culminating.World;
import me.justin.culminating.components.PhysicsComponent;

public class Player extends Entity {

    public PhysicsComponent physicsComponent;

    //Maintain a list of all collisions with the floor so we know when they can jump
    private ArrayList<Fixture> floorCollisions = new ArrayList<Fixture>();
    //The current terrain they are sticking to (so they don't get pulled off a planet)
    private ArrayList<TerrainSection> currentTerrain = new ArrayList<TerrainSection>();

    private static enum PlayerState {
        WALKING, JUMPING, FALLING;
    }

    private PlayerState state = PlayerState.FALLING;

    public Player(World world) {
        super(world);
        this.physicsComponent = new PhysicsComponent(this);
    }

    public void update() {

        Vector2 gravity = calculateGravity();

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

        physicsComponent.applyLocalForce(forceX, forceY, gravity);
        physicsComponent.applyFriction(gravity);
        physicsComponent.update();

        world.camera.position.set(physicsComponent.position.x, physicsComponent.position.y, 0);
        //interpolate between the current camera position and the desired one to make movement more smooth
        world.camera.up.scl(0.9f).add(new Vector3(-gravity.x, -gravity.y, 0).nor().scl(0.1f)).nor();
        physicsComponent.body.setTransform(physicsComponent.body.getPosition(), (float)Math.toRadians(gravity.angle() + 180));
    }

    @Override
    public void renderShapes(ShapeRenderer renderer) {
        renderer.circle(physicsComponent.position.x, physicsComponent.position.y, 1, 30);
    }

    //Claculate the current influence of gravity on the player
    private Vector2 calculateGravity() {
        if (currentTerrain.isEmpty()) {
            return world.calculateGravityDirection(physicsComponent.position, world.terrain);
        }
        else {
            return world.calculateGravityDirection(physicsComponent.position, currentTerrain);
        }
    }

    //Handle state changes when walking, falling and jumping
    public void onCollideGround(Contact contact) {
        state = PlayerState.WALKING;

        if (contact.getFixtureA().getBody() == physicsComponent.body) {
            floorCollisions.add(contact.getFixtureB());
            currentTerrain.add((TerrainSection) contact.getFixtureB().getBody().getUserData());
        }
        else {
            floorCollisions.add(contact.getFixtureA());
            currentTerrain.add((TerrainSection) contact.getFixtureA().getBody().getUserData());
        }
    }

    public void onLeaveGround(Contact contact) {

        if (contact.getFixtureA().getBody() == physicsComponent.body) {
            floorCollisions.remove(contact.getFixtureB());
            currentTerrain.remove((TerrainSection) contact.getFixtureB().getBody().getUserData());
        }
        else {
            floorCollisions.remove(contact.getFixtureA());
            currentTerrain.remove((TerrainSection) contact.getFixtureA().getBody().getUserData());
        }

        if (floorCollisions.isEmpty()) {
            if (state == PlayerState.WALKING) state = PlayerState.FALLING;
        }
    }
}
