package me.yic.xconomy.leaderboard;

import me.yic.xconomy.data.SQL;
import me.yic.xconomy.utils.KeyValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

/**
 * 作者 huanmeng_qwq<br>
 * 2020/11/6<br>
 * LeaderBoard
 */
public class SQLUtils {

    public static String toLoc(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    public static Location makeLoc(String s) {
        String[] s1 = s.split(",");
        return new Location(Bukkit.getWorld(s1[0]), Double.parseDouble(s1[1]), Double.parseDouble(s1[2]), Double.parseDouble(s1[3]));
    }
    public static void checkUUIDName(UUID uuid, String name){
        if(!SQL.database.isValueExists("cache",new KeyValue("uuid","uuid"),new KeyValue("uuid",uuid.toString()))){
            SQL.database.dbInsert("cache",new KeyValue("uuid",uuid.toString()).add("name",name));
        }
    }
}
