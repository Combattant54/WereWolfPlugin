package io.github.ph1lou.werewolfplugin.commands.roles;

import io.github.ph1lou.werewolfapi.Commands;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.enumlg.AngelForm;
import io.github.ph1lou.werewolfapi.events.AngelChoiceEvent;
import io.github.ph1lou.werewolfapi.rolesattributs.AngelRole;
import io.github.ph1lou.werewolfapi.rolesattributs.Roles;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandGuardianAngel implements Commands {


    private final Main main;

    public CommandGuardianAngel(Main main) {
        this.main = main;
    }

    @Override
    public void execute(Player player, String[] args) {

        GameManager game = main.getCurrentGame();
        UUID uuid = player.getUniqueId();
        PlayerWW plg = game.getPlayersWW().get(uuid);
        Roles angel = plg.getRole();

        if (!((AngelRole) angel).isChoice(AngelForm.ANGEL)) {
            player.sendMessage(game.translate("werewolf.check.power"));
            return;
        }

        Bukkit.getPluginManager().callEvent(new AngelChoiceEvent(uuid, AngelForm.GUARDIAN_ANGEL));
        ((AngelRole) angel).setChoice(AngelForm.GUARDIAN_ANGEL);
        player.sendMessage(game.translate("werewolf.role.angel.angel_choice_perform", game.translate("werewolf.role.guardian_angel.display")));
    }
}
