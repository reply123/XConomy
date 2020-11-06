package me.yic.xconomy.utils;

import com.zaxxer.hikari.HikariDataSource;
import me.yic.xconomy.XConomy;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DatabaseConnection {
    private String driver = "com.mysql.jdbc.Driver";
    //============================================================================================
    private static final File dataFolder = new File(XConomy.getInstance().getDataFolder(), "playerdata");
    private static String url = "jdbc:mysql://" + XConomy.config.getString("MySQL.host") + "/"
            + XConomy.config.getString("MySQL.database") + "?characterEncoding="
            + XConomy.config.getString("MySQL.encoding") + "&useSSL=false";
    private static final String username = XConomy.config.getString("MySQL.user");
    private static final String password = XConomy.config.getString("MySQL.pass");
    private static final Integer maxPoolSize = XConomy.config.getInt("Pool-Settings.maximum-pool-size");
    private static final Integer minIdle = XConomy.config.getInt("Pool-Settings.minimum-idle");
    private static final Integer maxLife = XConomy.config.getInt("Pool-Settings.maximum-lifetime");
    private static final Long idleTime = XConomy.config.getLong("Pool-Settings.idle-timeout");
    private static boolean secon = false;
    public static Integer waittimeout = 10;
    //============================================================================================
    public static File userdata = new File(dataFolder, "data.db");
    //============================================================================================
    private Connection connection = null;
    private HikariDataSource hikari = null;
    private boolean isfirstry = true;

    private void createNewHikariConfiguration() {
        hikari = new HikariDataSource();
        hikari.setPoolName("XConomy");
        hikari.setJdbcUrl(url);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(maxPoolSize);
        hikari.setMinimumIdle(minIdle);
        hikari.setMaxLifetime(maxLife);
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.addDataSourceProperty("userServerPrepStmts", "true");
        if (XConomy.ddrivers) {
            hikari.setDriverClassName(driver);
        }
        if (hikari.getMinimumIdle() < hikari.getMaximumPoolSize()) {
            hikari.setIdleTimeout(idleTime);
        } else {
            hikari.setIdleTimeout(0);
        }
    }

    private void setDriver() {
        if (XConomy.ddrivers) {
            if (XConomy.config.getBoolean("Settings.mysql")) {
                driver = ("me.yic.libs.mysql.cj.jdbc.Driver");
            } else {
                driver = ("me.yic.libs.sqlite.JDBC");
            }
        } else {
            if (XConomy.config.getBoolean("Settings.mysql")) {
                driver = ("com.mysql.jdbc.Driver");
            } else {
                driver = ("org.sqlite.JDBC");
            }
        }
    }

    private void setTimezone() {
        if (!XConomy.config.getString("MySQL.timezone").equals("")) {
            url = url + "&serverTimezone=" + XConomy.config.getString("MySQL.timezone");
        }
    }

    public boolean setGlobalConnection() {
        setTimezone();
        setDriver();
        try {
            if (XConomy.allowHikariConnectionPooling()) {
                createNewHikariConfiguration();
                Connection connection = getConnection();
                closeHikariConnection(connection);
            } else {
                Class.forName(driver);
                if (XConomy.config.getBoolean("Settings.mysql")) {
                    connection = DriverManager.getConnection(url, username, password);
                } else {
                    connection = DriverManager.getConnection("jdbc:sqlite:" + userdata.toString());
                }
            }

            if (XConomy.config.getBoolean("Settings.mysql")) {
                if (secon) {
                    XConomy.getInstance().logger("MySQL重新连接成功");
                } else {
                    secon = true;
                }
            }
            return true;

        } catch (SQLException e) {
            XConomy.getInstance().logger("无法连接到数据库-----");
            e.printStackTrace();
            close();
            return false;

        } catch (ClassNotFoundException e) {
            XConomy.getInstance().logger("JDBC驱动加载失败");
        }

        return false;
    }

    public Connection getConnectionAndCheck() {
        if (!canConnect()) {
            return null;
        }
        try {
            return getConnection();
        } catch (SQLException e1) {
            if (isfirstry) {
                isfirstry = false;
                close();
                return getConnectionAndCheck();
            } else {
                isfirstry = true;
                XConomy.getInstance().logger("无法连接到数据库-----");
                close();
                e1.printStackTrace();
                return null;
            }
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return getConnection().createStatement().executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return getConnection().createStatement().executeUpdate(sql);
    }

    public Connection getConnection() throws SQLException {
        if (XConomy.allowHikariConnectionPooling()) {
            return hikari.getConnection();
        } else {
            return connection;
        }
    }

    public boolean canConnect() {
        try {
            if (XConomy.allowHikariConnectionPooling()) {
                if (hikari == null) {
                    return setGlobalConnection();
                }

                if (hikari.isClosed()) {
                    return setGlobalConnection();
                }

            } else {
                if (connection == null) {
                    return setGlobalConnection();
                }

                if (connection.isClosed()) {
                    return setGlobalConnection();
                }

                if (XConomy.config.getBoolean("Settings.mysql")) {
                    if (!connection.isValid(waittimeout)) {
                        secon = false;
                        return setGlobalConnection();
                    }
                }
            }
        } catch (SQLException e) {
            Arrays.stream(e.getStackTrace()).forEach(d -> Bukkit.getLogger().info(d.toString()));
            return false;
        }
        return true;
    }

    public void closeHikariConnection(Connection connection) {
        if (!XConomy.allowHikariConnectionPooling()) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createTables(String tableName, KeyValue fields, String conditions)
            throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` ( "
                + fields.toCreateString()
                + (conditions == null ? ""
                : new StringBuilder(" , ").append(conditions).toString())
                + " ) ENGINE = InnoDB DEFAULT CHARSET=utf8;";
        return execute(sql);
    }

    public boolean execute(String sql) throws SQLException {
        return getConnection().createStatement().execute(sql);
    }

    public int dbDelete(String tableName, KeyValue fields) {
        String sql = "DELETE FROM `" + tableName + "` WHERE " + fields.toWhereString();
        try {
            return getConnection().createStatement().executeUpdate(sql);
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return 0;
    }

    public boolean dbExist(String tableName, KeyValue fields) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + fields.toWhereString();
        try {
            return getConnection().createStatement().executeQuery(sql).next();
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return false;
    }

    public int dbInsert(String tabName, KeyValue fields) {
        String sql = "INSERT INTO `" + tabName + "` " + fields.toInsertString();
        try {
            return getConnection().createStatement().executeUpdate(sql);
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return 0;
    }

    public List<KeyValue> dbSelect(String tableName, KeyValue fields, KeyValue selCondition) {
        String sql = "SELECT " + fields.toKeys() + " FROM `" + tableName + "`"
                + (selCondition == null ? ""
                : new StringBuilder().append(" WHERE ")
                .append(selCondition.toWhereString()).toString());
        List<KeyValue> kvlist = new ArrayList<>();
        try {
            ResultSet dbresult = getConnection().createStatement().executeQuery(sql);
            while (dbresult.next()) {
                KeyValue kv = new KeyValue();
                for (String col : fields.getKeys()) {
                    kv.add(col, dbresult.getString(col));
                }
                kvlist.add(kv);
            }
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return kvlist;
    }

    public String dbSelectFirst(String tableName, String fields, KeyValue selConditions) {
        String sql = "SELECT " + fields + " FROM " + tableName + " WHERE "
                + selConditions.toWhereString() + " LIMIT 1";
        try {
            ResultSet dbresult = getConnection().createStatement().executeQuery(sql);
            if (dbresult.next()) {
                return dbresult.getString(fields);
            }
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return null;
    }

    public int dbUpdate(String tabName, KeyValue fields, KeyValue upCondition) {
        String sql = "UPDATE `" + tabName + "` SET " + fields.toUpdateString() + " WHERE "
                + upCondition.toWhereString();
        try {
            return getConnection().createStatement().executeUpdate(sql);
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return 0;
    }


    public boolean isValueExists(String tableName, KeyValue fields, KeyValue selCondition) {
        String sql = "SELECT " + fields.toKeys() + " FROM `" + tableName + "`"
                + (selCondition == null ? ""
                : new StringBuilder().append(" WHERE ")
                .append(selCondition.toWhereString()).toString());
        try {
            ResultSet dbresult = getConnection().createStatement().executeQuery(sql);
            return dbresult.next();
        } catch (Exception e) {
            sqlerr(sql, e);
        }
        return false;
    }

    public boolean isFieldExists(String tableName, KeyValue fields) {
        try {
            DatabaseMetaData dbm = getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                ResultSet f = dbm.getColumns(null, null, tableName, fields.getKeys()[0]);
                return f.next();
            }
        } catch (SQLException e) {
            sqlerr("判断 表名:" + tableName + " 字段名:" + fields.getKeys()[0] + " 是否存在时出错!", e);
        }
        return false;
    }

    public boolean isTableExists(String tableName) {
        try {
            DatabaseMetaData dbm = getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            sqlerr("判断 表名:" + tableName + " 是否存在时出错!", e);
        }
        return false;
    }

    public void sqlerr(String sql, Exception e) {
        System.out.println("数据库操作出错: " + e.getMessage());
        System.out.println("SQL查询语句: " + sql);
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (hikari != null) {
                hikari.close();
            }
        } catch (SQLException e) {
            if (XConomy.config.getBoolean("Settings.mysql")) {
                XConomy.getInstance().logger("MySQL连接断开失败");
            }
            e.printStackTrace();
        }
    }
}
