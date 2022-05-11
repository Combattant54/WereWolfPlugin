package fr.ph1lou.werewolfplugin.roles.villagers;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.basekeys.RoleBase;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.basekeys.TimerBase;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.NightEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.registers.impl.RoleRegister;
import fr.ph1lou.werewolfapi.role.impl.RoleVillage;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import fr.ph1lou.werewolfplugin.RegisterManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Role(key = RoleBase.INTERPRETER,
        category = Category.VILLAGER, 
        attributes = {RoleAttribute.VILLAGER})
public class Interpreter extends RoleVillage implements IPower {

    private boolean power = false;
    private final Set<RoleRegister> roles = new HashSet<>();

    public Interpreter(WereWolfAPI game, IPlayerWW playerWW, String key) {
        super(game, playerWW, key);
        List<RoleRegister> roles = RegisterManager.get().getRolesRegister()
                .stream()
                .filter(roleRegister -> roleRegister.getAttributes().contains(RoleAttribute.VILLAGER))
                .filter(roleRegister -> !roleRegister.isRequireDouble())
                .filter(roleRegister -> !roleRegister.getKey().equals(RoleBase.INTERPRETER))
                .filter(roleRegister -> game.getConfig().getRoleCount(roleRegister.getKey()) == 0)
                .filter(roleRegister -> roleRegister.getAttributes().contains(RoleAttribute.INFORMATION))
                .collect(Collectors.toList());

        if(roles.size() == 0){
            return;
        }

        Collections.shuffle(roles, game.getRandom());

        this.roles.add(roles.get(0));

        roles = RegisterManager.get().getRolesRegister()
                .stream()
                .filter(roleRegister -> !this.roles.contains(roleRegister))
                .filter(roleRegister -> !roleRegister.isRequireDouble())
                .filter(roleRegister -> !roleRegister.getKey().equals(RoleBase.INTERPRETER))
                .filter(roleRegister -> roleRegister.getAttributes().contains(RoleAttribute.VILLAGER))
                .filter(roleRegister -> game.getConfig().getRoleCount(roleRegister.getKey()) == 0)
                .filter(roleRegister -> !roleRegister.getAttributes().contains(RoleAttribute.MINOR_INFORMATION))
                .filter(roleRegister -> !roleRegister.getAttributes().contains(RoleAttribute.INFORMATION))
                .collect(Collectors.toList());

        if(roles.size() == 0){
            return;
        }

        Collections.shuffle(roles, game.getRandom());

        this.roles.add(roles.get(0));

        roles = RegisterManager.get().getRolesRegister()
                .stream()
                .filter(roleRegister -> !this.roles.contains(roleRegister))
                .filter(roleRegister -> !roleRegister.isRequireDouble())
                .filter(roleRegister -> !roleRegister.getKey().equals(RoleBase.INTERPRETER))
                .filter(roleRegister -> roleRegister.getAttributes().contains(RoleAttribute.VILLAGER))
                .filter(roleRegister -> game.getConfig().getRoleCount(roleRegister.getKey()) == 0)
                .filter(roleRegister -> !roleRegister.getAttributes().contains(RoleAttribute.INFORMATION))
                .collect(Collectors.toList());

        if(roles.size() == 0){
            return;
        }

        Collections.shuffle(roles, game.getRandom());

        this.roles.add(roles.get(0));
    }

    @Override
    public @NotNull String getDescription() {
        return new DescriptionBuilder(game, this)
                .setDescription(game.translate("werewolf.role.interpreter.description"))
                .setPower(game.translate("werewolf.role.interpreter.power",
                        Formatter.format("&roles&",
                                this.roles.stream().map(roleRegister -> game.translate(roleRegister.getKey()))
                                        .collect(Collectors.joining(", ")))))
                .build();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNight(NightEvent nightEvent){

        if(!this.getPlayerWW().getRole().equals(this)){
            return;
        }

        BukkitUtils.scheduleSyncDelayedTask(() -> {
            if(!this.isAbilityEnabled()){
                return;
            }

            if(!this.getPlayerWW().isState(StatePlayer.ALIVE)){
                return;
            }

            this.setPower(true);

            this.roles.forEach(roleRegister -> this.getPlayerWW()
                    .sendMessage(changeRole(roleRegister.getKey())));
        }, game.getConfig().getTimerValue(TimerBase.DAY_DURATION) * 20L * 4 / 5L);

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(FinalDeathEvent event){

        if(!event.getPlayerWW().equals(this.getPlayerWW())){
            return;
        }

        if(this.getPlayerWW().getRole().equals(this)){
            return;
        }

        HandlerList.unregisterAll(this.getPlayerWW().getRole());
        this.getPlayerWW().setRole(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDay(DayEvent event){

        if(!this.getPlayerWW().getRole().equals(this)){
            return;
        }

        if(!this.isAbilityEnabled()){
            return;
        }

        if(!this.getPlayerWW().isState(StatePlayer.ALIVE)){
            return;
        }

        if(!this.hasPower()){
            return;
        }

        this.getPlayerWW().sendMessageWithKey(Prefix.RED, "werewolf.check.end_selection");

        this.setPower(false);

    }

    public boolean isRoleValid(String roleKey){
        return this.roles.stream().anyMatch(roleRegister -> roleRegister.getKey().equals(roleKey));
    }

    public void activateRole(String roleKey){

        this.roles.removeIf(roleRegister -> {
            if (roleRegister.getKey().equals(roleKey)) {

                try {
                    IRole role = (IRole) roleRegister.getConstructors().newInstance(game,
                            this.getPlayerWW(),
                            roleRegister.getKey());

                    role.recoverPotionEffects();

                    BukkitUtils.registerListener(role);

                    this.getPlayerWW().setRole(role);

                    Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(this.getPlayerWW()));

                    BukkitUtils.scheduleSyncDelayedTask(() -> {

                        if (!game.isState(StateGame.END) && !this.getPlayerWW().isState(StatePlayer.DEATH)) {
                            HandlerList.unregisterAll(role);
                            this.getPlayerWW().setRole(this);
                            Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(this.getPlayerWW()));
                            this.getPlayerWW().sendMessageWithKey(Prefix.GREEN,"werewolf.role.interpreter.end");
                        }
                    }, (long) (game.getConfig().getTimerValue(TimerBase.DAY_DURATION) * 20 * 1.6));

                    this.getPlayerWW().sendMessageWithKey(Prefix.YELLOW, "werewolf.role.interpreter.perform",
                            Formatter.role(game.translate(roleKey)));
                    return true;
                } catch (InstantiationException |
                        IllegalAccessException |
                        InvocationTargetException ignored) {
                }

            }

            return false;
        });
    }

    @Override
    public void recoverPower() {

    }

    private TextComponent changeRole(String roleKey) {
        TextComponent textComponent = new TextComponent(this.game.translate(Prefix.GREEN,"werewolf.role.interpreter.click",
                Formatter.role(game.translate(roleKey))));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ww %s %s",
                this.game.translate("werewolf.role.interpreter.command"), roleKey)));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(this.game.translate(roleKey)).create()));
        return textComponent;
    }

    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return this.power;
    }
}
