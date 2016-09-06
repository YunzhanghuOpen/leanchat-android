package com.avoscloud.chat.redpacket;

import com.yunzhanghu.redpacketsdk.bean.RPUserBean;

/**
 * Created by hhx on 16/6/30.
 */
public interface GetUserInfoCallback {
  void userInfoSuccess(RPUserBean rpUser);

  void userInfoError();
}
