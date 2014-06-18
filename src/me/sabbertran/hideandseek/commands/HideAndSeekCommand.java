package me.sabbertran.hideandseek.commands;

import me.sabbertran.hideandseek.Arena;
import me.sabbertran.hideandseek.HideAndSeek;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideAndSeekCommand implements CommandExecutor
{
    
    public HideAndSeek main;
    
    public HideAndSeekCommand(HideAndSeek has)
    {
        this.main = has;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (args[0].equalsIgnoreCase("leave"))
        {
            if (sender instanceof Player)
            {
                Player p = (Player) sender;
                if (p.hasPermission("hideandseek.user.arena.leave"))
                {
                    for (Arena a : main.getArenas().values())
                    {
                        if (a.getPlayers().containsKey(a))
                        {
                            a.leave(p);
                            return true;
                        }
                    }
//                p.sendMessage("You are currently in no arena.");
                    p.sendMessage(main.getMessages().get(28));
                    return true;
                } else
                {
                    p.sendMessage(main.getMessages().get(41));
                    return true;
                }
            } else
            {
//                sender.sendMessage("You have to be a player to use this command.");
                sender.sendMessage(main.getMessages().get(29));
                return true;
            }
        } else
        {
//            sender.sendMessage("Unknown command.");
            sender.sendMessage(main.getMessages().get(30));
            return true;
        }
    }
}
