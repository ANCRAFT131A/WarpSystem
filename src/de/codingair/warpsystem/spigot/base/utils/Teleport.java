package de.codingair.warpsystem.spigot.base.utils;

import de.codingair.codingapi.particles.animations.Animation;
import de.codingair.codingapi.particles.animations.playeranimations.CircleAnimation;
import de.codingair.codingapi.player.MessageAPI;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.utils.Action;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.Warp;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Example;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.money.AdapterType;
import de.codingair.warpsystem.transfer.packets.spigot.PrepareTeleportPacket;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Teleport {
    private Player player;
    private Animation animation;
    private BukkitRunnable runnable;

    private Sound finishSound = Sound.ENDERMAN_TELEPORT;
    private Sound cancelSound = Sound.ITEM_BREAK;

    private long startTime = 0;

    private Warp warp;
    private String globalWarpName;
    private String displayName;
    private double costs;

    private boolean showMessage = true;
    private boolean canMove = false;


    public Teleport(Player player, Warp warp) {
        this(player, warp, null);
    }

    public Teleport(Player player, Warp warp, String displayName) {
        this.player = player;
        this.warp = warp;
        this.displayName = displayName;

        this.costs = warp.getAction(Action.PAY_MONEY) == null ? 0 : warp.getAction(Action.PAY_MONEY).getValue();
        if(player.hasPermission(WarpSystem.PERMISSION_ByPass_Teleport_Costs)) this.costs = 0;

        this.animation = new CircleAnimation(WarpSystem.getInstance().getTeleportManager().getParticle(), player, WarpSystem.getInstance(), WarpSystem.getInstance().getTeleportManager().getRadius());
        this.runnable = new BukkitRunnable() {
            private int left = WarpSystem.getInstance().getTeleportManager().getSeconds();

            @Override
            public void run() {
                if(left == 0) {
                    teleport();
                    MessageAPI.sendActionBar(player, null);
                    return;
                }

                player.playSound(player.getLocation(), Sound.NOTE_PIANO.bukkitSound(), 1.5F, 0.5F);

                String msg = Lang.get("Teleporting_Info", new Example("ENG", "&cTeleport in §l§n%seconds%"), new Example("GER", "&cTeleport in &l&n%seconds%")).replace("%seconds%", left + "");

                MessageAPI.sendActionBar(player, msg);

                left--;
            }
        };
    }

    public Teleport(Player player, String globalWarpName, String displayName) {
        this(player, globalWarpName, displayName, 0);
    }

    public Teleport(Player player, String globalWarpName, String displayName, double costs) {
        this.player = player;
        this.globalWarpName = globalWarpName;
        this.displayName = displayName;
        this.costs = costs;
        this.animation = new CircleAnimation(WarpSystem.getInstance().getTeleportManager().getParticle(), player, WarpSystem.getInstance(), WarpSystem.getInstance().getTeleportManager().getRadius());
        this.runnable = new BukkitRunnable() {
            private int left = WarpSystem.getInstance().getTeleportManager().getSeconds();

            @Override
            public void run() {
                if(left == 0) {
                    teleport();
                    MessageAPI.sendActionBar(player, null);
                    return;
                }

                player.playSound(player.getLocation(), Sound.NOTE_PIANO.bukkitSound(), 1.5F, 0.5F);

                String msg = Lang.get("Teleporting_Info", new Example("ENG", "&cTeleport in §l§n%seconds%"), new Example("GER", "&cTeleport in &l&n%seconds%")).replace("%seconds%", left + "");

                MessageAPI.sendActionBar(player, msg);

                left--;
            }
        };
    }

    public void start() {
        if(!animation.isRunning()) {
            this.startTime = System.currentTimeMillis();
            this.animation.setRunning(true);
            this.runnable.runTaskTimer(WarpSystem.getInstance(), 0L, 20L);
        }
    }

    public void cancel(boolean sound, boolean finished) {
        if(animation.isRunning()) {
            this.startTime = 0;
            this.animation.setRunning(false);
            this.runnable.cancel();
            MessageAPI.sendActionBar(player, null);
        }
        if(sound) cancelSound.playSound(player);

        if(!finished) {
            if(AdapterType.getActive() != null) {
                AdapterType.getActive().setMoney(player, AdapterType.getActive().getMoney(player) + this.costs);
            }
        }
    }

    public void teleport() {
        cancel(false, true);

        String warpName = warp == null ? globalWarpName : warp.getName();

        if(warp != null) {
            player.setFallDistance(0F);
            player.teleport(warp.getLocation());
            WarpSystem.getInstance().getTeleportManager().getTeleports().remove(this);
        } else {
            ((GlobalWarpManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS)).teleport(getPlayer(), displayName, warpName, this.costs, new Callback<PrepareTeleportPacket.Result>() {
                @Override
                public void accept(PrepareTeleportPacket.Result result) {
                    switch(result) {
                        case TELEPORTED:
                            WarpSystem.getInstance().getTeleportManager().getTeleports().remove(Teleport.this);
                            break;

                        case WARP_NOT_EXISTS:
                            getPlayer().sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Not_Exists", new Example("ENG", "&7The GlobalWarp '&b%GLOBAL_WARP%&7' &cdoes not exist&7."), new Example("GER", "&7Der GlobalWarp '&b%GLOBAL_WARP%&7' &cexistiert nicht&7.")).replace("%GLOBAL_WARP%", globalWarpName));

                        case SERVER_NOT_AVAILABLE:
                            getPlayer().sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Server_Is_Not_Online", new Example("ENG", "&cThe target server is not online!"), new Example("GER", "&cDer Ziel-Server ist nicht online!")));

                        case PLAYER_ALREADY_ON_SERVER:
                            getPlayer().sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Player_Is_Already_On_Target_Server", new Example("ENG", "&cYou are already on the target server."), new Example("GER", "&cDu befindest dich bereits auf dem Ziel-Server.")));

                        default:
                            if(AdapterType.getActive() != null) {
                                AdapterType.getActive().setMoney(player, AdapterType.getActive().getMoney(player) + costs);
                            }
                            break;
                    }
                }
            });

            return;
        }

        finishSound.playSound(player);

        if(showMessage || this.costs > 0) {
            if(this.costs > 0) {
                player.sendMessage(Lang.getPrefix() + Lang.get("Money_Paid", new Example("ENG", "&7You have paid &c%AMOUNT% coin(s) &7to teleport to '&b%warp%&7'."), new Example("GER", "&7Du hast &c%AMOUNT% Coin(s) &7bezahlt, um dich nach '&b%warp%&7' zu teleportieren!")).replace("%AMOUNT%", costs + "").replace("%warp%", displayName != null ? ChatColor.translateAlternateColorCodes('&', displayName) : ChatColor.translateAlternateColorCodes('&', warp.getName())));
            } else {
                player.sendMessage(Lang.getPrefix() + Lang.get("Teleported_To", new Example("ENG", "&7You have been teleported to '&b%warp%&7'."), new Example("GER", "&7Du wurdest zu '&b%warp%&7' teleportiert.")).replace("%warp%", displayName != null ? ChatColor.translateAlternateColorCodes('&', displayName) : ChatColor.translateAlternateColorCodes('&', warp.getName())));
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Warp getTo() {
        return warp;
    }

    public Animation getAnimation() {
        return animation;
    }

    public BukkitRunnable getRunnable() {
        return runnable;
    }

    public Sound getFinishSound() {
        return finishSound;
    }

    public void setFinishSound(Sound finishSound) {
        this.finishSound = finishSound;
    }

    public Sound getCancelSound() {
        return cancelSound;
    }

    public void setCancelSound(Sound cancelSound) {
        this.cancelSound = cancelSound;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isShowMessage() {
        return showMessage;
    }

    public void setShowMessage(boolean showMessage) {
        this.showMessage = showMessage;
    }
}