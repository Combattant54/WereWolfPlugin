package io.github.ph1lou.werewolfplugin.roles.neutrals;


import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.enums.Day;
import io.github.ph1lou.werewolfapi.enums.StatePlayer;
import io.github.ph1lou.werewolfapi.events.DayEvent;
import io.github.ph1lou.werewolfapi.events.EnchantmentEvent;
import io.github.ph1lou.werewolfapi.events.NightEvent;
import io.github.ph1lou.werewolfapi.rolesattributs.RolesNeutral;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Assassin extends RolesNeutral {

    public Assassin(GetWereWolfAPI main, PlayerWW playerWW, String key) {
        super(main, playerWW, key);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNight(NightEvent event) {

        Player player = Bukkit.getPlayer(getPlayerUUID());

        if (player == null) {
            return;
        }

        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }

    @EventHandler
    public void onDay(DayEvent event) {


        Player player = Bukkit.getPlayer(getPlayerUUID());

        if (!getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if (player == null) {
            return;
        }

        getPlayerWW().addPotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }

    @EventHandler
    public void onEnchantment(EnchantmentEvent event){

        if (!getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }

        if (!event.getPlayerWW().equals(getPlayerWW())) return;

        ItemStack item = event.getItem();

        if (event.getEnchants().containsKey(Enchantment.PROTECTION_ENVIRONMENTAL)) {

            if (item.getType().equals(Material.DIAMOND_BOOTS) ||
                    item.getType().equals(Material.DIAMOND_LEGGINGS) ||
                    item.getType().equals(Material.DIAMOND_HELMET) ||
                    item.getType().equals(Material.DIAMOND_CHESTPLATE)) {
                event.getFinalEnchants().put(Enchantment.PROTECTION_ENVIRONMENTAL,
                        Math.min(event.getEnchants().get(Enchantment.PROTECTION_ENVIRONMENTAL),
                                game.getConfig().getLimitProtectionDiamond() + 1));
            } else {
                event.getFinalEnchants().put(
                        Enchantment.PROTECTION_ENVIRONMENTAL,
                        Math.min(event.getEnchants()
                                        .get(Enchantment.PROTECTION_ENVIRONMENTAL),
                                game.getConfig().getLimitProtectionIron() + 1));
            }
        }
        if(event.getEnchants().containsKey(Enchantment.DAMAGE_ALL)){
            if (item.getType().equals(Material.DIAMOND_SWORD)) {
                event.getFinalEnchants().put(Enchantment.DAMAGE_ALL,
                        Math.min(event.getEnchants().get(Enchantment.DAMAGE_ALL),
                                game.getConfig().getLimitSharpnessDiamond() + 1));
            }
            else {
                event.getFinalEnchants().put(Enchantment.DAMAGE_ALL,
                        Math.min(event.getEnchants().get(Enchantment.DAMAGE_ALL),
                                game.getConfig().getLimitSharpnessIron() + 1));
            }
        }
        if(event.getEnchants().containsKey(Enchantment.ARROW_DAMAGE)) {
            event.getFinalEnchants().put(Enchantment.ARROW_DAMAGE,
                    Math.min(event.getEnchants().get(Enchantment.ARROW_DAMAGE),
                            game.getConfig().getLimitPowerBow() + 1));
        }
    }

    @Override
    public @NotNull String getDescription() {

        return super.getDescription() +
                game.translate("werewolf.description.equipment",
                        game.translate("werewolf.role.assassin.limit")) +
                game.translate("werewolf.description.item",
                        game.translate("werewolf.role.assassin.items")) +
                game.translate("werewolf.description.effect",
                        game.translate("werewolf.role.assassin.effect"));
    }


    @Override
    public void recoverPower() {

    }


    @Override
    public void recoverPotionEffect() {

        super.recoverPotionEffect();

        if (game.isDay(Day.NIGHT)) return;

        getPlayerWW().addPotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }
}
