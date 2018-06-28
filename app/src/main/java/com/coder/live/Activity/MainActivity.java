package com.coder.live.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.coder.live.Activity.JoinLiveGroupActivity;
import com.coder.live.Activity.LiveActivity;
import com.coder.live.Activity.LiveGroupActivity;
import com.coder.live.Activity.WatchActivity;
import com.coder.live.Class.TCConstants2;
import com.coder.live.R;
import com.tencent.rtmp.TXLiveBase;


public class MainActivity extends AppCompatActivity {

    private EditText edtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String sdkver = TXLiveBase.getSDKVersionStr();
        Log.d("liteavsdk", "liteav sdk version is : " + sdkver);

        edtext = (EditText) findViewById(R.id.edtext);

    }

    public void live(View view) {
        startActivity(new Intent(this, LiveActivity.class));
    }

    public void watchlive(View view) {
        if (TCConstants2.GROUP_ID.equals("")) {
            Toast.makeText(this, "請先設定群組ID", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, WatchActivity.class));
    }

    public void createLive(View view) {
        startActivity(new Intent(this, LiveGroupActivity.class));
    }

    public void joinLive(View view) {
        startActivity(new Intent(this, JoinLiveGroupActivity.class));
    }

    public void setGroupID(View view) {
        if (edtext.getText().toString().equals("")) {
            Toast.makeText(this, "不能為空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            TCConstants2.GROUP_ID = edtext.getText().toString();
        }
    }
}
