package me.justin.culminating;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import java.util.ArrayList;

import me.justin.culminating.entities.Entity;
import me.justin.culminating.entities.GameObject;
import me.justin.culminating.entities.Player;
import me.justin.culminating.terrain.TerrainSection;
import me.justin.culminating.terrain.TerrainSectionOneWayGravity;
import me.justin.culminating.terrain.TerrainSectionPath;
import me.justin.culminating.terrain.TerrainSectionPolygon;
import me.justin.culminating.terrain.TerrainSectionSphere;

/**
 * Created by justin on 16/04/15.
 */
public class World {

    public Player player;
    public com.badlogic.gdx.physics.box2d.World physicsWorld;

    public ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();

    private Box2DDebugRenderer b2Renderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteRenderer;

    public OrthographicCamera camera;

    private float terrainScale = 0.055f;

    public ArrayList<TerrainSection> terrain = new ArrayList<TerrainSection>();

    public World() {
        physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, 0), true);
        b2Renderer = new Box2DDebugRenderer(true, false, false, false, false, false);
        shapeRenderer = new ShapeRenderer();
        spriteRenderer = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f/15;
        player = new Player(this, new Vector2(0,0));
        gameObjects.add(player);

        float g = 3;
        float[] balls = TerrainUtils.getBalls();

        int width = 5000, height = 5000;
        boolean[][] field = TerrainUtils.generateScalarField(balls, 0, 0, width, height);
        System.out.println("Done generating blobs & scalar field");

        ArrayList<TerrainSection> ts = TerrainUtils.loadFromMetaballs(this, field, terrainScale, 0.1f);
        for (TerrainSection t : ts) {
            terrain.add(t);
            gameObjects.add(t);
        }

        System.out.println("Done generating collision geometry");

        physicsWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                GameObject a = (GameObject) contact.getFixtureA().getBody().getUserData();
                GameObject b = (GameObject) contact.getFixtureB().getBody().getUserData();

                if (a instanceof Entity) ((Entity) a).onBeginContact(contact);
                if (b instanceof Entity) ((Entity) b).onBeginContact(contact);
            }

            @Override
            public void endContact(Contact contact) {
                GameObject a = (GameObject) contact.getFixtureA().getBody().getUserData();
                GameObject b = (GameObject) contact.getFixtureB().getBody().getUserData();

                if (a instanceof Entity) ((Entity) a).onEndContact(contact);
                if (b instanceof Entity) ((Entity) b).onEndContact(contact);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void addPlanet(com.badlogic.gdx.physics.box2d.World world, int x, int y, float radius, float mass) {
        terrain.add(new TerrainSectionSphere(this, x, y, radius, mass));
    }

    private void addRectangle(com.badlogic.gdx.physics.box2d.World world, int x, int y, float width, float height, float mass) {
        terrain.add(new TerrainSectionPolygon(this, x, y, width, height, mass));
    }

    private void addOneWayPlatform(com.badlogic.gdx.physics.box2d.World world, int x, int y) {
        terrain.add(new TerrainSectionOneWayGravity(this, x, y, new Vector2[] {
                new Vector2(0,0),new Vector2(10,0),new Vector2(5,-5),
        }, new Vector2(5,0), 100));
    }

    public void update() {

        if (Gdx.input.isKeyPressed(Input.Keys.Z)) camera.zoom = 1f/50;
        else if (Gdx.input.isKeyPressed(Input.Keys.X)) camera.zoom = 2;
        else camera.zoom = 1f/15;

        physicsWorld.step(1/60f, 6, 2);
        for (GameObject e : gameObjects) e.update();

        camera.update();
    }

    public void render() {
        if (Gdx.input.isKeyPressed(Input.Keys.B)) {
            b2Renderer.render(physicsWorld, camera.combined);
        }
        else {
            spriteRenderer.begin();
            spriteRenderer.setProjectionMatrix(camera.combined);
            //spriteRenderer.draw(collisionBackground, 0, 0, collisionBackground.getWidth() * terrainScale,
            //        collisionBackground.getHeight() * terrainScale);
            spriteRenderer.end();

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            for (GameObject e : gameObjects) e.renderShapes(shapeRenderer);

            shapeRenderer.end();
        }
    }


    //Claculate the weighted average of gravity directions
    public Vector2 calculateGravityDirection(Vector2 position, Iterable<TerrainSection> terrain) {
        Vector2 gravity = new Vector2();

        for (TerrainSection s : terrain) {
            //TODO clean this up
            if (s instanceof TerrainSectionPath) {
                for (TerrainSection ss : ((TerrainSectionPath) s).children) {
                    float dist = ss.getDistance(position);
                    if (dist < 500)
                        gravity.add(ss.getGravityDirection(position).scl(1f / (dist * dist) * s.mass));
                }
            }
            else {
                float dist = s.getDistance(position);
                if (dist < 500)
                    gravity.add(s.getGravityDirection(position).scl(1f / (dist * dist) * s.mass));
            }
        }

        return gravity.nor();
    }

}
