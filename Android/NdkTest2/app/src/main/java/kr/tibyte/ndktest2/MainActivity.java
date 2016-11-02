package kr.tibyte.ndktest2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity
{



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView)findViewById(R.id.text_view);

        tv.setText(getNativeText());
    }

    static
    {
        System.loadLibrary("getNativeText");
    }
    public native String getNativeText();
}
