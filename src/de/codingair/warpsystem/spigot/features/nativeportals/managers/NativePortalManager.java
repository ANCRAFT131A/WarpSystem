package de.codingair.warpsystem.spigot.features.nativeportals.managers;

import de.codingair.codingapi.API;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.tools.time.TimeList;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.nativeportals.Portal;
import de.codingair.warpsystem.spigot.features.nativeportals.PortalEditor;
import de.codingair.warpsystem.spigot.features.nativeportals.commands.CNativePortals;
import de.codingair.warpsystem.spigot.features.nativeportals.guis.GEditor;
import de.codingair.warpsystem.spigot.features.nativeportals.listeners.EditorListener;
import de.codingair.warpsystem.spigot.features.nativeportals.listeners.PortalListener;
import de.codingair.warpsystem.utils.Manager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NativePortalManager implements Manager {
    private List<Portal> portals = new ArrayList<>();
    private List<Player> noTeleport = new ArrayList<>();
    private TimeList<String> goingToDelete = new TimeList<>();
    private TimeList<String> goingToEdit = new TimeList<>();

    private boolean sendMessage;

    @Override
    public boolean load() {
        ConfigFile config = WarpSystem.getInstance().getFileManager().getFile("Config");
        Object test = config.getConfig().get("WarpSystem.Send.Teleport_Message.NativePortals", null);
        if(test == null) {
            config.getConfig().set("WarpSystem.Send.Teleport_Message.GlobalWarps", true);
            config.getConfig().set("WarpSystem.Send.Teleport_Message.Warps", true);
            config.getConfig().set("WarpSystem.Send.Teleport_Message.NativePortals", true);
            config.getConfig().set("WarpSystem.Send.Teleport_Message.Portals", true);
            sendMessage = true;
        } else if(test instanceof Boolean) {
            sendMessage = (boolean) test;
        }

        Bukkit.getPluginManager().registerEvents(new EditorListener(), WarpSystem.getInstance());
        Bukkit.getPluginManager().registerEvents(new PortalListener(), WarpSystem.getInstance());
        new CNativePortals().register(WarpSystem.getInstance());

        boolean success = true;

        this.portals.forEach(p -> {
            p.setVisible(false);
            p.clear();
        });
        this.portals.clear();

        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Teleporters");

        WarpSystem.log("  > Loading NativePortals");
        int fails = 0;
        for(String s : file.getConfig().getStringList("NativePortals")) {
            Portal p = Portal.fromJSONString(s);
            if(p != null) addPortal(p);
            else {
                fails++;
                success = false;
            }
        }

        if(fails > 0) WarpSystem.log("    > " + fails + " Error(s)");
        WarpSystem.log("    ...got " + portals.size() + " NativePortal(s)");

        showAll();

        return success;
    }

    @Override
    public void save(boolean saver) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Teleporters");
        if(!saver) WarpSystem.log("  > Saving NativePortals...");

        List<String> data = new ArrayList<>();

        for(Portal portal : this.portals) {
            data.add(portal.toJSONString());
        }

        if(!saver) hideAll();

        file.getConfig().set("NativePortals", data);
        file.saveConfig();

        if(!saver) WarpSystem.log("    ...saved " + data.size() + " NativePortal(s)");
    }

    public void hideAll() {
        for(Portal portal : this.portals) {
            portal.setVisible(false);
            portal.clear();
        }
    }

    public void showAll() {
        for(Portal portal : this.portals) {
            portal.setVisible(true);
        }
    }

    public List<PortalEditor> getEditors() {
        return API.getRemovables(PortalEditor.class);
    }

    public PortalEditor getEditor(Player player) {
        return API.getRemovable(player, PortalEditor.class);
    }

    public boolean isEditing(Player player) {
        return getEditor(player) != null;
    }

    private void init(Portal portal) {
        portal.getListeners().clear();
        portal.getListeners().add((player) -> {
            if(API.getRemovable(player, GUI.class) != null) return;
            else if(NativePortalManager.getInstance().isEditing(player)) return;
            else if(WarpSystem.getInstance().getTeleportManager().isTeleporting(player)) return;

            if(goingToDelete.contains(player.getName())) {
                setGoingToDelete(player, 0);
                noTeleport.add(player);

                player.setVelocity(player.getLocation().getDirection().normalize().multiply(-0.8));

                Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> {
                    new GEditor(player, portal, GEditor.Menu.DELETE).open();
                    noTeleport.remove(player);
                }, 4L);

            } else if(goingToEdit.contains(player.getName())) {
                setGoingToEdit(player, 0);
                noTeleport.add(player);

                player.setVelocity(player.getLocation().getDirection().normalize().multiply(-0.8));

                Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> {
                    new GEditor(player, portal).open();
                    noTeleport.remove(player);
                }, 4L);
            } else if(!noTeleport.contains(player)) {
                if(portal.getWarp() != null) {
                    WarpSystem.getInstance().getTeleportManager().teleport(player, portal.getWarp(), portal.getDisplayName(), true, true, sendMessage);
                } else if(portal.getGlobalWarp() != null) {
                    WarpSystem.getInstance().getTeleportManager().teleport(player, portal.getGlobalWarp(), portal.getDisplayName(), 0, true, true, sendMessage);
                }
            }
        });
    }

    public void addPortal(Portal portal) {
        init(portal);
        this.portals.add(portal);
    }

    public List<Portal> getPortals() {
        return portals;
    }

    public static NativePortalManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.NATIVE_PORTALS);
    }

    public TimeList<String> getGoingToDelete() {
        return goingToDelete;
    }

    public void setGoingToDelete(Player player, int time) {
        if(time == 0) {
            this.goingToDelete.remove(player.getName());
        } else {
            if(this.goingToDelete.contains(player.getName())) this.goingToDelete.setExpire(player.getName(), time);
            else this.goingToDelete.add(player.getName(), time);
        }
    }

    public TimeList<String> getGoingToEdit() {
        return goingToEdit;
    }

    public void setGoingToEdit(Player player, int time) {
        if(time == 0) {
            this.goingToEdit.remove(player.getName());
        } else {
            if(this.goingToEdit.contains(player.getName())) this.goingToEdit.setExpire(player.getName(), time);
            else this.goingToEdit.add(player.getName(), time);
        }
    }

    public List<Player> getNoTeleport() {
        return noTeleport;
    }

    public boolean isSendMessage() {
        return sendMessage;
    }
}
