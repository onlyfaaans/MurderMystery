package pl.plajer.murdermystery.perk.perks;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import pl.plajer.murdermystery.MurderMystery;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.perk.Perk;
import pl.plajer.murdermystery.perk.PerkAnn;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajer.murdermystery.utils.items.ItemPosition;

@PerkAnn
public class ExtremeGoldPerk extends Perk {

    public ExtremeGoldPerk() {
        super(
                "&eЗолотая лихорадка",
                500.0,
                Material.GOLD_INGOT,
                "&eУ Вас есть небольшой шанс",
                "&eполучить дополнительный слиток золота",
                "&7(есть побочные эффекты)"

        );
    }

    @Override
    public void handle(@NonNull final @NotNull Player player, Player target, @NonNull final @NotNull Arena arena) {
        val random = Utils.getRandomNumber(0, 100);
        if (random < 30) {
            ItemPosition.addItem(player, ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT));
            MurderMystery.getInstance().getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_GOLD, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 0));
        }
    }
}
