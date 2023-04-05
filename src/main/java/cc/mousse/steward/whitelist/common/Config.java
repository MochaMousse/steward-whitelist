package cc.mousse.steward.whitelist.common;

/**
 * 统一配置信息
 *
 * @author PhineasZ
 */
public class Config {
  private static String serverPort;
  private static String listenGroupId;
  private static String idLimit;
  private static String reportGroupId;
  private static String success;
  private static String playerNotFound;
  private static String whitelistAlreadyExists;
  private static String noLegalName;
  private static String outOfLimit;

  public static String getServerPort() {
    return serverPort;
  }

  public static void setServerPort(String serverPort) {
    Config.serverPort = serverPort;
  }

  public static String getListenGroupId() {
    return listenGroupId;
  }

  public static void setListenGroupId(String listenGroupId) {
    Config.listenGroupId = listenGroupId;
  }

  public static String getIdLimit() {
    return idLimit;
  }

  public static void setIdLimit(String idLimit) {
    Config.idLimit = idLimit;
  }

  public static String getReportGroupId() {
    return reportGroupId;
  }

  public static void setReportGroupId(String reportGroupId) {
    Config.reportGroupId = reportGroupId;
  }

  public static String getSuccess() {
    return success;
  }

  public static void setSuccess(String success) {
    Config.success = success;
  }

  public static String getPlayerNotFound() {
    return playerNotFound;
  }

  public static void setPlayerNotFound(String playerNotFound) {
    Config.playerNotFound = playerNotFound;
  }

  public static String getWhitelistAlreadyExists() {
    return whitelistAlreadyExists;
  }

  public static void setWhitelistAlreadyExists(String whitelistAlreadyExists) {
    Config.whitelistAlreadyExists = whitelistAlreadyExists;
  }

  public static String getNoLegalName() {
    return noLegalName;
  }

  public static void setNoLegalName(String noLegalName) {
    Config.noLegalName = noLegalName;
  }

  public static String getOutOfLimit() {
    return outOfLimit;
  }

  public static void setOutOfLimit(String outOfLimit) {
    Config.outOfLimit = outOfLimit;
  }

  private Config() {}
}
