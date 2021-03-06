package de.codingair.warpsystem.spigot.base.managers;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.player.MessageAPI;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.globalwarps.guis.affiliations.GlobalWarp;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.Warp;
import de.codingair.warpsystem.spigot.base.language.Example;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.Teleport;
import de.codingair.warpsystem.spigot.features.warps.guis.affiliations.utils.Action;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportManager {
    private List<Particle> particles = new ArrayList<>();
    private List<Teleport> teleports = new ArrayList<>();

    private boolean canMove = false;
    private int seconds = 5;
    private int particleId = 0;
    private double radius = 1.5;

    public TeleportManager() {
        particles.add(Particle.FIREWORKS_SPARK);
        particles.add(Particle.SUSPENDED_DEPTH);
        particles.add(Particle.CRIT);
        particles.add(Particle.CRIT_MAGIC);
        particles.add(Particle.SMOKE_NORMAL);
        particles.add(Particle.SMOKE_LARGE);
        particles.add(Particle.SPELL);
        particles.add(Particle.SPELL_INSTANT);
        particles.add(Particle.SPELL_MOB);
        particles.add(Particle.SPELL_WITCH);
        particles.add(Particle.DRIP_WATER);
        particles.add(Particle.DRIP_LAVA);
        particles.add(Particle.VILLAGER_ANGRY);
        particles.add(Particle.VILLAGER_HAPPY);
        particles.add(Particle.TOWN_AURA);
        particles.add(Particle.NOTE);
        particles.add(Particle.ENCHANTMENT_TABLE);
        particles.add(Particle.FLAME);
        particles.add(Particle.CLOUD);
        particles.add(Particle.REDSTONE);
        particles.add(Particle.SNOW_SHOVEL);
        particles.add(Particle.HEART);
        particles.add(Particle.DRAGON_BREATH);
        particles.add(Particle.END_ROD);
        particles.add(Particle.DAMAGE_INDICATOR);
    }

    /**
     * Have to be launched after the IconManager (see WarpSign.class - fromJSONString method - need warps and categories)
     */
    public boolean load() {
        boolean success = true;

        this.particleId = WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getInt("WarpSystem.Teleport.Animation", 17);
        this.seconds = WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getInt("WarpSystem.Teleport.Delay", 5);
        this.canMove = WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getBoolean("WarpSystem.Teleport.Allow_Move", false);

        return success;
    }

    public void save(boolean saver) {
        FileConfiguration config = WarpSystem.getInstance().getFileManager().getFile("Config").getConfig();

        config.set("WarpSystem.Teleport.Animation", this.particleId);
        config.set("WarpSystem.Teleport.Delay", this.seconds);
        config.set("WarpSystem.Teleport.Allow_Move", this.canMove);

        WarpSystem.getInstance().getFileManager().getFile("Config").saveConfig();
    }

    public boolean tryToTeleport(Player player, Warp warp) {
        if(warp == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS", new Example("ENG", "&cThis warp does not exist."), new Example("GER", "&cDieser Warp existiert nicht.")));
            return false;
        }

        if(warp.getCategory() != null && warp.getCategory().hasPermission() && !player.hasPermission(warp.getCategory().getPermission())) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Cannot_Use_Category", new Example("ENG", "&cYou are not allowed to open this category!"), new Example("GER", "&cSie dürfen diese Kategorie nicht öffnen!")));
            return false;
        }

        if(warp.hasPermission() && !player.hasPermission(warp.getPermission())) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Cannot_Use_Warp", new Example("ENG", "&cYou are not allowed to use this warp!"), new Example("GER", "&cSie dürfen diesen Warp nicht benutzen!")));
            return false;
        }

        warp.perform(player, false, Action.RUN_COMMAND);
        return true;
    }

    public boolean tryToTeleport(Player player, GlobalWarp warp) {
        if(warp == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS", new Example("ENG", "&cThis warp does not exist."), new Example("GER", "&cDieser Warp existiert nicht.")));
            return false;
        }

        if(warp.getCategory() != null && warp.getCategory().hasPermission() && !player.hasPermission(warp.getCategory().getPermission())) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Cannot_Use_Category", new Example("ENG", "&cYou are not allowed to open this category!"), new Example("GER", "&cSie dürfen diese Kategorie nicht öffnen!")));
            return false;
        }

        if(warp.hasPermission() && !player.hasPermission(warp.getPermission())) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Cannot_Use_Warp", new Example("ENG", "&cYou are not allowed to use this warp!"), new Example("GER", "&cSie dürfen diesen Warp nicht benutzen!")));
            return false;
        }

        warp.perform(player, false, Action.RUN_COMMAND);
        return true;
    }

    public boolean tryToTeleport(Player player, String globalWarp, String displayName, double costs) {
        if(GlobalWarpManager.getInstance().exists(globalWarp)) {
            teleport(player, globalWarp, displayName, costs);
            return true;
        }

        player.sendMessage(Lang.getPrefix() + Lang.get("GlobalWarp_Does_Not_Exist", new Example("ENG", "&cThis GlobalWarp does not exist."), new Example("GER", "&cDieser GlobalWarp existiert nicht.")));
        return false;
    }

    public void teleport(Player player, Warp warp) {
        teleport(player, warp, false);
    }

    public void teleport(Player player, Warp warp, boolean skip) {
        teleport(player, warp, skip, this.canMove);
    }

    public void teleport(Player player, Warp warp, boolean skip, boolean canMove) {
        teleport(player, warp, null, skip, canMove);
    }

    public void teleport(Player player, Warp warp, String displayName, boolean skip, boolean canMove) {
        teleport(player, warp, displayName, skip, canMove, WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getBoolean("WarpSystem.Send.Teleport_Message.Warps", true));
    }

    public void teleport(Player player, Warp warp, String displayName, boolean skip, boolean canMove, boolean showMessage) {
        if(isTeleporting(player)) {
            Teleport teleport = getTeleport(player);
            long diff = System.currentTimeMillis() - teleport.getStartTime();
            if(diff > 50)
                player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Already_Teleporting", new Example("ENG", "&cYou are already teleporting!"), new Example("GER", "&cDu wirst bereits teleportiert!")));
            return;
        }

        player.closeInventory();

        Teleport teleport = new Teleport(player, warp, displayName);
        teleport.setCanMove(canMove);
        teleport.setShowMessage(showMessage);

        this.teleports.add(teleport);

        if(seconds == 0 || (WarpSystem.OP_CAN_SKIP_DELAY && player.hasPermission(WarpSystem.PERMISSION_ByPass_Teleport_Delay)) || skip) teleport.teleport();
        else teleport.start();
    }

    public void teleport(Player player, String globalWarp, String globalWarpDisplayName, double costs) {
        teleport(player, globalWarp, globalWarpDisplayName, costs, false);
    }


    public void teleport(Player player, String globalWarp, String globalWarpDisplayName, double costs, boolean skip) {
        teleport(player, globalWarp, globalWarpDisplayName, costs, skip, this.canMove);
    }

    public void teleport(Player player, String globalWarp, String globalWarpDisplayName, double costs, boolean skip, boolean canMove) {
        teleport(player, globalWarp, globalWarpDisplayName, costs, skip, canMove, WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getBoolean("WarpSystem.Send.Teleport_Message.GlobalWarps", true));
    }

    public void teleport(Player player, String globalWarp, String globalWarpDisplayName, double costs, boolean skip, boolean canMove, boolean showMessage) {
        if(isTeleporting(player)) {
            Teleport teleport = getTeleport(player);
            long diff = System.currentTimeMillis() - teleport.getStartTime();
            if(diff > 50)
                player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Already_Teleporting", new Example("ENG", "&cYou are already teleporting!"), new Example("GER", "&cDu wirst bereits teleportiert!")));
            return;
        }

        player.closeInventory();

        Teleport teleport = new Teleport(player, globalWarp, globalWarpDisplayName, costs);
        teleport.setCanMove(canMove);
        teleport.setShowMessage(showMessage);

        this.teleports.add(teleport);

        if(seconds == 0 || (WarpSystem.OP_CAN_SKIP_DELAY && player.hasPermission(WarpSystem.PERMISSION_ByPass_Teleport_Delay)) || skip) teleport.teleport();
        else teleport.start();
    }

    public void cancelTeleport(Player p) {
        if(!isTeleporting(p)) return;

        Teleport teleport = getTeleport(p);
        teleport.cancel(true, false);
        this.teleports.remove(teleport);

        if(WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getBoolean("WarpSystem.Send.Teleport_Cancel_Message", true)) {
            MessageAPI.sendActionBar(p, Lang.get("Teleport_Cancelled", new Example("ENG", "&cThe teleport was cancelled."), new Example("GER", "&cDer Teleport wurde abgebrochen.")));
        }
    }

    public Teleport getTeleport(Player p) {
        for(Teleport teleport : teleports) {
            if(teleport.getPlayer().getName().equalsIgnoreCase(p.getName())) return teleport;
        }

        return null;
    }

    public boolean isTeleporting(Player p) {
        return getTeleport(p) != null;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getParticleId() {
        return particleId;
    }

    public Particle getParticle() {
        return particles.get(particleId);
    }

    public void setParticleId(int particleId) {
        this.particleId = particleId;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<Teleport> getTeleports() {
        return teleports;
    }
}
