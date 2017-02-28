package kr.ac.koreatech.opencvtest.gl;


import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


public class ShapeRenderer {
    // Source code of vertex shader
    private final String vsCode =
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";

    // Source code of fragment shader
    private final String fsCode =
            "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
            "}";

    private int program;
    private int vertexShader;
    private int fragmentShader;
    private FloatBuffer vertexBuffer;
    private ShortBuffer listBuffer;
    private int vertexCount = 3;

    private float[] mTransMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mImageHandle;
    private float[] mUvs;
    private FloatBuffer mUvBuffer;

    private int bmpWidth;
    private int bmpHeight;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // in counterclockwise order:
            0.0f+0.3f,  0.1f, 0f, // top vertex
            -0.1f+0.3f,  -0.1f, 0f, // bottom left
            0.1f+0.3f,  -0.1f, 0f  // bottom right
    };


    static float rectCoords[] = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,   // bottom right
            1.0f,  1.0f, 0.0f }; // top right
    private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};



    // Set color of displaying object
    // with red, green, blue and alpha (opacity) values
    float color[] = { 0.2f, 0.8f, 0.2f, 1.0f };

    // Create a Triangle object
    public ShapeRenderer(Bitmap bitmap) {
        // create empty OpenGL ES Program, load, attach, and link shaders
        program = GLES20.glCreateProgram();
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vsCode);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fsCode);
        GLES20.glAttachShader ( program, vertexShader );// add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // creates OpenGL ES program executables
        GLES20.glUseProgram(program);                  // use shader program

        ByteBuffer bb = ByteBuffer.allocateDirect(rectCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(rectCoords);
        vertexBuffer.position(0);

        ByteBuffer lb = ByteBuffer.allocateDirect(drawOrder.length*2);
        lb.order(ByteOrder.nativeOrder());
        listBuffer = lb.asShortBuffer();
        listBuffer.put(drawOrder);
        listBuffer.position(0);

        mImageHandle = getImageHandle(bitmap);
        bmpWidth = bitmap.getWidth();
        bmpHeight = bitmap.getHeight();


        mUvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        ByteBuffer bbUvs = ByteBuffer.allocateDirect(mUvs.length*4);
        bbUvs.order(ByteOrder.nativeOrder());
        mUvBuffer = bbUvs.asFloatBuffer();
        mUvBuffer.put(mUvs);
        mUvBuffer.position(0);
    }

    public static int loadShader(int type, String shaderCode )
    {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(program);

        // get handle to vertex shader's attribute variable vPosition
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        int matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        Matrix.setIdentityM(mTransMatrix, 0);
        Matrix.translateM(mTransMatrix, 0, 0.1f, 0.1f, 0);
        Matrix.scaleM(mTransMatrix, 0, 0.5f, 0.5f*((float)bmpHeight/bmpWidth), 0);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        int texCoordLoc = GLES20.glGetAttribLocation(program, "a_texCoord");
        GLES20.glEnableVertexAttribArray(texCoordLoc);
        GLES20.glVertexAttribPointer(texCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);


        //GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mTransMatrix, 0);

        // Draw the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mImageHandle);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, listBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    private int getImageHandle(Bitmap bitmap)
    {
        int[] texNames = new int[1];
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glGenTextures(1, texNames, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return texNames[0];
    }

}