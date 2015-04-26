package me.justin.culminating.editor;

/**
 * Created by justin on 26/04/15.
 */
public class TestEditorObject {

    private String name = "hehehe", property2 = "property2 Defaultk value";

    public void getSomething() {

    }

    public String editorGetName() {
        return name;
    }

    public void editorSetName(String name) {
        this.name = name;
    }


    public String editorGetProperty2() {
        return property2;
    }

    public void editorSetProperty2(String name) {
        this.property2 = name;
    }

}
