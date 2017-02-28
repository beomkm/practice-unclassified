package kr.ac.koreatech.opencvtest;

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

import kr.ac.koreatech.opencvtest.gl.VideoTextureRenderer;

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
        mTexture.setSurfaceTextureListener(this);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (mTexture.isAvailable())
            startPlaying();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mPlayer != null)
            mPlayer.release();
        if (mRenderer != null)
            mRenderer.onPause();
    }

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

            AssetFileDescriptor afd = getAssets().openFd("ryan.mp4");
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.setSurface(new Surface(mRenderer.getVideoTexture()));
            mPlayer.setLooping(true);
            mPlayer.setVolume(0f, 0f);
            mPlayer.prepare();
            mRenderer.setVideoSize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
            mPlayer.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open input video!");
        }
    }



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



