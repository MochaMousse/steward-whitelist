package cc.mousse.steward.whitelist.netty.handler;


import cc.mousse.steward.whitelist.to.ReceiptTo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.val;

import static cc.mousse.steward.whitelist.common.Common.*;

/**
 * 回执事件
 *
 * @author PhineasZ
 */
public class ReceiptHandler extends SimpleChannelInboundHandler<ReceiptTo> {
  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, ReceiptTo receipt) {
    // 若echo信息匹配，则将回执加入集合
    if (receipt != null) {
      val echo = receipt.getEcho();
      if (echo != null && echo.contains(ECHO_PREFIX)) {
        receiptMapPut(echo, receipt);
      }
    }
  }
}
