package cc.mousse.steward.whitelist.common;

import cc.mousse.steward.whitelist.to.ReceiptTo;
import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 常量信息
 *
 * @author PhineasZ
 */
public class Common {
  protected static final Map<String, ReceiptTo> RECEIPT_MAP = new ConcurrentHashMap<>();
  public static final DruidDataSource BLESSING_SKIN_DATA_SOURCE = new DruidDataSource();
  public static final DruidDataSource MULTI_LOGIN_DATA_SOURCE = new DruidDataSource();
  public static final NioEventLoopGroup LEADER = new NioEventLoopGroup(1);
  public static final NioEventLoopGroup WORKER = new NioEventLoopGroup(2);
  public static final ObjectMapper MAPPER = new ObjectMapper();
  public static final String APP_NAME = "steward-whitelist";
  public static final String ECHO_PREFIX = "ECHO--";
  private static Logger log;
  private static ChannelHandlerContext ctx;

  private Common() {}

  public static void receiptMapPut(String key, ReceiptTo value) {
    RECEIPT_MAP.put(key, value);
  }

  public static ReceiptTo receiptMapPoll(String key) {
    var value = RECEIPT_MAP.get(key);
    RECEIPT_MAP.remove(key);
    return value;
  }

  public static boolean receiptMapContainsKey(String key) {
    return RECEIPT_MAP.containsKey(key);
  }

  public static void setLog(Logger log) {
    Common.log = log;
  }

  public static void setCtx(ChannelHandlerContext ctx) {
    Common.ctx = ctx;
  }

  public static Logger getLog() {
    return log;
  }

  public static ChannelHandlerContext getCtx() {
    return ctx;
  }
}
