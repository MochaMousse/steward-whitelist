package cc.mousse.steward.whitelist.service;

import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.utils.DsUtil;

import java.util.Set;

/**
 * @author PhineasZ
 */
public class MultiLoginService {

  private MultiLoginService() {}

  public static Set<String> getAllWhitelistLowerNames() {
    var sql =
        "SELECT LOWER(current_username) FROM multilogin.multilogin_in_game_profile_v2 AS t1 JOIN multilogin.multilogin_user_data_v2 AS t2 ON t1.in_game_uuid = t2.in_game_profile_uuid WHERE whitelist = 1";
    return DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql);
  }

  public static Set<String> getAllLowerNames() {
    var sql =
        "SELECT LOWER(current_username) FROM multilogin.multilogin_in_game_profile_v2 AS t1 JOIN multilogin.multilogin_user_data_v2 AS t2 ON t1.in_game_uuid = t2.in_game_profile_uuid";
    return DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql);
  }

  public static boolean isExistWhitelist(String name) {
    var sql =
        "SELECT whitelist FROM multilogin.multilogin_in_game_profile_v2 AS t1 JOIN multilogin.multilogin_user_data_v2 AS t2 ON t1.in_game_uuid = t2.in_game_profile_uuid WHERE whitelist = 1 and current_username = ?";
    return !DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql, name).isEmpty();
  }

  public static boolean isExist(String name) {
    var sql =
        "SELECT whitelist FROM multilogin.multilogin_in_game_profile_v2 AS t1 JOIN multilogin.multilogin_user_data_v2 AS t2 ON t1.in_game_uuid = t2.in_game_profile_uuid WHERE current_username = ?";
    return !DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql, name).isEmpty();
  }

  public static void addWhitelist(String name) {
    var sql =
        "UPDATE multilogin.multilogin_user_data_v2 SET whitelist = 1 WHERE in_game_profile_uuid = (SELECT in_game_uuid FROM multilogin.multilogin_in_game_profile_v2 WHERE current_username = ?)";
    DsUtil.updateOne(Common.MULTI_LOGIN_DATA_SOURCE, sql, name);
  }
}
