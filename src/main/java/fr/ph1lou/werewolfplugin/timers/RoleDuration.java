package fr.ph1lou.werewolfplugin.timers;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.basekeys.ConfigBase;
import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.basekeys.RoleBase;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.basekeys.TimerBase;
import fr.ph1lou.werewolfapi.events.TrollEvent;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.timers.RepartitionEvent;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerManager;
import fr.ph1lou.werewolfapi.lovers.ILover;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.registers.impl.RoleRegister;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import fr.ph1lou.werewolfplugin.RegisterManager;
import fr.ph1lou.werewolfplugin.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Timer(key = TimerBase.ROLE_DURATION,
        defaultValue = 1200,
        meetUpValue = 1200,
        decrement = true,
        onZero = RepartitionEvent.class)
public class RoleDuration extends ListenerManager {


    public RoleDuration(GetWereWolfAPI main) {
        super(main);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRepartition(RepartitionEvent event) {

        GameManager game = (GameManager) this.getGame();

        game.setState(StateGame.GAME);

        List<IPlayerWW> playerWWS = new ArrayList<>(game.getPlayersWW());
        List<RoleRegister> config = new ArrayList<>();

        game.getConfig().setConfig(ConfigBase.CHAT, false);

        game.getConfig().setRole(RoleBase.VILLAGER,
                Math.max(0,
                        game.getConfig()
                                .getRoleCount(RoleBase.VILLAGER) +
                                playerWWS.size() -
                                game.getRoleInitialSize()));

        RegisterManager.get().getRolesRegister()
                .forEach(roleRegister -> {
                    for (int i = 0; i < game.getConfig().getRoleCount(roleRegister.getKey()); i++) {
                        config.add(roleRegister);
                    }
                });

        Collections.shuffle(playerWWS, game.getRandom());

        while (!playerWWS.isEmpty()) {
            IPlayerWW playerWW = playerWWS.remove(0);
            RoleRegister roleRegister = config.remove(0);
            try {
                IRole role = (IRole) roleRegister.getConstructors().newInstance(game,
                        playerWW,
                        roleRegister.getKey());
                BukkitUtils.registerListener(role);
                playerWW.setRole(role);
                Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(playerWW));
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
        game.getPlayersWW()
                .forEach(playerWW -> playerWW.getRole().roleAnnouncement());

        BukkitUtils.scheduleSyncDelayedTask(game::checkVictory);
    }



    @EventHandler(priority = EventPriority.MONITOR)
    public void onRepartitionFirst(RepartitionEvent event){
        if (this.getGame().getConfig().isTrollSV()) {
            Bukkit.getPluginManager().callEvent(new TrollEvent());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTroll(TrollEvent event) {

        GameManager game = (GameManager) this.getGame();

        game.setState(StateGame.GAME);
        game.getConfig().setConfig(ConfigBase.CHAT, false);

        RegisterManager.get().getRolesRegister()
                .forEach(roleRegister -> {

                    if (roleRegister.getKey().equals(game.getConfig().getTrollKey())) {

                        game.getPlayersWW()
                                .forEach(playerWW -> {
                                    try {
                                        IRole role = (IRole) roleRegister.getConstructors()
                                                .newInstance(game,
                                                        playerWW,
                                                        roleRegister.getKey());

                                        BukkitUtils.registerListener(role);

                                        playerWW.setRole(role);
                                    } catch (InstantiationException |
                                            InvocationTargetException |
                                            IllegalAccessException exception) {
                                        exception.printStackTrace();
                                    }
                                });
                    }
                });


        game.getPlayersWW()
                .forEach(playerWW -> playerWW.getRole().roleAnnouncement());

        BukkitUtils.scheduleSyncDelayedTask(() -> {

            if (!game.isState(StateGame.END)) {
                game.getPlayersWW()
                        .forEach(playerWW -> {
                            HandlerList.unregisterAll(playerWW.getRole());
                            Sound.PORTAL_TRIGGER.play(playerWW);
                            playerWW.clearPotionEffects();
                            playerWW.sendMessageWithKey(Prefix.RED , "werewolf.announcement.troll");
                            playerWW.addPlayerMaxHealth(20 - playerWW.getMaxHealth());
                        });
                game.getPlayersWW().forEach(IPlayerWW::clearLover);
                Iterator<? extends ILover> iterator = game.getLoversManager().getLovers().iterator();
                while (iterator.hasNext()){
                    HandlerList.unregisterAll(iterator.next());
                    iterator.remove();
                }

                if (game.getConfig().isConfigActive(ConfigBase.DOUBLE_TROLL)) {
                    Bukkit.getPluginManager().callEvent(new TrollEvent());
                    game.getConfig().switchConfigValue(ConfigBase.DOUBLE_TROLL);
                    game.setDebug(false);
                } else {
                    game.getConfig().setTrollSV(false);
                    Bukkit.getPluginManager().callEvent(new RepartitionEvent());
                }
            }

        }, 1800L);
    }

}
