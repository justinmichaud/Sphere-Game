package me.justin.culminating;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class CulminatingGame extends ApplicationAdapter {

    public Player player;
    public World world;
    private Box2DDebugRenderer b2Renderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteRenderer;
    public OrthographicCamera camera;

    private float totalFrameTime = 0;
    private int frames = 0;

    private float terrainScale = 0.071f;
    private String terrainImage = "test3.png";

    private Texture collisionBackground;

    public ArrayList<TerrainSection> terrain = new ArrayList<TerrainSection>();
	
	@Override
	public void create () {
		world = new World(new Vector2(0, 0), true);
        b2Renderer = new Box2DDebugRenderer(true, false, false, false, false, false);
        shapeRenderer = new ShapeRenderer();
        spriteRenderer = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f/15;
        player = new Player(this);

        collisionBackground = new Texture(terrainImage);

//        TerrainSection[] path = TerrainUtils.generatePath(world, new Vector2[] {
//                new Vector2(0,0),
//                new Vector2(10,0),
//                new Vector2(20,10),
//                new Vector2(20, 40),
//                new Vector2(10, 50),
//                new Vector2(0, 50),
//                new Vector2(0, 55),
//                new Vector2(15, 55),
//                new Vector2(25, 40),
//                new Vector2(25, 10),
//                new Vector2(10, -5),
//                new Vector2(0, -5),
//                new Vector2(0, 0),
//        }, 1, 30, 0, 10);
//        for (TerrainSection t : path) terrain.add(t);
//
//        addOneWayPlatform(world, 5, 0);
//
//        addPlanet(world, -15,0,10, 100);
//        addPlanet(world, -30,15,5, 100);
//        addRectangle(world, -25,30,5,2, 100);
//
//        addOneWayPlatform(world, -15, 30);
//        addOneWayPlatform(world, 5, 30);

        ArrayList<TerrainSection> ts = TerrainUtils.loadFromImage(world, new Pixmap(Gdx.files.internal(terrainImage)), terrainScale, 0.5f);
        for (TerrainSection t : ts) terrain.add(t);

        world.setContactListener(new ContactListener() {
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

    private void addPlanet(World world, int x, int y, float radius, float mass) {
        terrain.add(new TerrainSectionSphere(world, x, y, radius, mass));
    }

    private void addRectangle(World world, int x, int y, float width, float height, float mass) {
        terrain.add(new TerrainSectionPolygon(world, x, y, width, height, mass));
    }

    private void addOneWayPlatform(World world, int x, int y) {
        terrain.add(new TerrainSectionOneWayGravity(world, x, y, new Vector2[] {
                new Vector2(0,0),new Vector2(10,0),new Vector2(5,-5),
        }, new Vector2(5,0), 100));
    }

	@Override
	public void render () {
        world.step(1/60f, 6, 2);
        player.update();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        spriteRenderer.begin();
        spriteRenderer.setProjectionMatrix(camera.combined);
        spriteRenderer.draw(collisionBackground, 0, 0, collisionBackground.getWidth() * terrainScale,
                collisionBackground.getHeight() * terrainScale);
        spriteRenderer.end();

		if (Gdx.input.isKeyPressed(Input.Keys.B)) b2Renderer.render(world, camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.circle(player.position.x, player.position.y, 1, 30);

        shapeRenderer.end();

        frames++;
        totalFrameTime += Gdx.graphics.getDeltaTime();
        if (totalFrameTime > 2) {
            System.out.println("Current time per frame (60fps is 16ms) " + totalFrameTime/frames*1000 + "ms");
            frames = 0;
            totalFrameTime = 0;
        }
    }
}
