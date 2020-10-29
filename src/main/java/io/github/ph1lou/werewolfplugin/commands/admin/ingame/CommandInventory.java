package io.github.ph1lou.werewolfplugin.commands.admin.ingame;

import io.github.ph1lou.werewolfapi.Commands;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CommandInventory implements Commands {


    private final Main main;

    public CommandInventory(Main main) {
        this.main = main;
    }

    @Override
    public void execute(Player player, String[] args) {

        GameManager game = main.getCurrentGame();


        if (args.length != 1) {
            player.sendMessage(game.translate("werewolf.check.player_input"));
            return;
        }

        Player pInv = Bukkit.getPlayer(args[0]);

        if (pInv == null) {
            player.sendMessage(game.translate("werewolf.check.offline_player"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 45, args[0]);

        for (int i = 0; i < 40; i++) {
            inv.setItem(i, pInv.getInventory().getItem(i));
        }

        player.openInventory(inv);
    }
}
