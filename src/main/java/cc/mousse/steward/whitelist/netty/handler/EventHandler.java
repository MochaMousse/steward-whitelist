package cc.mousse.steward.whitelist.netty.handler;

import cc.mousse.steward.whitelist.service.GroupService;
import cc.mousse.steward.whitelist.to.EventTo;
import io.netty.channel.ChannelHandlerContext;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.netty.channel.SimpleChannelInboundHandler;
import lombok.val;

/**
 * 普通事件
 *
 * @author PhineasZ
 */
public class EventHandler extends SimpleChannelInboundHandler<EventTo> {
  /** 自定义线程池 */
  private static final ThreadPoolExecutor THREAD_POOL =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(4),
          r -> {
            val thread = new Thread(r);
            thread.setName("EventHandler");
            return thread;
          });

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, EventTo event) {
    // 忽略自己发言
    if (event.getUserId() != null && Objects.equals(event.getUserId(), event.getSelfId())) {
      return;
    }
    var groupService = new GroupService();
    THREAD_POOL.execute(
        () -> {
          var postType = event.getPostType();
          if ("notice".equals(postType)) {
            var noticeType = event.getNoticeType();
            if ("group_card".equals(noticeType)) {
              // 群名片更新（不保证时效性）
              groupService.memberChangeCard(event);
            } else if ("group_decrease".equals(noticeType)) {
              // 群成员减少
              groupService.memberDecrease(event);
            }
          } else if ("message".equals(postType) && ("group".equals(event.getMessageType()))) {
            groupService.memberMessage(event);
          }
        });
  }
}
