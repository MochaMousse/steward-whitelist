package cc.mousse.steward.whitelist.netty;

import cc.mousse.steward.whitelist.common.Config;
import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.netty.handler.EventHandler;
import cc.mousse.steward.whitelist.netty.handler.ReceiptHandler;
import cc.mousse.steward.whitelist.to.EventTo;
import cc.mousse.steward.whitelist.to.ReceiptTo;
import cc.mousse.steward.whitelist.utils.ApiUtil;
import cc.mousse.steward.whitelist.utils.StrUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.util.Objects;

import static cc.mousse.steward.whitelist.common.Common.*;

/**
 * 消息入口
 *
 * @author PhineasZ
 */
public class Server {
  public void start() {
    var log = Common.getLog();
    new ServerBootstrap()
        .group(LEADER, WORKER)
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<NioSocketChannel>() {
              @Override
              protected void initChannel(NioSocketChannel nioSocketChannel) {
                nioSocketChannel
                    .pipeline()
                    .addLast(new HttpServerCodec())
                    .addLast(new ChunkedWriteHandler())
                    .addLast(new HttpObjectAggregator(65535))
                    .addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE))
                    .addLast(new WebSocketServerCompressionHandler())
                    .addLast(new WebSocketServerProtocolHandler("/"))
                    .addLast(
                        new SimpleChannelInboundHandler<TextWebSocketFrame>() {
                          @Override
                          protected void channelRead0(
                              ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame)
                              throws Exception {
                            var text = textWebSocketFrame.text();
                            // 判断事件类型
                            var jsonNode = MAPPER.readTree(text);
                            var eventType = jsonNode.get("meta_event_type");
                            Common.setCtx(ctx);
                            if (eventType != null
                                && Objects.equals(
                                    "lifecycle", StrUtil.removeQuotes(eventType.toString()))) {
                              var message = "已就绪";
                              ApiUtil.sendLog(message);
                            } else if (jsonNode.get("retcode") != null) {
                              // 消息类型为消息回执
                              try {
                                var receipt = MAPPER.readValue(text, ReceiptTo.class);
                                super.channelRead(ctx, receipt);
                              } catch (Exception e) {
                                log.info(e.getMessage());
                              }
                            } else {
                              // 消息类型为事件消息
                              try {
                                var event = MAPPER.readValue(text, EventTo.class);
                                super.channelRead(ctx, event);
                              } catch (Exception e) {
                                log.severe(e.getMessage());
                              }
                            }
                          }
                        })
                    .addLast(new EventHandler())
                    .addLast(new ReceiptHandler());
              }
            })
        .bind(Integer.parseInt(Config.getServerPort()));
    log.info("已就绪");
  }
}
