package com.avoscloud.chat.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.chat.model.LCIMRedPacketMessage;
import com.avoscloud.chat.model.LCIMRedPcketAckMessage;
import com.avoscloud.chat.viewholder.ChatItemRedPacketAckHolder;
import com.avoscloud.chat.viewholder.ChatItemRedPacketEmptyHolder;
import com.avoscloud.chat.viewholder.ChatItemRedPacketHolder;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.adapter.LCIMChatAdapter;

/**
 * Created by wli on 16/7/11.
 */
public class ChatAdapter extends LCIMChatAdapter {

  private final int ITEM_LEFT_TEXT_REDPACKET = 1005;//自己不是发送红包者
  private final int ITEM_RIGHT_TEXT_REDPACKET = 2005;//自己是发送红包者
  private final int ITEM_TEXT_REDPACKET_NOTIFY = 3000;//会话详情显示领取红包后显示的回执消息(自己是发送者或者是接收者)
  private final int ITEM_TEXT_REDPACKET_NOTIFY_MEMBER = 3001;//会话详情页领取红包后回执的空消息(自己不是发送者也不是接收者)

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case ITEM_LEFT_TEXT_REDPACKET:
        return new ChatItemRedPacketHolder(parent.getContext(), parent, true);
      case ITEM_RIGHT_TEXT_REDPACKET:
        return new ChatItemRedPacketHolder(parent.getContext(), parent, false);
      case ITEM_TEXT_REDPACKET_NOTIFY:
        return new ChatItemRedPacketAckHolder(parent.getContext(), parent, false);
      case ITEM_TEXT_REDPACKET_NOTIFY_MEMBER:
        return new ChatItemRedPacketEmptyHolder(parent.getContext(), parent);
      default:
        return super.onCreateViewHolder(parent, viewType);
    }
  }

  /**
   * 判断是什么消息类型
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
}
