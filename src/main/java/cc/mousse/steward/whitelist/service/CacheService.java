package cc.mousse.steward.whitelist.service;

import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.utils.DsUtil;
import org.intellij.lang.annotations.Language;

import java.util.Set;

/**
 * @author PhineasZ
 */
public class CacheService {
  private CacheService() {}

  public static void intCache() {
    @Language("MySQL")  var sql =
        "CREATE TABLE IF NOT EXISTS steward_cache (`name` VARCHAR(32) NOT NULL , CONSTRAINT `name` UNIQUE (`name`))";
    DsUtil.updateOne(Common.MULTI_LOGIN_DATA_SOURCE, sql);
  }

  public static Set<String> getAll() {
    var sql = "SELECT LOWER(name) FROM multilogin.steward_cache";
    return DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql);
  }

  public static void removeOne(String name) {
    var sql = "DELETE FROM multilogin.steward_cache WHERE name = ?";
    DsUtil.updateOne(Common.MULTI_LOGIN_DATA_SOURCE, sql, name);
  }

  public static void addOne(String name) {
    var sql = "INSERT IGNORE INTO multilogin.steward_cache VALUES (?)";
    DsUtil.updateOne(Common.MULTI_LOGIN_DATA_SOURCE, sql, name);
  }

  public static boolean isExist(String name) {
    var sql = "SELECT name FROM multilogin.steward_cache WHERE name = ? LIMIT 1";
    return !DsUtil.getOneFieldSet(Common.MULTI_LOGIN_DATA_SOURCE, sql, name).isEmpty();
  }
}
