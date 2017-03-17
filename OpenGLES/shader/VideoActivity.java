package kr.tibyte.shader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import kr.tibyte.shader.gl.VideoTextureRenderer;

public class VideoActivity extends Activity implements TextureView.SurfaceTextureListener{

    private TextureView mTexture;
    private MediaPlayer mPlayer;
    private VideoTextureRenderer mRenderer;

    private int surfaceWidth;
    private int surfaceHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);

        mTexture  = (TextureView)findViewById(R.id.surface);
        //이 클래스가 TextureView.SurfaceTextureListener를 implements 했기 때문에 이 클래스를 리스너로 지정가능
        mTexture.setSurfaceTextureListener(this);
    }


    //액티비티가 최상단에 나타났을 때 호출.
    //ex) 처음 시작시, 액티비티 구동 중 전화통화 후 복귀했을때 등
    @Override
    protected void onResume()
    {
        super.onResume();
        //텍스쳐뷰가 정상적으로 준비되었다면 함수 실행(동영상재생)
        if (mTexture.isAvailable())
            startPlaying();
    }

    //액티비티가 최상단에서 벗어났을 때 호출.
    //ex) 액티비티 구동 중 전화가 왔을 때 등.
    @Override
    protected void onPause()
    {
        super.onPause();
        if (mPlayer != null)
            mPlayer.release();
        if (mRenderer != null)
            mRenderer.onPause();
    }


    //액티비티 종료 시 호출
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mPlayer != null)
            mPlayer.release();

        if (mRenderer != null)
            mRenderer.onPause();

    }


    private void startPlaying()
    {
        mRenderer = new VideoTextureRenderer(this, mTexture.getSurfaceTexture(), surfaceWidth, surfaceHeight);
        mPlayer = new MediaPlayer();

        try
        {
            //app/src/main/assets 위치에 있는 파일 불러옴
            AssetFileDescriptor afd = getAssets().openFd("ryan.mp4");
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            //MediaPlayer 객체와 TextureView를 연결
            mPlayer.setSurface(new Surface(mRenderer.getVideoTexture()));

            //동영상 반복재생
            mPlayer.setLooping(true);
            //좌우음량
            mPlayer.setVolume(0f, 0f);

            //동영상 재생 전 점검을 위해 반드시 호출해야 하는 함수
            mPlayer.prepare();

            mRenderer.setVideoSize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
            mPlayer.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open input video!");
        }
    }



    //implements TextureView.SurfaceTextureListener 를 위해 구현해야 하는 함수 4개
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        surfaceWidth = width;
        surfaceHeight = height;
        startPlaying();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
    {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface)
    {
    }

}



