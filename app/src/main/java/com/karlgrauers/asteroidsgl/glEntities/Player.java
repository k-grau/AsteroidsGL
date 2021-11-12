package com.karlgrauers.asteroidsgl.glEntities;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.karlgrauers.asteroidsgl.R;
import com.karlgrauers.asteroidsgl.glTools.Mesh;
import com.karlgrauers.asteroidsgl.utils.Utils;

public class Player extends GLEntity {
    private static final String TAG = "Player";
    private final float ROTATION_VELOCITY = Float.parseFloat(_game.resources.getString(R.string.rotation_velocity));
    private final float THRUST = Float.parseFloat(_game.resources.getString(R.string.thrust));
    private final float DRAG = Float.parseFloat(_game.resources.getString(R.string.drag));
    private final float TIME_BETWEEN_SHOTS = Float.parseFloat(_game.resources.getString(R.string.time_between_shots));
    private float _bulletCooldown = 0;
    private volatile boolean hasCollided = false;
    public volatile boolean isImmortal = false;
    private long collidedAt = 0;
    private final int IMMORTAL_FOR = _game.resources.getInteger(R.integer.immortal_for);



    public Player(final float x, final float y){
        super();
        hasCollided = false;
        _x = x;
        _y = y;
        _width = Float.parseFloat(_game.resources.getString(R.string.player_width));
        _height = Float.parseFloat(_game.resources.getString(R.string.player_height));
        _mesh = new Mesh(triangleVertrices, GLES20.GL_TRIANGLES);
        _mesh.setWidthHeight(_width, _height);
        _mesh.flipY();
    }


    @Override
    public boolean isColliding(final GLEntity that){
        if(!areBoundingSpheresOverlapping(this, that)){
            return false;
        }
        final PointF[] shipHull = getPointList();
        final PointF[] asteroidHull = that.getPointList();
        if(CollisionDetection.polygonVsPolygon(shipHull, asteroidHull)){
            return true;
        }
        return CollisionDetection.polygonVsPoint(asteroidHull, _x, _y); //finally, check if we're inside the asteroid
    }



    @Override
    public void onCollision(final GLEntity that) {
        collidedAt = Utils.activateTimeStamp();
        hasCollided = Utils.isActiveTimeStamp(collidedAt, IMMORTAL_FOR);
    }

    @Override
    public void update(double dt) {
        hasCollided = Utils.checkTimeStamp(hasCollided, Utils.isActiveTimeStamp(collidedAt, IMMORTAL_FOR));
        _rotation += (dt*ROTATION_VELOCITY) * _game._inputs._horizontalFactor;
        _game.updateFlamePosition(this);


        if(_game.getControls()._pressingB && !_game.getIsTeleporting()){
            final float theta = _rotation* (float)Utils.TO_RAD;
            _velX += (float)Math.sin(theta) * THRUST;
            _velY -= (float)Math.cos(theta) * THRUST;
        }
        _velX *= DRAG;
        _velY *= DRAG;

        _bulletCooldown -= dt;
        if(_game.getControls()._pressingA){
            setColors(1, 0, 1, 1);
            if(_bulletCooldown <= 0 && _game.maybeFireBullet(this)) {
                _bulletCooldown = TIME_BETWEEN_SHOTS;
            }

        } else{
            setColors(1.0f, 1, 1,1);
        }

         if(hasCollided) {
             setColors(1f, 0f, 0f, 1f);
             isImmortal = true;
         } else {
             isImmortal = false;
         }
        super.update(dt);
    }


    @Override
    public void render(float[] viewportMatrix) {
        //ask the super class (GLEntity) to render us
        super.render(viewportMatrix);
    }

}
