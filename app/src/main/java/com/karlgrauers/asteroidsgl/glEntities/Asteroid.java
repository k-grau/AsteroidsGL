package com.karlgrauers.asteroidsgl.glEntities;

import android.opengl.GLES20;

import com.karlgrauers.asteroidsgl.R;
import com.karlgrauers.asteroidsgl.glTools.Mesh;
import com.karlgrauers.asteroidsgl.utils.*;

public class Asteroid extends GLEntity {
    public final static String TAG = "Asteroid";

    private final static float SMALL_ASTEROID = 8.0f;
    private final static float MEDIUM_ASTEROID = 11.0f;
    private final static float LARGE_ASTEROID = 14.0f;

    private final static int SMALL = 0;
    private final static int MEDIUM = 1;
    private final static int LARGE = 2;


    private static final float MAX_VEL = 2f;
    private static final float MIN_VEL = -2f;
    private double radius = 0;


    public Asteroid(final float x, final float y, int points, int size){

        if(points < 3){ points = 3; } //triangles or more, please. :)
        _x = x;
        _y = y;


        float heightWidthRatio = (float) (2 * Math.sin(2 * Math.PI / points *
                Math.floor((points + 2) / 4)) / (1 - Math.cos(2 * Math.PI / points
                * Math.floor(points / 2))));

        float velIncreaseMin = 0;
        float velIncreaseMax = 0;

        if(size == SMALL) {
            _width = SMALL_ASTEROID;
            velIncreaseMin = Float.parseFloat(_game.resources.getString(R.string.small_asteroid_increase_min));
            velIncreaseMax = Float.parseFloat(_game.resources.getString(R.string.small_asteroid_increase_max));


        } else if (size == MEDIUM) {
            _width = MEDIUM_ASTEROID;
            velIncreaseMin = Float.parseFloat(_game.resources.getString(R.string.medium_asteroid_increase_min));
            velIncreaseMax = Float.parseFloat(_game.resources.getString(R.string.medium_asteroid_increase_max));


        } else if(size == LARGE) {
            _width = LARGE_ASTEROID;
            velIncreaseMin = Float.parseFloat(_game.resources.getString(R.string.large_asteroid_increase_min));
            velIncreaseMax = Float.parseFloat(_game.resources.getString(R.string.large_asteroid_increase_max));
        }
        _height = _width * heightWidthRatio;
        _velX = setSpeed(velIncreaseMin, velIncreaseMax);
        _velY = setSpeed(velIncreaseMin, velIncreaseMax);
        _velR = setSpeed(velIncreaseMin, velIncreaseMax);

        radius = _width*0.5f;
        final float[] verts = Mesh.generateLinePolygon(points, radius);
        _mesh = new Mesh(verts, GLES20.GL_LINES);
        _mesh.setWidthHeight(_width, _height);
        _mesh.flipY();
    }


    public double getRadius() {
        return radius;
    }

    public float getLargeAsteroidRadius() {
        return LARGE_ASTEROID/2;
    }

    public float getMediumAsteroidRadius() {
        return MEDIUM_ASTEROID/2;
    }

    public float getSmallAsteroidRadius() {
        return SMALL_ASTEROID/2;
    }

    private float setSpeed(final float velIncreaseMin, final float velIncreaseMax) {
        int direction = Utils.RNG.nextInt(2);

        float velType = 0f;
        float velocity = 0f;

        if(direction == 0) {
            velType = MAX_VEL;
        } else {
            velType = MIN_VEL;
        }
        velocity = Utils.between(velType*velIncreaseMin, velType*velIncreaseMax);
        return velocity;
    }

    @Override
    public void update(double dt) {
        if(_velR < 0) {
            _rotation --;
        } else {
            _rotation ++;
        }
        super.update(dt);
    }
}
