package me.justin.culminating.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import me.justin.culminating.entities.GameObject;

/**
 * Created by justin on 02/05/15.
 */
public class Level {

    public ArrayList<Metaball> metaballs = new ArrayList<Metaball>();
    public ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();

    public OrthographicCamera camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private EditorApplication editor;

    public Level(EditorApplication editor) {
        this.editor = editor;
    }

    public void renderMetaballs() {
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Metaball m : metaballs) {
            if (editor.getCurrentlySelected() != null
                    && editor.getCurrentlySelected().equals(m))
                shapeRenderer.setColor(Color.GREEN);
            else
                shapeRenderer.setColor(Color.RED);

            shapeRenderer.circle(m.x, m.y, m.radius);
        }

        //player zoom marker
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(camera.position.x - camera.viewportWidth*camera.zoom/2f + 1,
                camera.position.y - camera.viewportHeight*camera.zoom/2f + 1, 1, 30);
        shapeRenderer.end();
    }

}
