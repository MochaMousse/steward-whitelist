package cc.mousse.steward.whitelist.service;

import cc.mousse.steward.whitelist.Application;
import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.common.Config;
import cc.mousse.steward.whitelist.common.Result;
import cc.mousse.steward.whitelist.to.EventTo;
import cc.mousse.steward.whitelist.to.GroupMemberTo;
import cc.mousse.steward.whitelist.utils.ApiUtil;
import cc.mousse.steward.whitelist.utils.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author PhineasZ
 */
public class BaseService {
  /** 服务器启动时初始化白名单 */
  public static void initWhitelist() {
    CacheService.intCache();
    CacheService.getAll().forEach(Application::addWhitelist);
  }

  /** 清理白名单 */
  private static void flushWhitelist() {
    var log = Common.getLog();
    try {
      // 垃圾桶
      var bin = new HashSet<String>();
      // 获取所有群成员信息
      var groupMembers = ApiUtil.getGroupMemberList(Config.getListenGroupId());
      // 获取所有有效群名片并转为小写
      var legalCards = new HashSet<String>();
      groupMembers.forEach(
          groupMember ->
              StrUtil.getLegal(groupMember.getCard())
                  .get(0)
                  .forEach(s -> legalCards.add(s.toLowerCase())));
      // blessingsink.player角色集合
      var playersInBlessingSkin = BlessingSkinService.getAllLowerNames();
      // 遍历所有有效群名片
      // 若blessingsink.player中不存在则过滤
      legalCards.removeIf(name -> !playersInBlessingSkin.contains(name));
      // multilogin.multilogin_in_game_profile_v3所有角色集合
      var playersInMultiLogin = MultiLoginService.getAllLowerNames();
      // 遍历multilogin.steward_cache缓存集合
      for (var player : CacheService.getAll()) {
        if (playersInMultiLogin.contains(player)) {
          // 若正式表中有记录，删除缓存
          CacheService.removeOne(player);
        } else if (!legalCards.contains(player)) {
          // 若有效群名片不存在，取消白名单
          Application.removeWhitelist(player);
          // 删除缓存
          CacheService.removeOne(player);
          bin.add(player);
        }
      }
      // multilogin.multilogin_in_game_profile_v3有白名单的角色集合
      for (var player : MultiLoginService.getAllWhitelistLowerNames()) {
        // 若有效群名片不存在，取消白名单
        if (!legalCards.contains(player)) {
          Application.removeWhitelist(player);
          bin.add(player);
        }
      }
      if (!bin.isEmpty()) {
        var message = StrUtil.getString("清理[{}]条白名单: {}", bin.size(), bin);
        log.info(message);
        ApiUtil.sendLog(message);
      }
      log.info("完成清理白名单");
    } catch (Exception e) {
      ApiUtil.apiFailMsg(e);
    }
  }

  /**
   * 添加有限制的白名单
   *
   * @param event 事件
   */
  private void addWhitelist(EventTo event) {
    try {
      // 结果集
      var result = new Result();
      var userId = event.getUserId();
      // 获取玩家群名片
      var groupMember = ApiUtil.getGroupMemberInfo(userId);
      var card = groupMember.getCard();
      var names = StrUtil.getLegal(card);
      // 超出数量限制的角色名
      for (var illegalName : names.get(1)) {
        result.outOfLimit(illegalName);
      }
      var legalNames = names.get(0);
      if (legalNames != null) {
        // 存在有效角色名时，处理每个角色名
        for (var legalName : legalNames) {
          // 查看blessingsink.player是否存在该角色
          if (BlessingSkinService.isNameExist(legalName)) {
            addWhitelist(result, legalName);
          } else {
            // 不存在角色
            result.playerNotFound(legalName);
          }
        }
      } else {
        // 不存在有效角色名
        result.noLegalName();
      }
      sendResult(result, event);
    } catch (Exception e) {
      ApiUtil.apiFailMsg(e);
    }
  }

  private void sendResult(Result result, EventTo event) {
    var log = Common.getLog();
    result
        .getDict()
        .forEach(
            (status, list) -> {
              var message = "";
              switch (status) {
                case SUCCESS -> {
                  try {
                    ApiUtil.sendLog(StrUtil.getString("添加[{}]条白名单: {}", list.size(), list));
                  } catch (JsonProcessingException e) {
                    log.severe(e.getMessage());
                  }
                  message = StrUtil.splice(status.getMsg(), list);
                }
                case OUT_OF_LIMIT -> {
                  list.add(String.valueOf(Config.getIdLimit()));
                  message = StrUtil.splice(status.getMsg(), list);
                }
                default -> message = StrUtil.splice(status.getMsg(), list);
              }
              try {
                ApiUtil.sendGroupReply(
                    String.valueOf(event.getMessageId()), message, event.getUserId());
              } catch (JsonProcessingException e) {
                ApiUtil.apiFailMsg(e);
              }
            });
  }

  /**
   * 添加临时的白名单
   *
   * @param event 事件
   */
  private void addTempWhitelist(EventTo event) {
    // 获取所有有效角色名
    var legalNames = StrUtil.getLegal(event.getMessage(), false);
    // 结果集
    var result = new Result();
    for (var legalName : legalNames.get(0)) {
      addWhitelist(result, legalName);
    }
    result
        .getDict()
        .forEach(
            (status, list) -> {
              var message = "";
              switch (status) {
                case SUCCESS -> message = StrUtil.getString("添加[{}]条临时白名单: {}", list.size(), list);
                case WHITELIST_ALREADY_EXISTS -> message = StrUtil.getString("已拥有白名单: {}", list);
                default -> message = "未知分支";
              }
              try {
                ApiUtil.sendLog(message);
              } catch (JsonProcessingException e) {
                ApiUtil.apiFailMsg(e);
              }
            });
  }

  /**
   * 添加白名单
   *
   * @param result 结果集
   * @param name 角色名
   */
  private void addWhitelist(Result result, String name) {
    if (MultiLoginService.isExistWhitelist(name)) {
      // 有正式记录且存在白名单
      CacheService.removeOne(name);
      result.whitelistAlreadyExists(name);
    } else if (CacheService.isExist(name)) {
      // 缓存中存在白名单
      result.whitelistAlreadyExists(name);
    } else if (MultiLoginService.isExist(name)) {
      // 有正式记录但没白名单
      CacheService.removeOne(name);
      MultiLoginService.addWhitelist(name);
      result.success(name);
    } else {
      // 没正式记录
      Application.addWhitelist(name);
      CacheService.addOne(name);
      result.success(name);
    }
  }

  public void memberDecrease(EventTo event) {
    if (Config.getListenGroupId().equals(event.getGroupId())) {
      flushWhitelist();
    }
  }

  public void memberMessage(EventTo event) {
    if (Config.getListenGroupId().equals(event.getGroupId())) {
      listenGroupMessage(event);
    }
    if (Config.getReportGroupId().equals(event.getGroupId())) {
      reportGroupMessage(event);
    }
  }

  /**
   * 监听群消息事件
   *
   * @param event 事件
   */
  private void listenGroupMessage(EventTo event) {
    var log = Common.getLog();
    try {
      var sender = event.getSender();
      var userId = sender.getUserId();
      if ("白名单".equals(event.getMessage())) {
        var card = sender.getCard();
        if (card.isEmpty()) {
          setNickNameAsCard(userId, sender.getNickname());
          // 群名片为空时，把昵称作为群名片
          var groupMember = ApiUtil.getGroupMemberInfo(userId);
          card = groupMember.getCard();
        }
        // 白名单请求
        var message = StrUtil.getString("[{}]::请求更新白名单: \"{}\"", userId, card);
        log.info(message);
        ApiUtil.sendLog(message);
        flushWhitelist();
        addWhitelist(event);
      }
    } catch (JsonProcessingException e) {
      ApiUtil.apiFailMsg(e);
    }
  }

  /**
   * 为玩家设置昵称为群名片
   *
   * @param nickName 昵称
   * @param userId QQ号
   */
  private void setNickNameAsCard(String userId, String nickName) {
    var log = Common.getLog();
    try {
      ApiUtil.setGroupCard(userId, nickName);
      var massage = StrUtil.getString("[{}]设置群名片: \"{}\"", userId, nickName);
      log.info(massage);
      ApiUtil.sendLog(massage);
    } catch (JsonProcessingException e) {
      ApiUtil.apiFailMsg(e);
    }
  }

  /**
   * 日志群消息事件
   *
   * @param event 事件
   */
  private void reportGroupMessage(EventTo event) {
    var message = event.getMessage();
    if ("刷新".equals(message)) {
      flushWhitelist();
    } else if (!StringUtils.isBlank(message) && message.startsWith("白名单")) {
      addTempWhitelist(event);
    }
  }

  public void memberChangeCard(EventTo event) {
    var log = Common.getLog();
    if (!Config.getListenGroupId().equals(event.getGroupId())) {
      return;
    }
    var userId = event.getUserId();
    var cardNew = event.getCardNew();
    var cardOld = event.getCardOld();
    if (!cardNew.isEmpty() && !Objects.equals(cardOld, cardNew)) {
      var message = StrUtil.getString("[{}]更新群名片: \"{}\" → \"{}\"", userId, cardOld, cardNew);
      log.info(message);
      try {
        ApiUtil.sendLog(message);
      } catch (JsonProcessingException e) {
        ApiUtil.apiFailMsg(e);
      }
    } else if (cardNew.isEmpty()) {
      // 群名片为空时自动设置群名片
      GroupMemberTo groupMember;
      try {
        groupMember = ApiUtil.getGroupMemberInfo(userId);
        if (groupMember.getCard().isEmpty()) {
          // 双重检测
          setNickNameAsCard(userId, groupMember.getNickName());
        }
      } catch (JsonProcessingException e) {
        ApiUtil.apiFailMsg(e);
      }
    }
    flushWhitelist();
  }
}
