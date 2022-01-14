package fr.ph1lou.werewolfplugin.roles.villagers;

import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.registers.impl.RoleRegister;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.enums.Prefix;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.TimerBase;
import fr.ph1lou.werewolfapi.events.game.vote.VoteEndEvent;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.ILimitedUse;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.impl.RoleVillage;
import fr.ph1lou.werewolfapi.utils.Utils;
import fr.ph1lou.werewolfplugin.RegisterManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Citizen extends RoleVillage implements ILimitedUse, IAffectedPlayers, IPower {

    private int use = 0;
    private final List<IPlayerWW> affectedPlayer = new ArrayList<>();
    private boolean power = true;

    public Citizen(WereWolfAPI api, IPlayerWW playerWW, String key) {
        super(api, playerWW, key);
    }

    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return (this.power);
    }

    @Override
    public void addAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    @Override
    public void removeAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    @Override
    public List<IPlayerWW> getAffectedPlayers() {
        return (this.affectedPlayer);
    }

    @Override
    public int getUse() {
        return use;
    }

    @Override
    public void setUse(int use) {
        this.use = use;
    }

    @EventHandler
    public void onVoteEnd(VoteEndEvent event) {


        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }


        if (getUse() < 2) {
            this.getPlayerWW().sendMessage(seeVote());
        }

        if (hasPower()) {
            this.getPlayerWW().sendMessage(cancelVote());
        }
    }

    @Override
    public @NotNull String getDescription() {

        return new DescriptionBuilder(game, this)
                .setDescription(game.translate("werewolf.role.citizen.description"))
                .addExtraLines(game.translate("werewolf.role.citizen.description_extra"))
                .build();
    }

    @EventHandler
    public void onDay(DayEvent event){

        if(this.getPlayerWW().isState(StatePlayer.DEATH)){
            return;
        }

        List<String> roles = RegisterManager
                .get()
                .getRolesRegister()
                .stream()
                .map(RoleRegister::getKey)
                .collect(Collectors.toList());

        if(roles.size() < 3){
            return;
        }

        Collections.shuffle(roles, game.getRandom());

        int count = roles
                .subList(0,3)
                .stream()
                .mapToInt(s -> game.getConfig().getRoleCount(s))
                .sum();

        this.getPlayerWW().sendMessageWithKey(Prefix.ORANGE.getKey(),"werewolf.role.citizen.hide_composition",
                Formatter.format("&role1&", game.translate(roles.get(0))),
                Formatter.format("&role2&", game.translate(roles.get(1))),
                Formatter.format("&role3&", game.translate(roles.get(2))),
                Formatter.number(count));
    }


    @Override
    public void recoverPower() {

    }

    public TextComponent cancelVote() {

        TextComponent cancelVote = new TextComponent(
                game.translate("werewolf.role.citizen.click"));
        cancelVote.setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        String.format("/ww %s",
                                game.translate("werewolf.role.citizen.command_2"))));
        cancelVote.setHoverEvent(
                new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(game.translate("werewolf.role.citizen.cancel"))
                                .create()));


        TextComponent cancel =
                new TextComponent(game.translate(Prefix.YELLOW.getKey() , "werewolf.role.citizen.cancel_vote_message",
                        Formatter.number(hasPower() ? 1 : 0)));

        cancel.addExtra(cancelVote);

        cancel.addExtra(new TextComponent(game.translate("werewolf.role.citizen.time_left",
                Formatter.timer(Utils.conversion(
                        game.getConfig().getTimerValue(
                                TimerBase.VOTE_WAITING.getKey()))))));


        return cancel;

    }


    public TextComponent seeVote() {

        TextComponent seeVote = new TextComponent(
                game.translate("werewolf.role.citizen.click"));
        seeVote.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                String.format("/ww %s",
                        game.translate("werewolf.role.citizen.command_1"))));
        seeVote.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(game.translate("werewolf.role.citizen.see"))
                        .create()));


        TextComponent see = new TextComponent(
                game.translate(Prefix.YELLOW.getKey() , "werewolf.role.citizen.see_vote_message",
                        Formatter.number(2 - getUse())));
        see.addExtra(seeVote);


        see.addExtra(new TextComponent(game.translate("werewolf.role.citizen.time_left",
                Formatter.timer(Utils.conversion(
                        game.getConfig().getTimerValue(
                                TimerBase.VOTE_WAITING.getKey()))))));


        return see;

    }


}
