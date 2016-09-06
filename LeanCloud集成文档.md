# LeanCloud集成文档


## 1. redpacketlibrary-aar简介

**redpacketlibrary-aar**，在LeanCloud**SDK**的基础上提供了收发红包和零钱页的功能。


## 2. redpacketlibrary目录说明

* libs ：
* 1、redpacket3.0.1是集成红包功能所依赖的jar包。
* 2、alipaySdk-20160516z支付宝支付
* 3、glide-3.6.1图片加载库
* 4、volley-1.0.19请求框架
* res ：包含了红包SDK和聊天页面中的资源文件。（红包SDK相关以rp开头）
* redpacket ：此包包含红包发送接收的工具类
* 1、GetSignInfoCallback 获取签名接口回调**
* 2、GetGroupMemberCallback 获取群里面的人数（app开发者需要自己处理）的接口回调。
* 3、GetUserInfoCallback 根据用户id获取用户信息接口回调（打开专属红包所需，回调可以根据需要自己处理）
* 4、RedPacketUtils 发送打开红包相关的工具类
* message ：
* 1、LCIMRedPacketMessage 自定义红包消息。
* 2、LCIMRedPcketAckMessage 自定义通知消息，用于领取了红包之后，回执消息发给红包者。  **用于群/个人领取了红包之后，1、接受者先向本地插入一条“你领取了XX的红包”，然后发送一条空消息（不在聊天界面展示），发送红包者收到消息之后，向本地插入一条“XX领取了你的红包”，2、如果接受者和发送者是一个人就直接向本地插入一条“你领取了自己的红包”**
* 3、InputRedPacketClickEvent 红包按钮点击事件
*  viewholder ：
*  1、ChatItemRedPacketHolder 红包消息处理机制
*  2、ChatItemRedPacketAckHolder 回执消息UI展示提供者
*  3、ChatItemRedPacketEmptyHolder 空消息用于隐藏和自己不相关的消息
* * **注意: redpacketlibrary-aar支持AndroidStudio**。

## 3. 集成步骤

###3.1 添加对红包工程的依赖
* leanchat-android的build.gradle中

```
    compile files('libs/baidumapapi_v3_0_0.jar')
    compile files('libs/locSDK_4.2.jar')
    compile files('libs/alipaySdk-20160516.jar')
    compile files('libs/glide-3.6.1.jar')
    compile files('libs/volley-1.0.19.jar')

    compile ('cn.leancloud.android:chatkit:1.0.1')
    compile project(':redpacketlibrary-aar')//红包sdk

    compile 'org.ocpsoft.prettytime:prettytime:3.2.7.Final'
    compile 'cn.leancloud.android:avoscloud-statistics:v3.4.5@aar'
    compile 'org.apache.httpcomponents:httpmime:4.2.4'
    compile 'com.jakewharton:butterknife:7.0.1'
    compile 'com.github.stuxuhai:jpinyin:1.0'

```

* leanchat-android的setting.gradle中

```
include ':leanchat', ':redpacketlibrary-aar'

```
###3.2 leanchat-android清单文件中注册红包相关组件

```

    <uses-sdk
        android:minSdkVersion="15"/>
 
    <!--红包相关界面 start-->
        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPRedPacketActivity"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustPan|stateVisible" />
        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPDetailActivity"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustPan" />

        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPRecordActivity"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustPan" />

        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPWebViewActivity"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustResize|stateHidden" />
        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPChangeActivity"
            android:configChanges="orientation|keyboardHidden|screenSize"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustResize|stateHidden" />
        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPBankCardActivity"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustPan|stateHidden" />
        <activity
            android:name="com.yunzhanghu.redpacketui.ui.activity.RPGroupMemberActivity"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustPan|stateHidden" />
        <activity
            android:name="com.alipay.sdk.app.H5PayActivity"
            android:configChanges="orientation|keyboardHidden|navigation|screenSize"
            android:exported="false"
            android:screenOrientation="behind"
            android:windowSoftInputMode="adjustResize|stateHidden" />
    <!--红包相关界面 end-->
```
###3.3 初始化红包上下文
* App中初始化红包上下文。

```
   import com.yunzhanghu.redpacketsdk.RedPacket;
   import com.yunzhanghu.redpacketui.RedPacketUtil;
    
    @Override
    public void onCreate() {
        /**
         * 注意：
         *
         * LeanCloud SDK调用第一步 初始化
         *
         * context上下文
         *
         * 只有两个进程需要初始化，主进程和 push 进程
         */
        // 初始化红包操作
        RedPacket.getInstance().initContext(ctx);
        //控制红包SDK中Log打印
        RedPacket.getInstance().setDebugMode(true);

    }
```
###3.4 初始化红包Token和用户信息
*MainActivity获取个人信息成功后请求签名（同步群组信息成功之后）并初始化红包token

```
    import com.yunzhanghu.redpacketui.RedPacketUtil;
    
RedPacketUtils.getInstance().setRefreshSign(MainActivity.this,mockUrl);                         

```

###3.5 ProfileFragment添加零钱页的入口

* 在需要添加零钱的页面调用下面的方法
   
```

  RPChangeActivity（零钱页面），跳转到零钱页面需要用户头像和用户名。具体的形式在RedPacketUtils
  的toChangeActivity方法中有展示。


```

##4. 关于群、单聊红包的释义

### 4.1 ConversationFragment中、在扩展栏中增加红包按钮
* 增加按钮的方法

```
   private void addRedPacketView() {
    View readPacketView = LayoutInflater.from(getContext()).inflate(R.layout.input_bottom_redpacket_view, null);
    readPacketView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EventBus.getDefault().post(new InputRedPacketClickEvent(imConversation.getConversationId()));
      }
    });
    inputBottomBar.addActionView(readPacketView);
  }
```
* InputRedPacketClickEvent  红包按钮的点击事件

```
  public void onEvent(InputRedPacketClickEvent clickEvent) {
    if (null != imConversation && null != clickEvent
      && imConversation.getConversationId().equals(clickEvent.tag)) {
      selectRedPacket();
    }
  }
```

* 添加单聊红包入口

```
单聊发红包页面RPRedPacketActivity（chatType为1），需要参数有发送人头像，发送人的用户名，单聊的chatType值，接收者id。
实例如下所示

 private void gotoSingleRedPacket(final String peerId) {
    
    int chatType = RPConstant.CHATTYPE_SINGLE;
    int membersNum = 0;
    String tpGroupId = "";
    String selfName = LeanchatUser.getCurrentUser().getUsername();
    String selfAvatar = LeanchatUser.getCurrentUser().getAvatarUrl();
    RedPacketUtils.selectRedPacket(ConversationFragment.this, peerId, selfName, selfAvatar, chatType, tpGroupId, membersNum, REQUEST_CODE_SEND_RED_PACKET);
  }     
```

* 添加群聊红包入口

```
群聊发红包页面为RPRedPacketActivity（chatType为2），需要参数有发送人头像，发送人的用户名，chatType值，群id,群成员数量。
实例如下所示：

   private void gotoGroupRedPacket() {
    final String fromNickname = LeanchatUser.getCurrentUser().getUsername();
    final String fromAvatarUrl = LeanchatUser.getCurrentUser().getAvatarUrl();
    final String toUserId=LeanchatUser.getCurrentUserId();
    Log.e("msg","======Group touserid=====>"+toUserId+LeanchatUser.getCurrentUserId());
    /**
     * 发送专属红包用的,获取群组成员
     */
    RedPacketUtils.getInstance().initRpGroupMember(imConversation.getMembers(), new GetGroupMemberCallback() {
      @Override
      public void groupInfoSuccess(final List<RPUserBean> rpUserList) {
        /**
         * 获取群成员消息成功调用
         */
        RPGroupMemberUtil.getInstance().setGroupMemberListener(new NotifyGroupMemberCallback() {
          @Override
          public void getGroupMember(String s, GroupMemberCallback groupMemberCallback) {
            groupMemberCallback.setGroupMember(rpUserList);
          }
        });
      }

      @Override
      public void groupInfoError() {

      }
    });

    imConversation.getMemberCount(new AVIMConversationMemberCountCallback() {
      @Override
      public void done(Integer integer, AVIMException e) {
        int chatType = RPConstant.CHATTYPE_GROUP;
        String tpGroupId = imConversation.getConversationId();
        int membersNum = integer;
        RedPacketUtils.selectRedPacket(ConversationFragment.this, toUserId, fromNickname, fromAvatarUrl, chatType, tpGroupId, membersNum, REQUEST_CODE_SEND_RED_PACKET);
      }
    });
  }         
```    
* 发送红包之后数据展示

```
@Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (Activity.RESULT_OK == resultCode) {
      switch (requestCode) {
        case REQUEST_CODE_SEND_RED_PACKET:
          processReadPack(data);
          break；
      }
    }
  }

  /**
   * 发送红包之后设置红包消息的数据
   * @param data
   */
  private void processReadPack(Intent data) {
    if (data != null) {
      String greetings = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_GREETING);
      String moneyID = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_ID);
      String sponsorName = getResources().getString(R.string.leancloud_luckymoney);
      String redPacketType = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_TYPE);//群红包类型
      String specialReceiveId = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_RECEIVER_ID);//专属红包接受者ID

      LCIMRedPacketMessage redPacketMessage = new LCIMRedPacketMessage();
      redPacketMessage.setGreeting(greetings);
      redPacketMessage.setReadPacketId(moneyID);
      redPacketMessage.setSponsorName(sponsorName);
      redPacketMessage.setRedPacketType(redPacketType);
      redPacketMessage.setReceiverId(specialReceiveId);
      sendMessage(redPacketMessage);
    }
  }    
```  

###4.2 ChatItemRedPacketHolder中打开红包消息和回执消息处理

* 接受红包消息打开红包

```

打开红包需要调用RPOpenPacketUtil.getInstance().openRedPacket()方法，需要传的参数为用户名，头像，是发送者还是接收者，是单聊还是群聊；要是专属红包需要再多加3个字段，接收者的用户名和头像，以及发送者的id。
实例如下：

   /**
  * Method name:openRedPacket
  * Describe: 打开红包
  * Remarks：
  */
  private void openRedPacket(final Context context, final LCIMRedPacketMessage message) {
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setCanceledOnTouchOutside(false);

    final String selfName = LeanchatUser.getCurrentUser().getUsername();
    String selfAvatar = LeanchatUser.getCurrentUser().getAvatarUrl();
    final String selfId = LeanchatUser.getCurrentUserId();
    String moneyMsgDirect; /*判断发送还是接收*/
    if (message.getFrom() != null && message.getFrom().equals(selfId)) {
      moneyMsgDirect = RedPacketUtils.MESSAGE_DIRECT_SEND;
    } else {
      moneyMsgDirect = RedPacketUtils.MESSAGE_DIRECT_RECEIVE;
    }
    int chatType=1;
    if (!TextUtils.isEmpty(message.getRedPacketType())){
      chatType=2;
    }else {
      chatType=1;
    }

    final RedPacketInfo redPacketInfo = RedPacketUtils.initRedPacketInfo_received(
      selfName, selfAvatar, moneyMsgDirect, chatType, message.getReadPacketId());
    if (null != rpUserBean) {
      /**
       * 打开专属红包需要多传一下的参数
       */
      redPacketInfo.specialNickname = rpUserBean.userNickname;
      redPacketInfo.specialAvatarUrl = rpUserBean.userAvatar;
      redPacketInfo.toUserId=selfId;
    }
    RPOpenPacketUtil.getInstance().openRedPacket(redPacketInfo,
      RedPacketUtils.getInstance().getTokenData(),
      (FragmentActivity) context,
      new RPOpenPacketUtil.RPOpenPacketCallBack() {
        @Override
        public void onSuccess(String senderId, String senderNickname) {
        
          LCIMRedPcketAckMessage ackMessage = new LCIMRedPcketAckMessage();
          ackMessage.setSenderId(senderId);
          ackMessage.setSenderName(senderNickname);
          ackMessage.setRecipientId(selfId);
          ackMessage.setRecipientName(selfName);
          ackMessage.setRedPacketType(message.getRedPacketType());
          EventBus.getDefault().post(new RedPacketAckEvent(ackMessage));
        }

        @Override
        public void showLoading() {
          progressDialog.show();
        }

        @Override
        public void hideLoading() {
          progressDialog.dismiss();
        }

        @Override
        public void onError(String code, String message) { /*错误处理*/
          Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
      });
  }    
```
* 接受红包消息之后会话列表回执消息的处理 LCIMRedPcketAckMessage

```
    @Override
  public String getShorthand() {
    String userId=LeanchatUser.getCurrentUserId();
    if (userId.equals(senderId)&&userId.equals(recipientId)){
      return "你领取了自己的红包";
    }else if (userId.equals(senderId)&&!userId.equals(recipientId)){
      return recipientName+"领取了你的红包";
    }else if (!userId.equals(senderId)&&userId.equals(recipientId)){
      return "你领取了"+senderName+"的红包";
    }else if (!userId.equals(senderId)&&!userId.equals(recipientId)){
      if (senderId.equals(recipientId)){
        return recipientName+"领取了自己的红包";
      }else {
        return recipientName+"领取了"+senderName+"的红包";
      }
    }
    return null;
  }
    
``` 
* 接受红包消息之后会话详情回执消息的处理ChatItemRedPacketAckHolder

```
   /**
   * @param senderName    红包发送者名字
   * @param recipientName 红包接收者名字
   * @param isSelf        是不是自己领取了自己的红包
   * @param isSend        消息是不是自己发送的
   * @param isSingle      是单聊还是群聊
   */
  private void initRedPacketAckChatItem(String senderName, String recipientName, boolean isSelf, boolean isSend, boolean isSingle) {
    if (isSend) {
      if (!isSingle) {
        if (isSelf) {
          contentView.setText(R.string.money_msg_take_money);
        } else {
          contentView.setText(String.format(getContext().getResources().getString(R.string.money_msg_take_someone_money), senderName));
        }
      } else {
        contentView.setText(String.format(getContext().getResources().getString(R.string.money_msg_take_someone_money), senderName));
      }
    } else {
      if (isSelf) {
        contentView.setText(String.format(getContext().getResources().getString(R.string.money_msg_someone_take_money), recipientName));
      } else {
        contentView.setText(String.format(getContext().getResources().getString(R.string.money_msg_someone_take_money_same), recipientName, senderName));
      }
    }
  }
    
``` 

###4.3 ChatAdapter中处理是哪种红包消息

```
   /**
   * 判断是什么红包类型的消息
   * @param position
   * @return
   */

  @Override
  public int getItemViewType(int position) {
    AVIMMessage message = messageList.get(position);
    if (null != message && message instanceof AVIMTypedMessage) {
      AVIMTypedMessage typedMessage = (AVIMTypedMessage) message;
      boolean isMe = fromMe(typedMessage);
      if (typedMessage.getMessageType() == LCIMRedPacketMessage.PACKMESSAGE_TYPE) {
        return isMe ? ITEM_RIGHT_TEXT_REDPACKET : ITEM_LEFT_TEXT_REDPACKET;
      } else if (typedMessage.getMessageType() == LCIMRedPcketAckMessage.RED_PACKET_ACK_MESSAGE_TYPE) {
        String selfId = LCChatKit.getInstance().getCurrentUserId();
        LCIMRedPcketAckMessage ackMessage = (LCIMRedPcketAckMessage) typedMessage;
        if (!TextUtils.isEmpty(ackMessage.getSenderId()) && !TextUtils.isEmpty(ackMessage.getRecipientId())) {
          return ackMessage.getSenderId().equals(selfId) || ackMessage.getRecipientId().equals(selfId)
            ? ITEM_TEXT_REDPACKET_NOTIFY : ITEM_TEXT_REDPACKET_NOTIFY_MEMBER;
        } else {
          return ITEM_TEXT_REDPACKET_NOTIFY_MEMBER;
        }
      }
    }
    return super.getItemViewType(position);
  }    
``` 





