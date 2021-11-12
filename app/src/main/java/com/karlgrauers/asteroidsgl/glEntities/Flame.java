package com.karlgrauers.asteroidsgl.glEntities;

import android.opengl.GLES20;

import com.karlgrauers.asteroidsgl.glTools.Mesh;
import com.karlgrauers.asteroidsgl.utils.Utils;

public class Flame extends GLEntity {



    public Flame(){
        super();
        _width = 5f; //TODO: gameplay values!
        _height = 5f;
        _mesh = new Mesh(triangleVertrices, GLES20.GL_TRIANGLES);
        _mesh.setWidthHeight(_width, _height);
    }


    public void flameFrom(final GLEntity source){
        float theta = source._rotation*(float) Utils.TO_RAD;

        _x = source._x - (float)Math.sin(theta) * _width;
        _y = source._y + (float)Math.cos(theta) * _height;

        setColors(1f, 0f, 0f, 1f);
        _rotation = source._rotation;
    }



    @Override
    public void update(double dt) {
        super.update(dt);

    }

    @Override
    public void render(float[] viewportMatrix) {
        super.render(viewportMatrix);

    }

}
