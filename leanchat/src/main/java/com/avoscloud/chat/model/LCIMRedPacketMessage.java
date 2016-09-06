package com.avoscloud.chat.model;

import com.avos.avoscloud.im.v2.AVIMMessageCreator;
import com.avos.avoscloud.im.v2.AVIMMessageField;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import cn.leancloud.chatkit.LCChatMessageInterface;

/**
 * Created by wli on 16/7/11.
 */

@AVIMMessageType(type = LCIMRedPacketMessage.PACKMESSAGE_TYPE)
public class LCIMRedPacketMessage extends AVIMTypedMessage implements LCChatMessageInterface {

  public static final int PACKMESSAGE_TYPE = 1001;

  public LCIMRedPacketMessage() {}

  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_ID)
  private String readPacketId;

  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_GREETING)
  private String greeting;

  @AVIMMessageField(name = RPConstant.EXTRA_SPONSOR_NAME)
  private String sponsorName;

  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_TYPE)
  private String redPacketType;

  @AVIMMessageField(name = RPConstant.EXTRA_RED_PACKET_RECEIVER_ID)
  private String receiverId;

  public static final Creator<LCIMRedPacketMessage> CREATOR = new AVIMMessageCreator<LCIMRedPacketMessage>(LCIMRedPacketMessage.class);

  @Override
  public String getShorthand() {
    return "["+sponsorName+"]" + greeting;
  }

  public String getReadPacketId() {
    return readPacketId;
  }

  public void setReadPacketId(String readPacketId) {
    this.readPacketId = readPacketId;
  }

  public String getSponsorName() {
    return sponsorName;
  }

  public void setSponsorName(String sponsorName) {
    this.sponsorName = sponsorName;
  }

  public String getGreeting() {
    return greeting;
  }

  public void setGreeting(String greeting) {
    this.greeting = greeting;
  }

  public String getRedPacketType() {
    return redPacketType;
  }

  public void setRedPacketType(String redPacketType) {
    this.redPacketType = redPacketType;
  }

  public String getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }
}