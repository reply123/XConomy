package me.yic.xconomy.leaderboard;

import me.yic.xconomy.XConomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 作者 huanmeng_qwq<br>
 * 2020/11/6<br>
 * LeaderBoard
 */
public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!XConomy.getInstance().isLeaderBoard()) {
            return;
        }
        LeaderBoardImpl hd = new LeaderBoardImpl(e.getPlayer());
        new BukkitRunnable() {
            @Override
            public void run() {
                SQLUtils.checkUUIDName(e.getPlayer().getUniqueId(), e.getPlayer().getName());
            }
        }.runTaskAsynchronously(XConomy.getInstance());
        hd.init();
        XConomy.getInstance().getHd().put(e.getPlayer(), hd);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!XConomy.getInstance().isLeaderBoard()) {
            return;
        }
        XConomy.getInstance().getHd().get(e.getPlayer()).remove();
        XConomy.getInstance().getHd().remove(e.getPlayer());
    }
}
