package me.justin.culminating;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import me.justin.culminating.entities.TerrainSection;
import me.justin.culminating.entities.TerrainSectionOneWayGravity;
import me.justin.culminating.entities.TerrainSectionPolygon;
import me.justin.culminating.entities.TerrainSectionSphere;

/**
 * Created by justin on 16/04/15.
 */
public class World {

    public Player player;
    public com.badlogic.gdx.physics.box2d.World physicsWorld;

    public ArrayList<GameObject> entities = new ArrayList<GameObject>();

    private Box2DDebugRenderer b2Renderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteRenderer;

    public OrthographicCamera camera;

    private float terrainScale = 0.055f;

    private Texture collisionBackground;

    public ArrayList<TerrainSection> terrain = new ArrayList<TerrainSection>();

    public World() {
        physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, 0), true);
        b2Renderer = new Box2DDebugRenderer(true, false, false, false, false, false);
        shapeRenderer = new ShapeRenderer();
        spriteRenderer = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f/15;
        player = new Player(this, new Vector2(0,0));
        entities.add(player);

        float g = 3;
        float[] balls = TerrainUtils.getBalls();

        int width = 2000, height = 2000;
        boolean[][] field = TerrainUtils.generateScalarField(balls, 0, 0, width, height);
        System.out.println("Done generating blobs & scalar field");

        Pixmap img = new Pixmap(width,height, Pixmap.Format.RGBA8888);

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                if (field[x][y]) img.drawPixel(x,y,Color.rgba8888(0,0,0,1));
                else img.drawPixel(x,y,Color.rgba8888(1, 1, 1, 1));
            }
        }

        collisionBackground = new Texture(img);

        System.out.println("Done loading blobs texture");

        ArrayList<TerrainSection> ts = TerrainUtils.loadFromMetaballs(this, field, terrainScale, 0.1f);
        for (TerrainSection t : ts) terrain.add(t);

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
        else camera.zoom = 1f/15;

        physicsWorld.step(1/60f, 6, 2);
        for (GameObject e : entities) e.update();

        camera.update();
    }

    public void render() {
        spriteRenderer.begin();
        spriteRenderer.setProjectionMatrix(camera.combined);
        spriteRenderer.draw(collisionBackground, 0, 0, collisionBackground.getWidth() * terrainScale,
                collisionBackground.getHeight() * terrainScale);
        spriteRenderer.end();

        if (Gdx.input.isKeyPressed(Input.Keys.B)) b2Renderer.render(physicsWorld, camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (GameObject e : entities) e.renderShapes(shapeRenderer);

        shapeRenderer.end();
    }


    //Claculate the weighted average of gravity directions
    public Vector2 calculateGravityDirection(Vector2 position, Iterable<TerrainSection> terrain) {
        Vector2 gravity = new Vector2();
        TerrainSection closest = null;

        for (TerrainSection s : terrain) {
            float dist = s.getDistance(position);
            if (dist < 500)
                gravity.add(s.getGravityDirection(position).scl(1f / (dist * dist) * s.mass));
        }

        return gravity.nor();
    }

}
