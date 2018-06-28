package com.coder.live.Activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.coder.live.Adapter.MyAdapter2;
import com.coder.live.Adapter.MyAdapter4;
import com.coder.live.Class.Person;
import com.coder.live.Class.TCConstants;
import com.coder.live.Class.TCConstants2;
import com.coder.live.Class.TCSimpleUserInfo;
import com.coder.live.IM;
import com.coder.live.R;
import com.tencent.TIMCallBack;
import com.tencent.TIMConnListener;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupDetailInfo;
import com.tencent.TIMGroupManager;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMTextElem;
import com.tencent.TIMUser;
import com.tencent.TIMUserStatusListener;
import com.tencent.TIMValueCallBack;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WatchActivity extends AppCompatActivity implements TIMConnListener, TIMMessageListener, TIMUserStatusListener, IM.TCChatRoomListener {

    private TXCloudVideoView mView;
    private TXLivePlayer mLivePlayer;
    String flvUrl = "http://18455.liveplay.myqcloud.com/live/18455_3843cec4c2.flv";//直播收看地址
    private WatchActivity context;
    private TXLivePlayConfig mPlayConfig;
    private EditText edtext;
    private ListView listview;
    List<Person> mlist;
    private MyAdapter4 adapter;
    private TIMMessage lastMsg;
    String TAG = "liteavsdk";
    private TIMConversation conversation;
    private String GroupOwner;
    private InputMethodManager imm;


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


        edtext = (EditText) findViewById(R.id.edtext);
        listview = (ListView) findViewById(R.id.listview);
        mlist = new ArrayList<>();
        adapter = new MyAdapter4(this, mlist);
        listview.setAdapter(adapter);

        TIMManager.getInstance().init(getApplicationContext());//初始化
        Login();
        //禁用 Crash 上报
        TIMManager.getInstance().disableCrashReport();

        TIMManager.getInstance().setUserStatusListener(this);
        TIMManager.getInstance().setConnectionListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLivePlayer.resume();
        TIMManager.getInstance().addMessageListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLivePlayer.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeMessageListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLivePlayer.stopPlay(true); // true 代表清除最后一帧画面
        mView.onDestroy();
        mlist = null;
        adapter = null;
        removeMessageListener(this);
        quitGroup();
        LoginOut();
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

    public void landScape(View view) {
        // 设置填充模式
     //   mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
// 设置画面渲染方向
      //  mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);
    }

    public class MyITXLivePlayListener implements ITXLivePlayListener {

        @Override
        public void onPlayEvent(int event, Bundle param) {
            switch (event) {
                case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://视频播放开始，如果有转菊花什么的这个时候该停了
                    Toast.makeText(context, "訊號不良", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PLAY_EVT_PLAY_LOADING://视频播放 loading，如果能够恢复，之后会有 BEGIN 事件
                    //Toast.makeText(context, "2007", Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PLAY_EVT_GET_MESSAGE://用于接收夹在音视频流中的消息
                    String msg = null;
                    try {
                        msg = new String(param.getByteArray(TXLiveConstants.EVT_GET_MSG), "UTF-8");
                        Toast.makeText(context, msg+"", Toast.LENGTH_SHORT).show();
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
        // Toast.makeText(context, "收到新消息", Toast.LENGTH_SHORT).show();
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
        user.setIdentifier(TCConstants2.USER_ID);
//发起登录请求
        TIMManager.getInstance().login(TCConstants2.IMSDK_APPID, user, TCConstants2.USERSIG, new TIMCallBack() {//回调接口

            @Override
            public void onSuccess() {//登录成功
                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                joinGroup();
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

    //退出群組  觀眾用
    public void quitGroup() {

        TIMGroupManager.getInstance().quitGroup(TCConstants2.GROUP_ID, new TIMCallBack() {

            @Override
            public void onError(int code, String desc) {
                Log.d(TAG, "退出群組失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "退出群组失敗", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(context, "退出群组成功", Toast.LENGTH_SHORT).show();
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
            sendJson.put("userId", TCConstants2.USER_ID);
            sendJson.put("userName", TCConstants2.USER_NAME);
            sendJson.put("color",1);
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
                    mlist.add(new Person(TCConstants2.USER_NAME, param, 1));
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
                Log.d(TAG, "接收消息:" + jsonString);

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

    public void getGroupDeta() {
        //创建待获取信息的群组 ID 列表
        ArrayList<String> groupList = new ArrayList<String>();
        groupList.add(TCConstants2.GROUP_ID);
        TIMGroupManager.getInstance().getGroupDetailInfo(groupList, new TIMValueCallBack<List<TIMGroupDetailInfo>>() {
            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onSuccess(List<TIMGroupDetailInfo> timGroupDetailInfos) {
                for (TIMGroupDetailInfo info : timGroupDetailInfos) {
                    GroupOwner = info.getGroupOwner(); //獲取群創建者
                    // info.getMemberNum();  獲取群成員
                }
            }
        });

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

}
