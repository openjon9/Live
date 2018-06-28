package com.coder.live.Activity;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.coder.live.Adapter.MyAdapter;
import com.coder.live.Adapter.MyAdapter3;
import com.coder.live.Class.Person;
import com.coder.live.Class.TCConstants;
import com.coder.live.Class.TCConstants2;
import com.coder.live.Class.TCSimpleUserInfo;
import com.coder.live.IM;
import com.coder.live.Live;
import com.coder.live.R;
import com.tencent.TIMCallBack;
import com.tencent.TIMConnListener;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupManager;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMTextElem;
import com.tencent.TIMUser;
import com.tencent.TIMUserStatusListener;
import com.tencent.TIMValueCallBack;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tencent.tls.platform.TLSAccountHelper;
import tencent.tls.platform.TLSLoginHelper;

import static android.graphics.BitmapFactory.decodeResource;


public class LiveActivity extends AppCompatActivity implements TIMConnListener, TIMMessageListener, TIMUserStatusListener, IM.TCChatRoomListener {

    private TXLivePusher mLivePusher;
    private TXLivePushConfig mLivePushConfig;
    private TXCloudVideoView mCaptureView;
    String rtmpUrl = "rtmp://18455.livepush.myqcloud.com/live/18455_3843cec4c2?bizid=18455&txSecret=4a3f17ad1901bfbd94629468410e0277&txTime=5B3505FF";//推流地址
    private LiveActivity context;
    private TLSLoginHelper loginHelper;
    private TLSAccountHelper accountHelper;
    private TIMConversation conversation;
    String TAG = "liteavsdk";
    private Live mlive;
    private ListView listview;
    List<Person> mlist = new ArrayList<>();
    private MyAdapter3 adapter;
    private TIMMessage lastMsg;
    private EditText edtext;
    private InputMethodManager imm;



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
        pauseImg();//推流前設置暫停圖片

        mLivePusher.startCameraPreview(mCaptureView);
        mLivePusher.startPusher(rtmpUrl);

        mLivePusher.setPushListener(new MyITXLivePushListener());
        edtext = (EditText) findViewById(R.id.edtext);
        listview = (ListView) findViewById(R.id.listview);
        adapter = new MyAdapter3(context, mlist);
        listview.setAdapter(adapter);
        TIMManager.getInstance().init(getApplicationContext());//初始化
        Login();
        //禁用 Crash 上报
        TIMManager.getInstance().disableCrashReport();

        TIMManager.getInstance().addMessageListener(this);
        TIMManager.getInstance().setUserStatusListener(this);
        TIMManager.getInstance().setConnectionListener(this);


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
        mlist = null;
        adapter = null;
        removeMessageListener(this);
        deleteGroup();
    }

    public void camera_change(View view) {
        mLivePusher.switchCamera();
    }

    public void sendtext(View view) {
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (edtext.getText().toString().equals("")) {
            return;
        }
        send(edtext.getText().toString());
        edtext.setText("");
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    public void pauseImg(){
        mLivePushConfig.setPauseImg(300,5);
// 300 为后台播放暂停图片的最长持续时间,单位是秒
// 10 为后台播放暂停图片的帧率,最小值为 5,最大值为 20
        Bitmap bitmap = decodeResource(getResources(), R.drawable.pause_publish);
        mLivePushConfig.setPauseImg(bitmap);
// 设置推流暂停时,后台播放的暂停图片, 图片最大尺寸不能超过 1920*1920.
        mLivePusher.setConfig(mLivePushConfig);
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

    /********連線監聽**********/
    @Override
    public void onConnected() {
        //连接建立
        Toast.makeText(context, "連線成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected(int code, String desc) {
        Log.d(TAG, "連線失败code:" + code + "\tdesc:" + desc);
    }

    @Override
    public void onWifiNeedAuth(String s) {

    }

    /************消息監聽***********/
    /**
     * 添加一个消息监听器
     * 默认情况下所有消息监听器都将按添加顺序被回调一次
     * 除非用户在 onNewMessages 回调中返回 true，此时将不再继续回调下一个消息监听器
     */
    @Override
    public boolean onNewMessages(List<TIMMessage> list) {
      //  Toast.makeText(context, "收到新消息", Toast.LENGTH_SHORT).show();
        parseIMMessage(list); //接收消息
        return false;
    }

    /**
     * 消息监听器被删除后，将不再被调用。
     * 删除一个消息监听器：
     **/
    public void removeMessageListener(TIMMessageListener listener) {
        TIMManager.getInstance().removeMessageListener(listener);
    }

    /*********用戶狀態*********/
    @Override
    public void onForceOffline() {
        //被踢下线
        Toast.makeText(context, "被踢下线", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserSigExpired() {
        //票据过期，需要换票后重新登录
        Toast.makeText(context, "票据过期，需要换票后重新登录", Toast.LENGTH_SHORT).show();
    }

    /*********登入登出********/
    public void Login() {
// identifier 为用户名，userSig 为用户登录凭证
        TIMUser user = new TIMUser();
        user.setIdentifier(TCConstants.USER_ID);
//发起登录请求
        TIMManager.getInstance().login(TCConstants.IMSDK_APPID, user, TCConstants.USERSIG, new TIMCallBack() {//回调接口

            @Override
            public void onSuccess() {//登录成功
                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                joinGroup();
                // createGroup();
            }

            @Override
            public void onError(int code, String desc) {//登录失败
                Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "登录失败code:" + code + "\tdesc:" + desc);

            }
        });
    }

    public void LoginOut() {
        //登出
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                Toast.makeText(context, "登出失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "登出失败code:" + code + "\tdesc:" + desc);

            }

            @Override
            public void onSuccess() {
                //登出成功
                Toast.makeText(context, "登出成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加入群组回调
     *
     * @param code 错误码，成功时返回0，失败时返回相应错误码
     * @param msg  返回信息，成功时返回群组Id，失败时返回相应错误信息
     */
    @Override
    public void onJoinGroupCallback(int code, String msg) {

    }

    /**
     * 发送消息结果回调
     *
     * @param code       错误码，成功时返回0，失败时返回相应错误码
     * @param timMessage 发送的TIM消息
     */
    @Override
    public void onSendMsgCallback(int code, TIMMessage timMessage) {

    }

    /**
     * 接受消息监听接口
     * 文本消息回调
     *
     * @param type     消息类型
     * @param userInfo 发送者信息
     * @param content  内容
     */
    @Override
    public void onReceiveMsg(int type, TCSimpleUserInfo userInfo, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "接受消息:type:" + type + "\nuserInfo_id:" + userInfo.userid + "\nuserInfo_name:" + userInfo.nickname + "\ncontent:" + content);
    }

    /**
     * 群组删除回调，在主播群组解散时被调用
     */
    @Override
    public void onGroupDelete() {
        Toast.makeText(context, "群組已解散", Toast.LENGTH_SHORT).show();
    }


    //创建聊天群组 创建者默认加入  創群是主播用
    // 群类型：私有群（Private）、公开群（Public）、聊天室（ChatRoom）、互动直播聊天室（AVChatRoom）和在线成员广播大群（BChatRoom）
    public void createGroup() {

        //创建直播大群
        //groupName , 回調
        TIMGroupManager.getInstance().createAVChatroomGroup("TVShow", new TIMValueCallBack<String>() {
            @Override
            public void onError(int code, String desc) {
                Log.d(TAG, "创建直播大群失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "创建直播大群失敗", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String groupId) {//回调返回创建的群组 會自動產生ID
                Toast.makeText(context, "创建直播大群成功", Toast.LENGTH_SHORT).show();
                TCConstants.GROUP_ID = groupId;
                Log.d(TAG, "groupId:" + groupId);
                //取得會話物件          會話類型(這是列舉只有4種),群ID
                conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, groupId);
            }
        });
    }


    //加入聊天群组  觀眾用 直播大群：可以任意加入群组。
    public void joinGroup() {
        //群ID,申請理由(選填),回調
        TIMGroupManager.getInstance().applyJoinGroup(TCConstants2.GROUP_ID, "some reason", new TIMCallBack() {
            @java.lang.Override
            public void onError(int code, String desc) {
                Log.d(TAG, "加入聊天群组失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "加入群组失败", Toast.LENGTH_SHORT).show();
            }

            @java.lang.Override
            public void onSuccess() {
                Toast.makeText(context, "加入群组成功", Toast.LENGTH_SHORT).show();
                //取得會話物件          會話類型(這是列舉只有4種),群ID
                conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, TCConstants2.GROUP_ID);
            }
        });
    }

    //解散群组  注意：直播大群只有群主可以解散
    public void deleteGroup() {
        if (TCConstants.GROUP_ID == null) {
            return;
        }
        TIMGroupManager.getInstance().deleteGroup(TCConstants.GROUP_ID, new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                Log.d(TAG, "解散群组失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "聊天室解散失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                //解散群组成功
                Toast.makeText(context, "聊天室解散成功", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //禁言 只有主播可以用
    public void modifyGroupMemberInfoSetSilence(String user, int sec) {

        TIMGroupManager.getInstance().modifyGroupMemberInfoSetSilence(TCConstants.GROUP_ID, user, sec, new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                Log.d(TAG, "禁言失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "禁言失敗", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(context, "禁言成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * userAction 消息类型 ，可自己设定。比如1代表弹幕消息，2代表普通文本消息
     *
     * @param param 消息内容
     */
    public void send(final String param) {
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("userAction", 2);
            sendJson.put("userId", TCConstants.USER_ID);
            sendJson.put("userName", TCConstants.USER_NAME);
            sendJson.put("color", 0);
            //  sendJson.put("headPic", TCConstants.USER_HEADPIC);
            sendJson.put("msg", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String str = sendJson.toString();
        TIMMessage msg = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(str);
        if (msg.addElement(elem) != 0) {
            return;
        }
        if (conversation != null)
            conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int code, String desc) {
                    Log.d(TAG, "发送消息失败code:" + code + "\tdesc:" + desc);
                    Toast.makeText(context, "发送消息失敗", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {//发送消息成功
                    mlist.add(new Person(TCConstants.USER_NAME, param, 0));
                    if (mlist.size() > 1000) {
                        for (int i = 0; i < 100; i++) {
                            mlist.remove(i);
                        }
                    }
                     adapter.notifyDataSetChanged();
                    listview.setSelection(mlist.size()-1);
                   // Toast.makeText(context, "发送消息成功", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * 解析TIM消息列表
     *
     * @param list 消息列表
     */
    private void parseIMMessage(List<TIMMessage> list) {
        Log.d(TAG, "接收消息");
        TIMMessage msg = list.get(0);
        for (int i = 0; i < msg.getElementCount(); ++i) {
            TIMElem elem = msg.getElement(i);
            //获取当前元素的类型
            TIMElemType elemType = elem.getType();
            if (elemType == TIMElemType.Text) {
                //处理文本消息
                String jsonString = ((TIMTextElem) elem).getText();
                Log.d(TAG, "接收消息jsonString:" + jsonString);
                try {
                    JSONObject json = new JSONObject(jsonString);
                    int action = json.getInt("userAction");
                    String userId = json.getString("userId");
                    String userName = json.getString("userName");
                    int color = json.getInt("color");
                    // userName = TextUtils.isEmpty(userName) ? userId : userName;
                    //  String headPic = (String) json.get("headPic"); //頭像
                    String str = json.getString("msg");
                    mlist.add(new Person(userName, str, color));
                    if (mlist.size() > 1000) {
                        for (int k = 0; k < 100; k++) {
                            mlist.remove(k);
                        }
                    }
                     adapter.notifyDataSetChanged();
                    listview.setSelection(mlist.size()-1);
                    //  Log.d(TAG, "接收消息action:" + action + "\tuserId:" + userId + "\tuserName:" + userName + "\tstr:" + str+"\n");
                    //  onReceiveMsg(action, new TCSimpleUserInfo(userId, userName), str);
                } catch (JSONException e) {
                    Log.d(TAG, "解析消息失敗:" + e.getLocalizedMessage());
                }
            }
        }
    }

    //獲取最近10條消息 此方法为异步方法
    //參數說明  从最后一条消息往前的消息条数，已获取的最后一条消息，当传 null 的时候，从最新的消息开始读取，回调
    public void getMessage() {
        conversation.getLocalMessage(10, null, new TIMValueCallBack<List<TIMMessage>>() {
            @Override
            public void onError(int code, String desc) {
                Log.d(TAG, "獲取最近10條消息失败code:" + code + "\tdesc:" + desc);
            }

            @Override
            public void onSuccess(List<TIMMessage> timMessages) {
                for (TIMMessage msg : timMessages) {
                    lastMsg = msg;
                    Log.d(TAG, "獲取最近10條消息:msg:" + msg.timestamp() + "\tself:" + msg.isSelf() + "\tseq:" + msg.msg.seq());
                }


            }
        });
    }

    //结束推流，注意做好清理工作
    public void stopRtmpPublish() {
        mLivePusher.stopCameraPreview(true); //停止摄像头预览
        mLivePusher.stopPusher();            //停止推流
        mLivePusher.setPushListener(null);   //解绑 listener
    }

}
