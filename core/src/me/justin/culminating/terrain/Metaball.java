package me.justin.culminating.terrain;

/**
 * Created by justin on 02/05/15.
 */
public class Metaball {

    public float x=0,y=0,radius=1;

    public float editorGetX() {
        return x;
    }

    public void editorSetX(String x) {
        this.x = Float.parseFloat(x);
    }

    public float editorGetY() {
        return y;
    }

    public void editorSetY(String y) {
        this.y = Float.parseFloat(y);
    }

    public float editorGetRadius() {
        return radius;
    }

    public void editorSetRadius(String radius) {
        this.radius = Float.parseFloat(radius);
    }
}
