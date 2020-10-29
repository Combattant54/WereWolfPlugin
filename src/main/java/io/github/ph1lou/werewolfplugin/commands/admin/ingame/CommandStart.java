package io.github.ph1lou.werewolfplugin.commands.admin.ingame;

import io.github.ph1lou.werewolfapi.Commands;
import io.github.ph1lou.werewolfapi.enumlg.StateLG;
import io.github.ph1lou.werewolfapi.events.StartEvent;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import io.github.ph1lou.werewolfplugin.save.FileUtils_;
import io.github.ph1lou.werewolfplugin.save.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.DecimalFormat;

public class CommandStart implements Commands {


    private final Main main;

    public CommandStart(Main main) {
        this.main = main;
    }

    @Override
    public void execute(Player player, String[] args) {

        GameManager game = main.getCurrentGame();

        if (!game.isState(StateLG.LOBBY)) {
            player.sendMessage(game.translate("werewolf.check.already_begin"));
            return;
        }
        if (game.getScore().getRole() - game.getScore().getPlayerSize() > 0) {
            player.sendMessage(game.translate("werewolf.commands.admin.start.too_much_role"));
            return;
        }

        if (game.getMapManager().getWft() == null) {
            player.sendMessage(game.translate("werewolf.commands.admin.generation.not_generated"));
            return;
        }

        if (game.getMapManager().getWft().getPercentageCompleted() < 100) {
            player.sendMessage(game.translate("werewolf.commands.admin.generation.not_finished", new DecimalFormat("0.0").format(game.getMapManager().getWft().getPercentageCompleted())));
            return;
        }


        World world = game.getMapManager().getWorld();
        WorldBorder wb = world.getWorldBorder();
        wb.setCenter(world.getSpawnLocation().getX(), world.getSpawnLocation().getZ());
        wb.setSize(game.getConfig().getBorderMax());
        wb.setWarningDistance((int) (wb.getSize() / 7));
        game.setState(StateLG.TRANSPORTATION);
        File file = new File(main.getDataFolder() + File.separator + "configs" + File.separator, "saveCurrent.json");
        FileUtils_.save(file, Serializer.serialize(game.getConfig()));
        game.getStuffs().save("saveCurrent");
        Bukkit.getPluginManager().callEvent(new StartEvent(game));
    }
}
