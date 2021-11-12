package com.karlgrauers.asteroidsgl.glEntities;

import android.opengl.Matrix;

import com.karlgrauers.asteroidsgl.glTools.GLManager;
import com.karlgrauers.asteroidsgl.glTools.GLPixelFont;
import com.karlgrauers.asteroidsgl.glTools.Mesh;

public class Text extends GLEntity {
    private static final GLPixelFont FONT = new GLPixelFont();
    private static final float GLYPH_WIDTH = FONT.WIDTH;
    private static final float GLYPH_HEIGHT = FONT.HEIGHT;
    private static final float GLYPH_SPACING = 1f;

    private Mesh[] _meshes = null;
    private float _spacing = GLYPH_SPACING; //spacing between characters
    private float _glyphWidth = GLYPH_WIDTH;
    private String currentString = "";

    public Text(final String s, final float x, final float y) {
        setString(s);
        _x = x;
        _y = y;
        setScale(0.60f);
    }

    public Text(final String s) {
        setString(s);
    }

    public String getString() {
        return currentString;
    }

    @Override
    public void render(final float[] viewportMatrix){
        final int OFFSET = 0;
        for(int i = 0; i < _meshes.length; i++){
            if(_meshes[i] == null){ continue; }
            Matrix.setIdentityM(modelMatrix, OFFSET); //reset model matrix
            Matrix.translateM(modelMatrix, OFFSET, _x + (_glyphWidth+_spacing)*i, _y, _depth);
            Matrix.scaleM(modelMatrix, OFFSET, _scale, _scale, 1f);
            Matrix.multiplyMM(viewportModelMatrix, OFFSET, viewportMatrix, OFFSET, modelMatrix, OFFSET);
            GLManager.draw(_meshes[i], viewportModelMatrix, _color);
        }
    }


    @Override
    public void update(double dt) {
        /*super.update(dt);*/
    }



    private void setScale(float factor){
        _scale = factor;
        _spacing = GLYPH_SPACING*_scale;
        _glyphWidth = GLYPH_WIDTH*_scale;
        float _glyphHeight = GLYPH_HEIGHT * _scale;
        _height = _glyphHeight;
        _width = (_glyphWidth+_spacing)*_meshes.length;
    }

    public void setString(final String s){
        currentString = s;
        _meshes = FONT.getString(s);
    }

}
