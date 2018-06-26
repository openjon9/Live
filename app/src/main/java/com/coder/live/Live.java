package com.coder.live;

import android.content.Context;
import android.net.wifi.aware.DiscoverySession;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

/**
 * Created by Rey on 2018/6/26.
 */

public class Live implements TIMConnListener, TIMMessageListener, TIMUserStatusListener, IM.TCChatRoomListener {

    String TAG = "liteavsdk";
    private Context context;
    private TIMConversation conversation;

    public Live(Context c) {
        context = c;
        TIMManager.getInstance();//初始化
        TIMManager.getInstance().addMessageListener(this);
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
            }

            @Override
            public void onError(int code, String desc) {//登录失败
                Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "登录失败code:" + code + "\tdesc:" + desc);

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
                //取得會話物件          會話類型(這是列舉只有4種),群ID
                conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, groupId);
            }
        });


//        TIMGroupManager.CreateGroupParam param = TIMGroupManager.getInstance().new CreateGroupParam();
//        param.setGroupType("AVChatRoom"); // type 群类型, 目前支持的群类型："Public", "Private", "ChatRoom", "AVChatRoom", "BChatRoom"
//        param.setGroupName("小直播");//设置要创建的群的名称（必填）
//        param.setGroupId(TCConstants.GROUP_ID);//设置要创建的群的群 ID
//        TIMGroupManager.getInstance().createGroup(param, new TIMValueCallBack<String>() {
//            @Override
//            public void onError(int i, String s) {
//                Toast.makeText(context, "聊天群创建失败", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSuccess(String roomId) {
//                Toast.makeText(context, "聊天群创建成功roomId:" + roomId, Toast.LENGTH_SHORT).show();
//                 conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, TCConstants.GROUP_ID);
//            }
//        });
    }

    //解散群组  注意：直播大群只有群主可以解散
    public void deleteGroup() {
        if (TCConstants.GROUP_ID == null) {
            return;
        }
        TIMGroupManager.getInstance().deleteGroup(TCConstants.GROUP_ID, new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                //错误码 code 和错误描述 desc，可用于定位请求失败原因
                //错误码 code 列表请参见错误码表
                Log.d(TAG, "解散群组失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "聊天室解散失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                //解散群组成功
                Toast.makeText(context, "聊天室解散成功", Toast.LENGTH_SHORT).show();
            }
        });

//        TIMManager.getInstance().deleteConversation(TIMConversationType.Group, TCConstants.GROUP_ID);
//        TIMGroupManager.getInstance().deleteGroup(TCConstants.GROUP_ID, new TIMCallBack() {
//            @Override
//            public void onError(int i, String s) {
//                Toast.makeText(context, "聊天室解散失败", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSuccess() {
//                Toast.makeText(context, "聊天室解散成功", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    //加入聊天群组  觀眾用 直播大群：可以任意加入群组。
    public void joinGroup() {
        //群ID,申請理由(選填),回調
        TIMGroupManager.getInstance().applyJoinGroup(TCConstants.GROUP_ID, "some reason", new TIMCallBack() {
            @java.lang.Override
            public void onError(int code, String desc) {
                Log.d(TAG, "加入聊天群组失败code:" + code + "\tdesc:" + desc);
                Toast.makeText(context, "加入群组失败", Toast.LENGTH_SHORT).show();
            }

            @java.lang.Override
            public void onSuccess() {
                Toast.makeText(context, "加入群组成功", Toast.LENGTH_SHORT).show();
                //取得會話物件          會話類型(這是列舉只有4種),群ID
                conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, TCConstants.GROUP_ID);
            }
        });
    }

    //退出群組  觀眾用
    public void quitGroup() {

        TIMGroupManager.getInstance().quitGroup(TCConstants.GROUP_ID, new TIMCallBack() {

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
    private void send(String param) {
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("userAction", 2);
            sendJson.put("userId", TCConstants.USER_ID);
            sendJson.put("userName", TCConstants.USER_NAME);
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
                    Toast.makeText(context, "发送消息成功", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * 解析TIM消息列表
     *
     * @param list 消息列表
     */
    private void parseIMMessage(List<TIMMessage> list) {

        TIMMessage msg = list.get(0);
        for (int i = 0; i < msg.getElementCount(); ++i) {
            TIMElem elem = msg.getElement(i);
            //获取当前元素的类型
            TIMElemType elemType = elem.getType();
            if (elemType == TIMElemType.Text) {
                //处理文本消息
                try {
                    String jsonString = ((TIMTextElem) elem).getText();
                    JSONTokener jsonParser = new JSONTokener(jsonString);
                    JSONObject json = (JSONObject) jsonParser.nextValue();
                    int action = json.getInt("userAction");
                    String userId = json.getString("userId");
                    String userName = json.getString("userName");
                    // userName = TextUtils.isEmpty(userName) ? userId : userName;
                    //  String headPic = (String) json.get("headPic"); //頭像
                    String str = json.getString("msg");
                    onReceiveMsg(action, new TCSimpleUserInfo(userId, userName), str);
                } catch (JSONException e) {
                    Log.d(TAG, "解析消息失敗:" + e.getLocalizedMessage());
                }
            }
        }


//        for (int i = list.size() - 1; i >= 0; i--) {
//            TIMMessage msg = list.get(i);
//            for (int j = 0; j < msg.getElementCount(); ++j) {
//                if (msg.getElement(j) == null)
//                    continue;
//                TIMElem elem = msg.getElement(j);
//                //获取当前元素的类型
//                TIMElemType elemType = elem.getType();
//                if (elemType == TIMElemType.Text) {
//                    //处理文本消息
//                    try {
//                        String jsonString = ((TIMTextElem) elem).getText();
//                        JSONTokener jsonParser = new JSONTokener(jsonString);
//                        JSONObject json = (JSONObject) jsonParser.nextValue();
//                        int action = (int) json.get("userAction");
//                        String userId = (String) json.get("userId");
//                        String userName = (String) json.get("userName");
//                        userName = TextUtils.isEmpty(userName) ? userId : userName;
//                        String headPic = (String) json.get("headPic"); //頭像
//                        String str = (String) json.get("msg");
//                        onReceiveMsg(action, new TCSimpleUserInfo(userId, userName, headPic), str);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        // 异常处理代码
//                    }
//                } else if (elemType == TIMElemType.Image) {
//                    //处理图片消息
//                }//...处理更多消息
//            }
//        }
    }


}
