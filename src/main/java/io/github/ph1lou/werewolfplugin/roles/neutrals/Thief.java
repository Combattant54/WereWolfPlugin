package io.github.ph1lou.werewolfplugin.roles.neutrals;


import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.LoverAPI;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.enums.ConfigsBase;
import io.github.ph1lou.werewolfapi.enums.StateGame;
import io.github.ph1lou.werewolfapi.enums.StatePlayer;
import io.github.ph1lou.werewolfapi.events.DayEvent;
import io.github.ph1lou.werewolfapi.events.FirstDeathEvent;
import io.github.ph1lou.werewolfapi.events.NewWereWolfEvent;
import io.github.ph1lou.werewolfapi.events.NightEvent;
import io.github.ph1lou.werewolfapi.events.StealEvent;
import io.github.ph1lou.werewolfapi.rolesattributs.AffectedPlayers;
import io.github.ph1lou.werewolfapi.rolesattributs.Power;
import io.github.ph1lou.werewolfapi.rolesattributs.Roles;
import io.github.ph1lou.werewolfapi.rolesattributs.RolesNeutral;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Thief extends RolesNeutral implements AffectedPlayers, Power {

    private final List<PlayerWW> affectedPlayer = new ArrayList<>();

    public Thief(GetWereWolfAPI main, PlayerWW playerWW, String key) {
        super(main, playerWW, key);
    }

    private boolean power = true;

    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return (this.power);
    }

    @Override
    public void addAffectedPlayer(PlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    @Override
    public void removeAffectedPlayer(PlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    @Override
    public List<PlayerWW> getAffectedPlayers() {
        return (this.affectedPlayer);
    }


    @Override
    public @NotNull String getDescription() {
        return super.getDescription() +
                game.translate("werewolf.description.description",
                        (game.getConfig().isConfigActive(ConfigsBase.EVIL_THIEF.getKey()) ?
                                game.translate("werewolf.role.thief.description2") :
                                game.translate("werewolf.role.thief.description")));

    }


    @Override
    public void recoverPower() {

    }

    @Override
    public void recoverPotionEffect() {

        super.recoverPotionEffect();

        getPlayerWW().addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        if (!killer.getUniqueId().equals(getPlayerUUID())) return;

        killer.removePotionEffect(PotionEffectType.ABSORPTION);
        killer.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                1200,
                0,
                false,
                false));
        killer.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.ABSORPTION,
                        1200,
                        0,
                        false,
                        false));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFirstDeathEvent(FirstDeathEvent event){

        PlayerWW playerWW = event.getPlayerWW();

        if (playerWW.getLastKiller() == null) return;

        if (!playerWW.getLastKiller().equals(getPlayerWW())) return;

        if(!hasPower())return;

        event.setCancelled(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) main, () -> {
            if (!game.isState(StateGame.END)) {
                if (getPlayerWW().isState(StatePlayer.ALIVE)
                        && hasPower()) {
                    thiefRecoverRole(playerWW);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) main, () -> {
                        if (!game.isState(StateGame.END)) {
                            Bukkit.getPluginManager().callEvent(
                                    new FirstDeathEvent(playerWW));
                        }

                    }, 20L);
                }
            }

        },7*20);
    }


    public void thiefRecoverRole(PlayerWW playerWW) {

        Roles role = playerWW.getRole();

        setPower(false);
        getPlayerWW().setThief(true);
        HandlerList.unregisterAll((Listener) getPlayerWW().getRole());
        Roles roleClone = role.publicClone();
        getPlayerWW().setRole(roleClone);
        assert roleClone != null;
        Bukkit.getPluginManager().registerEvents((Listener) roleClone, (Plugin) main);
        if (this.getInfected()) {
            roleClone.setInfected();
        } else if (roleClone.isWereWolf()) {
            Bukkit.getPluginManager().callEvent(new NewWereWolfEvent(getPlayerWW()));
        }
        roleClone.setTransformedToNeutral(game.getConfig().isConfigActive(ConfigsBase.EVIL_THIEF.getKey()));

        getPlayerWW().sendMessage(game.translate("werewolf.role.thief.realized_theft",
                game.translate(role.getKey())));
        getPlayerWW().sendMessage(game.translate("werewolf.announcement.review_role"));

        getPlayerWW().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        Bukkit.getPluginManager().callEvent(new StealEvent(getPlayerWW(),
                playerWW,
                roleClone.getKey()));

        getPlayerWW().getRole().recoverPotionEffect();

        for (int i = 0; i < playerWW.getLovers().size(); i++) {
            LoverAPI loverAPI = playerWW.getLovers().get(i);
            if (loverAPI.swap(playerWW, getPlayerWW())) {
                getPlayerWW().addLover(loverAPI);
                playerWW.removeLover(loverAPI);
                i--;
            }
        }
        game.death(playerWW);
    }

    
    @EventHandler
    public void onDay(DayEvent event) {
        restoreResistance();
    }

    @EventHandler
    public void onNight(NightEvent event){
        restoreResistance();
    }


    public void restoreResistance() {

        if (!hasPower()) return;

        if (!getPlayerWW().isState(StatePlayer.ALIVE)) return;

        getPlayerWW().addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    }

}
