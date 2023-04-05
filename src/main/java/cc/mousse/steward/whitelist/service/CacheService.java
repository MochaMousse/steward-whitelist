package cc.mousse.steward.whitelist.service;

import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.utils.DsUtil;

import java.util.Set;

/**
 * @author PhineasZ
 */
public class CacheService {
  private CacheService() {}

  public static synchronized Set<String> getAll() {
    var sql = "SELECT LOWER(name) FROM multilogin.steward_cache";
    return DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql);
  }

  public static synchronized void removeOne(String name) {
    var sql = "DELETE FROM multilogin.steward_cache WHERE name = ?";
    DsUtil.updateOne(Common.MULTI_LOGIN_DATA_SOURCE, sql, name);
  }

  public static synchronized void addOne(String name) {
    var sql = "INSERT IGNORE INTO multilogin.steward_cache VALUES (?)";
    DsUtil.updateOne(Common.MULTI_LOGIN_DATA_SOURCE, sql, name);
  }
}
