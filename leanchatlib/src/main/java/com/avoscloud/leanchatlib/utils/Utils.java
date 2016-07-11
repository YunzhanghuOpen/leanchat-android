package com.avoscloud.leanchatlib.utils;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.EmotionHelper;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import utils.RedPacketUtils;

/**
 * Created by lzw on 15/4/27.
 */
public class Utils {
    public static String millisecsToDateString(long timestamp) {
        long gap = System.currentTimeMillis() - timestamp;
        if (gap < 1000 * 60 * 60 * 24) {
            String s = (new PrettyTime()).format(new Date(timestamp));
            return s;
        } else {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            return format.format(new Date(timestamp));
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
        }
    }

    static CharSequence cTemp = "";

    public static CharSequence getMessageeShorthand(final Context context, AVIMMessage message) {
        if (message instanceof AVIMTypedMessage) {
            AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(((AVIMTypedMessage) message).getMessageType());
            switch (type) {
                case TextMessageType:
                    return getTextMessageTypeShorthand(context, message);
                case ImageMessageType:
                    return "[图片]";
                case LocationMessageType:
                    return "[位置]";
                case AudioMessageType:
                    return "[语音]";
                default:
                    return "[未知]";
            }
        } else {
            return ProcessRedPacketMessage(context, message);
        }
    }

    /**
     * @param context
     * @param message
     * @return
     */
    private static CharSequence getTextMessageTypeShorthand(final Context context, AVIMMessage message) {
        Map<String, Object> attrs = ((AVIMTextMessage) message).getAttrs();
        if (attrs != null && attrs.containsKey(RedPacketUtils.KEY_RED_PACKET)) {
            JSONObject rpJSON = (JSONObject) attrs.get(RedPacketUtils.KEY_RED_PACKET);
            if (rpJSON != null && rpJSON.size() != 0) {
                String money_greeting = rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_GREETING);
                return "[" + context.getResources().getString(R.string.leancloud_luckymoney) + "]" + money_greeting;
            }
        }
        return EmotionHelper.replace(context, ((AVIMTextMessage) message).getText());
    }

    /**
     * 收取红包时会话列表最后一条消息要展示不同的消息内容
     *
     * @param context
     * @param message
     * @return
     */
    private static CharSequence ProcessRedPacketMessage(final Context context, AVIMMessage message) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(message.getContent());
            if (jsonObject != null) if (jsonObject.containsKey(RedPacketUtils.KEY_RED_PACKET)) {
                ChatManager chatManager = ChatManager.getInstance();
                String selfId = chatManager.getSelfId();
                if (jsonObject.containsKey(RedPacketUtils.KEY_TYPE) && jsonObject.getString(RedPacketUtils.KEY_TYPE).equals(RedPacketUtils.VALUE_TYPE)) {
                    JSONObject rpJSON = jsonObject.getJSONObject(RedPacketUtils.KEY_RED_PACKET);
                    if (rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_SENDER_ID).equals(selfId)) {
                        if (rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_RECEIVER_ID).equals(selfId)) {
                            return context.getResources().getString(R.string.money_msg_take_money);
                        }
                        String money_receiver = rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_RECEIVER_NAME);
                        return String.format(context.getResources().getString(R.string.money_msg_someone_take_money), money_receiver);
                    } else if (rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_RECEIVER_ID).equals(selfId)) {
                        String money_sender = rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_SENDER_NAME);
                        return String.format(context.getResources().getString(R.string.money_msg_take_someone_money), money_sender);
                    } else {
                        AVIMConversation conversation = AVIMClient.getInstance(ChatManager.getInstance().getSelfId()).getConversation(message.getConversationId());
                        /**
                         * 拉取消息，必须加入 conversation 后才能拉取消息
                         */
                        conversation.queryMessages(new AVIMMessagesQueryCallback() {
                            @Override
                            public void done(List<AVIMMessage> list, AVIMException e) {
                                if (filterException(e)) {
                                    cTemp = checkMsgs(list, context);
                                }
                            }
                        });
                        if (!TextUtils.isEmpty(cTemp)) {
                            return cTemp;
                        }
                    }
                }
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return "[" + context.getResources().getString(R.string.leancloud_luckymoney) + "]";
    }

    private static boolean filterException(Exception e) {
        if (e != null) {
            e.printStackTrace();
            //    toast(e.getMessage());
            return false;
        } else return true;
    }

    private static CharSequence checkMsgs(List<AVIMMessage> list, Context context) {
        CharSequence charSequence = "";
        for (int i = 0; i < list.size(); i++) {
            AVIMMessage message = list.get(list.size() - i - 1);
            if (message instanceof AVIMTypedMessage) {
                charSequence = isTypeMessage(context, message);
                break;
            } else {

                try {
                    JSONObject jsonObject = JSONObject.parseObject(message.getContent());
                    if (jsonObject != null)
                        if (jsonObject.containsKey(RedPacketUtils.KEY_RED_PACKET)) {
                            ChatManager chatManager = ChatManager.getInstance();
                            String selfId = chatManager.getSelfId();
                            if (jsonObject.containsKey(RedPacketUtils.KEY_TYPE) && jsonObject.getString(RedPacketUtils.KEY_TYPE).equals(RedPacketUtils.VALUE_TYPE)) {
                                JSONObject rpJSON = jsonObject.getJSONObject(RedPacketUtils.KEY_RED_PACKET);
                                if (rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_SENDER_ID).equals(selfId)) {
                                    if (rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_RECEIVER_ID).equals(selfId)) {
                                        charSequence = context.getResources().getString(R.string.money_msg_take_money);
                                        break;
                                    }
                                    String money_receiver = rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_RECEIVER_NAME);
                                    charSequence = String.format(context.getResources().getString(R.string.money_msg_someone_take_money), money_receiver);
                                    break;
                                } else if (rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_RECEIVER_ID).equals(selfId)) {
                                    String money_sender = rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_SENDER_NAME);
                                    charSequence = String.format(context.getResources().getString(R.string.money_msg_take_someone_money), money_sender);
                                    break;
                                }
                            }
                        }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return charSequence;
    }


    private static CharSequence isTypeMessage(Context context, AVIMMessage message) {

        AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(((AVIMTypedMessage) message).getMessageType());
        switch (type) {
            case TextMessageType:
                Map<String, Object> attrs = ((AVIMTextMessage) message).getAttrs();
                if (attrs != null && attrs.containsKey(RedPacketUtils.KEY_RED_PACKET)) {
                    JSONObject rpJSON = (JSONObject) attrs.get(RedPacketUtils.KEY_RED_PACKET);
                    if (rpJSON != null && rpJSON.size() != 0) {
                        String money_greeting = rpJSON.getString(RedPacketUtils.EXTRA_RED_PACKET_GREETING);
                        return "[LeanCloud红包]" + money_greeting;
                    }
                }
                return EmotionHelper.replace(context, ((AVIMTextMessage) message).getText());
            case ImageMessageType:
                return "[图片]";
            case LocationMessageType:
                return "[位置]";
            case AudioMessageType:
                return "[语音]";
            default:
                return "[未知]";
        }
    }
}