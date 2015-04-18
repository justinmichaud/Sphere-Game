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
public class BoundingBox {

    public Body body;
    private Entity parent;

    public Vector2 position = new Vector2(0,0);

    public BoundingBox(Entity entity) {

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

    public void update() {
        this.position = body.getPosition();
    }

}
