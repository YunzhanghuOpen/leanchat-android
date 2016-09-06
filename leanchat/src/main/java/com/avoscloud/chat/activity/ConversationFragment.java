package com.avoscloud.chat.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationMemberCountCallback;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.ChatAdapter;
import com.avoscloud.chat.event.InputRedPacketClickEvent;
import com.avoscloud.chat.event.RedPacketAckEvent;
import com.avoscloud.chat.model.ConversationType;
import com.avoscloud.chat.model.LCIMRedPacketMessage;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.chat.redpacket.GetGroupMemberCallback;
import com.avoscloud.chat.redpacket.RedPacketUtils;
import com.avoscloud.chat.util.ConversationUtils;
import com.yunzhanghu.redpacketsdk.bean.RPUserBean;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.callback.GroupMemberCallback;
import com.yunzhanghu.redpacketui.callback.NotifyGroupMemberCallback;
import com.yunzhanghu.redpacketui.utils.RPGroupMemberUtil;

import java.util.List;

import cn.leancloud.chatkit.activity.LCIMConversationFragment;
import cn.leancloud.chatkit.adapter.LCIMChatAdapter;
import cn.leancloud.chatkit.event.LCIMInputBottomBarEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarLocationClickEvent;
import cn.leancloud.chatkit.event.LCIMLocationItemClickEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 16/7/11.
 */
public class ConversationFragment extends LCIMConversationFragment {

  private static final int REQUEST_CODE_SEND_RED_PACKET = 4;
  public static final int LOCATION_REQUEST = 100;

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    addBaiduView();
    addRedPacketView();
  }

  @Override
  protected LCIMChatAdapter getAdpter() {
    return new ChatAdapter();
  }

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

  private void addBaiduView() {
    View mapView = LayoutInflater.from(getContext()).inflate(R.layout.input_bottom_map_view, null);
    mapView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EventBus.getDefault().post(new LCIMInputBottomBarLocationClickEvent(
          LCIMInputBottomBarEvent.INPUTBOTTOMBAR_LOCATION_ACTION, getTag()));
      }
    });
    inputBottomBar.addActionView(mapView);
  }

  public void onEvent(InputRedPacketClickEvent clickEvent) {
    if (null != imConversation && null != clickEvent
      && imConversation.getConversationId().equals(clickEvent.tag)) {
      selectRedPacket();
    }
  }

  public void onEvent(RedPacketAckEvent event) {
    sendMessage(event.ackMessage);
  }

  public void onEvent(LCIMInputBottomBarLocationClickEvent event) {
    LocationActivity.startToSelectLocationForResult(ConversationFragment.this, LOCATION_REQUEST);
  }

  public void onEvent(LCIMLocationItemClickEvent event) {
    if (null != event && null != event.message && event.message instanceof AVIMLocationMessage) {
      AVIMLocationMessage locationMessage = (AVIMLocationMessage) event.message;
      LocationActivity.startToSeeLocationDetail(getActivity(), locationMessage.getLocation().getLatitude(),
        locationMessage.getLocation().getLongitude());
    }
  }

  /**
   * 点击红包按钮之后的逻辑处理,分为两个部分,一是单聊发红包,二是,群聊发红包
   */
  public void selectRedPacket() {
    if (ConversationUtils.typeOfConversation(imConversation) == ConversationType.Single) {
      gotoSingleRedPacket(ConversationUtils.getConversationPeerId(imConversation));
    } else if (ConversationUtils.typeOfConversation(imConversation) == ConversationType.Group) {
      gotoGroupRedPacket();
    }
  }

  private void gotoSingleRedPacket(final String peerId) {
    int chatType = RPConstant.CHATTYPE_SINGLE;
    int membersNum = 0;
    String tpGroupId = "";
    String selfName = LeanchatUser.getCurrentUser().getUsername();
    String selfAvatar = LeanchatUser.getCurrentUser().getAvatarUrl();
    RedPacketUtils.selectRedPacket(ConversationFragment.this, peerId, selfName, selfAvatar, chatType, tpGroupId, membersNum, REQUEST_CODE_SEND_RED_PACKET);
  }

  private void gotoGroupRedPacket() {
    final String fromNickname = LeanchatUser.getCurrentUser().getUsername();
    final String fromAvatarUrl = LeanchatUser.getCurrentUser().getAvatarUrl();
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
        RedPacketUtils.selectRedPacket(ConversationFragment.this, "", fromNickname, fromAvatarUrl, chatType, tpGroupId, membersNum, REQUEST_CODE_SEND_RED_PACKET);
      }
    });
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (Activity.RESULT_OK == resultCode) {
      switch (requestCode) {
        case REQUEST_CODE_SEND_RED_PACKET:
          processReadPack(data);
          break;
        case LOCATION_REQUEST:
          processMap(data);
          break;
        default:
          break;
      }
    }
  }

  private void processMap(Intent intent) {
    final double latitude = intent.getDoubleExtra(LocationActivity.LATITUDE, 0);
    final double longitude = intent.getDoubleExtra(LocationActivity.LONGITUDE, 0);
    final String address = intent.getStringExtra(LocationActivity.ADDRESS);
    if (!TextUtils.isEmpty(address)) {
      AVIMLocationMessage locationMsg = new AVIMLocationMessage();
      locationMsg.setLocation(new AVGeoPoint(latitude, longitude));
      locationMsg.setText(address);
      sendMessage(locationMsg);
    } else {
      Toast.makeText(getContext(), R.string.chat_cannotGetYourAddressInfo, Toast.LENGTH_SHORT).show();
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
}
