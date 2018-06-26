package com.coder.live;



import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.TIMConnListener;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMGroupManager;
import com.tencent.TIMLogListener;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMUser;
import com.tencent.TIMUserStatusListener;
import com.tencent.TIMValueCallBack;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import java.util.List;
import tencent.tls.platform.TLSAccountHelper;
import tencent.tls.platform.TLSLoginHelper;


public class LiveActivity extends AppCompatActivity {

    private TXLivePusher mLivePusher;
    private TXLivePushConfig mLivePushConfig;
    private TXCloudVideoView mCaptureView;
    String rtmpUrl = "rtmp://18455.livepush.myqcloud.com/live/18455_9493ddef5a?bizid=18455&txSecret=4959c8bb3d086e198aed4662f0ae6179&txTime=5B2D1CFF";//推流地址
    private LiveActivity context;
    private TLSLoginHelper loginHelper;
    private TLSAccountHelper accountHelper;
    private TIMConversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        context = this;

        mCaptureView = (TXCloudVideoView) findViewById(R.id.video_view);

        //TXLivePusher 負責推流
        mLivePusher = new TXLivePusher(this);
        mLivePushConfig = new TXLivePushConfig();
        mLivePusher.setConfig(mLivePushConfig);

        mLivePusher.startCameraPreview(mCaptureView);
        mLivePusher.startPusher(rtmpUrl);

        mLivePusher.setPushListener(new MyITXLivePushListener());

    }

    @Override
    public void onResume() {
        super.onResume();
        mCaptureView.onResume();     // mCaptureView 是摄像头的图像渲染view
        mLivePusher.resumePusher();  // 通知 SDK 重回前台推流
    }

    @Override
    public void onStop() {
        super.onStop();
        mCaptureView.onPause();  // mCaptureView 是摄像头的图像渲染view
        mLivePusher.pausePusher(); // 通知 SDK 进入“后台推流模式”了
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRtmpPublish();
    }

    public void camera_change(View view) {
        mLivePusher.switchCamera();
    }

    public class MyITXLivePushListener implements ITXLivePushListener {

        @Override
        public void onPushEvent(int event, Bundle param) {
            switch (event) {
                case TXLiveConstants.PUSH_ERR_NET_DISCONNECT:
                    Toast.makeText(context, "网络断连,请自行重启推流", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL:
                    Toast.makeText(context, "打开摄像头失败", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL:
                    Toast.makeText(context, "打开麦克风失败", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL:
                    Toast.makeText(context, "视频编码失败", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL:
                    Toast.makeText(context, "音频编码失败", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_UNSUPPORTED_RESOLUTION:
                    Toast.makeText(context, "不支持的视频分辨率", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_UNSUPPORTED_SAMPLERATE:
                    Toast.makeText(context, "不支持的音频采样率", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_WARNING_NET_BUSY:
                    Toast.makeText(context, "网络状况不佳", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_WARNING_RECONNECT:
                    Toast.makeText(context, "网络断连, 已启动自动重连", Toast.LENGTH_SHORT).show();//自动重连连续失败超过三次会放弃
                    break;
                case TXLiveConstants.PUSH_WARNING_SEVER_CONN_FAIL:
                    Toast.makeText(context, "服务器连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_WARNING_SERVER_DISCONNECT:
                    //推流请求被后台拒绝了。出现这个问题一般是由于推流地址里的 txSecret 计算错了，或者是推流地址被其他人占用了（一个推流 URL 同时只能有一个端推流）
                    Toast.makeText(context, "服务器主动断开连接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onNetStatus(Bundle bundle) {

        }
    }

    //结束推流，注意做好清理工作
    public void stopRtmpPublish() {
        mLivePusher.stopCameraPreview(true); //停止摄像头预览
        mLivePusher.stopPusher();            //停止推流
        mLivePusher.setPushListener(null);   //解绑 listener
    }

}
