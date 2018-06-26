package com.coder.live;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tencent.rtmp.TXLiveBase;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String sdkver = TXLiveBase.getSDKVersionStr();
        Log.d("liteavsdk", "liteav sdk version is : " + sdkver);

    }

    public void live(View view) {
        startActivity(new Intent(this, LiveActivity.class));
    }

    public void watchlive(View view) {
        startActivity(new Intent(this, WatchActivity.class));
    }

}
