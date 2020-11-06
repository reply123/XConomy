package me.yic.xconomy.leaderboard;

import me.yic.xconomy.XConomy;
import me.yic.xconomy.utils.Cmd;
import me.yic.xconomy.utils.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 作者 huanmeng_qwq<br>
 * 2020/11/6<br>
 * LeaderBoard
 */
public class LeaderBoardCommand extends ICommand {
    public LeaderBoardCommand() {
        super(LeaderBoardCommand.class, "lb", "blb","leaderboard");
    }

    @Override
    public void sendHelpMsg(CommandSender sender) {
        sendMessage(sender,"&a/lb set - Set LeaderBoard Location");
    }

    @Cmd(value = "set",permission = "bconomy.leaderboard.set",cmdSender = Cmd.CmdSender.PLAYER)
    public void set(CommandSender sender,String label,String[] args){
        XConomy.getInstance().getConfig().set("leaderboard.location", SQLUtils.toLoc(((Player) sender).getLocation()));
        if (XConomy.getInstance().isLeaderBoard()) {
            for (LeaderBoardImpl value : XConomy.getInstance().getHd().values()) {
                value.hd.teleport(((Player) sender).getLocation());
            }
        }
        sendMessage(sender, "&a设置成功: " + SQLUtils.toLoc(((Player) sender).getLocation()));
        XConomy.getInstance().saveConfig();
    }
}
