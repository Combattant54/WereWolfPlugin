package fr.ph1lou.werewolfplugin.roles.werewolfs;

import fr.minuskube.inv.ClickableItem;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.basekeys.RoleBase;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.day_cycle.NightEvent;
import fr.ph1lou.werewolfapi.events.roles.howling_werewolf.HowlEvent;
import fr.ph1lou.werewolfapi.role.impl.RoleWereWolf;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Role(key = RoleBase.HOWLING_WEREWOLF, category = Category.WEREWOLF,
        attributes = {RoleAttribute.WEREWOLF},
        intValues = {@IntValue(key = "werewolf.role.howling_werewolf.distance",
                defaultValue = 80, meetUpValue = 80, step = 2, item = UniversalMaterial.LIGHT_GRAY_WOOL)})
public class HowlingWerewolf extends RoleWereWolf {
    public HowlingWerewolf(WereWolfAPI game, IPlayerWW playerWW, String key) {
        super(game, playerWW, key);
    }

    @Override
    public @NotNull String getDescription() {
        return new DescriptionBuilder(game,this)
                .setDescription(game.translate("werewolf.role.howling_werewolf.description",
                        Formatter.number(game.getConfig().getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance"))))
                .setEffects(game.translate("werewolf.description.werewolf"))
                .build();
    }

    @Override
    public void recoverPower() {

    }

    @EventHandler
    public void onNight(NightEvent event){

        if(!this.getPlayerWW().isState(StatePlayer.ALIVE)){
            return;
        }

        Set<IPlayerWW> playerWWS = Bukkit.getOnlinePlayers()
                .stream().map(Entity::getUniqueId)
                .filter(uuid -> !this.getPlayerUUID().equals(uuid))
                .map(game::getPlayerWW)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(playerWW -> {
                    Location location = playerWW.getLocation();
                    Location playerLocation = this.getPlayerWW().getLocation();
                    return location.getWorld() == playerLocation.getWorld() &&
                            location.distance(playerLocation) < game.getConfig().getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance");
                })
                .collect(Collectors.toSet());

        if(playerWWS.size() < 5){
            return;
        }

        HowlEvent howlEvent = new HowlEvent(this.getPlayerWW(),playerWWS, (int) playerWWS
                .stream()
                .map(playerWW -> !playerWW.getRole().isWereWolf())
                .count());

        Bukkit.getPluginManager().callEvent(howlEvent);

        if(howlEvent.isCancelled()){
            this.getPlayerWW().sendMessageWithKey(Prefix.RED , "werewolf.check.cancel");
            return;
        }

        playerWWS.forEach(playerWW -> playerWW.sendSound(Sound.WOLF_HOWL));

        int heart=0;
        if(howlEvent.getNotWerewolfSize() > 2){
            if(howlEvent.getNotWerewolfSize() <= 3){
                heart = 2;
            }
            else if(howlEvent.getNotWerewolfSize() >= 6){
                heart = 3;
            }
        }

        if(heart == 0){
            return;
        }

        int finalHeart = heart*2;

        this.getPlayerWW().addPlayerMaxHealth(finalHeart);

        this.getPlayerWW().sendMessageWithKey(Prefix.YELLOW,"werewolf.role.howling_werewolf.message",
                Formatter.number(howlEvent.getNotWerewolfSize()),
                Formatter.format("&heart&",heart));

        BukkitUtils.scheduleSyncDelayedTask(() ->  {
            if(game.isState(StateGame.GAME)){
                this.getPlayerWW().removePlayerMaxHealth(finalHeart);
            }
        });
    }

    public static ClickableItem config(WereWolfAPI game) {
        List<String> lore = Arrays.asList(game.translate("werewolf.menu.left"),
                game.translate("werewolf.menu.right"));
        IConfiguration config = game.getConfig();

        return ClickableItem.of((
                new ItemBuilder(UniversalMaterial.LIGHT_GRAY_WOOL.getStack())
                        .setDisplayName(game.translate("werewolf.role.howling_werewolf.distance",
                                Formatter.number(config.getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance"))))
                        .setLore(lore).build()), e -> {

            if (e.isLeftClick()) {
                config.setValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance",
                        config.getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance") + 2);
            } else if (config.getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance") - 2 > 0) {
                config.setValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance",
                        config.getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance") - 2);
            }


            e.setCurrentItem(new ItemBuilder(e.getCurrentItem())
                    .setLore(lore)
                    .setDisplayName(game.translate("werewolf.role.howling_werewolf.distance",
                            Formatter.number(config.getValue(RoleBase.HOWLING_WEREWOLF, "werewolf.role.howling_werewolf.distance"))))
                    .build());

        });
    }
}
