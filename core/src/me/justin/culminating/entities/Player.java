package me.justin.culminating.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import me.justin.culminating.World;
import me.justin.culminating.terrain.TerrainSection;

public class Player extends Entity {

    private static enum PlayerState {
        WALKING, JUMPING, FALLING;
    }

    private PlayerState state = PlayerState.FALLING;

    public Player(World world, Vector2 position) {
        super(world, position);
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

        applyLocalForce(forceX, forceY, gravity);
        applyFriction(gravity);

        world.camera.position.set(body.getPosition().x, body.getPosition().y, 0);
        //interpolate between the current camera position and the desired one to make movement more smooth
        world.camera.up.scl(0.92f).add(new Vector3(-gravity.x, -gravity.y, 0).nor().scl(0.08f)).nor();
        body.setTransform(body.getPosition(), (float)Math.toRadians(gravity.angle() + 180));
    }

    @Override
    protected void onBeginContactWith(GameObject other) {
        if (other instanceof TerrainSection) state = PlayerState.WALKING;
    }

    @Override
    protected void onEndContactWith(GameObject other) {
        if (currentCollisions.isEmpty()) state = PlayerState.FALLING;
    }

    @Override
    public void renderShapes(ShapeRenderer renderer) {
        renderer.circle(body.getPosition().x, body.getPosition().y, 1, 30);
    }


}
