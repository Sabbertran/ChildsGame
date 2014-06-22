package me.sabbertran.childsgame;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Arena
{

    private ChildsGame main;

    private String name;
    private Location loc1, loc2, spawnHider, spawnSeeker, spawnWaiting, spawnEnd;
    private HashMap<Integer, String> blocks; //BlockID, BlockName
    private Sign sign;
    private int maxPlayers, startPlayers;
    private HashMap<String, Integer> players; //player (hiders & sekeers), BlockID
    private HashMap<String, Boolean> seekers; //seeker, ableToMove
    private HashMap<String, Location> solidBlocks;
    private HashMap<String, Integer> sneakingTasks;
    private int secondsLeft, secondsLeftTask;
    private int sneakingCountdown;
    private HashMap<String, Integer> sneakingCountdowns;

    private int state = 0; //0 = waiting for players; 1 = countdown started; 2 = game running, 3 = game ended

    private int countdownTask, seekerCountdownTask;
    private int countdown, seekerCountdown;
    private int seekerRespawnCountdown;
    private HashMap<String, Integer> seekerRespawnCountdowns;
    private HashMap<String, Integer> seekerRespawnTasks;

    public Arena(ChildsGame has, String name, int maxPlayers, int startPlayers)
    {
        this.main = has;
        this.name = name;
        this.blocks = new HashMap<Integer, String>();
        this.maxPlayers = maxPlayers;
        this.startPlayers = startPlayers;
        this.players = new HashMap<String, Integer>();
        this.seekers = new HashMap<String, Boolean>();
        this.solidBlocks = new HashMap<String, Location>();
        this.sneakingTasks = new HashMap<String, Integer>();
        this.sneakingCountdown = 5;
        this.sneakingCountdowns = new HashMap<String, Integer>();

        this.secondsLeft = main.getConfig().getInt("Arena.GameTime");
        this.countdown = main.getConfig().getInt("Arena.Countdown");
        this.seekerCountdown = main.getConfig().getInt("Arena.SeekerCountdown");
        this.seekerRespawnCountdown = main.getConfig().getInt("Arena.SeekerRespawnCountdown");
        this.seekerRespawnCountdowns = new HashMap<String, Integer>();
        this.seekerRespawnTasks = new HashMap<String, Integer>();
    }

    public Arena(ChildsGame has, String name, Location loc1, Location loc2, Location spawnHider, Location spawnSeeker, Location spawnWaiting, Location spawnEnd, Sign sign, int maxPlayers, int startPlayers, HashMap<Integer, String> bl)
    {
        this.main = has;
        this.name = name;
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.spawnHider = spawnHider;
        this.spawnSeeker = spawnSeeker;
        this.spawnWaiting = spawnWaiting;
        this.spawnEnd = spawnEnd;
        this.sign = sign;
        this.maxPlayers = maxPlayers;
        this.startPlayers = startPlayers;
        this.blocks = bl;

        this.players = new HashMap<String, Integer>();
        this.seekers = new HashMap<String, Boolean>();
        this.solidBlocks = new HashMap<String, Location>();
        this.sneakingTasks = new HashMap<String, Integer>();
        this.sneakingCountdown = 5;
        this.sneakingCountdowns = new HashMap<String, Integer>();

        this.secondsLeft = main.getConfig().getInt("Arena.GameTime");
        this.countdown = main.getConfig().getInt("Arena.Countdown");
        this.seekerCountdown = main.getConfig().getInt("Arena.SeekerCountdown");
        this.seekerRespawnCountdown = main.getConfig().getInt("Arena.SeekerRespawnCountdown");
        this.seekerRespawnCountdowns = new HashMap<String, Integer>();
        this.seekerRespawnTasks = new HashMap<String, Integer>();
    }

    public void join(Player p)
    {
        p.setGameMode(GameMode.SURVIVAL);
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(main.getConfig().getString("Arena.BlockChooseItemAndInventoryName"));
        is.setItemMeta(meta);
        p.getInventory().addItem(is);
        p.updateInventory();

        p.teleport(spawnWaiting);

        players.put(p.getUniqueId().toString(), (Integer) this.blocks.keySet().toArray()[new Random().nextInt(this.blocks.size())]);
//        p.sendMessage("You joined the arena " + name + ".");
        p.sendMessage(main.getMessages().get(0).replace("%name", name));

        updateSign();
        if (state == 0 && players.size() >= startPlayers)
        {
            startCountdown();
        }
    }

    public void startCountdown()
    {
        state = 1;
        countdownTask = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable()
        {
            @Override
            public void run()
            {
                for (String uuid : players.keySet())
                {
                    Player p = main.getServer().getPlayer(UUID.fromString(uuid));
                    p.setExp(0);
                    p.setLevel(countdown);

                    if (countdown == 0)
                    {
                        p.setExp(0);
                        p.setLevel(0);
                    } else if (countdown % 10 == 0 || countdown <= 5)
                    {
//                        p.sendMessage("The game starts in " + countdown + " seconds.");
                        p.sendMessage(main.getMessages().get(1).replace("%secondsLeft", String.valueOf(countdown)));
                    }
                }

                if (countdown == 0)
                {
                    countdown = -1;
                    main.getServer().getScheduler().cancelTask(countdownTask);
                    startGame();
                }
                updateSign();
                countdown--;
            }
        }, 20L, 20L);
    }

    public void startGame()
    {
        state = 2;
        seekers.put((String) players.keySet().toArray()[new Random().nextInt(players.size())], false);

        startHiders();
        startSeekers();

        secondsLeftTask = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable()
        {
            public void run()
            {
                if (secondsLeft == 0)
                {
                    main.getServer().getScheduler().cancelTask(secondsLeftTask);
                    resetArena();
                }
                updateScoreboard();
                secondsLeft--;
            }
        }, 20L, 20L);
    }

    public void startHiders()
    {
        for (String uuid : players.keySet())
        {
            if (!seekers.containsKey(uuid))
            {
                Player p = main.getServer().getPlayer(UUID.fromString(uuid));
                MiscDisguise disguise = new MiscDisguise(DisguiseType.FALLING_BLOCK, players.get(p.getUniqueId().toString()));
                DisguiseAPI.disguiseToAll(p, disguise);
                p.getInventory().clear();
                p.updateInventory();
                p.teleport(spawnHider);
//                p.sendMessage("The game starts now! (You are a " + blocks.get(players.get(p.getUniqueId().toString())) + ")");
                p.sendMessage(main.getMessages().get(2).replace("%block", blocks.get(players.get(p.getUniqueId().toString()))));
//                p.sendMessage("The seeker will come to find you in " + seekerCountdown + " seconds.");
                p.sendMessage(main.getMessages().get(3).replace("%secondsLeft", String.valueOf(seekerCountdown)));
            }
        }
    }

    public void startSeekers()
    {
        for (String uuid : seekers.keySet())
        {
            Player p = main.getServer().getPlayer(UUID.fromString(uuid));
            p.getInventory().clear();
            p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
            p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
            p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
            p.teleport(spawnSeeker);
//            p.sendMessage("You are the seeker.");
            p.sendMessage(main.getMessages().get(4));
        }
        seekerCountdownTask = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable()
        {
            @Override
            public void run()
            {
                for (String uuid : seekers.keySet())
                {
                    Player p = main.getServer().getPlayer(UUID.fromString(uuid));
                    if (seekerCountdown == 0)
                    {
                        seekers.put(uuid, true);
                        p.removePotionEffect(PotionEffectType.BLINDNESS);
//                        p.sendMessage("You are now able to move.");
                        p.sendMessage(main.getMessages().get(5));
                    } else if (seekerCountdown % 10 == 0 || seekerCountdown <= 5)
                    {
//                        p.sendMessage("You will be able to move in " + seekerCountdown + " seconds.");
                        p.sendMessage(main.getMessages().get(6).replace("%secondsLeft", String.valueOf(seekerCountdown)));
                    }
                    p.setExp(0);
                    p.setLevel(seekerCountdown);
                }

                if (seekerCountdown == 0)
                {
                    for (String uuid : players.keySet())
                    {
                        if (!seekers.containsKey(uuid))
                        {
                            Player p = main.getServer().getPlayer(UUID.fromString(uuid));
//                            p.sendMessage("The seeker is coming to find you.");
                            p.sendMessage(main.getMessages().get(7));
                        }
                    }
                    seekerCountdown = -1;
                    main.getServer().getScheduler().cancelTask(seekerCountdownTask);
                }
                seekerCountdown--;
            }
        }, 20L, 20L);
    }

    public void updateScoreboard()
    {
        ScoreboardManager manager = main.getServer().getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        if (state == 2)
        {
            Objective childsgame = board.registerNewObjective(main.getConfig().getString("Name"), "dummy");
            childsgame.setDisplaySlot(DisplaySlot.SIDEBAR);
            childsgame.setDisplayName(main.getConfig().getString("Name"));
            Score time = childsgame.getScore(ChatColor.GOLD + "Seconds:");
            Score hider = childsgame.getScore(ChatColor.GREEN + "Hider:");
            Score seeker = childsgame.getScore(ChatColor.GREEN + "Seeker:");
            int hi = 0;
            for (String s : players.keySet())
            {
                if (!seekers.containsKey(s))
                {
                    hi++;
                }
            }
            time.setScore(secondsLeft);
            hider.setScore(hi);
            seeker.setScore(seekers.size());
        }

        for (String uuid : players.keySet())
        {
            Player p = main.getServer().getPlayer(UUID.fromString(uuid));
            p.setScoreboard(board);
        }
    }

    public void respawnSeeker(final Player p)
    {
        seekers.put(p.getUniqueId().toString(), false);
        p.getInventory().clear();
        p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
        p.updateInventory();
        DisguiseAPI.undisguiseToAll(p);
        for (Player pl : main.getServer().getOnlinePlayers())
        {
            pl.showPlayer(p);
        }
        main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
        {
            @Override
            public void run()
            {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
            }
        }, 1L);

        for (Map.Entry<String, Location> entry : solidBlocks.entrySet())
        {
            Player block = main.getServer().getPlayer(UUID.fromString(entry.getKey()));
            for (Player pl : main.getServer().getOnlinePlayers())
            {
                if (!pl.getUniqueId().toString().equals(block.getUniqueId().toString()))
                {
                    pl.sendBlockChange(entry.getValue(), Material.getMaterial(players.get(entry.getKey())), (byte) 0);
                    pl.hidePlayer(block);
                }
            }
        }

        seekerRespawnCountdowns.put(p.getUniqueId().toString(), seekerRespawnCountdown);
        seekerRespawnTasks.put(p.getUniqueId().toString(), main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable()
        {
            @Override
            public void run()
            {
                p.setExp(0);
                p.setLevel(seekerRespawnCountdowns.get(p.getUniqueId().toString()));
                if (seekerRespawnCountdowns.get(p.getUniqueId().toString()) == 0)
                {
                    p.removePotionEffect(PotionEffectType.BLINDNESS);
                    seekers.put(p.getUniqueId().toString(), true);
//                    p.sendMessage("You are now able to move.");
                    main.getMessages().get(5);
                    int temp = seekerRespawnTasks.get(p.getUniqueId().toString());
                    seekerRespawnTasks.remove(p.getUniqueId().toString());
                    main.getServer().getScheduler().cancelTask(temp);
                } else if (seekerRespawnCountdowns.get(p.getUniqueId().toString()) % 10 == 0 || seekerRespawnCountdowns.get(p.getUniqueId().toString()) <= 5)
                {
//                    p.sendMessage("You will be able to move in " + seekerRespawnCountdowns.get(p.getUniqueId().toString()) + " seconds.");
                    p.sendMessage(main.getMessages().get(6).replace("%secondsLeft", String.valueOf(seekerRespawnCountdowns.get(p.getUniqueId().toString()))));
                }
                seekerRespawnCountdowns.put(p.getUniqueId().toString(), seekerRespawnCountdowns.get(p.getUniqueId().toString()) - 1);
            }
        }, 20L, 20L));
    }

    public void leave(Player p)
    {
        players.remove(p.getUniqueId().toString());
        seekers.remove(p.getUniqueId().toString());
        seekerRespawnCountdowns.remove(p.getUniqueId().toString());
        if (seekerRespawnTasks.containsKey(p.getUniqueId().toString()))
        {
            main.getServer().getScheduler().cancelTask(seekerRespawnTasks.get(p.getUniqueId().toString()));
        }
        seekerRespawnTasks.remove(p.getUniqueId().toString());
        sneakingCountdowns.remove(p.getUniqueId().toString());
        if (sneakingTasks.containsKey(p.getUniqueId().toString()))
        {
            main.getServer().getScheduler().cancelTask(sneakingTasks.get(p.getUniqueId().toString()));
        }
        sneakingTasks.remove(p.getUniqueId().toString());
        if (solidBlocks.containsKey(p.getUniqueId().toString()))
        {
            unsolid(p);
        }
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.setExp(0);
        p.setLevel(0);
        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        p.updateInventory();
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setScoreboard(main.getServer().getScoreboardManager().getNewScoreboard());
        DisguiseAPI.undisguiseToAll(p);
        for (Player pl : main.getServer().getOnlinePlayers())
        {
            pl.showPlayer(p);
        }
        p.teleport(spawnEnd);
//        p.sendMessage("You left the arena " + name);
        if (players.size() == 0 || players.size() < startPlayers)
        {
            state = 0;
            main.getServer().getScheduler().cancelTask(countdownTask);
            countdown = main.getConfig().getInt("Arena.Countdown");
        }
        updateSign();
        p.sendMessage(main.getMessages().get(8).replace("%name", name));

        if (players.size() < 2)
        {
            resetArena();
        } else if (seekers.size() == 0)
        {
            Player seek = main.getServer().getPlayer(UUID.fromString((String) players.keySet().toArray()[new Random().nextInt(players.size())]));
            seek.teleport(spawnSeeker);
            respawnSeeker(seek);
        }
    }

    public void solid(Player p)
    {
        DisguiseAPI.undisguiseToAll(p);
        for (Player pl : main.getServer().getOnlinePlayers())
        {
            if (!pl.getUniqueId().toString().equals(p.getUniqueId().toString()))
            {
                pl.sendBlockChange(p.getLocation(), Material.getMaterial(players.get(p.getUniqueId().toString())), (byte) 0);
                pl.hidePlayer(p);
            }
        }
//        p.sendMessage("You are now a solid " + blocks.get(players.get(p.getUniqueId().toString())) + ".");
        p.sendMessage(main.getMessages().get(9).replace("%block", blocks.get(players.get(p.getUniqueId().toString()))));
        solidBlocks.put(p.getUniqueId().toString(), new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
    }

    public void unsolid(Player p)
    {
        MiscDisguise disguise = new MiscDisguise(DisguiseType.FALLING_BLOCK, players.get(p.getUniqueId().toString()));
        DisguiseAPI.disguiseToAll(p, disguise);
        for (Player pl : main.getServer().getOnlinePlayers())
        {
            if (!pl.getUniqueId().toString().equals(p.getUniqueId().toString()))
            {
                pl.sendBlockChange(solidBlocks.get(p.getUniqueId().toString()), Material.AIR, (byte) 0);
                pl.showPlayer(p);
            }
        }
//        p.sendMessage("You are no longer a solid " + blocks.get(players.get(p.getUniqueId().toString())) + ".");
        p.sendMessage(main.getMessages().get(10).replace("%block", blocks.get(players.get(p.getUniqueId().toString()))));
        solidBlocks.remove(p.getUniqueId().toString());
    }

    public void updateSign()
    {
        sign.setLine(0, main.getConfig().getString("Arena.Sign.Name"));
        sign.setLine(1, "§b" + name);
        sign.setLine(2, players.size() + "/" + maxPlayers);
        if (state == 0)
        {
            sign.setLine(3, main.getConfig().getString("Arena.Sign.Waiting"));
        } else if (state == 1)
        {
            sign.setLine(3, main.getConfig().getString("Arena.Sign.Countdown").replace("%seconds", String.valueOf(countdown)));
        } else if (state == 2 || state == 3)
        {
            sign.setLine(3, main.getConfig().getString("Arena.Sign.GameRunning"));
        }
        sign.update();
    }

    public void resetArena()
    {
        state = 3;
        for (String uuid : getPlayers().keySet())
        {
            Player p = main.getServer().getPlayer(UUID.fromString(uuid));
            if (players.size() == seekers.size())
            {
                p.sendMessage(main.getMessages().get(11));
            } else
            {
                p.sendMessage(main.getMessages().get(46));
            }
        }
        for (Player p : main.getServer().getOnlinePlayers())
        {
            for (Location l : solidBlocks.values())
            {
                p.sendBlockChange(l, Material.AIR, (byte) 0);
            }
        }

        main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
        {
            @Override
            public void run()
            {
                for (String uuid : getPlayers().keySet())
                {
                    Player p = main.getServer().getPlayer(UUID.fromString(uuid));
                    p.getInventory().clear();
                    p.getInventory().setHelmet(null);
                    p.getInventory().setChestplate(null);
                    p.getInventory().setLeggings(null);
                    p.getInventory().setBoots(null);
                    p.updateInventory();
                    p.setHealth(p.getMaxHealth());
                    p.setFoodLevel(20);
                    p.setExp(0);
                    p.setLevel(0);
                    for (PotionEffect ef : p.getActivePotionEffects())
                    {
                        p.removePotionEffect(ef.getType());
                    }
                    DisguiseAPI.undisguiseToAll(p);
                    for (Player pl : main.getServer().getOnlinePlayers())
                    {
                        pl.showPlayer(p);
                    }
                    p.teleport(spawnEnd);
                }

                players.clear();
                seekers.clear();
                secondsLeft = main.getConfig().getInt("Arena.GameTime");
                countdown = main.getConfig().getInt("Arena.Countdown");
                seekerCountdown = main.getConfig().getInt("Arena.SeekerCountdown");
                seekerRespawnCountdown = main.getConfig().getInt("Arena.SeekerRespawnCountdown");
                seekerRespawnCountdowns.clear();
                seekerRespawnTasks.clear();
                state = 0;
                updateSign();
            }
        }, 100L);
    }

    public boolean inArena(Location loc)
    {
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        if ((loc.getWorld() == loc1.getWorld()) && (loc.getBlockX() >= minX) && (loc.getBlockX() <= maxX) && (loc.getBlockY() >= minY) && (loc.getBlockY() <= maxY) && (loc.getBlockZ() >= minZ) && (loc.getBlockZ() <= maxZ))
        {
            return true;
        }
        return false;
    }

    public String getName()
    {
        return name;
    }

    public Location getLoc1()
    {
        return loc1;
    }

    public void setLoc1(Location loc1)
    {
        this.loc1 = loc1;
    }

    public Location getLoc2()
    {
        return loc2;
    }

    public void setLoc2(Location loc2)
    {
        this.loc2 = loc2;
    }

    public Location getSpawnHider()
    {
        return spawnHider;
    }

    public void setSpawnHider(Location spawnHider)
    {
        this.spawnHider = spawnHider;
    }

    public Location getSpawnSeeker()
    {
        return spawnSeeker;
    }

    public void setSpawnSeeker(Location spawnSeeker)
    {
        this.spawnSeeker = spawnSeeker;
    }

    public Location getSpawnWaiting()
    {
        return spawnWaiting;
    }

    public void setSpawnWaiting(Location spawnWaiting)
    {
        this.spawnWaiting = spawnWaiting;
    }

    public Location getSpawnEnd()
    {
        return spawnEnd;
    }

    public void setSpawnEnd(Location spawnEnd)
    {
        this.spawnEnd = spawnEnd;
    }

    public HashMap<Integer, String> getBlocks()
    {
        return blocks;
    }

    public Sign getSign()
    {
        return sign;
    }

    public void setSign(Sign sign)
    {
        this.sign = sign;
    }

    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers)
    {
        this.maxPlayers = maxPlayers;
    }

    public int getStartPlayers()
    {
        return startPlayers;
    }

    public void setStartPlayers(int startPlayers)
    {
        this.startPlayers = startPlayers;
    }

    public HashMap<String, Integer> getPlayers()
    {
        return players;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public HashMap<String, Boolean> getSeekers()
    {
        return seekers;
    }

    public int getSeekerCountdown()
    {
        return seekerCountdown;
    }

    public HashMap<String, Location> getSolidBlocks()
    {
        return solidBlocks;
    }

    public HashMap<String, Integer> getSneakingTasks()
    {
        return sneakingTasks;
    }

    public int getSneakingCountdown()
    {
        return sneakingCountdown;
    }

    public HashMap<String, Integer> getSneakingCountdowns()
    {
        return sneakingCountdowns;
    }

}
