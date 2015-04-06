package me.justin.culminating;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
    private ShapeRenderer renderer;
    public OrthographicCamera camera;

    public ArrayList<TerrainSection> terrain = new ArrayList<TerrainSection>();
	
	@Override
	public void create () {
		world = new World(new Vector2(0, 0), true);
        b2Renderer = new Box2DDebugRenderer(true, false, false, false, false, false);
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f/10;
        player = new Player(this);

        addRectangle(world, -100,0,10,5,100);
        addRectangle(world, 30,0,5,15,100);

        addPlanet(world, -60, 0, 3, 1);
        addPlanet(world, -80, 0, 3, 100);
        addPlanet(world, -30, 0, 30, 100);
        addPlanet(world, 20, 5, 10, 100);

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

	@Override
	public void render () {
        world.step(1/60f, 6, 2);
        player.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
		b2Renderer.render(world, camera.combined);

//        renderer.setProjectionMatrix(camera.combined);
//        renderer.setColor(Color.RED);
//        renderer.begin(ShapeRenderer.ShapeType.Line);
//        for (TerrainSection s : terrain) renderer.line(player.position, player.position.cpy().add(s.getGravityDirection(player.position).scl(10)));
//        for (TerrainSection s : terrain) ((TerrainSectionPolygon)s).dbg(renderer, player.position);
//        renderer.end();
	}
}
