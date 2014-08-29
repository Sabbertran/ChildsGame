package me.sabbertran.childsgame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.sabbertran.childsgame.commands.ArenaCommand;
import me.sabbertran.childsgame.commands.LeaveCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ChildsGame extends JavaPlugin
{

    public static Logger log = Bukkit.getLogger();
    private File messagesFile;

    private ArrayList<String> messages;
    private HashMap<String, Arena> arenas;
    private ArrayList<String> seekerWinCommands, killCommands;

    @Override
    public void onEnable()
    {
        messages = new ArrayList<String>();
        arenas = new HashMap<String, Arena>();
        messagesFile = new File("plugins/ChildsGame/messages.yml");

        getConfig().addDefault("Name", "Child's Game");
        getConfig().addDefault("Arena.toolID", 369);
        getConfig().addDefault("Arena.BlockChooseItemAndInventoryName", ChatColor.AQUA + "Choose Block");
        getConfig().addDefault("Arena.Sign.CreateIdentification", "ChildsGame");
        getConfig().addDefault("Arena.Sign.Name", ChatColor.RED + "Child's Game");
        getConfig().addDefault("Arena.Sign.Waiting", "Waiting...");
        getConfig().addDefault("Arena.Sign.Countdown", "Countdown: %seconds s");
        getConfig().addDefault("Arena.Sign.GameRunning", "Game running");
        getConfig().addDefault("Arena.GameTime", 300);
        getConfig().addDefault("Arena.Countdown", 30);
        getConfig().addDefault("Arena.SeekerCountdown", 20);
        getConfig().addDefault("Arena.SeekerRespawnCountdown", 10);
        getConfig().addDefault("SeekerWinCommands", new String[]
        {
            "give %player 266 10", "msg %player Congratulations, you won ;)"
        });
        getConfig().addDefault("KillCommands", new String[]
        {
            "msg %player You killed %killed"
        });
        getConfig().options().copyDefaults(true);
        saveConfig();

        seekerWinCommands = (ArrayList<String>) getConfig().getStringList("SeekerWinCommands");
        killCommands = (ArrayList<String>) getConfig().getStringList("KillCommands");

        File arenaFolder = new File("plugins/ChildsGame/arenas/");
        if (!arenaFolder.exists())
        {
            arenaFolder.mkdirs();
        }
        for (File f : arenaFolder.listFiles())
        {
            String name = null;
            Location loc1 = null, loc2 = null, spawnHider = null, spawnSeeker = null, spawnWaiting = null, spawnEnd = null;
            Sign sign = null;
            int maxPlayers = 0, startPlayers = 0;
            HashMap<ItemStack, String> bl = null;

            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (!line.startsWith("#"))
                    {
                        String[] split = line.split(": ");
                        if (split.length > 1)
                        {
                            if (split[0].equals("name"))
                            {
                                name = split[1];
                            } else if (split[0].equals("loc1"))
                            {
                                String[] loc = split[1].split(",");
                                loc1 = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                            } else if (split[0].equals("loc2"))
                            {
                                String[] loc = split[1].split(",");
                                loc2 = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                            } else if (split[0].equals("spawnHider"))
                            {
                                String[] loc = split[1].split(",");
                                spawnHider = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                            } else if (split[0].equals("spawnSeeker"))
                            {
                                String[] loc = split[1].split(",");
                                spawnSeeker = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                            } else if (split[0].equals("spawnWaiting"))
                            {
                                String[] loc = split[1].split(",");
                                spawnWaiting = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                            } else if (split[0].equals("spawnEnd"))
                            {
                                String[] loc = split[1].split(",");
                                spawnEnd = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                            } else if (split[0].equals("sign"))
                            {
                                String[] loc = split[1].split(",");
                                Location l = new Location(getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
                                if (l.getBlock().getState() instanceof Sign)
                                {
                                    sign = (Sign) l.getBlock().getState();
                                }
                            } else if (split[0].equals("maxPlayers"))
                            {
                                maxPlayers = Integer.parseInt(split[1]);
                            } else if (split[0].equals("startPlayers"))
                            {
                                startPlayers = Integer.parseInt(split[1]);
                            } else if (split[0].equals("blocks"))
                            {
                                bl = new HashMap<ItemStack, String>();
                                String[] block = split[1].split(";");
                                for (String b : block)
                                {
                                    String[] b_split = b.split(",");
                                    String[] b_split1 = b_split[0].split(":");
                                    ItemStack is;
                                    if (b_split1.length >= 2)
                                    {
                                        is = new ItemStack(Material.getMaterial(Integer.parseInt(b_split1[0])), 1, Short.parseShort(b_split1[1]));
                                    } else
                                    {
                                        is = new ItemStack(Material.getMaterial(Integer.parseInt(b_split[0])));
                                    }
                                    bl.put(is, b_split[1]);

                                }
                            }
                        }
                    }
                }
                reader.close();

                Arena a = new Arena(this, name, loc1, loc2, spawnHider, spawnSeeker, spawnWaiting, spawnEnd, sign, maxPlayers, startPlayers, bl);
                arenas.put(name, a);
            } catch (FileNotFoundException ex)
            {
                Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex)
            {
                Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (messagesFile.exists())
        {
            try
            {
                BufferedReader read = new BufferedReader(new FileReader(messagesFile));
                String line;
                while ((line = read.readLine()) != null)
                {
                    if (!line.startsWith("#"))
                    {
                        messages.add(line);
                    }
                }
                read.close();
            } catch (FileNotFoundException ex)
            {
                Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex)
            {
                Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
        {
            setupMessages();
        }
        if (messages.size() != 47)
        {
            setupMessages();

        }
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        //Alternative commands to work around conflicts
        getCommand("cgarena").setExecutor(new ArenaCommand(this));
        getCommand("cgleave").setExecutor(new LeaveCommand(this));
        getServer().getPluginManager().registerEvents(new Events(this), this);

        log.info("Child's game enabled.");
    }

    @Override
    public void onDisable()
    {
        File folder = new File("plugins/ChildsGame/arenas");
        for (File content : folder.listFiles())
        {
            content.delete();
        }

        for (Arena a : arenas.values())
        {
            File f = new File("plugins/ChildsGame/arenas/" + a.getName() + ".yml");
            try
            {
                f.createNewFile();
                PrintWriter pw = new PrintWriter(new FileOutputStream(f));

                pw.print("name: ");
                if (a.getName() != null)
                {
                    pw.println(a.getName());
                }
                pw.print("loc1: ");
                if (a.getLoc1() != null)
                {
                    pw.println(a.getLoc1().getWorld().getName() + "," + a.getLoc1().getBlockX() + "," + a.getLoc1().getBlockY() + "," + a.getLoc1().getBlockZ());
                }
                pw.print("loc2: ");
                if (a.getLoc2() != null)
                {
                    pw.println(a.getLoc2().getWorld().getName() + "," + a.getLoc2().getBlockX() + "," + a.getLoc2().getBlockY() + "," + a.getLoc2().getBlockZ());
                }
                pw.print("spawnHider: ");
                if (a.getSpawnHider() != null)
                {
                    pw.println(a.getSpawnHider().getWorld().getName() + "," + a.getSpawnHider().getBlockX() + "," + a.getSpawnHider().getBlockY() + "," + a.getSpawnHider().getBlockZ());
                }
                pw.print("spawnSeeker: ");
                if (a.getSpawnSeeker() != null)
                {
                    pw.println(a.getSpawnSeeker().getWorld().getName() + "," + a.getSpawnSeeker().getBlockX() + "," + a.getSpawnSeeker().getBlockY() + "," + a.getSpawnSeeker().getBlockZ());
                }
                pw.print("spawnWaiting: ");
                if (a.getSpawnWaiting() != null)
                {
                    pw.println(a.getSpawnWaiting().getWorld().getName() + "," + a.getSpawnWaiting().getBlockX() + "," + a.getSpawnWaiting().getBlockY() + "," + a.getSpawnWaiting().getBlockZ());
                }
                pw.print("spawnEnd: ");
                if (a.getSpawnEnd() != null)
                {
                    pw.println(a.getSpawnEnd().getWorld().getName() + "," + a.getSpawnEnd().getBlockX() + "," + a.getSpawnEnd().getBlockY() + "," + a.getSpawnEnd().getBlockZ());
                }
                pw.print("sign: ");
                if (a.getSign() != null)
                {
                    pw.println(a.getSign().getWorld().getName() + "," + a.getSign().getX() + "," + a.getSign().getY() + "," + a.getSign().getZ());
                }
                pw.print("maxPlayers: ");
                if (a.getMaxPlayers() != 0)
                {
                    pw.println(a.getMaxPlayers());
                }
                pw.print("startPlayers: ");
                if (a.getStartPlayers() != 0)
                {
                    pw.println(a.getStartPlayers());
                }
                pw.print("blocks: ");
                if (a.getBlocks() != null)
                {
                    String blocks = "";
                    for (Map.Entry<ItemStack, String> entry : a.getBlocks().entrySet())
                    {
                        String b;
                        if (entry.getKey().getDurability() != (short) 0)
                        {
                            b = entry.getKey().getTypeId() + ":" + entry.getKey().getDurability() + "," + entry.getValue();
                        } else
                        {
                            b = entry.getKey().getTypeId() + "," + entry.getValue();
                        }
                        blocks = blocks + b + ";";
                    }
                    if (blocks.length() >= 1)
                    {
                        blocks = blocks.substring(0, blocks.length() - 1);
                    }
                    pw.println(blocks);
                }
                pw.close();
            } catch (FileNotFoundException ex)
            {
                Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex)
            {
                Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        log.info("Child's game disabled.");
    }

    public void setupMessages()
    {
        messages.clear();

        //Arena class (12)
        messages.add("You joined the arena %name.");
        messages.add("The game starts in %secondsLeft seconds.");
        messages.add("The game starts now! (You are a %block)");
        messages.add("The seeker will come to find you in %secondsLeft seconds.");
        messages.add("You are a seeker.");
        messages.add("You are now able to move.");
        messages.add("You will be able to move in %secondsLeft seconds.");
        messages.add("The seeker is coming to find you.");
        messages.add("You left the arena %name.");
        messages.add("You are now a solid %block.");
        messages.add("You are no longer a solid %block.");
        messages.add("The seekers won.");

        //Events class (16)
        messages.add("The arena %name does not exist.");
        messages.add("There are too many possible blocks for this arena. Please contact an administrator.");
        messages.add("Successfully set the first corner for arena %name.");
        messages.add("Successfully set the second corner for arena %name.");
        messages.add("Successfully set the lobby spawn for arena %name.");
        messages.add("Successfully set the hider spawn for arena %name.");
        messages.add("Successfully set the seeker spawn for arena %name.");
        messages.add("Successfully set the after match spawn for arena %name.");
        messages.add("You will be a %block.");
        messages.add("The arena is not completely set up yet.");
        messages.add("You can't join the game you are already in.");
        messages.add("The game is full.");
        messages.add("The game has already started.");
        messages.add("Hider %hider (%block) was slain by %seeker.");
        messages.add("Seeker %seeker was slain by %hider (%block).");
        messages.add("You cannot go solid here.");

        //Command classes (12)
        messages.add("You are currently in no arena.");
        messages.add("You have to be a player to use this command.");
        messages.add("Unknown command.");
        messages.add("Successfully created arena %name.");
        messages.add("There need to be at least two players to start a game.");
        messages.add("Use /arena create 'name' 'max. players' 'players to start' to create a new arena.");
        messages.add("An arena with the name %name already exists.");
        messages.add("Use this item to modify the arena.");
        messages.add("Use /arena tool 'name' to get the arena tool for the specified arena.");
        messages.add("Use /arena blocks 'arena' 'ID1:Name,ID2:Name,ID3:Name,...' to set the blocks for an arena.");
        messages.add("Successfully set the blocks for arena %name.");
        messages.add("Currently there are %amount arenas:");

        //Added later
        messages.add("You don't have permission to join an arena.");
        messages.add("You don't have permission to use this command.");
        messages.add("The sign for this arena already exists.");
        messages.add("Use /arena delete 'name' to delete an arena.");
        messages.add("There have to be no players in the arena.");
        messages.add("Successfully deleted the arena %name");
        messages.add("The hiders won.");

        //Currently 47 messages
        try
        {
            messagesFile.delete();
            messagesFile.createNewFile();
            PrintWriter pw = new PrintWriter(new FileOutputStream(messagesFile));
            pw.println("# Do not change the order of the messages, otherwise they will be messed up ingame! #");
            pw.println("#####################################################################################");
            for (String m : messages)
            {
                pw.println(m);
            }
            pw.close();
        } catch (IOException ex)
        {
            Logger.getLogger(ChildsGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> getMessages()
    {
        return messages;
    }

    public HashMap<String, Arena> getArenas()
    {
        return arenas;
    }

    public ArrayList<String> getSeekerWinCommands()
    {
        return seekerWinCommands;
    }

    public ArrayList<String> getKillCommands()
    {
        return killCommands;
    }

}
