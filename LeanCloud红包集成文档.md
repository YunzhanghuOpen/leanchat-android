# LeanCloud红包SDK集成文档

## 集成概述

* 红包SDK分为两个版本，即钱包版红包SDK与支付宝版红包SDK，目前Demo只集成了钱包版红包SDK。
* 使用钱包版红包SDK的用户，可以使用银行卡支付或支付宝支付等第三方支付来发红包；收到的红包金额会进入到钱包余额，并支持提现到绑定的银行卡。
* 使用支付宝版红包SDK的用户，发红包仅支持支付宝支付；收到的红包金额即时入账至绑定的支付宝账号。
* 请选择希望接入的版本并下载对应的SDK进行集成，钱包版红包SDK与支付宝版红包SDK集成方式相同。
* 需要注意的是如果已经集成了钱包版红包SDK，暂不支持切换到支付宝版红包SDK（两个版本不支持互通）。

## 支付宝UI开源版本

* git clone git@github.com:YunzhanghuOpen/leanchat-android.git
* cd leanchat-android
* git checkout AliPayOpenSource
* git submodule add git@github.com:YunzhanghuOpen/redpacketui-open.git
* cd redpacketui-open
* git submodule init
* git submodule update

## 红包SDK的更新

以钱包版红包SDK为例，修改com.yunzhanghu.redpacket:redpacket-wallet:3.4.5中的3.4.5为已发布的更高版本(例如3.4.6)，同步之后即可完成红包SDK的更新。

## 开始集成

### 红包相关文件说明

* libs：包含了集成红包功能所依赖的jar包。
* res：包含了聊天页面中的资源文件（例如红包消息卡片，回执消息的UI等）。
* redpacket包 ：
  * GetSignInfoCallback ： 获取签名接口的回调
  * GetGroupMemberCallback ：获取群成员的接口回调
  * RedPacketUtils ：发送及拆红包相关的工具类
* model ：
  * LCIMRedPacketMessage ：红包消息
  * LCIMRedPcketAckMessage ：红包回执消息
  * LCIMTransferMessage ：转账消息
  * InputRedPacketClickEvent ：红包按钮点击事件监听
* viewholder ：
  * ChatItemRedPacketHolder ：红包UI展示类
  * ChatItemRedPacketAckHolder ：回执消息UI展示类
  * ChatItemRedPacketEmptyHolder ：与自己无关的回执消息类
  * ChatItemTransferHolder ：转账UI展示类

### 添加对红包SDK的依赖

* leanchat-android的build.gradle中添加远程仓库地址

```java
allprojects {
   repositories {
      jcenter()
      maven {
              url "https://raw.githubusercontent.com/YunzhanghuOpen/redpacket-maven-repo/master/release"
      }
   }
}
```

* leanchat的build.gradle增加对红包SDK及三方库的依赖

```java
dependencies {
    compile('com.yunzhanghu.redpacket:redpacket-wallet:3.5.0')//钱包版
    compile('com.yunzhanghu.redpacket:redpacket-alipay:2.0.1')//支付宝版
    compile files('libs/alipaySdk-20160516.jar')
    compile files('libs/glide-3.6.1.jar')
    compile files('libs/volley-1.0.19.jar')
    compile 'com.android.support:support-v4:25.3.1'
    compile 'com.android.support:recyclerview-v7:25.3.1'
}
```

### 兼容Android 7.0以上系统(仅钱包版)

* Android 7.0强制启用了被称作StrictMode的策略，带来的影响就是你的App对外无法暴露file://类型URI了。
* 如果你使用Intent携带这样的URI去打开外部App(比如：打开系统相机拍照)，那么会抛出FileUriExposedException异常。
* 由于钱包版SDK中有上传身份信息的功能，该功能调用了系统相机拍照，为了兼容Android 7.0以上系统，使用了FileProvider。
* 为保证红包SDK声明的FileProvider唯一，且不与其他应用中的FileProvider冲突，需要在App的build.gradle中增加resValue。
* 示例如下：
```java
defaultConfig {
    applicationId "your applicationId"
    minSdkVersion androidMinSdkVersion
    targetSdkVersion androidTargetSdkVersion
    resValue "string", "rp_provider_authorities_name", "${applicationId}.redpacket.fileProvider"
}
```
* 如果你的应用中也定义了FileProvider，会报合并清单文件错误，需要你在定义的FileProvider中添加tools:replace="android:authorities" 、 tools:replace="android:resource"

* 示例如下：

```java
<provider
   android:name="android.support.v4.content.FileProvider"
   tools:replace="android:authorities"
   android:authorities="包名.FileProvider"
   android:exported="false"
   android:grantUriPermissions="true">
   <meta-data
     android:name="android.support.FILE_PROVIDER_PATHS"
     tools:replace="android:resource"
     android:resource="@xml/rc_file_path" />
</provider>
```


## 初始化红包SDK

* Application的onCreate方法中

```java
@Override
public void onCreate() {
  // 初始化红包操作
  RedPacket.getInstance().initRedPacket(ctx,RPConstant.AUTH_METHOD_SIGN,  new RPInitRedPacketCallback() {       
      @Override
      public void initTokenData(final RPValueCallback<TokenData> rpValueCallback) {
          //在此方法中设置Token
          RedPacketUtils.getInstance().getRedPacketSign(ctx, new GetSignInfoCallback() {                        
              @Override
              public void signInfoSuccess(TokenData tokenData) {
                rpValueCallback.onSuccess(tokenData);
              }
    
              @Override
              public void signInfoError(String errorMsg) {
              }
            });
          }
    
          @Override
          public RedPacketInfo initCurrentUserSync() {
            //这里需要同步设置当前用户id、昵称和头像url
            RedPacketInfo redPacketInfo = new RedPacketInfo();
            redPacketInfo.currentUserId = LeanchatUser.getCurrentUserId();
            redPacketInfo.currentAvatarUrl = LeanchatUser.getCurrentUser().getAvatarUrl();
            redPacketInfo.currentNickname = LeanchatUser.getCurrentUser().getUsername();
            return redPacketInfo;
          }
  });
}
//控制红包SDK中Log打印
RedPacket.getInstance().setDebugMode(true);
```
* **initRedPacket(context, authMethod, callback) 参数说明**

| 参数名称       | 参数类型                    | 参数说明  | 必填     |
| ---------- | ----------------------- | ----- | ------ |
| context    | Context                 | 上下文   | 是      |
| authMethod | String                  | 授权类型  | 是（见注1） |
| callback   | RPInitRedPacketCallback | 初始化接口 | 是      |

* **RPInitRedPacketCallback 接口说明**

| **initTokenData(RPValueCallback<TokenData> callback)** |
| :--------------------------------------- |
| **该方法用于初始化TokenData，在进入红包相关页面、红包Token不存在或红包Token过期时调用。TokenData是请求红包Token所需要的数据模型，建议在该方法中异步向APP服务器获取相关参数，以保证数据的有效性；不建议从本地缓存中获取TokenData所需的参数，可能导致获取红包Token无效。** |
| **initCurrentUserSync()**                |
| **该方法用于初始化当前用户信息，在进入红包相关页面时调用，需同步获取。**   |

* **注1 ：**

**使用签名方式获取红包Token时，authMethod赋值必须为RPConstant.AUTH_METHOD_SIGN。**

* **注意：App Server提供的获取签名的接口必须先验证用户身份，并保证签名的用户和该登录用户一致，防止该接口被滥用。详见云账户[REST API开发文档](http://yunzhanghu-com.oss-cn-qdjbp-a.aliyuncs.com/%E4%BA%91%E8%B4%A6%E6%88%B7%E7%BA%A2%E5%8C%85%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3-v3_1_0.pdf)** 


## 注册红包消息组件
* Application的onCreate方法中

```java
AVIMMessageManager.registerAVIMMessageType(LCIMRedPacketMessage.class);
AVIMMessageManager.registerAVIMMessageType(LCIMRedPacketAckMessage.class);
AVIMMessageManager.registerAVIMMessageType(LCIMTransferMessage.class);
```

## 发红包及转账

### 添加红包按钮

* ConversationFragment中

```java 
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
### 添加转账按钮

* ConversationFragment中

```java 
private void addTransferView() {
  View transferView = LayoutInflater.from(getContext()).inflate(R.layout.input_bottom_transfer_view, null);
  transferView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      EventBus.getDefault().post(new InputTransferClickEvent(imConversation.getConversationId()));
    }
  });
  inputBottomBar.addActionView(transferView);
}
```
### 处理红包按钮点击事件

* ConversationFragment中

```java
public void onEvent(InputRedPacketClickEvent clickEvent) {
   if (ConversationUtils.typeOfConversation(imConversation) == ConversationType.Single) {
     toChatId = ConversationUtils.getConversationPeerId(imConversation);
     itemType = RPConstant.RP_ITEM_TYPE_SINGLE;
   } else if (ConversationUtils.typeOfConversation(imConversation) == ConversationType.Group) {
     itemType = RPConstant.RP_ITEM_TYPE_GROUP;
     toChatId = imConversation.getConversationId();
   }
   RedPacketUtils.getInstance().startRedPacket(getActivity(), imConversation, itemType, toChatId, new RPSendPacketCallback() {
     @Override
     public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
       //发送红包
       sendMessage(RedPacketUtils.getInstance().createRPMessage(getActivity(), redPacketInfo));
     }

     @Override
     public void onGenerateRedPacketId(String redPacketId) {

     }
   });
}
```
### 处理转账按钮点击事件

* ConversationFragment中

```java 
public void onEvent(InputTransferClickEvent clickEvent) {
  String toUserId = ConversationUtils.getConversationPeerId(imConversation);
  int itemType = RPConstant.RP_ITEM_TYPE_TRANSFER;
  RedPacketUtils.getInstance().startRedPacket(getActivity(), imConversation, itemType, toUserId, new RPSendPacketCallback() {
   @Override
   public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
     //发送红包
     sendMessage(RedPacketUtils.getInstance().createRPMessage(getActivity(), redPacketInfo));
   }

   @Override
   public void onGenerateRedPacketId(String redPacketId) {

   }
 });
}
```

### 进入红包及转账页方法

```java
public void startRedPacket(final FragmentActivity activity, final AVIMConversation imConversation, final int itemType, final String toUserId, final RPSendPacketCallback callback) {
  
  final RedPacketInfo redPacketInfo = new RedPacketInfo();
  
  if (itemType == RPConstant.RP_ITEM_TYPE_GROUP) {  
  
      //发送专属红包用的,获取群组成员
      RedPacket.getInstance().setRPGroupMemberListener(new RPGroupMemberListener() {
      
          @Override
          public void getGroupMember(String s, final RPValueCallback<List<RPUserBean>> rpValueCallback) {
          
              initRpGroupMember(imConversation.getMembers(), new GetGroupMemberCallback() {
                @Override
                public void groupInfoSuccess(List<RPUserBean> rpUserList) {
                  rpValueCallback.onSuccess(rpUserList);
                }
    
                @Override
                public void groupInfoError() {
    
                }
              });
            }
          });
          
          imConversation.getMemberCount(new AVIMConversationMemberCountCallback() {
            @Override
            public void done(Integer integer, AVIMException e) {
              redPacketInfo.groupId = imConversation.getConversationId();
              redPacketInfo.groupMemberCount = integer;
              RPRedPacketUtil.getInstance().startRedPacket(activity, itemType, redPacketInfo, callback);
            }
          });
        } else {
            String receiveAvatarUrl = "none";
            String receiveNickname = toUserId;
            redPacketInfo.receiverId = toUserId;
            LeanchatUser leanchatUser = UserCacheUtils.getCachedUser(toUserId);
            if (leanchatUser != null) {
                receiveNickname = TextUtils.isEmpty(leanchatUser.getUsername()) ? toUserId : leanchatUser.getUsername();
                receiveAvatarUrl = TextUtils.isEmpty(leanchatUser.getAvatarUrl()) ? "none" : leanchatUser.getAvatarUrl();
            }
            redPacketInfo.receiverNickname = receiveNickname;
            redPacketInfo.receiverAvatarUrl = receiveAvatarUrl;
            RPRedPacketUtil.getInstance().startRedPacket(activity, itemType, redPacketInfo, callback);
        }
} 
```

### 创建红包消息

```java
 public LCIMRedPacketMessage createRPMessage(Context context, RedPacketInfo redPacketInfo) {
    String selfName = LeanchatUser.getCurrentUser().getUsername();
    String selfID = LeanchatUser.getCurrentUserId();
    LCIMRedPacketMessage redPacketMessage = new LCIMRedPacketMessage();
    redPacketMessage.setGreeting(redPacketInfo.redPacketGreeting);
    redPacketMessage.setRedPacketId(redPacketInfo.redPacketId);
    redPacketMessage.setSponsorName(context.getResources().getString(R.string.leancloud_luckymoney));
    redPacketMessage.setRedPacketType(redPacketInfo.redPacketType);
    redPacketMessage.setReceiverId(redPacketInfo.receiverId);
    redPacketMessage.setMoney(true);
    redPacketMessage.setSenderName(selfName);
    redPacketMessage.setSenderId(selfID);
    return redPacketMessage;
}
```

### 创建转账消息

```java
 public LCIMTransferMessage createTRMessage(RedPacketInfo redPacketInfo) {
    LCIMTransferMessage transferMessage = new LCIMTransferMessage();
    transferMessage.setTransferAmount(redPacketInfo.redPacketAmount);
    transferMessage.setTransferTime(redPacketInfo.transferTime);
    transferMessage.setTransferMessage(true);
    transferMessage.setTransferReceivedNickname(redPacketInfo.receiverNickname);
    transferMessage.setTransferSenderNickname(redPacketInfo.senderNickname);
    return transferMessage;
}
```

## 拆红包及转账

### 设置红包卡片点击事件

* ChatItemRedPacketHolder的initView方法中

```java
@Override
public void initView() {
 redPacketLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mRedPacketMessage) {
          RedPacketUtils.getInstance().openRedPacket(getContext(), mRedPacketMessage);
        }
      }
    });
}
```

### 拆红包的方法

```java 
public void openRedPacket(final Context context, final LCIMRedPacketMessage message) {
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setCanceledOnTouchOutside(false);
    String redPacketId = message.getRedPacketId();
    String redPacketType = message.getRedPacketType();
    //支付宝版
    RPRedPacketUtil.getInstance().openRedPacket(redPacketId, redPacketType, 
    (FragmentActivity) context,new   RPRedPacketUtil.RPOpenPacketCallback() {
    
          @Override
          public void onSuccess(RedPacketInfo redPacketInfo) {
             String selfName = LeanchatUser.getCurrentUser().getUsername();
             String selfId = LeanchatUser.getCurrentUserId();
             RedPacketUtil.getInstance().sendRedPacketAckMsg(redPacketInfo.senderId, redPacketInfo.senderNickname, 
             selfId, selfName,  message);
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
          public void onError(String code, String message) { 
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
          }
        });
    //钱包版
    RedPacketInfo redPacketInfo = new RedPacketInfo();
    redPacketInfo.redPacketId = message.getRedPacketId();
    RPRedPacketUtil.getInstance().openRedPacket(redPacketInfo,
     (FragmentActivity) context,new RPRedPacketUtil.RPOpenPacketCallback() {

        @Override
        public void onSuccess(RedPacketInfo redPacketInfo) {
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

### 会话列表回执消息的处理 

* LCIMRedPcketAckMessage的getShorthand方法中

```java
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

### 会话详情回执消息的处理

* ChatItemRedPacketAckHolder的initRedPacketAckChatItem方法中

```java
private void initRedPacketAckChatItem(String senderName, String recipientName, boolean isSelf, boolean isSend, boolean isSingle) {
    if (isSend) {
      if (!isSingle) {
        if (isSelf) {
          contentView.setText(R.string.money_msg_take_money);
        } else {           
           contentView.setText(String.format(getContext().           
           getResources().
           getString(R.string.money_msg_take_someone_money),     
           senderName));
        }
      } else {
           contentView.setText(String.format(getContext().
           getResources().
           getString(R.string.money_msg_take_someone_money),  
           senderName));
      }
    } else {
      if (isSelf) {
           contentView.setText(String.format(getContext().
           getResources().
           getString(R.string.money_msg_someone_take_money),     
           recipientName));
      } else {
           contentView.setText(String.format(getContext().
           getResources().
           getString(R.string.money_msg_someone_take_money_same),    
           recipientName, senderName));
      }
    }
}      
```
### 设置转账卡片点击事件

* 仅支持钱包版
* ChatItemTransferHolder的initView方法中

```java
@Override
public void initView() {
 transferLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != transferMessage) {
          RedPacketUtils.getInstance().openTransfer(getContext(), transferMessage);
        }
      }
    });
}
```

### 拆转账的方法

* 仅支持钱包版

```java 
public void openTransfer(final Context context, final LCIMTransferMessage message) {
  RPRedPacketUtil.getInstance().openTransferPacket(context, wrapperTransferInfo(message));
}
```

* 封装拆转账所需参数

```java
private RedPacketInfo wrapperTransferInfo(LCIMTransferMessage message) {
   RedPacketInfo redPacketInfo = new RedPacketInfo();
   redPacketInfo.messageDirect = getMessageDirect(message);
   redPacketInfo.redPacketAmount = message.getTransferAmount();
   redPacketInfo.transferTime = message.getTransferTime();
   redPacketInfo.receiverNickname = message.getTransferReceivedNickname();
   redPacketInfo.senderNickname = message.getTransferSenderNickname();
    return redPacketInfo;
 }
```

```java

private String getMessageDirect(LCIMTransferMessage message) {
    String selfId = LeanchatUser.getCurrentUserId();
    String messageDirect; 
    if (message.getFrom() != null && message.getFrom().equals(selfId)) {
      messageDirect = RPConstant.MESSAGE_DIRECT_SEND;
    } else {
      messageDirect = RPConstant.MESSAGE_DIRECT_RECEIVE;
    }
    return messageDirect;
}
```

## 进入零钱页方法

* 仅支持钱包版

```java
RPRedPacketUtil.getInstance().startChangeActivity(getActivity())
```
* 获取零钱余额接口(仅支持钱包版)

```java
RPRedPacketUtil.getInstance().getChangeBalance(new RPValueCallback<String>() {
      @Override
      public void onSuccess(String changeBalance) {

      }

      @Override
      public void onError(String errorCode, String errorMsg) {

      }
});
```

## detachView接口

* RPRedPacketUtil.getInstance().detachView()

* 在拆红包方法所在页面销毁时调用，可防止内存泄漏。

* 调用示例(以ConversationFragment为例)

```java
@Override
public void onDestroy() {
    super.onDestroy();
    RPRedPacketUtil.getInstance().detachView();
}
```

## 拆红包音效
* 在assets目录下添加open_packet_sound.mp3或者open_packet_sound.wav文件即可(文件大小不要超过1M)。





