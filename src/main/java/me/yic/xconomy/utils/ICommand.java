package me.yic.xconomy.utils;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
public abstract class ICommand extends Command {

    private final Class SUB_CLASS;

    @Setter
    private String onlyConsole, onlyPlayer, noSubCommand, noPermission;

    public ICommand(Class clz, String name, String... aliases) {
        super(name, "bconomy", "/<command>", Arrays.asList(aliases));
        SUB_CLASS = clz;
        onlyConsole = "&conly console use.";
        onlyPlayer = "&conly player use.";
        noSubCommand = "&cunknown subcommand";
        noPermission = "&cyou don't have permissions.";
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length >= 1) {
            Method method = getMethodByCommand(args[0]);
            if (method != null) {
                if (check(sender, method.getAnnotation(Cmd.class))) {
                    try {
                        method.invoke(this, sender, label, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noSubCommand));
            }
        } else {
            sendHelpMsg(sender);
        }
        return true;
    }

    /**
     * 检查权限、玩家、控制台
     *
     * @param sender
     * @param cmd
     * @return
     */
    private boolean check(CommandSender sender, Cmd cmd) {
        if (!sender.hasPermission(cmd.permission())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
            return false;
        } else if (cmd.cmdSender() == Cmd.CmdSender.PLAYER && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyPlayer));
            return false;
        } else if (cmd.cmdSender() == Cmd.CmdSender.CONSOLE && sender instanceof Player) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyConsole));
            return false;
        }
        return true;
    }

    /**
     * 获取处理子命令的method
     *
     * @param subCmd 请求的子命令
     * @return Method 处理请求命令的方法
     */
    private Method getMethodByCommand(String subCmd) {
        Method[] methods = SUB_CLASS.getMethods();
        for (Method method : methods) {
            Cmd cmd = method.getAnnotation(Cmd.class);
            if (cmd != null)
                if (cmd.IgnoreCase() ? subCmd.equals(cmd.value()) : subCmd.equalsIgnoreCase(cmd.value())) {
                    Parameter[] parameter = method.getParameters();
                    if (parameter.length == 3 && parameter[0].getType() == CommandSender.class && parameter[1].getType() == String.class && parameter[2].getType() == String[].class)
                        return method;
                    else
                        Bukkit.getLogger().warning("found a Illegal sub command method in command " + getName() + " called: " + method.getName() + " in class: " + SUB_CLASS);
                }
        }
        return null;
    }

    /**
     * 发送帮助信息
     *
     * @return void   无返回值
     * @Param sender 被发送的对象
     */
    public abstract void sendHelpMsg(CommandSender sender);

    protected void sendMessage(CommandSender sender,String... message){
        for (String s : message) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',s));
        }
    }
    public static final List<ICommand> commands=new ArrayList<>();
    public boolean Register() {
        try {
            Field commandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMap.setAccessible(true);
            CommandMap map = (CommandMap) commandMap.get(Bukkit.getServer());
            map.register("bconomy", this);
            return true;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("Error while register Command: " + this + " due to\n");
            Bukkit.getLogger().warning(e.getLocalizedMessage());
        }finally {
            commands.add(this);
        }
        return false;
    }
    public void register(){
        Register();
    }

    @SneakyThrows
    public void unregister() {
        Field commandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMap.setAccessible(true);
        CommandMap map = (CommandMap) commandMap.get(Bukkit.getServer());
        unregister(map);
    }

    protected String buildString(Object... objs) {
        StringBuilder sb = new StringBuilder();
        for (Object tmp : objs) {
            sb.append(tmp);
        }
        return sb.toString();
    }
}
