package com.avoscloud.leanchatlib.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.ChatManager;

import java.util.Map;

import utils.RedPacketUtils;

/**
 * Created by ustc on 2016/5/30.
 */
public class ChatItemReceivedRedpacketHolder extends ChatItemHolder {

    protected TextView contentView;

    public ChatItemReceivedRedpacketHolder(Context context, ViewGroup root, boolean isLeft) {
        super(context, root, isLeft);

    }

    @Override
    public void initView() {
        super.initView();
        conventLayout.addView(View.inflate(getContext(), R.layout.chat_item_money_message, null));
        avatarView.setVisibility(View.GONE);

        contentView = (TextView) itemView.findViewById(R.id.tv_money_msg);


    }

    @Override
    public void bindData(Object o) {
        super.bindData(o);
        nameView.setText("");
        AVIMMessage message = (AVIMMessage) o;
        if (message instanceof AVIMTextMessage) {
            final AVIMTextMessage textMessage = (AVIMTextMessage) message;
            //获取附加字段
            final Map<String, Object> attrs = textMessage.getAttrs();
            ChatManager chatManager = ChatManager.getInstance();
            String selfId = chatManager.getSelfId();
            boolean isSend = fromMe(textMessage);
            RedPacketUtils.initReceivedRedpacketChatItem(attrs, isSend, selfId, contentView, getContext());
        }


    }


    private boolean fromMe(AVIMTypedMessage msg) {
        ChatManager chatManager = ChatManager.getInstance();
        String selfId = chatManager.getSelfId();
        return msg.getFrom() != null && msg.getFrom().equals(selfId);
    }



}
