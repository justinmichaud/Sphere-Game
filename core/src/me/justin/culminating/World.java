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

import me.justin.culminating.entities.Player;

/**
 * Created by justin on 16/04/15.
 */
public class World {

    public Player player;
    public com.badlogic.gdx.physics.box2d.World physicsWorld;

    private Box2DDebugRenderer b2Renderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteRenderer;

    public OrthographicCamera camera;

    private float terrainScale = 0.07f;
    private String terrainImage = "test3.png";

    private Texture collisionBackground;

    public ArrayList<TerrainSection> terrain = new ArrayList<TerrainSection>();

    public World() {
        physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, 0), true);
        b2Renderer = new Box2DDebugRenderer(true, false, false, false, false, false);
        shapeRenderer = new ShapeRenderer();
        spriteRenderer = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f/15;
        player = new Player(this);

        collisionBackground = new Texture(terrainImage);

        ArrayList<TerrainSection> ts = TerrainUtils.loadFromImage(physicsWorld, new Pixmap(Gdx.files.internal(terrainImage)), terrainScale, 0.1f);
        for (TerrainSection t : ts) terrain.add(t);

        physicsWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (contact.getFixtureA().getBody().getUserData() instanceof Player
                        || contact.getFixtureB().getBody().getUserData() instanceof Player)
                    player.onCollideGround(contact);
            }

            @Override
            public void endContact(Contact contact) {
                if (contact.getFixtureA().getBody().getUserData() instanceof Player
                        || contact.getFixtureB().getBody().getUserData() instanceof Player)
                    player.onLeaveGround(contact);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void addPlanet(com.badlogic.gdx.physics.box2d.World world, int x, int y, float radius, float mass) {
        terrain.add(new TerrainSectionSphere(world, x, y, radius, mass));
    }

    private void addRectangle(com.badlogic.gdx.physics.box2d.World world, int x, int y, float width, float height, float mass) {
        terrain.add(new TerrainSectionPolygon(world, x, y, width, height, mass));
    }

    private void addOneWayPlatform(com.badlogic.gdx.physics.box2d.World world, int x, int y) {
        terrain.add(new TerrainSectionOneWayGravity(world, x, y, new Vector2[] {
                new Vector2(0,0),new Vector2(10,0),new Vector2(5,-5),
        }, new Vector2(5,0), 100));
    }

    public void update() {

        if (Gdx.input.isKeyPressed(Input.Keys.Z)) camera.zoom = 1f/50;
        else camera.zoom = 1f/15;

        physicsWorld.step(1/60f, 6, 2);
        player.update();

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

        shapeRenderer.circle(player.boundingBox.position.x, player.boundingBox.position.y, 1, 30);

        shapeRenderer.end();
    }

}
