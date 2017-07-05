package com.avoscloud.chat.model;

import com.avos.avoscloud.im.v2.AVIMMessageCreator;
import com.avos.avoscloud.im.v2.AVIMMessageField;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import cn.leancloud.chatkit.LCChatMessageInterface;

/**
 * Created by hhx on 16/10/12.
 */
@AVIMMessageType(type = LCIMTransferMessage.TRANSFER_MESSAGE_TYPE)
public class LCIMTransferMessage extends AVIMTypedMessage implements LCChatMessageInterface {

    public static final int TRANSFER_MESSAGE_TYPE = 5;

    public LCIMTransferMessage() {
    }

    /**
     * 红包金额
     */
    @AVIMMessageField(name = RPConstant.MESSAGE_ATTR_TRANSFER_AMOUNT)
    private String transferAmount;

    /**
     * 转账时间
     */
    @AVIMMessageField(name = RPConstant.MESSAGE_ATTR_TRANSFER_TIME)
    private String transferTime;

    /**
     * 是否是转账消息
     */
    @AVIMMessageField(name = RPConstant.MESSAGE_ATTR_IS_TRANSFER_PACKET_MESSAGE)
    private boolean isTransferMessage;

    /**
     * 转账接受者id
     */
    @AVIMMessageField(name = RPConstant.MESSAGE_ATTR_RED_PACKET_RECEIVER_ID)
    private String transferToUserId;
    /**
     * 转账接受者昵称
     */
    @AVIMMessageField(name = RPConstant.MESSAGE_ATTR_RED_PACKET_RECEIVER_NICKNAME)
    private String transferReceivedNickname;
    /**
     * 转账发送者昵称
     */
    @AVIMMessageField(name = RPConstant.MESSAGE_ATTR_RED_PACKET_SENDER_NICKNAME)
    private String transferSenderNickname;

    public static final Creator<LCIMTransferMessage> CREATOR = new AVIMMessageCreator<LCIMTransferMessage>(LCIMTransferMessage.class);

    @Override
    public String getShorthand() {
        String userId = LeanchatUser.getCurrentUserId();
        if (userId.equals(transferToUserId)) {
            return "[转账]向你转账" + transferAmount + "元";
        } else {
            return "[转账]转账" + transferAmount + "元";
        }
    }

    public String getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(String transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public boolean isTransferMessage() {
        return isTransferMessage;
    }

    public void setTransferMessage(boolean transferMessage) {
        isTransferMessage = transferMessage;
    }

    public String getTransferToUserId() {
        return transferToUserId;
    }

    public void setTransferToUserId(String transferToUserId) {
        this.transferToUserId = transferToUserId;
    }

    public String getTransferReceivedNickname() {
        return transferReceivedNickname;
    }

    public void setTransferReceivedNickname(String transferReceivedNickname) {
        this.transferReceivedNickname = transferReceivedNickname;
    }

    public String getTransferSenderNickname() {
        return transferSenderNickname;
    }

    public void setTransferSenderNickname(String transferSenderNickname) {
        this.transferSenderNickname = transferSenderNickname;
    }
}
