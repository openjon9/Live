package com.coder.live;

import android.text.TextUtils;
import android.util.Log;

import com.coder.live.Class.TCConstants;
import com.coder.live.Class.TCSimpleUserInfo;
import com.tencent.TIMCallBack;
import com.tencent.TIMConnListener;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupManager;
import com.tencent.TIMLogListener;
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
 * Created by Rey on 2018/6/22.
 */

//cmd 消息类型 ，可自己设定。比如1代表弹幕消息，2代表普通文本消息

//主播流程  先登入>創建群(得到群ID，並默認加入)>消息監聽器 && 發言監聽器 > 退出(關閉群聊)
//觀眾流程  登入(自身ID,userSig(用戶簽名,需要後端給),sdkAPPID(騰訊後台))>加入群(群ID,自身ID)>消息監聽器(消息種類(彈幕或一般),群ID) && TIMCallBack發言監聽器(cmd,發言內容,TIMCallBack發言監聽器)>退出群(群ID,TIMCallBack監聽器)

public class IM {
    private TIMConversation conversation;
    private TCChatRoomListener mTCChatRoomListener;

    /*********初始化相關*****************/

    /**
     * 添加一个消息监听器
     *
     * @param listener 消息监听器
     *                 默认情况下所有消息监听器都将按添加顺序被回调一次
     *                 除非用户在 onNewMessages 回调中返回 true，此时将不再继续回调下一个消息监听器
     */
    public void addMessageListener(TIMMessageListener listener) {
        TIMManager.getInstance().addMessageListener(listener);
    }

    /**
     * 消息监听器被删除后，将不再被调用。
     * 删除一个消息监听器：
     **/
    public void removeMessageListener(TIMMessageListener listener) {
        TIMManager.getInstance().removeMessageListener(listener);
    }

    //网络事件通知
    public void setConnectionListener() {
        TIMManager.getInstance().setConnectionListener(new TIMConnListener() {
            @Override
            public void onConnected() {//连接建立

            }

            @Override
            public void onDisconnected(int i, String s) {//连接断开

            }

            @Override
            public void onWifiNeedAuth(String s) {

            }
        });
    }

    //用户状态变更
    public void setUserStatusListener() {
        TIMManager.getInstance().setUserStatusListener(new TIMUserStatusListener() {
            /**
             * 被踢下线时回调
             */
            @Override
            public void onForceOffline() {
                //被踢下线
            }

            /**
             * 票据过期时回调
             */
            @Override
            public void onUserSigExpired() {
                //票据过期，需要换票后重新登录
            }
        });
    }

    //日志事件
    public void setLogListener() {
        TIMManager.getInstance().setLogListener(new TIMLogListener() {
            @Override
            public void log(int i, String s, String s1) {

            }
        });
    }

    /*******監聽器*******/
    //消息監聽器
    TIMMessageListener messageListener = new TIMMessageListener() {

        @Override
        public boolean onNewMessages(List<TIMMessage> list) {
            parseIMMessage(list); //接收消息
            return false;
        }
    };
    //發言監聽器
    TIMValueCallBack<TIMMessage> timValueCallBack =new TIMValueCallBack<TIMMessage>() {
        @Override
        public void onError(int i, String s) {

        }

        @Override
        public void onSuccess(TIMMessage timMessage) {//发送消息成功

        }
    };


    /***登入相關**/

    TIMCallBack timCallBack = new TIMCallBack() {

        @Override
        public void onError(int i, String s) {//登录失败
            //错误码 code 和错误描述 desc，可用于定位请求失败原因
            //错误码 code 含义请参见错误码表
        }

        @Override
        public void onSuccess() {//登录成功  //登出成功

        }
    };


    // identifier 为用户名，sdkAppId 由腾讯分配 ， userSig 为用户登录凭证 ，
    public void Login(String identifier, int sdkAppId, String userSig, TIMCallBack callback) {

        TIMUser user = new TIMUser();
        user.setIdentifier(identifier);

        TIMManager.getInstance().login(sdkAppId, user, userSig, callback);

    }

    public void logout(TIMCallBack callback) {

        TIMManager.getInstance().logout(callback);

    }

    public String getLoginUser() {
        return TIMManager.getInstance().getLoginUser();
    }


    /*************消息发送************/

    //创建聊天群组 创建者默认加入
    // 群类型：私有群（Private）、公开群（Public）、聊天室（ChatRoom）、互动直播聊天室（AVChatRoom）和在线成员广播大群（BChatRoom）
    public void createGroup() {
        TIMGroupManager.CreateGroupParam param = TIMGroupManager.getInstance().new CreateGroupParam();
        param.setGroupType("AVChatRoom"); // type 群类型, 目前支持的群类型："Public", "Private", "ChatRoom", "AVChatRoom", "BChatRoom"
        param.setGroupName("小直播");//设置要创建的群的名称（必填）
        param.setGroupId(TCConstants.GROUP_ID);//设置要创建的群的群 ID
        TIMGroupManager.getInstance().createGroup(param, new TIMValueCallBack<String>() {
            @Override
            public void onError(int i, String s) {
                //  Log.e("+++", "聊天群创建失败" + i + s);
            }

            @Override
            public void onSuccess(String roomId) {
                //   Log.e("+++", "聊天群创建成功roomId:" + roomId);
                // conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, TCConstants.GROUP_ID);
            }
        });
    }

    //解散群组  注意：直播大群只有群主可以解散
    public void deleteGroup() {

        if (TCConstants.GROUP_ID == null)
            return;

        TIMManager.getInstance().deleteConversation(TIMConversationType.Group, TCConstants.GROUP_ID);
        TIMGroupManager.getInstance().deleteGroup(TCConstants.GROUP_ID, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                Log.e("+++", "聊天室解散失败：" + i + s);
            }

            @Override
            public void onSuccess() {
                Log.e("+++", "聊天室解散成功");
            }
        });
    }

    //加入聊天群组
    public void joinGroup() {
        TIMGroupManager.getInstance().applyJoinGroup(TCConstants.GROUP_ID, "some reason", new TIMCallBack() {
            @java.lang.Override
            public void onError(int code, String desc) {
                Log.e("+++", "加入群组失败" + desc);
            }

            @java.lang.Override
            public void onSuccess() {
                Log.e("+++", "加入群组成功");
                conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, TCConstants.GROUP_ID);
            }
        });
    }

    /**
     * @param cmd   消息类型 ，可自己设定。比如1代表弹幕消息，2代表普通文本消息
     * @param param 消息内容
     */
    private void send(int cmd, String param, TIMValueCallBack<TIMMessage> timValueCallBack) {
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("userAction", cmd);
            sendJson.put("userId", TCConstants.USER_ID);
            sendJson.put("userName", TCConstants.USER_NAME);
            sendJson.put("headPic", TCConstants.USER_HEADPIC);
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
            conversation.sendMessage(msg, timValueCallBack);
    }

    /**
     * 解析TIM消息列表
     *
     * @param list 消息列表
     */
    private void parseIMMessage(List<TIMMessage> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            TIMMessage msg = list.get(i);
            for (int j = 0; j < msg.getElementCount(); ++j) {
                if (msg.getElement(j) == null)
                    continue;
                TIMElem elem = msg.getElement(j);
                //获取当前元素的类型
                TIMElemType elemType = elem.getType();
                if (elemType == TIMElemType.Text) {
                    //处理文本消息
                    try {
                        String jsonString = ((TIMTextElem) elem).getText();
                        JSONTokener jsonParser = new JSONTokener(jsonString);
                        JSONObject json = (JSONObject) jsonParser.nextValue();
                        int action = (int) json.get("userAction");
                        String userId = (String) json.get("userId");
                        String userName = (String) json.get("userName");
                        userName = TextUtils.isEmpty(userName) ? userId : userName;
                        String headPic = (String) json.get("headPic"); //頭像
                        String str = (String) json.get("msg");
                        mTCChatRoomListener.onReceiveMsg(action, new TCSimpleUserInfo(userId, userName, headPic), str);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // 异常处理代码
                    }
                } else if (elemType == TIMElemType.Image) {
                    //处理图片消息
                }//...处理更多消息
            }
        }
    }


    /**
     * 消息循环监听类
     */
    public interface TCChatRoomListener {

        /**
         * 加入群组回调
         *
         * @param code 错误码，成功时返回0，失败时返回相应错误码
         * @param msg  返回信息，成功时返回群组Id，失败时返回相应错误信息
         */
        void onJoinGroupCallback(int code, String msg);

        //void onGetGroupMembersList(int code, List<TIMUserProfile> result);

        /**
         * 发送消息结果回调
         *
         * @param code       错误码，成功时返回0，失败时返回相应错误码
         * @param timMessage 发送的TIM消息
         */
        void onSendMsgCallback(int code, TIMMessage timMessage);

        /**
         * 接受消息监听接口
         * 文本消息回调
         *
         * @param type     消息类型
         * @param userInfo 发送者信息
         * @param content  内容
         */
        void onReceiveMsg(int type, TCSimpleUserInfo userInfo, String content);

        /**
         * 群组删除回调，在主播群组解散时被调用
         */
        void onGroupDelete();
    }

}
