package com.karlgrauers.asteroidsgl.utils;

import android.util.Log;

import static java.lang.Long.MAX_VALUE;

public class Utils {

    private final static String TAG = "Utils";



    public static float clamp(float val, final float min, final float max) {
        if(val > max) {
            val = max;
        } else if(val < min) {
            val = min;
        }
        return val;

    }

    public final static java.util.Random RNG = new java.util.Random();

    public static int spawn(final int axis, final int position) {
        return axis + position;
    }

    public static boolean checkTimeStamp(boolean getTimeStamp, boolean setTimeStamp) {

        if(getTimeStamp) {
            return setTimeStamp;
        }
        return setTimeStamp;
    }


    public static boolean isActiveTimeStamp(long activatedAt, final int DURATION) {
        long activeFor = System.currentTimeMillis() - activatedAt;

        return activeFor >= 0 && activeFor <= DURATION;
    }

    public static long activateTimeStamp() {
        long activatedAt = MAX_VALUE;
        activatedAt = System.currentTimeMillis();
        return activatedAt;
    }

    public static int between( final int min, final int max){
        return RNG.nextInt(max-min)+min;
    }

    public static float between( final float min, final float max){
        return min+RNG.nextFloat()*(max-min);
    }

    public static void expect(final boolean condition, final String tag) {
        Utils.expect(condition, tag, "Expectation was broken.");
    }

    public static void expect(final boolean condition, final String tag, final String message) {
        if(!condition) {
            Log.e(tag, message);
        }
    }
    public static void require(final boolean condition) {
        Utils.require(condition, "Assertion failed!");
    }
    public static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static final double TO_DEG = 180.0/Math.PI;
    public static final double TO_RAD = Math.PI/180.0;

}

