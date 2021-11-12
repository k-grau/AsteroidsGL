package com.karlgrauers.asteroidsgl.glEntities;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.karlgrauers.asteroidsgl.glTools.Mesh;
import com.karlgrauers.asteroidsgl.utils.Utils;



public class Bullet extends GLEntity{

    private static final Mesh BULLET_MESH = new Mesh(Mesh.POINT, GLES20.GL_POINTS); //Q&D pool, Mesh.POINT is just [0,0,0] float array
    private static final float SPEED = 120f;
    public static final float TIME_TO_LIVE = 3.0f; //seconds
    public final static String TAG = "Bullet";

    private float _ttl = TIME_TO_LIVE;

    public Bullet() {
        setColors(1, 0, 1, 1);
        _mesh = BULLET_MESH; //all bullets use the exact same mesh
        _mesh.flipY();
    }

    public void fireFrom(GLEntity source){
        final float theta = source._rotation*(float) Utils.TO_RAD;
        _x = source._x + (float)Math.sin(theta) * (source._width*0.5f);
        _y = source._y - (float)Math.cos(theta) * (source._height*0.5f);
        _velX = source._velX;
        _velY = source._velY;
        _velX += (float)Math.sin(theta) * SPEED;
        _velY -= (float)Math.cos(theta) * SPEED;
        _ttl = TIME_TO_LIVE;

    }


    @Override
    public boolean isColliding(final GLEntity that){
        if(!areBoundingSpheresOverlapping(this, that)){ //quick rejection
            return false;
        }
        final PointF[] asteroidVerts = that.getPointList();
        return CollisionDetection.polygonVsPoint(asteroidVerts, _x, _y);
    }

    @Override
    public boolean isDead(){
        return _ttl < 1;
    }

    @Override
    public void update(double dt) {
        if(_ttl > 0) {
            _ttl -= dt;
            super.update(dt);
        }
        if(_x == 0 || _x == _game.getWorldWidth()
                || _y == 0 || _y == _game.getWorldHeight()) {
            _ttl = 0;
        }
    }

    @Override
    public void render(final float[] viewportMatrix){
        if(_ttl > 0) {
            super.render(viewportMatrix);
        }
    }
}
