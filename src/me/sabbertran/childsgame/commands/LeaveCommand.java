package me.sabbertran.childsgame.commands;

import me.sabbertran.childsgame.Arena;
import me.sabbertran.childsgame.ChildsGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor
{

    private ChildsGame main;

    public LeaveCommand(ChildsGame has)
    {
        this.main = has;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (sender instanceof Player)
        {
            Player p = (Player) sender;
            for (Arena a : main.getArenas().values())
            {
                if (a.getPlayers().containsKey(p.getUniqueId().toString()))
                {
                    a.leave(p);
                    return true;
                }
            }
            p.sendMessage(main.getMessages().get(28));
            return true;
        } else
        {
            sender.sendMessage(main.getMessages().get(29));
            return true;
        }
    }
}
