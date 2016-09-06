package com.avoscloud.chat.model;

import com.avos.avoscloud.im.v2.AVIMMessageCreator;
import com.avos.avoscloud.im.v2.AVIMMessageField;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import cn.leancloud.chatkit.LCChatMessageInterface;

/**
 * Created by wli on 16/7/14.
 * 红包发送被别人接收后的 tip message
 */
@AVIMMessageType(type = LCIMRedPcketAckMessage.RED_PACKET_ACK_MESSAGE_TYPE)
public class LCIMRedPcketAckMessage extends AVIMTypedMessage implements LCChatMessageInterface {
  public LCIMRedPcketAckMessage() {
  }

  public static final Creator<LCIMRedPcketAckMessage> CREATOR = new AVIMMessageCreator<LCIMRedPcketAckMessage>(LCIMRedPcketAckMessage.class);

  public static final int RED_PACKET_ACK_MESSAGE_TYPE = 1002;

  /**
   * 红包的发送者 id
   */
  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_SENDER_ID)
  private String senderId;

  /**
   * 红包的发送者 name
   */
  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_SENDER_NAME)
  private String senderName;

  /**
   * 红包的接收者 id
   */
  @AVIMMessageField(name = "recipient_id")
  private String recipientId;

  /**
   * 红包的接收者 name
   */
  @AVIMMessageField(name = "recipient_name")
  private String recipientName;

  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_TYPE)
  private String redPacketType;

  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_GREETING)
  private String greeting;

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
      return greeting;
    }
    return null;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public String getRecipientName() {
    return recipientName;
  }

  public void setRecipientName(String recipientName) {
    this.recipientName = recipientName;
  }

  public String getRedPacketType() {
    return redPacketType;
  }

  public void setRedPacketType(String redPacketType) {
    this.redPacketType = redPacketType;
  }

  public String getGreeting() {
    return greeting;
  }

  public void setGreeting(String greeting) {
    this.greeting = greeting;
  }
}
