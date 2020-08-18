package pl.plajer.murdermystery.perks;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.plajer.murdermystery.MurderMystery;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajer.murdermystery.utils.effects.particle.ParticlePlayer;
import pl.plajer.murdermystery.utils.effects.particle.effect.SpiralEffect;
import pl.plajer.murdermystery.utils.items.ItemBuilder;

public class InvisibleHeadPerk extends Perk {


    protected InvisibleHeadPerk() {
        super(
                "§5Магическая шляпа",
                750.0,
                new ItemBuilder(Material.IRON_HELMET)
                        .name("&5Магическая шляпа")
                        .lore("&eС помощью этой способности Вы сможете")
                        .lore("&eс маленьким шансом получить невидимость в течение игры.")
                        .lore("&eЦена: &c750.0&e монет")
                        .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL)
                        .build()
        );
    }


    @Override
    public void handle(final Player player, final Player target, final Arena arena) {
        val random = Utils.getRandomNumber(0, 100);
        if (random < 3) {
            val effect = new SpiralEffect(MurderMystery.getInstance().getScheduledExecutorService(),
                    player.getLocation(),
                    new ParticlePlayer(Particle.FLAME),
                    3,
                    2,
                    10,
                    10,
                    1,
                    false,
                    3
            ).play();
            Bukkit.getScheduler().runTaskLater(MurderMystery.getInstance(), effect::stop, 20);

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 0, true, true));
        }
    }
}
