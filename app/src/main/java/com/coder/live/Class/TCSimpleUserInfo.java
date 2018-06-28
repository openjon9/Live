package com.coder.live.Class;

/**
 * Created by Rey on 2018/6/26.
 */

public class TCSimpleUserInfo {

    public String userid;
    public String nickname;
    public String headpic;

    public TCSimpleUserInfo(String userid, String nickname) {
        this.userid = userid;
        this.nickname = nickname;
    }

    public TCSimpleUserInfo(String userId, String nickname, String headpic) {
        this.userid = userId;
        this.nickname = nickname;
        this.headpic = headpic;
    }
}
