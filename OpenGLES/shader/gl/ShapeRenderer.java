package kr.tibyte.shader.gl;


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
    //attribute : 버텍스 셰이더에서만 사용가능하며, 정점좌표, 텍스쳐좌표 정보 저장
    //uniform : 읽기전용 변수, 버텍스셰이더와 프래그먼트셰이더에서 모두 읽을 수 있음
    //varying : 버텍스셰이더에서 프래그먼트셰이더로 값을 전달하는 변수
    //자바에서 glsl(셰이더 코드) 쪽으로  attribute와 uniform 변수의 값을 전달할 수 있음
    //(하단 draw()함수 참고)

    private final String vsCode =
            //전달받은 정점 위치
            "attribute vec4 vPosition;" +
            //전달받은 텍스쳐 위치(원본 비트맵에서 읽어올 부분을 의미함. mUvs배열의 값이 여기로 전달됨)
            "attribute vec2 a_texCoord;" +
            //프래그먼트셰이더로 넘겨줄 값
            "varying vec2 v_texCoord;" +
            //변환행렬. (위치변환, 크기변환)
            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
                //기준위치를 행렬변환한 결과를 gl_Position에 대입하여 버텍스셰이더의 결과 출력함.
                //gl_Position은 내부적으로 정의된 변수.
                //uMVPMatrix이 4*4행렬이고 vPosition이 vec4(4*1행렬)이므로 곱하면 됨..
            "  gl_Position = uMVPMatrix * vPosition;" +
               //프래그먼트셰이더로 전달할 변수
            "  v_texCoord = a_texCoord;" +
            "}";


    // Source code of fragment shader
    private final String fsCode =
            //GPU가 소숫점 계산할 때 highp, mediump, lowp중 어떤 정밀도를 사용할 것인지 지정
            //버텍스셰이더에서는 기본값이 highp이기 때문에 별도 설정 필요없음
            //프래그먼트 셰이더에서는 정밀도를 반드시 지정해 주어야 함
            //openGL ES 2.0에서 프래그먼트 셰이더 정밀도 관련 이슈가 있기 때문
            "precision mediump float;" +
            "varying vec2 v_texCoord;" +
             //sampler2D는 텍스쳐(이미지)를 나타내는 변수타입
            "uniform sampler2D s_texture;" +
            "void main() {" +
               //gl_Position과 마찬가지로 내부적으로 정의된 gl_FragColor변수에 텍스쳐를 적용함
               //텍스쳐 원본에서 원하는 부분(v_texCoord)를 잘라서 사용함
            "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
            "}";

    //program : opengl 셰이더를 다루는 객체
    private int program;
    private int vertexShader;
    private int fragmentShader;

    //아래 rectCoords와 관련
    private FloatBuffer vertexBuffer;
    //아래 drawOrder와 관련
    private ShortBuffer listBuffer;

    //변환행렬(텍스쳐의 위치, 크기)
    private float[] mTransMatrix = new float[16];

    //비트맵 이미지의 핸들(id개념)
    private int mImageHandle;
    //이미지 원본에서 텍스쳐로 사용할 부분 지정
    private float[] mUvs;
    private FloatBuffer mUvBuffer;

    private int bmpWidth;
    private int bmpHeight;

    //정점당 좌표수. x,y,z 3개이므로 3
    static final int COORDS_PER_VERTEX = 3;

    //텍스쳐 정점 좌표
    static float rectCoords[] = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,   // bottom right
            1.0f,  1.0f, 0.0f }; // top right
    //정점 좌표 배열에 있는 요소 참고
    //반시계방향으로 삼각형 2개를 그려서 사각형을 만들어야 함
    private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};


    public ShapeRenderer(Bitmap bitmap) {
        //프로그램 객체 생성
        program = GLES20.glCreateProgram();
        //loadShader()는 이 함수 바로 아래쪽에 있는 함수.
        //위에서 작성한 셰이더 코드를 로드하여, 컴파일하는 작업 들어있음
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vsCode);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fsCode);
        GLES20.glAttachShader ( program, vertexShader );// add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // creates OpenGL ES program executables
        GLES20.glUseProgram(program);                  // use shader program
        //셰이더 소스(glsl) 로드 -> 컴파일 -> attach(셰이더를 프로그램객체에 추가) -> link -> use 의 순서!

        //정점위치를 지정할 때 사용할 버퍼
        //바이트버퍼 형태만 opengl es에서 사용가능하므로 변환함
        ByteBuffer bb = ByteBuffer.allocateDirect(rectCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(rectCoords);
        vertexBuffer.position(0);
        //버퍼에 데이터를 쓰고 나면 position이 가리키는 위치가 맨 뒤이기 때문에
        //맨앞으로 당김

        //텍스쳐위치를 지정할 때 사용할 버퍼
        ByteBuffer lb = ByteBuffer.allocateDirect(drawOrder.length*2);
        lb.order(ByteOrder.nativeOrder());
        listBuffer = lb.asShortBuffer();
        listBuffer.put(drawOrder);
        listBuffer.position(0);

        //비트맵으로부터 이미지 얻어옴
        mImageHandle = getImageHandle(bitmap);
        bmpWidth = bitmap.getWidth();
        bmpHeight = bitmap.getHeight();

        //원본 이미지에서 텍스쳐로 쓸 부분 지정(여기서는 이미지 전체)
        //값들의 순서는, 정점좌표와 drawOrder에 따름
        mUvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        //mUvs역시 바이트버터 형태로 마늗러준다...
        ByteBuffer bbUvs = ByteBuffer.allocateDirect(mUvs.length*4);
        bbUvs.order(ByteOrder.nativeOrder());
        mUvBuffer = bbUvs.asFloatBuffer();
        mUvBuffer.put(mUvs);
        mUvBuffer.position(0);
    }

    //위에서 사용한 함수. 셰이더소스 로드하고, 컴파일함
    public static int loadShader(int type, String shaderCode )
    {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    //텍스쳐뷰 갱신될때 60fps정도로 계속 호출되는 함수
    //VideoTextureRenderer클래스의 draw()함수에서 이 클래스의 draw()함수를 호출하고있음
    public void draw()
    {
        //위에서 만든 프로그램을 사용
        GLES20.glUseProgram(program);

        //glsl 버텍스셰이더에서 사용한 변수에 대한 핸들을 얻음
        //이 핸들을 통해 값을 넣어줄것임
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        int matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        //단위행렬을 만들고.. (0은 행렬 시작위치. 특별한 작업 없으므로 그냥 0)
        Matrix.setIdentityM(mTransMatrix, 0);
        //위치변환. translateM(대상행렬, 오프셋(행렬시작위치), x, y, z)
        //x와 y를 0.1씩 이동하는거..
        Matrix.translateM(mTransMatrix, 0, 0.1f, 0.1f, 0);
        //크기변환. scaleM(대상행렬, 오프셋(행렬시작위치), x, y, z)
        //x크기를 절반으로.
        //y크기는 비트맵의 원래 가로세로 비율에 맞춰서 조정함.
        //이 가로세로비율 곱해주지 않으면 직사각형모양의 원본이미지가 정사각형으로 늘여져서 나옴
        Matrix.scaleM(mTransMatrix, 0, 0.5f, 0.5f*((float)bmpHeight/bmpWidth), 0);


        //glVertexAttribPointer()을 하기 전에 핸들을 enable함.
        GLES20.glEnableVertexAttribArray(positionHandle);
        //핸들을 통해 vertexBuffer값을 glsl vPosition값을 전달함
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //false : nomalized 소수점 인코딩방식을 사용할 것인지. (사용안함)
        //0 : 바이트간격을 특별히 지정해줄것인지 (지정안함)

        //mUvBuffer값을 glsl의 a_texCoord로 전달함
        int texCoordLoc = GLES20.glGetAttribLocation(program, "a_texCoord");
        GLES20.glEnableVertexAttribArray(texCoordLoc);
        GLES20.glVertexAttribPointer(texCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);

        //mTransMatrix를 glsl uMVPMatrix로 전달함
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mTransMatrix, 0);

        //텍스쳐를 그림. (바인딩 -> Draw)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mImageHandle);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, listBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    private int getImageHandle(Bitmap bitmap)
    {
        int[] texNames = new int[1];
        GLES20.glEnable(GLES20.GL_BLEND);
        //배경이 투명한 이미지를 사용하기 위해 필요
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //Gen -> Active -> Bind 과정 필요
        //glGenTextures(개수(1개), 배열, 배열의 어디에 담을것인지 인덱스)
        GLES20.glGenTextures(1, texNames, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNames[0]);
        //이미지 확대시 어떻게 표현될 것인지 지정함(MIN_FILTER, MAG_FILTER)
        //GL_LINEAR일 시 안티앨리어싱을 하고, GL_NEAREST일 때는 안티앨리어싱 없이 거칠게 확대
        //http://skyfe.tistory.com/entry/iOS-OpenGL-ES-%ED%8A%9C%ED%86%A0%EB%A6%AC%EC%96%BC-11%ED%8E%B8 참고
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return texNames[0];
    }

}