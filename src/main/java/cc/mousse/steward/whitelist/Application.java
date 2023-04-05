package cc.mousse.steward.whitelist;

import cc.mousse.steward.whitelist.common.Config;
import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.netty.Server;
import cc.mousse.steward.whitelist.utils.ApiUtil;
import cc.mousse.steward.whitelist.utils.DsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

import static cc.mousse.steward.whitelist.common.Common.*;

/**
 * @author PhineasZ
 */
public class Application extends JavaPlugin {
  private static final Thread THREAD = new Thread(() -> new Server().start());
  private static Application instance;

  @Override
  public void onEnable() {
    // Plugin startup logic
    Application.setInstance(this);
    Common.setLog(this.getLogger());
    init();
    THREAD.start();
  }

  @Override
  public void onDisable() {
    try {
      ApiUtil.sendLog("已关闭");
    } catch (JsonProcessingException e) {
      ApiUtil.apiFailMsg(e);
    }
    // Plugin shutdown logic
    LEADER.shutdownGracefully();
    WORKER.shutdownGracefully();
  }

  private void init() {
    var log = Common.getLog();
    // 加载配置文件
    this.saveDefaultConfig();
    this.reloadConfig();
    // 读取配置文件
    var config = this.getConfig();
    var driverClassName = config.getString("datasource.driver-class-name");
    var multiLoginUrl = config.getString("datasource.multi-login.url");
    var multiLoginUsername = config.getString("datasource.multi-login.username");
    var multiLoginPassword = config.getString("datasource.multi-login.password");
    var blessingSkinUrl = config.getString("datasource.blessing-skin.url");
    var blessingSkinUsername = config.getString("datasource.blessing-skin.username");
    var blessingSkinPassword = config.getString("datasource.blessing-skin.password");
    if (StringUtils.isAnyBlank(
        driverClassName,
        multiLoginUrl,
        multiLoginUsername,
        multiLoginPassword,
        blessingSkinUrl,
        blessingSkinUsername,
        blessingSkinPassword)) {
      log.severe("数据源配置文件不完整");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    try {
      DsUtil.init(
          MULTI_LOGIN_DATA_SOURCE,
          driverClassName,
          multiLoginUrl,
          multiLoginUsername,
          multiLoginPassword);
      DsUtil.init(
          BLESSING_SKIN_DATA_SOURCE,
          driverClassName,
          blessingSkinUrl,
          blessingSkinUsername,
          blessingSkinPassword);
    } catch (SQLException e) {
      log.severe("数据源配置错误");
      e.printStackTrace();
    }
    var severPort = config.getString("steward.server.port");
    var listenGroupId = config.getString("steward.server.listen-group.id");
    var playerIdLimit = config.getString("steward.server.listen-group.player-id-limit");
    var reportGroupId = config.getString("steward.server.report-group.id");
    if (StringUtils.isAnyBlank(severPort, listenGroupId, playerIdLimit, reportGroupId)) {
      log.severe("机器人配置文件不完整");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    Config.setServerPort(severPort);
    Config.setListenGroupId(listenGroupId);
    Config.setIdLimit(playerIdLimit);
    Config.setReportGroupId(reportGroupId);
    var success = config.getString("steward.message.success");
    var playerNotFound = config.getString("steward.message.player-not-found");
    var whitelistAlreadyExists = config.getString("steward.message.whitelist-already-exists");
    var noLegalName = config.getString("steward.message.no-legal-name");
    var outOfLimit = config.getString("steward.message.out-of-limit");
    if (StringUtils.isAnyBlank(
        success, playerNotFound, whitelistAlreadyExists, noLegalName, outOfLimit)) {
      log.severe("应答消息配置文件不完整");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    Config.setSuccess(success);
    Config.setPlayerNotFound(playerNotFound);
    Config.setWhitelistAlreadyExists(whitelistAlreadyExists);
    Config.setNoLegalName(noLegalName);
    Config.setOutOfLimit(outOfLimit);
  }

  public static void setInstance(Application instance) {
    Application.instance = instance;
  }

  public static void addWhitelist(String name) {
    Bukkit.getScheduler()
        .runTask(
            instance,
            () ->
                Bukkit.getServer()
                    .dispatchCommand(
                        Bukkit.getConsoleSender(), "multilogin whitelist add " + name));
  }

  public static void removeWhitelist(String name) {
    Bukkit.getScheduler()
        .runTask(
            instance,
            () ->
                Bukkit.getServer()
                    .dispatchCommand(
                        Bukkit.getConsoleSender(), "multilogin whitelist remove " + name));
  }
}
