package com.karlgrauers.asteroidsgl.glTools;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.InputMismatchException;
import java.util.Scanner;



public class GLManager {
    private final static String TAG = "GLManager";
    private static final int OFFSET = 0; //just to have a name for the parameter
    private static int colorUniformHandle; //handle to the color setting
    private static int positionAttributeHandle; //handle to the vertex position setting
    private static int MVPMatrixHandle; //handle to the model-view-projection matrix
    private static final String vertexShaderCodeFile = "vertex_shader_code.txt";
    private static final String fragmentShaderCodeFile = "fragment_shader_code.txt";



    private static String readShaderString(final Resources resources, final String fileName) {
        final AssetManager assetManager = resources.getAssets();
        InputStream inputStream = null;

        String shaderString = "";

        try {
            inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 8000);

            try {
                Scanner scanner = new Scanner(reader);

                while (scanner.hasNext()) {
                    shaderString += scanner.nextLine() + "\n";

                }
                scanner.close();

            } catch (InputMismatchException e) {
                e.printStackTrace();
            }

            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return shaderString.trim();
    }



    private static void checkGLError(final String func) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(func, "glError " + error);
        }

    }

    private static int compileShader(final int type, final String shaderCode) {
        assert (type == GLES20.GL_VERTEX_SHADER || type == GLES20.GL_FRAGMENT_SHADER);
        final int handle = GLES20.glCreateShader(type); // Create a shader object and store its handle
        GLES20.glShaderSource(handle, shaderCode); // Pass in the code
        GLES20.glCompileShader(handle); // then compile the shader
        Log.d(TAG, "Shader Compile Log: \n" + GLES20.glGetShaderInfoLog(handle));
        checkGLError("compileShader");
        return handle;
    }

    private static int linkShaders(final int vertexShader, final int fragmentShader) {
        final int handle = GLES20.glCreateProgram();
        GLES20.glAttachShader(handle, vertexShader);
        GLES20.glAttachShader(handle, fragmentShader);
        GLES20.glLinkProgram(handle);
        Log.d(TAG, "Shader Link Log: \n" + GLES20.glGetProgramInfoLog(handle));
        checkGLError("linkShaders");
        return handle;
    }

    public static void buildProgram(Resources resources) {
        final String vertexShaderCode = readShaderString(resources, vertexShaderCodeFile);
        final String fragmentShaderCode = readShaderString(resources, fragmentShaderCodeFile);

        final int vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        final int fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        //handles to various GL objects:
        //handle to the compiled shader program
        int glProgramHandle = linkShaders(vertex, fragment);
        // delete the shaders as they're linked into our program now and no longer necessary
        GLES20.glDeleteShader(vertex);
        GLES20.glDeleteShader(fragment);
        //get the handles to our shader settings
        //so that we can manipulate these later
        positionAttributeHandle = GLES20.glGetAttribLocation(glProgramHandle, "position");
        colorUniformHandle = GLES20.glGetUniformLocation(glProgramHandle, "color");
        MVPMatrixHandle = GLES20.glGetUniformLocation(glProgramHandle, "modelViewProjection");
        //activate the program
        GLES20.glUseProgram(glProgramHandle);
        GLES20.glLineWidth(10f);
        checkGLError("buildProgram");
    }



    private static void setModelViewProjection(final float[] modelViewMatrix) {
        final int COUNT = 1;
        final boolean TRANSPOSED = false;
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, COUNT, TRANSPOSED, modelViewMatrix, OFFSET);
        checkGLError("setModelViewProjection");
    }


    float[] vertices = { // in counterclockwise order:
            0.0f, 0.5f, 0.0f,        // top
            -0.5f, -0.5f, 0.0f,        // bottom left
            0.5f, -0.5f, 0.0f        // bottom right
    };

    public static void draw(final Mesh model, final float[] modelViewMatrix, final float[] color){
        setShaderColor(color);
        uploadMesh(model._vertexBuffer);
        setModelViewProjection(modelViewMatrix);
        drawMesh(model._drawMode, model._vertexCount);
    }


    private static void uploadMesh(final FloatBuffer vertexBuffer) {
        final boolean NORMALIZED = false;
        // enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(GLManager.positionAttributeHandle);
        // prepare the vertex coordinate data
        GLES20.glVertexAttribPointer(GLManager.positionAttributeHandle, Mesh.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, NORMALIZED, Mesh.VERTEX_STRIDE,
                vertexBuffer);
        checkGLError("uploadMesh");
    }

    private static void setShaderColor(final float[] color) {
        final int COUNT = 1;
        // set color for drawing the pixels of our geometry
        GLES20.glUniform4fv(GLManager.colorUniformHandle, COUNT, color, OFFSET);
        checkGLError("setShaderColor");
    }

    private static void drawMesh(final int drawMode, final int vertexCount) {

        assert (drawMode == GLES20.GL_TRIANGLES
                || drawMode == GLES20.GL_LINES
                || drawMode == GLES20.GL_POINTS);
        // draw the previously uploaded vertices
        GLES20.glDrawArrays(drawMode, OFFSET, vertexCount);
        // disable vertex array
        GLES20.glDisableVertexAttribArray(GLManager.positionAttributeHandle);
        checkGLError("drawMesh");
    }

}
