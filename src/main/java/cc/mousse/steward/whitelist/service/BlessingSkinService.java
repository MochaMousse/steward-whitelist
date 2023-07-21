package cc.mousse.steward.whitelist.service;

import cc.mousse.steward.whitelist.common.Common;
import cc.mousse.steward.whitelist.utils.DsUtil;
import org.intellij.lang.annotations.Language;

import java.util.Set;

/**
 * @author PhineasZ
 */
public class BlessingSkinService {
  private BlessingSkinService() {}

  public static Set<String> getAllLowerNames() {
    @Language("MySQL")
    var sql = "SELECT LOWER(name) FROM blessingskin.players";
    return DsUtil.getOneFieldSet(Common.BLESSING_SKIN_DATA_SOURCE, sql);
  }

  public static boolean isNameExist(String name) {
    @Language("MySQL")
    var sql = "SELECT name FROM blessingskin.players WHERE name = ?";
    return !DsUtil.getOneFieldSet(Common.BLESSING_SKIN_DATA_SOURCE, sql, name).isEmpty();
  }
}
