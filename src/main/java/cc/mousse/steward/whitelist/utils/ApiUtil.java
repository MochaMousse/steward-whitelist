package cc.mousse.steward.whitelist.utils;

import cc.mousse.steward.whitelist.common.Config;
import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.common.CqCode;
import cc.mousse.steward.whitelist.to.GroupMemberTo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import static cc.mousse.steward.whitelist.common.Common.*;

/**
 * @author PhineasZ
 */
public class ApiUtil {
  private ApiUtil() {}

  /**
   * 获取监听群群成员列表
   *
   * @param groupId 群号
   * @return 群成员信息列表
   * @throws JsonProcessingException 异常
   */
  public static List<GroupMemberTo> getGroupMemberList(String groupId)
      throws JsonProcessingException {
    MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    var api = Api.action("get_group_member_list").putGroupId(groupId);
    var echoId = sendRequest(api);
    // 转换data字段
    return MAPPER.readValue(receiptMapPoll(echoId).getData().toString(), new TypeReference<>() {});
  }

  /**
   * 获取监听群群成员信息
   *
   * @param userId QQ号
   * @return 群成员信息
   * @throws JsonProcessingException 异常
   */
  public static GroupMemberTo getGroupMemberInfo(String userId) throws JsonProcessingException {
    var api =
        Api.action("get_group_member_info").putGroupId(Config.getListenGroupId()).putUserId(userId);
    var echoId = sendRequest(api);
    // 转换data字段
    return MAPPER.readValue(receiptMapPoll(echoId).getData().toString(), GroupMemberTo.class);
  }

  /**
   * 设置监听群群成员的群名片（群备注）
   *
   * @param userId QQ号
   * @param card 群名片
   * @throws JsonProcessingException 异常
   */
  public static void setGroupCard(String userId, String card) throws JsonProcessingException {
    var api =
        Api.action("set_group_card")
            .putGroupId(Config.getListenGroupId())
            .putUserId(userId)
            .putCard(card);
    sendRequest(api);
  }

  /**
   * 发送日志消息
   *
   * @param message 消息内容
   * @throws JsonProcessingException 异常
   */
  public static void sendLog(String message) throws JsonProcessingException {
    sendGroupMsg(Config.getReportGroupId(), "[" + APP_NAME + "]::[info]::" + message);
  }

  /**
   * 发送监听群回复消息
   *
   * @param id 消息ID
   * @param message 消息内容
   * @param userId QQ号
   * @throws JsonProcessingException 异常
   */
  public static void sendGroupReply(String id, String message, String userId)
      throws JsonProcessingException {
    sendGroupReply(Config.getListenGroupId(), id, message, userId);
  }

  /**
   * 发送回复消息
   *
   * @param groupId 群号
   * @param id 消息ID
   * @param message 消息内容
   * @param userId QQ号
   * @throws JsonProcessingException 异常
   */
  public static void sendGroupReply(String groupId, String id, String message, String userId)
      throws JsonProcessingException {
    var replyCqCode = CqCode.reply().putId(id);
    var atCqCode = CqCode.at().putQq(userId);
    sendGroupMsg(groupId, replyCqCode.toString() + atCqCode + message);
  }

  /**
   * 发送群消息
   *
   * @param groupId 群号
   * @param message 消息
   * @throws JsonProcessingException 异常
   */
  public static void sendGroupMsg(String groupId, String message) throws JsonProcessingException {
    var api =
        Api.action("send_group_msg")
            .putGroupId(groupId)
            .putMessage(message)
            .putAutoEscape(String.valueOf(false));
    Common.getCtx().channel().writeAndFlush(new TextWebSocketFrame(MAPPER.writeValueAsString(api)));
  }

  /**
   * 统一发送API请求
   *
   * @param api API信息
   * @return Echo ID
   * @throws JsonProcessingException 异常
   */
  private static String sendRequest(Api api) throws JsonProcessingException {
    var log = Common.getLog();
    // 生成EchoId
    var echoId = ECHO_PREFIX + UUID.randomUUID();
    // 补充给bean对象
    api.setEcho(echoId);
    // 发送API
    Common.getCtx().channel().writeAndFlush(new TextWebSocketFrame(MAPPER.writeValueAsString(api)));
    var startWaitTs = System.currentTimeMillis();
    while (!receiptMapContainsKey(echoId)) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.severe(e.getMessage());
        Thread.currentThread().interrupt();
      }
      if (System.currentTimeMillis() - startWaitTs > 5000) {
        log.severe("等待回执超时");
        return null;
      }
    }
    return echoId;
  }

  public static void apiFailMsg(Exception e) {
    var log = Common.getLog();
    log.severe("API发送错误，可能未与GO-CQHTTP连接");
    log.severe(e.getMessage());
  }

  @Data
  @AllArgsConstructor
  static class Api {
    /** 终结点名称，如'send_group_msg' */
    private String action;
    /** 参数 */
    private Map<String, String> params;
    /** '回声'，如果指定了echo字段，那么响应包也会同时包含一个echo字段，它们会有相同的值 */
    private String echo;

    public Api() {
      this.params = new HashMap<>(6);
    }

    public static Api action(String action) {
      var api = new Api();
      api.action = action;
      // 设置不使用缓存
      api.params.put("no_cache", String.valueOf(true));
      return api;
    }

    public Api putGroupId(String groupId) {
      this.params.put("group_id", groupId);
      return this;
    }

    public Api putUserId(String userId) {
      this.params.put("user_id", userId);
      return this;
    }

    public Api putCard(String card) {
      this.params.put("card", card);
      return this;
    }

    public Api putMessage(String message) {
      this.params.put("message", message);
      return this;
    }

    public Api putAutoEscape(String autoEscape) {
      this.params.put("auto_escape", autoEscape);
      return this;
    }
  }
}
