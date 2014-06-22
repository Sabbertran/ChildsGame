package me.sabbertran.childsgame.commands;

import me.sabbertran.childsgame.Arena;
import me.sabbertran.childsgame.ChildsGame;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaCommand implements CommandExecutor
{

    private ChildsGame main;

    public ArenaCommand(ChildsGame has)
    {
        this.main = has;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (args.length > 0)
        {
            if (args[0].equals("create"))
            {
                if (sender.hasPermission("childsgame.admin.arena.create"))
                {
                    if (args.length == 4)
                    {
                        if (!main.getArenas().containsKey(args[1]))
                        {
                            try
                            {
                                if (!(Integer.parseInt(args[3]) < 2))
                                {
                                    main.getArenas().put(args[1], new Arena(main, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])));
//                                sender.sendMessage("Successfully created arena " + args[1] + ".");
                                    sender.sendMessage(main.getMessages().get(31).replace("%name", args[1]));
                                    return true;
                                } else
                                {
//                                sender.sendMessage("There need to be at least two players to start a game.");
                                    sender.sendMessage(main.getMessages().get(32));
                                    return true;
                                }
                            } catch (NumberFormatException ex)
                            {
//                            sender.sendMessage("Use /arena create 'name' 'max. players' 'players to start' to create a new arena.");
                                sender.sendMessage(main.getMessages().get(33));
                                return true;
                            }
                        } else
                        {
//                        sender.sendMessage("An arena with the name " + args[1] + " already exists.");
                            sender.sendMessage(main.getMessages().get(34).replace("%name", args[1]));
                            return true;
                        }
                    } else
                    {
//                    sender.sendMessage("Use /arena create 'name' 'max. players' 'players to start' to create a new arena.");
                        sender.sendMessage(main.getMessages().get(33));
                        return true;
                    }
                } else
                {
                    sender.sendMessage(main.getMessages().get(41));
                    return true;
                }
            } else if (args[0].equals("tool"))
            {
                if (sender instanceof Player)
                {
                    Player p = (Player) sender;
                    if (p.hasPermission("childsgame.admin.arena.tool"))
                    {
                        if (args.length == 2)
                        {
                            if (main.getArenas().containsKey(args[1]))
                            {
                                ItemStack item = new ItemStack(Material.BLAZE_ROD);
                                ItemMeta meta = item.getItemMeta();
                                meta.setDisplayName("§6Arena tool - " + args[1]);
                                item.setItemMeta(meta);
                                p.getInventory().addItem(item);
//                            p.sendMessage("Use this item to modify the arena.");
                                p.sendMessage(main.getMessages().get(35));
                                return true;
                            } else
                            {
//                            p.sendMessage("The arena " + args[1] + " does not exist.");
                                sender.sendMessage(main.getMessages().get(12).replace("%name", args[1]));
                                return true;
                            }
                        } else
                        {
//                        p.sendMessage("Use /arena tool 'name' to get the arena tool for the specified arena.");
                            p.sendMessage(main.getMessages().get(36));
                            return true;
                        }
                    } else
                    {
                        p.sendMessage(main.getMessages().get(41));
                        return true;
                    }
                } else
                {
//                    sender.sendMessage("You have to be a player to use this command.");
                    sender.sendMessage(main.getMessages().get(29));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("blocks"))
            {
                if (sender.hasPermission("childsgame.admin.arena.blocks"))
                {
                    if (args.length == 3)
                    {
                        if (main.getArenas().containsKey(args[1]))
                        {
                            try
                            {
                                String[] ids = args[2].split(",");
                                if (main.getArenas().get(args[1]).getBlocks() != null)
                                {
                                    main.getArenas().get(args[1]).getBlocks().clear();
                                }
                                for (String id : ids)
                                {
                                    String[] block = id.split(":");
                                    if (block.length == 2)
                                    {
                                        main.getArenas().get(args[1]).getBlocks().put(Integer.parseInt(block[0]), block[1].replace("_", " "));
                                    } else
                                    {
//                                    sender.sendMessage("Use /arena blocks 'arena' 'ID1:Name,ID2:Name,ID3:Name,...' to set the blocks for an arena.");
                                        sender.sendMessage(main.getMessages().get(37));
                                        return true;
                                    }
                                }
//                            sender.sendMessage("Successfully set the blocks for arena " + args[1] + ".");
                                sender.sendMessage(main.getMessages().get(38).replace("%name", args[1]));
                                return true;
                            } catch (NumberFormatException ex)
                            {
//                            sender.sendMessage("Use /arena blocks 'arena' 'ID1:Name,ID2:Name,ID3:Name,...' to set the blocks for an arena.");
                                sender.sendMessage(main.getMessages().get(37));
                                return true;
                            }
                        } else
                        {
//                            sender.sendMessage("The arena " + args[1] + " does not exist.");
                            sender.sendMessage(main.getMessages().get(12).replace("%name", args[1]));
                            return true;
                        }
                    } else
                    {
//                    sender.sendMessage("Use /arena blocks 'arena' 'ID1:Name,ID2:Name,ID3:Name,...' to set the blocks for an arena.");
                        sender.sendMessage(main.getMessages().get(37));
                        return true;
                    }
                } else
                {
                    sender.sendMessage(main.getMessages().get(41));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("delete"))
            {
                if (sender.hasPermission("childsgame.admin.arena.delete"))
                {
                    if (args.length == 2)
                    {
                        if (main.getArenas().containsKey(args[1]))
                        {
                            Arena a = main.getArenas().get(args[1]);
                            if (a.getState() == 0 && a.getPlayers().isEmpty())
                            {
                                main.getArenas().remove(a.getName());
                                sender.sendMessage(main.getMessages().get(45).replace("%name", a.getName()));
                                return true;
                            } else
                            {
                                sender.sendMessage(main.getMessages().get(44));
                                return true;
                            }
                        } else
                        {
//                            sender.sendMessage("The arena " + args[1] + " does not exist.");
                            sender.sendMessage(main.getMessages().get(12).replace("%name", args[1]));
                            return true;
                        }
                    } else
                    {
                        sender.sendMessage(main.getMessages().get(43));
                        return true;
                    }
                } else
                {
                    sender.sendMessage(main.getMessages().get(41));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("list"))
            {
                if (sender.hasPermission("childsgame.admin.arena.list"))
                {
//                sender.sendMessage("Currently there are " + main.getArenas().size() + " arenas:");
                    sender.sendMessage(main.getMessages().get(39).replace("%amount", String.valueOf(main.getArenas().size())));
                    String send = "";
                    for (String arena : main.getArenas().keySet())
                    {
                        send = send + arena + ", ";
                    }
                    if (send.length() >= 2)
                    {
                        send = send.substring(0, send.length() - 2);
                    }
                    sender.sendMessage(send);
                    return true;
                } else
                {
                    sender.sendMessage(main.getMessages().get(41));
                    return true;
                }
            } else
            {
//                sender.sendMessage("Unknown command.");
                sender.sendMessage(main.getMessages().get(30));
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
