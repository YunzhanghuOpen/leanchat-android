package com.avoscloud.chat.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.model.LCIMTransferMessage;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.chat.redpacket.RedPacketUtils;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.ui.activity.RPTransferDetailActivity;

import cn.leancloud.chatkit.viewholder.LCIMChatItemHolder;

/**
 * 转账
 */
public class ChatItemTransferHolder extends LCIMChatItemHolder {

  private TextView mTvTransfer;

  private RelativeLayout mTransferLayout;

  LCIMTransferMessage transferMessage;

  public ChatItemTransferHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    if (isLeft) {
      conventLayout.addView(View.inflate(getContext(),
              R.layout.lc_chat_item_left_text_transfer_layout, null));
    } else {
      conventLayout.addView(View.inflate(getContext(),
              R.layout.lc_chat_item_right_text_transfer_layout, null)); /*转账view*/
    }
    mTransferLayout = (RelativeLayout) itemView.findViewById(R.id.transfer_layout);
    mTvTransfer = (TextView) itemView.findViewById(R.id.tv_transfer_amount);

    mTransferLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != transferMessage) {
          openTransfer(getContext(), transferMessage);
        }
      }
    });
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    AVIMMessage message = (AVIMMessage) o;
    if (message instanceof LCIMTransferMessage) {
      transferMessage = (LCIMTransferMessage) message;
      mTvTransfer.setText(String.format("%s元", transferMessage.getTransferAmount()));
    }
  }

  /**
   * Method name:openRedPacket
   * Describe: 打开红包
   * Create person：侯洪旭
   * Create time：16/7/29 下午3:27
   * Remarks：
   */
  private void openTransfer(final Context context, final LCIMTransferMessage message) {
    final String fromNickname = LeanchatUser.getCurrentUser().getUsername();
    String fromAvatarUrl = LeanchatUser.getCurrentUser().getAvatarUrl();
    final String selfId = LeanchatUser.getCurrentUserId();
    String moneyMsgDirect; /*判断发送还是接收*/
    if (message.getFrom() != null && message.getFrom().equals(selfId)) {
      moneyMsgDirect = RPConstant.MESSAGE_DIRECT_SEND;
    } else {
      moneyMsgDirect = RPConstant.MESSAGE_DIRECT_RECEIVE;
    }
    RedPacketInfo redPacketInfo = new RedPacketInfo();
    redPacketInfo.moneyMsgDirect = moneyMsgDirect;
    redPacketInfo.redPacketAmount = message.getTransferAmount();
    redPacketInfo.fromNickName = fromNickname;
    redPacketInfo.fromAvatarUrl = fromAvatarUrl;
    redPacketInfo.transferTime = message.getTransferTime();
    Intent intent = new Intent(context, RPTransferDetailActivity.class);
    intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
    intent.putExtra(RPConstant.EXTRA_TOKEN_DATA, RedPacketUtils.getInstance().getTokenData());
    context.startActivity(intent);
  }
}
