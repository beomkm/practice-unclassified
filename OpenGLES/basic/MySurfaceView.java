import android.content.Context;
import android.opengl.GLSurfaceView;

public class MySurfaceView extends GLSurfaceView {

    private final MyRenderer mRenderer;

    public MySurfaceView(Context context){
        super(context);

        setEGLContextClientVersion(2);

        mRenderer = new MyRenderer();
        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
