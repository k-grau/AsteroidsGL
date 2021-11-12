package com.karlgrauers.asteroidsgl.glEntities;

import android.graphics.PointF;
import android.opengl.Matrix;

import com.karlgrauers.asteroidsgl.Game;
import com.karlgrauers.asteroidsgl.glTools.GLManager;
import com.karlgrauers.asteroidsgl.glTools.Mesh;

import java.util.Objects;

public class GLEntity {
    public static Game _game = null; //shared ref, managed by the Game-class!
    Mesh _mesh = null;
    final float[] _color = {1.0f, 1.0f, 1.0f, 1.0f}; //default white
    float _x = 0.0f;
    float _y = 0.0f;
    float _velX = 0f;
    float _velY = 0f;
    float _velR = 0f;
    final float _depth = 0.0f; //we'll use _depth for z-axis
    float _scale = 1f;
    float _rotation = 0f;
    static final float[] modelMatrix = new float[4*4];
    static final float[] viewportModelMatrix = new float[4*4];
    private static final float[] rotationViewportModelMatrix = new float[4*4];
    static final float[] triangleVertrices = {
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
    };
    float _width = 0.0f;
    float _height = 0.0f;
    private boolean _isAlive = true;





    GLEntity(){
    }

    void update(final double dt) {
        _x += _velX * dt;
        _y += _velY * dt;

        if(left() > _game.getWorldWidth()){
            setRight(0);
        }else if(right() < 0){
            setLeft(_game.getWorldWidth());
        }

        if(top() > _game.getWorldHeight()){
            setBottom(0);
        }else if(bottom() < 0){
            setTop(_game.getWorldHeight());
        }
    }



    private float left() {
        return _x+_mesh.left();
    }
    private float right() {
        return _x+_mesh.right();
    }
    private void setLeft(final float leftEdgePosition) {
        _x = leftEdgePosition - _mesh.left();
    }

    public void setBottom(final float bottomEdgePosition) {
        _y = bottomEdgePosition - _mesh.bottom();
    }

    private void setTop(final float topEdgePosition) {
        _y = topEdgePosition - _mesh.top();
    }

    private float bottom() {
        return _y+_mesh.bottom();
    }

    private float top() {
        return _y+_mesh.top();
    }


    private void setRight(final float rightEdgePosition) {
        _x = rightEdgePosition - _mesh.right();
    }

    public void setCustom(final int x, final int y){
        _y = y;
        _x = x;
    }


    public void render(final float[] viewportMatrix){
        final int OFFSET = 0;

        //reset the model matrix and then translate (move) it into world space
        Matrix.setIdentityM(modelMatrix, OFFSET); //reset model matrix
        Matrix.translateM(modelMatrix, OFFSET, _x, _y, _depth);
        //viewportMatrix * modelMatrix combines into the viewportModelMatrix
        //NOTE: projection matrix on the left side and the model matrix on the right side.
        Matrix.multiplyMM(viewportModelMatrix, OFFSET, viewportMatrix, OFFSET, modelMatrix, OFFSET);
        //apply a rotation around the Z-axis to our modelMatrix. Rotation is in degrees.
        Matrix.setRotateM(modelMatrix, OFFSET, _rotation, 0, 0, 1.0f);
        //apply scaling to our modelMatrix, on the x and y axis only.
        Matrix.scaleM(modelMatrix, OFFSET, _scale, _scale, 1f);
        //finally, multiply the rotated & scaled model matrix into the model-viewport matrix
        //creating the final rotationViewportModelMatrix that we pass on to OpenGL
        Matrix.multiplyMM(rotationViewportModelMatrix, OFFSET, viewportModelMatrix, OFFSET, modelMatrix, OFFSET);

        GLManager.draw(_mesh, rotationViewportModelMatrix, _color);
    }


    public void setColors(final float[] colors){
        Objects.requireNonNull(colors);
        assert(colors.length >= 4);
        setColors(colors[0], colors[1], colors[2], colors[3]);
    }
    void setColors(final float r, final float g, final float b, final float a){
        _color[0] = r; //red
        _color[1] = g; //green
        _color[2] = b; //blue
        _color[3] = a; //alpha (transparency)
    }

    public PointF[] getPointList(float _x, float _y){
        return _mesh.getPointList(this._x, this._y, _rotation);
    }

    PointF[] getPointList(){
        return _mesh.getPointList(_x, _y, _rotation);
    }

    public boolean isDead(){
        return !_isAlive;
    }

    public void onCollision(final GLEntity that) {
        _isAlive = false;
    }

    public boolean isColliding(final GLEntity that) {
        if (this == that) {
            throw new AssertionError("isColliding: You shouldn't test Entities against themselves!");
        }
        return GLEntity.isAABBOverlapping(this, that);
    }

    private float centerX() {
        return _x; //assumes our mesh has been centered on [0,0] (normalized)
    }

    private float centerY() {
        return _y; //assumes our mesh has been centered on [0,0] (normalized)
    }

    private float radius() {
        //use the longest side to calculate radius
        return (_width > _height) ? _width * 0.5f : _height * 0.5f;
    }

    //axis-aligned intersection test
    //returns true on intersection, and sets the least intersecting axis in the "overlap" output parameter
    static final PointF overlap = new PointF( 0 , 0 ); //Q&D PointF pool for collision detection. Assumes single threading.
    @SuppressWarnings("UnusedReturnValue")
    static boolean getOverlap(final GLEntity a, final GLEntity b, final PointF overlap) {
        overlap.x = 0.0f;
        overlap.y = 0.0f;
        final float centerDeltaX = a.centerX() - b.centerX();
        final float halfWidths = (a._width + b._width) * 0.5f;
        float dx = Math.abs(centerDeltaX); //cache the abs, we need it twice

        if (dx > halfWidths) return false ; //no overlap on x == no collision

        final float centerDeltaY = a.centerY() - b.centerY();
        final float halfHeights = (a._height + b._height) * 0.5f;
        float dy = Math.abs(centerDeltaY);

        if (dy > halfHeights) return false ; //no overlap on y == no collision

        dx = halfWidths - dx; //overlap on x
        dy = halfHeights - dy; //overlap on y
        if (dy < dx) {
            overlap.y = (centerDeltaY < 0 ) ? -dy : dy;
        } else if (dy > dx) {
            overlap.x = (centerDeltaX < 0 ) ? -dx : dx;
        } else {
            overlap.x = (centerDeltaX < 0 ) ? -dx : dx;
            overlap.y = (centerDeltaY < 0 ) ? -dy : dy;
        }
        return true ;
    }
    //Some good reading on bounding-box intersection tests:
//https://gamedev.stackexchange.com/questions/586/what-is-the-fastest-way-to-work-out-2d-bounding-box-intersection
    private static boolean isAABBOverlapping(final GLEntity a, final GLEntity b) {
        return !(a.right() <= b.left()
                || b.right() <= a.left()
                || a.bottom() <= b.top()
                || b.bottom() <= a.top());
    }

    static boolean areBoundingSpheresOverlapping(final GLEntity a, final GLEntity b) {
        final float dx = a.centerX()-b.centerX(); //delta x
        final float dy = a.centerY()-b.centerY();
        final float distanceSq = (dx*dx + dy*dy);
        final float minDistance = a.radius() + b.radius();
        final float minDistanceSq = minDistance*minDistance;
        return distanceSq < minDistanceSq;
    }


}
