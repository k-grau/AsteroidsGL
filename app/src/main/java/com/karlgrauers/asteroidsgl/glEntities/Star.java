package com.karlgrauers.asteroidsgl.glEntities;

import android.opengl.GLES20;

import com.karlgrauers.asteroidsgl.glTools.Mesh;

public class Star extends GLEntity {
    private static Mesh m = null; //Q&D pool

    public Star(final float x, final float y){
        super();
        _x = x;
        _y = y;
        setColors(135/255f, 206/255f, 235/255f, 1f);
        if(m == null) {

            m = new Mesh(Mesh.POINT, GLES20.GL_POINTS);
        }
        _mesh = m; //all Stars use the exact same Mesh instance.
        _mesh.flipY();
    }


}

