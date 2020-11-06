package me.yic.xconomy.leaderboard;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import lombok.Getter;
import me.yic.xconomy.XConomy;
import me.yic.xconomy.data.SQL;
import me.yic.xconomy.utils.DatabaseConnection;
import me.yic.xconomy.utils.KeyValue;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.util.*;

/**
 * 作者 huanmeng_qwq<br>
 * 2020/11/6<br>
 * LeaderBoard
 */
public class LeaderBoardImpl {
    static UUIDAPI uuidapi = uuid -> SQL.database.dbSelectFirst("cache", "name", new KeyValue("uuid", uuid.toString()));
    Location leaderboard;
    static List<TimeType> timeTypes = TimeType.getValues();
    static List<DataType> modes = DataType.getValues();
    static DatabaseConnection dataBaseCore = SQL.database;
    TimeType curtype = TimeType.ALL;
    DataType curtDataType = DataType.MONEY;
    @Getter
    private Player player;
    Hologram hd;

    public LeaderBoardImpl(Player player) {
        this.player = player;
        leaderboard = SQLUtils.makeLoc(Objects.requireNonNull(XConomy.getInstance().getConfig().get("leaderboard.location")).toString());
    }

    public void init() {
        hd = HologramsAPI.createHologram(XConomy.getInstance(), leaderboard);
        hd.getVisibilityManager().setVisibleByDefault(false);
        hd.getVisibilityManager().showTo(player);


        handler(hd.appendTextLine("§b§l" + curtype.getName() + curtDataType.getName()), curtDataType);
        for (int i = 1; i <= 10; i++) {
            handler(hd.appendTextLine("无"), curtDataType);
        }
        handler(hd.appendTextLine("§6§l点击刷新`"), curtDataType);
        handler(hd.appendTextLine(getlb()), curtDataType);
        update(false);
    }

    public void handler(TextLine textLine, DataType type) {
        if (type == DataType.MONEY) {
            textLine.setTouchHandler(money);
        }
    }

    public void remove() {
        hd.delete();
    }

    public void update(boolean tip) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = null;
                ResultSet rs = null;
                try {
                    int line;
                    sql = "SELECT * FROM "+SQL.tableName+" ORDER BY `" + (curtype.getSql() == null ? curtDataType.getSql() : curtype.getSql() + "_" + curtDataType.getSql())
                            + "` DESC LIMIT 10";
                    rs = dataBaseCore.executeQuery(sql);
                    line = 1;
                    int rankaa = 1;
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String name = uuidapi.getName(uuid);
                        int i = rs.getInt(getField(curtype, curtDataType));
                        TextLine textLine = (TextLine) hd.getLine(line);
                        if (name != null) {
                            textLine.setText("§a" + (rankaa++) + ". " + "§7" + name + " §7- §e" + MathUtils.Format(i));
                            line++;
                        }
                    }
                    for (; line <= 10; line++) {
                        ((TextLine) hd.getLine(line)).setText("无");
                    }
                    ((TextLine) hd.getLine(0)).setText("§b§l" + curtype.getName() + curtDataType.getName());
                    for (int i = 0; i <= 12; i++) {
                        handler(((TextLine) hd.getLine(i)), curtDataType);
                    }
                    if (tip) player.sendMessage("§a你已切换到§b§l" + curtype.getName() + "§a排行榜");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println(sql);
                }
            }
        }.runTaskAsynchronously(XConomy.getInstance());
    }

    public String getField(TimeType timeType, DataType dataType) {
        if (timeType == TimeType.ALL) {
            return dataType.getSql();
        } else {
            return timeType.getSql() + "_" + dataType.getSql();
        }
    }

    public enum TimeType {
        ALL("生涯", null)//, DAY("每日", "daily"), WEEK("每周", "week"),
        ;
        @Getter
        String name, sql;

        TimeType(String name, String sql) {
            this.name = name;
            this.sql = sql;
        }

        public static List<TimeType> getValues() {
            return Arrays.asList(values().clone());
        }
    }

    public enum DataType {
        MONEY("金币", "balance");
        @Getter
        String name, sql;

        DataType(String name, String sql) {
            this.name = name;
            this.sql = sql;
            this.sql = sql;
        }

        public static List<DataType> getValues() {
            return Arrays.asList(values().clone());
        }
    }

    public DataType getModenext(int i) {
        if (modes.indexOf(curtDataType) + i > modes.size() - 1) {
            return modes.get(modes.indexOf(curtDataType) + i - modes.size());
        } else {
            return modes.get(modes.indexOf(curtDataType) + i);
        }
    }

    TouchHandler money = player -> {
        if (timeTypes.indexOf(curtype) >= timeTypes.size() - 1) {
            curtype = timeTypes.get(0);
        } else {
            curtype = timeTypes.get(timeTypes.indexOf(curtype) + 1);
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
        curtDataType = DataType.MONEY;
        update(true);
        ((TextLine) hd.getLine(11)).setText("§6§l点击切换");
        ((TextLine) hd.getLine(12)).setText(getlb());
    };

    public String getlb() {
//        StringBuilder sb = new StringBuilder();
//        TimeType curtt = curtype;
//        Iterator<TimeType> it = timeTypes.iterator();
//        while (it.hasNext()) {
//            TimeType tt = it.next();
//            if (tt == curtt) {
//                sb.append("§a").append(tt.getName());
//            } else {
//                sb.append("§7").append(tt.getName());
//            }
//            if (it.hasNext()) {
//                sb.append(" ");
//            }
//        }
//        return sb.toString();
        return "§a"+curtDataType.getName();
    }

}
