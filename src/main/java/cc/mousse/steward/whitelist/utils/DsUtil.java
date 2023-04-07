package cc.mousse.steward.whitelist.utils;

import cc.mousse.steward.whitelist.common.Common;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.*;
import java.util.*;

/**
 * @author PhineasZ
 */
public class DsUtil {
  private DsUtil() {}

  public static void init(
      DruidDataSource dataSource,
      String driverClassName,
      String url,
      String username,
      String password)
      throws SQLException {
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.init();
  }

  public static Set<String> getOneFieldSet(
      DruidDataSource dataSource, String sql, String... args) {
    var log = Common.getLog();
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    var result = new HashSet<String>();
    try (var connection = getConnection(dataSource)) {
      preparedStatement = connection.prepareStatement(sql);
      var i = 1;
      for (var arg : args) {
        preparedStatement.setString(i++, arg);
      }
      resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        result.add(resultSet.getString(1));
      }
    } catch (Exception e) {
      log.severe(e.getMessage());
    } finally {
      closeConnection(resultSet, preparedStatement);
    }
    return result;
  }

  public static void updateOne(DruidDataSource dataSource, String sql, String... args) {
    var log = Common.getLog();
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    try (var connection = getConnection(dataSource)) {
      preparedStatement = connection.prepareStatement(sql);
      var i = 1;
      for (var arg : args) {
        preparedStatement.setString(i++, arg);
      }
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      log.warning(e.getMessage());
    } finally {
      closeConnection(resultSet, preparedStatement);
    }
  }

  private static DruidPooledConnection getConnection(DruidDataSource dataSource)
      throws SQLException {
    return dataSource.getConnection();
  }

  private static void closeConnection(ResultSet resultSet, PreparedStatement preparedStatement) {
    var log = Common.getLog();
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        log.severe(e.getMessage());
      }
    }
    if (preparedStatement != null) {
      try {
        preparedStatement.close();
      } catch (SQLException e) {
        log.severe(e.getMessage());
      }
    }
  }
}
