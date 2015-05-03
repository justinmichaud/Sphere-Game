package me.justin.culminating;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by justin on 25/04/15.
 */
public abstract class Input {

    //Called before asking for input events, but after libgdx is initialized
    public abstract void init();

    public abstract boolean isLeftPressed();
    public abstract boolean isRightPressed();
    public abstract boolean isJumpPressed();
    public abstract boolean isZoomInPressed();
    public abstract boolean isZoomOutPressed();
    public abstract boolean isDebugPressed();
    public abstract boolean isArrowLeftPressed();
    public abstract boolean isArrowRightPressed();
    public abstract boolean isArrowUpPressed();
    public abstract boolean isArrowDownPressed();
    public abstract boolean isIncreaseScalePressed();
    public abstract boolean isDecreaseScalePressed();

    public abstract boolean isPendingTouchEvent();
    public abstract Vector2 getTouchLocation();

    public static class DefaultInput extends Input implements InputProcessor {

        private int scrollPosition = 0;
        private boolean pendingTouch = false, touchDown = false;

        @Override
        public void init() {
            Gdx.input.setInputProcessor(this);
        }

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

        @Override
        public boolean isArrowLeftPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT);
        }

        @Override
        public boolean isArrowRightPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT);
        }

        @Override
        public boolean isArrowUpPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP);
        }

        @Override
        public boolean isArrowDownPressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN);
        }

        @Override
        public boolean isIncreaseScalePressed() {
            //for some reason, equaps isn't working, so I'm using p and O instead
            return  Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.P);
        }

        @Override
        public boolean isDecreaseScalePressed() {
            return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.O);
        }

        @Override
        public boolean isPendingTouchEvent() {
            boolean pending = pendingTouch;
            pendingTouch = false;
            return pending;
        }

        @Override
        public Vector2 getTouchLocation() {
            return new Vector2(Gdx.input.getX(), Gdx.input.getY());
        }

        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (!touchDown) {
                touchDown = true;
                pendingTouch = true;
                return true;
            }
            else return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            touchDown = false;
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            scrollPosition += amount;
            return true;
        }
    }

}
