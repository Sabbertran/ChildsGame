package me.sabbertran.childsgame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Events implements Listener
{

    private ChildsGame main;

    public Events(ChildsGame has)
    {
        this.main = has;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev)
    {
        Player p = ev.getPlayer();

        if ((ev.getItem() != null) && (ev.getItem().getItemMeta().getDisplayName() != null) && (ev.getItem().getType() == Material.getMaterial(main.getConfig().getInt("Arena.toolID"))) && (ev.getItem().getItemMeta().getDisplayName().startsWith(ChatColor.GOLD+ "Arena tool - ")))
        {
            if (p.hasPermission("childsgame.admin.arena.tool"))
            {
                String arena = ev.getItem().getItemMeta().getDisplayName().split(" ")[3];
                if (main.getArenas().containsKey(arena))
                {
                    ev.setCancelled(true);

                    Inventory inv = main.getServer().createInventory(p, 9, ev.getItem().getItemMeta().getDisplayName());

                    ItemStack item_0, item_1, item_3, item_5, item_6, item_8;
                    ItemMeta meta_0, meta_1, meta_3, meta_5, meta_6, meta_8;

                    item_0 = new ItemStack(Material.WOOL);
                    meta_0 = item_0.getItemMeta();
                    meta_0.setDisplayName("Set first arena corner");
                    item_0.setItemMeta(meta_0);
                    inv.setItem(0, item_0);

                    item_1 = new ItemStack(Material.WOOL);
                    item_1.setDurability((short) 1);
                    meta_1 = item_1.getItemMeta();
                    meta_1.setDisplayName("Set second arena corner");
                    item_1.setItemMeta(meta_1);
                    inv.setItem(1, item_1);

                    item_3 = new ItemStack(Material.WOOL);
                    item_3.setDurability((short) 2);
                    meta_3 = item_3.getItemMeta();
                    meta_3.setDisplayName("Set arena waiting room");
                    item_3.setItemMeta(meta_3);
                    inv.setItem(3, item_3);

                    item_5 = new ItemStack(Material.WOOL);
                    item_5.setDurability((short) 3);
                    meta_5 = item_5.getItemMeta();
                    meta_5.setDisplayName("Set hider spawn");
                    item_5.setItemMeta(meta_5);
                    inv.setItem(5, item_5);

                    item_6 = new ItemStack(Material.WOOL);
                    item_6.setDurability((short) 4);
                    meta_6 = item_6.getItemMeta();
                    meta_6.setDisplayName("Set seeker spawn");
                    item_6.setItemMeta(meta_6);
                    inv.setItem(6, item_6);

                    item_8 = new ItemStack(Material.WOOL);
                    item_8.setDurability((short) 5);
                    meta_8 = item_8.getItemMeta();
                    meta_8.setDisplayName("Set arena leaving/ending room");
                    item_8.setItemMeta(meta_8);
                    inv.setItem(8, item_8);

                    p.openInventory(inv);
                } else
                {
//                p.sendMessage("The arena " + arena + " does not exist.");
                    p.sendMessage(main.getMessages().get(12).replace("%name", arena));
                }
            }
        } else if ((ev.getItem() != null) && (ev.getItem().getItemMeta().getDisplayName() != null) && (ev.getItem().getType() == Material.BOOK) && (ev.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Choose block")))
        {
            for (Arena a : main.getArenas().values())
            {
                if (a.getPlayers().containsKey(p.getUniqueId().toString()))
                {
                    int size = 9;
                    if (a.getBlocks().size() <= 9)
                    {
                        size = 9;
                    } else if (a.getBlocks().size() <= 18)
                    {
                        size = 18;
                    } else if (a.getBlocks().size() <= 27)
                    {
                        size = 27;
                    } else if (a.getBlocks().size() <= 36)
                    {
                        size = 36;
                    } else if (a.getBlocks().size() <= 45)
                    {
                        size = 45;
                    } else if (a.getBlocks().size() <= 54)
                    {
                        size = 54;
                    } else
                    {
//                        p.sendMessage("There are too many possible blocks for this arena. Please contact an administrator.");
                        p.sendMessage(main.getMessages().get(13));
                    }
                    Inventory inv = main.getServer().createInventory(p, size, main.getConfig().getString("Arena.BlockChooseItemAndInventoryName"));
                    int i = 0;
                    for (Map.Entry<Integer, String> entry : a.getBlocks().entrySet())
                    {
                        int block = entry.getKey();
                        ItemStack is = new ItemStack(Material.getMaterial(block));
                        ItemMeta im = is.getItemMeta();
                        im.setDisplayName(entry.getValue());
                        is.setItemMeta(im);
                        inv.setItem(i, is);
                        i++;
                    }
                    p.openInventory(inv);
                }
            }
        } else if (ev.getAction() == Action.LEFT_CLICK_BLOCK || ev.getAction() == Action.LEFT_CLICK_AIR)
        {
            if (ev.getClickedBlock() != null)
            {
                for (Arena a : main.getArenas().values())
                {
                    for (Map.Entry<String, Location> entry : a.getSolidBlocks().entrySet())
                    {
                        if (entry.getValue().equals(ev.getClickedBlock().getLocation()) && a.getSeekers().containsKey(p.getUniqueId().toString()))
                        {
                            Player hit = main.getServer().getPlayer(UUID.fromString(entry.getKey()));
                            a.unsolid(hit);

                            PacketContainer useEntity = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Client.USE_ENTITY, false);
                            useEntity.getIntegers().write(0, hit.getEntityId());
                            useEntity.getEntityUseActions().write(0, EntityUseAction.ATTACK);
                            try
                            {
                                ProtocolLibrary.getProtocolManager().recieveClientPacket(p, useEntity);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev)
    {
        Player p = (Player) ev.getWhoClicked();
        if (ev.getInventory().getName().startsWith(ChatColor.GOLD + "Arena tool - "))
        {
            String arena = ev.getInventory().getName().split(" ")[3];
            if (ev.getCurrentItem() != null)
            {
                if (ev.getCurrentItem().getDurability() == (short) 0)
                {
                    main.getArenas().get(arena).setLoc1(p.getLocation());
//                    p.sendMessage("Successfully set the first corner for arena " + arena + ".");
                    p.sendMessage(main.getMessages().get(14).replace("%name", arena));
                } else if (ev.getCurrentItem().getDurability() == (short) 1)
                {
                    main.getArenas().get(arena).setLoc2(p.getLocation());
//                    p.sendMessage("Successfully set the second corner for arena " + arena + ".");
                    p.sendMessage(main.getMessages().get(15).replace("%name", arena));
                } else if (ev.getCurrentItem().getDurability() == (short) 2)
                {
                    main.getArenas().get(arena).setSpawnWaiting(p.getLocation());
//                    p.sendMessage("Successfully set the lobby spawn for arena " + arena + ".");
                    p.sendMessage(main.getMessages().get(16).replace("%name", arena));
                } else if (ev.getCurrentItem().getDurability() == (short) 3)
                {
                    main.getArenas().get(arena).setSpawnHider(p.getLocation());
//                    p.sendMessage("Successfully set the hider spawn for arena " + arena + ".");
                    p.sendMessage(main.getMessages().get(17).replace("%name", arena));
                } else if (ev.getCurrentItem().getDurability() == (short) 4)
                {
                    main.getArenas().get(arena).setSpawnSeeker(p.getLocation());
//                    p.sendMessage("Successfully set the seeker spawn for arena " + arena + ".");
                    p.sendMessage(main.getMessages().get(18).replace("%name", arena));
                } else if (ev.getCurrentItem().getDurability() == (short) 5)
                {
                    main.getArenas().get(arena).setSpawnEnd(p.getLocation());
//                    p.sendMessage("Successfully set the after match spawn for arena " + arena + ".");
                    p.sendMessage(main.getMessages().get(19).replace("%name", arena));
                }
                ev.setCancelled(true);
                p.closeInventory();
            }
        } else if (ev.getInventory().getName().equals(main.getConfig().getString("Arena.BlockChooseItemAndInventoryName")))
        {
            for (Arena a : main.getArenas().values())
            {
                if (a.getPlayers().containsKey(p.getUniqueId().toString()))
                {
                    if (a.getBlocks().containsKey(ev.getCurrentItem().getTypeId()))
                    {
                        a.getPlayers().put(p.getUniqueId().toString(), ev.getCurrentItem().getTypeId());
                        ev.setCancelled(true);
                        p.closeInventory();
//                        p.sendMessage("You will be a " + a.getBlocks().get(ev.getCurrentItem().getTypeId()) + ".");
                        p.sendMessage(main.getMessages().get(20).replace("%block", a.getBlocks().get(ev.getCurrentItem().getTypeId())));
                    }
                }
            }
        }
        for (Arena a : main.getArenas().values())
        {
            if (a.getPlayers().containsKey(p.getUniqueId().toString()))
            {
                ev.setCancelled(true);
                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent ev)
    {
        Player p = ev.getPlayer();
        if (ev.getLine(0).equals(main.getConfig().getString("Arena.Sign.CreateIdentification")))
        {
            if (p.hasPermission("childsgame.admin.arena.sign"))
            {
                if (main.getArenas().containsKey(ev.getLine(1)))
                {
                    Arena a = main.getArenas().get(ev.getLine(1));
                    if (a.getLoc1() != null && a.getLoc2() != null && a.getSpawnEnd() != null && a.getSpawnHider() != null && a.getSpawnSeeker() != null && a.getSpawnWaiting() != null && a.getBlocks() != null)
                    {
                        if (a.getSign() == null)
                        {
                            Sign s = (Sign) ev.getBlock().getState();
                            a.setSign(s);

                            ev.setLine(0, main.getConfig().getString("Arena.Sign.Name"));
                            ev.setLine(1, ChatColor.AQUA + a.getName());
                            ev.setLine(2, a.getPlayers().size() + "/" + a.getMaxPlayers());
                            ev.setLine(3, main.getConfig().getString("Arena.Sign.Waiting"));
                            return;
                        } else
                        {
                            ev.getBlock().setType(Material.AIR);
                            ev.setCancelled(true);
                            p.sendMessage(main.getMessages().get(42));
                            return;
                        }
                    } else
                    {
                        ev.getBlock().setType(Material.AIR);
                        ev.setCancelled(true);
//                    p.sendMessage("The arena is not completely set up yet.");
                        p.sendMessage(main.getMessages().get(21));
                    }
                } else
                {
                    ev.getBlock().setType(Material.AIR);
                    ev.setCancelled(true);
//                p.sendMessage("The arena " + ev.getLine(1) + " does not exist.");
                    p.sendMessage(main.getMessages().get(12).replace("%name", ev.getLine(1)));
                }
            }
        }
    }

    @EventHandler
    public void onArenaJoin(PlayerInteractEvent ev)
    {
        Player p = ev.getPlayer();
        if (ev.getClickedBlock() != null && ev.getAction() == Action.RIGHT_CLICK_BLOCK && (ev.getClickedBlock().getType() == Material.SIGN || ev.getClickedBlock().getType() == Material.SIGN_POST || ev.getClickedBlock().getType() == Material.WALL_SIGN))
        {
            Sign s = (Sign) ev.getClickedBlock().getState();
            if (s.getLine(0).equals(main.getConfig().getString("Arena.Sign.Name")))
            {
                if (p.hasPermission("childsgame.user.arena.join"))
                {
                    Arena a = main.getArenas().get(s.getLine(1).substring(2, s.getLine(1).length()));
                    if (a.getState() == 0 || a.getState() == 1)
                    {
                        if (a.getPlayers().size() < a.getMaxPlayers())
                        {
                            if (!a.getPlayers().containsKey(p.getUniqueId().toString()))
                            {
                                a.join(p);
                            } else
                            {
//                            p.sendMessage("You can't join the game you are already in.");
                                p.sendMessage(main.getMessages().get(22));
                                return;
                            }
                        } else
                        {
//                        p.sendMessage("The game is full.");
                            p.sendMessage(main.getMessages().get(23));
                            return;
                        }
                    } else
                    {
//                    p.sendMessage("The game has already started.");
                        p.sendMessage(main.getMessages().get(24));
                        return;
                    }
                } else
                {
                    p.sendMessage(main.getMessages().get(40));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onDamageEvent(EntityDamageByEntityEvent ev)
    {
        if (ev.getEntity() instanceof Player && ev.getDamager() instanceof Player)
        {
            Player damager = (Player) ev.getDamager();
            Player victim = (Player) ev.getEntity();
            for (Arena a : main.getArenas().values())
            {
                for (String uuid : a.getPlayers().keySet())
                {
                    if ((a.getState() == 0 || a.getState() == 1) && victim.getUniqueId().toString().equals(uuid))
                    {
                        ev.setCancelled(true);
                        return;
                    } else if (a.getState() == 2 && a.getSeekerCountdown() >= 0 && a.getSeekers().containsKey(damager.getUniqueId().toString()))
                    {
                        ev.setCancelled(true);
                        return;
                    } else if (a.getState() == 2 && a.getSeekerCountdown() >= 0 && a.getSeekers().containsKey(victim.getUniqueId().toString()))
                    {
                        ev.setCancelled(true);
                        return;
                    } else if (a.getState() == 2 && !a.getSeekers().containsKey(damager.getUniqueId().toString()) && !a.getSeekers().containsKey(victim.getUniqueId().toString()))
                    {
                        ev.setCancelled(true);
                        return;
                    } else if (a.getState() == 2 && a.getSeekers().containsKey(damager.getUniqueId().toString()) && a.getSeekers().containsKey(victim.getUniqueId().toString()))
                    {
                        ev.setCancelled(true);
                        return;
                    } else if (a.getState() == 3)
                    {
                        ev.setCancelled(true);
                        return;
                    }
                }

                if (a.getPlayers().containsKey(damager.getUniqueId().toString()) && a.getPlayers().containsKey(victim.getUniqueId().toString()))
                {
                    if (!ev.isCancelled())
                    {
                        damager.playSound(victim.getLocation(), Sound.SUCCESSFUL_HIT, 10, 0);
                    }
                }

            }
        }
    }

    @EventHandler
    public void onBlockMove(PlayerMoveEvent ev)
    {
        Player p = ev.getPlayer();
        if (!(ev.getFrom().getBlockX() == ev.getTo().getBlockX() && ev.getFrom().getBlockY() == ev.getTo().getBlockY() && ev.getFrom().getBlockZ() == ev.getTo().getBlockZ()))
        {
            for (Arena a : main.getArenas().values())
            {
                if (a.getSeekers().containsKey(p.getUniqueId().toString()) && !a.getSeekers().get(p.getUniqueId().toString()))
                {
                    p.teleport(ev.getFrom());
                    return;
                } else if (a.getState() == 2 && a.getPlayers().containsKey(p.getUniqueId().toString()) && !a.getSeekers().containsKey(p.getUniqueId().toString()))
                {
                    if (a.getSneakingTasks().containsKey(p.getUniqueId().toString()))
                    {
                        main.getServer().getScheduler().cancelTask(a.getSneakingTasks().get(p.getUniqueId().toString()));
                        a.getSneakingTasks().remove(p.getUniqueId().toString());
                        p.setLevel(0);
                        p.setExp(0);
                        return;
                    } else if (a.getSolidBlocks().containsKey(p.getUniqueId().toString()))
                    {
                        a.unsolid(p);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent ev)
    {
        if (ev.getEntity() instanceof Player)
        {
            Player p = (Player) ev.getEntity();
            for (Arena a : main.getArenas().values())
            {
                if (a.getPlayers().containsKey(p.getUniqueId().toString()))
                {
                    ev.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent ev)
    {
        Player dead = ev.getEntity().getPlayer();
        Player killer = ev.getEntity().getKiller();
        for (Arena a : main.getArenas().values())
        {
            if (a.getPlayers().containsKey(dead.getUniqueId().toString()) && !a.getSeekers().containsKey(dead.getUniqueId().toString()))
            {
                ev.setDeathMessage(null);
                for (String uuid : a.getPlayers().keySet())
                {
                    Player p = main.getServer().getPlayer(UUID.fromString(uuid));
//                    p.sendMessage("Hider " + dead.getName() + " (" + a.getBlocks().get(a.getPlayers().get(dead.getUniqueId().toString())) + ") was slain by " + killer.getName());
                    p.sendMessage(main.getMessages().get(25).replace("%hider", dead.getName()).replace("%block", a.getBlocks().get(a.getPlayers().get(dead.getUniqueId().toString()))).replace("%seeker", killer.getName()));
                }
                ev.setDroppedExp(0);
                ev.getDrops().clear();
                a.getSeekers().put(dead.getUniqueId().toString(), true);

                if (a.getSeekers().size() == a.getPlayers().size() && a.getState() != 3)
                {
                    a.resetArena();
                }
                return;
            } else if (a.getPlayers().containsKey(dead.getUniqueId().toString()) && a.getSeekers().containsKey(dead.getUniqueId().toString()))
            {
                ev.setDeathMessage(null);
                for (String uuid : a.getPlayers().keySet())
                {
                    Player p = main.getServer().getPlayer(UUID.fromString(uuid));
//                    p.sendMessage("Seeker " + dead.getName() + " was slain by " + killer.getName() + " (" + a.getBlocks().get(a.getPlayers().get(killer.getUniqueId().toString())) + ")");
                    p.sendMessage(main.getMessages().get(26).replace("%seeker", dead.getName()).replace("%hider", killer.getName()).replace("%block", a.getBlocks().get(a.getPlayers().get(killer.getUniqueId().toString()))));
                }
                ev.setDroppedExp(0);
                ev.getDrops().clear();
                return;
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent ev)
    {
        Player p = ev.getPlayer();
        for (Arena a : main.getArenas().values())
        {
            if (a.getSeekers().containsKey(p.getUniqueId().toString()))
            {
                ev.setRespawnLocation(a.getSpawnSeeker());
                if (a.getState() != 3)
                {
                    a.respawnSeeker(p);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent ev)
    {
        Player p = ev.getPlayer();
        for (Arena a : main.getArenas().values())
        {
            if (a.getPlayers().containsKey(p.getUniqueId().toString()))
            {
                ev.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent ev)
    {
        final Player p = ev.getPlayer();
        for (final Arena a : main.getArenas().values())
        {
            if (a.getState() == 2 && a.getPlayers().containsKey(p.getUniqueId().toString()) && !a.getSeekers().containsKey(p.getUniqueId().toString()))
            {
                if (p.isSneaking())
                {
                    for (Location l : a.getSolidBlocks().values())
                    {
                        if (p.getWorld() == l.getWorld() && p.getLocation().getBlockX() == l.getBlockX() && p.getLocation().getBlockZ() == l.getBlockZ() && p.getLocation().getBlockY() == l.getBlockY() + 1)
                        {
//                            p.sendMessage("You cannot go solid here.");
                            p.sendMessage(main.getMessages().get(27));
                        }
                    }

                    p.setLevel(0);
                    p.setExp(1f);
                    a.getSneakingCountdowns().put(p.getUniqueId().toString(), a.getSneakingCountdown());
                    a.getSneakingTasks().put(p.getUniqueId().toString(), main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (a.getSneakingCountdowns().get(p.getUniqueId().toString()) == 0)
                            {
                                p.setExp(0);
                                a.solid(p);
                                int temp = a.getSneakingTasks().get(p.getUniqueId().toString());
                                a.getSneakingTasks().remove(p.getUniqueId().toString());
                                main.getServer().getScheduler().cancelTask(temp);
                            } else if ((a.getSneakingCountdowns().get(p.getUniqueId().toString()) % 10 == 0) || a.getSneakingCountdowns().get(p.getUniqueId().toString()) <= 5)
                            {
                                p.setExp((a.getSneakingCountdowns().get(p.getUniqueId().toString()) * 0.2f) - 0.2f);
                            }

                            a.getSneakingCountdowns().put(p.getUniqueId().toString(), a.getSneakingCountdowns().get(p.getUniqueId().toString()) - 1);
                            return;
                        }
                    }, 20L, 20L));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent ev)
    {
        Player p = ev.getPlayer();
        if (ev.getReason().contains("Flying"))
        {
            for (Arena a : main.getArenas().values())
            {
                for (Location l : a.getSolidBlocks().values())
                {
                    if (p.getWorld() == l.getWorld() && p.getLocation().getBlockX() == l.getBlockX() && p.getLocation().getBlockZ() == l.getBlockZ() && p.getLocation().getBlockY() == l.getBlockY() + 1)
                    {
                        ev.setReason(null);
                        ev.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent ev)
    {
        Player p = ev.getPlayer();
        for (Arena a : main.getArenas().values())
        {
            if (a.getPlayers().containsKey(p.getUniqueId().toString()) && a.inArena(ev.getBlock().getLocation()))
            {
                ev.setCancelled(true);
                return;
            } else if (a.getSign() != null && a.getSign().getLocation() == ev.getBlock().getLocation())
            {
                a.setSign(null);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev
    )
    {
        Player p = ev.getPlayer();
        for (Arena a : main.getArenas().values())
        {
            if (a.getPlayers().containsKey(p.getUniqueId().toString()))
            {
                a.leave(p);
                return;
            }
        }
    }
}
