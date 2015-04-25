package me.justin.culminating;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by justin on 25/04/15.
 */
public abstract class Input {

    public abstract boolean isLeftPressed();
    public abstract boolean isRightPressed();
    public abstract boolean isJumpPressed();
    public abstract boolean isZoomInPressed();
    public abstract boolean isZoomOutPressed();
    public abstract boolean isDebugPressed();

    public static class DefaultInput extends Input {
        @Override
        public boolean isLeftPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A);
        }

        @Override
        public boolean isRightPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D);
        }

        @Override
        public boolean isJumpPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SPACE);
        }

        @Override
        public boolean isZoomInPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.Z);
        }

        @Override
        public boolean isZoomOutPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.X);
        }

        @Override
        public boolean isDebugPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.B);
        }
    }

}
