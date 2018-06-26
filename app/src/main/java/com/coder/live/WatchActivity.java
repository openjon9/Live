package com.coder.live;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.UnsupportedEncodingException;

public class WatchActivity extends AppCompatActivity {

    private TXCloudVideoView mView;
    private TXLivePlayer mLivePlayer;
    String flvUrl = "http://18455.liveplay.myqcloud.com/live/18455_9493ddef5a.flv";//直播收看地址
    private WatchActivity context;
    private TXLivePlayConfig mPlayConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        context = this;

        mView = (TXCloudVideoView) findViewById(R.id.video_view);
        //创建 player 对象 TXLivePlayer 模块负责实现直播播放功能
        mLivePlayer = new TXLivePlayer(this);
        mLivePlayer.setPlayerView(mView);


        mPlayConfig = new TXLivePlayConfig();
        //自动模式
        mPlayConfig.setAutoAdjustCacheTime(true);
        mPlayConfig.setMinAutoAdjustCacheTime(1);
        mPlayConfig.setMaxAutoAdjustCacheTime(5);

        mPlayConfig.setEnableMessage(true);
        mLivePlayer.setConfig(mPlayConfig);

        // mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);//将图像等比例缩放，适配最长边，缩放后的宽和高都不会超过显示区域，居中显示，画面可能会留有黑边。
        mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);//将图像等比例铺满整个屏幕，多余部分裁剪掉，此模式下画面不会留黑边，但可能因为部分区域被裁剪而显示不全。
        mLivePlayer.startPlay(flvUrl, TXLivePlayer.PLAY_TYPE_LIVE_FLV); //推荐 FLV


        mLivePlayer.setPlayListener(new MyITXLivePlayListener());

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLivePlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLivePlayer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLivePlayer.stopPlay(true); // true 代表清除最后一帧画面
        mView.onDestroy();
    }

    public class MyITXLivePlayListener implements ITXLivePlayListener {

        @Override
        public void onPlayEvent(int event, Bundle param) {
            switch (event) {
                case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://视频播放开始，如果有转菊花什么的这个时候该停了
                    Toast.makeText(context, "2004", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PLAY_EVT_PLAY_LOADING://视频播放 loading，如果能够恢复，之后会有 BEGIN 事件
                    Toast.makeText(context, "2007", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PLAY_EVT_GET_MESSAGE://用于接收夹在音视频流中的消息
                    String msg = null;
                    try {
                        msg = new String(param.getByteArray(TXLiveConstants.EVT_GET_MSG), "UTF-8");
                        //   roomListenerCallback.onRecvAnswerMsg(msg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case TXLiveConstants.PLAY_EVT_PLAY_END://视频播放结束
                    Toast.makeText(context, "视频播放结束", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PLAY_ERR_NET_DISCONNECT://网络断连,且经多次重连亦不能恢复,更多重试请自行重启播放
                    Toast.makeText(context, "网络断连,请自行重启播放", Toast.LENGTH_SHORT).show();
                    break;
            }

        }

        @Override
        public void onNetStatus(Bundle bundle) {

        }
    }
}
