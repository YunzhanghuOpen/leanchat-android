package com.avoscloud.chat;

import android.app.Application;
import android.os.StrictMode;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avoscloud.chat.friends.AddRequest;
import com.avoscloud.chat.model.LCIMRedPacketMessage;
import com.avoscloud.chat.model.LCIMRedPcketAckMessage;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.chat.model.UpdateInfo;
import com.avoscloud.chat.service.PushManager;
import com.avoscloud.chat.util.LeanchatUserProvider;
import com.avoscloud.chat.util.Utils;
import com.baidu.mapapi.SDKInitializer;
import com.yunzhanghu.redpacketsdk.RedPacket;

import cn.leancloud.chatkit.LCChatKit;

/**
 * Created by lzw on 14-5-29.
 */
public class App extends Application {
  public static boolean debug = true;
  public static App ctx;

  @Override
  public void onCreate() {
    super.onCreate();
    ctx = this;
    Utils.fixAsyncTaskBug();

    String appId = "x3o016bxnkpyee7e9pa5pre6efx2dadyerdlcez0wbzhw25g";
    String appKey = "057x24cfdzhffnl3dzk14jh9xo2rq6w1hy1fdzt5tv46ym78";

    LeanchatUser.alwaysUseSubUserClass(LeanchatUser.class);

    AVObject.registerSubclass(AddRequest.class);
    AVObject.registerSubclass(UpdateInfo.class);

    // 节省流量
    AVOSCloud.setLastModifyEnabled(true);

    AVIMMessageManager.registerAVIMMessageType(LCIMRedPacketMessage.class);
    AVIMMessageManager.registerAVIMMessageType(LCIMRedPcketAckMessage.class);
    LCChatKit.getInstance().setProfileProvider(new LeanchatUserProvider());
    LCChatKit.getInstance().init(this, appId, appKey);

    // 初始化红包操作
    RedPacket.getInstance().initContext(ctx);
    //控制红包SDK中Log输出
    RedPacket.getInstance().setDebugMode(false);

    PushManager.getInstance().init(ctx);
    AVOSCloud.setDebugLogEnabled(debug);
    AVAnalytics.enableCrashReport(this, !debug);
    initBaiduMap();
    if (App.debug) {
      openStrictMode();
    }
  }

  public void openStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()   // or .detectAll() for all detectable problems
            .penaltyLog()
            .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .penaltyLog()
            //.penaltyDeath()
            .build());
  }

  private void initBaiduMap() {
    SDKInitializer.initialize(this);
  }
}
