package cc.mousse.steward.whitelist.common;

import java.util.*;
import lombok.Data;

/**
 * 白名单设置结果
 *
 * @author PhineasZ
 */
@Data
public class Result {
  private Map<Status, List<String>> dict;
  private Set<String> existed;

  public Result() {
    dict = new EnumMap<>(Status.class);
    existed = new HashSet<>();
  }

  public void success(String data) {
    this.put(Status.SUCCESS, data);
  }

  public void playerNotFound(String data) {
    this.put(Status.PLAYER_NOT_FOUND, data);
  }

  public void whitelistAlreadyExists(String data) {
    this.put(Status.WHITELIST_ALREADY_EXISTS, data);
  }

  public void noLegalName() {
    dict.putIfAbsent(Status.NO_LEGAL_NAME, new ArrayList<>());
  }

  public void outOfLimit(String data) {
    this.put(Status.OUT_OF_LIMIT, data);
  }

  private void put(Status status, String data) {
    if (!existed.contains(data)) {
      dict.putIfAbsent(status, new ArrayList<>());
      dict.get(status).add(data);
      existed.add(data);
    }
  }

  public enum Status {
    /**
     * SUCCESS：添加成功<br>
     * PLAYER_NOT_FOUND：BlessingSkin找不到玩家<br>
     * WHITELIST_ALREADY_EXISTS：白名单已经存在<br>
     * NO_LEGAL_data：没有合规角色名<br>
     * OUT_OF_LIMIT：超出数量限制<br>
     */
    SUCCESS(Config.getSuccess()),
    PLAYER_NOT_FOUND(Config.getPlayerNotFound()),
    WHITELIST_ALREADY_EXISTS(Config.getWhitelistAlreadyExists()),
    NO_LEGAL_NAME(Config.getNoLegalName()),
    OUT_OF_LIMIT(Config.getOutOfLimit());
    private final String message;

    Status(String message) {
      this.message = message;
    }

    public String getMsg() {
      return message;
    }
  }
}
