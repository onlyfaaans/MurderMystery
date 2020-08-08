package pl.plajer.murdermystery.perks;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajer.murdermystery.utils.particle.ParticlePlayer;
import pl.plajer.murdermystery.utils.particle.effect.SpiralEffect;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;


public class SpeedPerk extends Perk {

    protected SpeedPerk() {
        super(
                "§bБеги, пока можешь",
                200.0,
                new ItemBuilder(Material.RABBIT_FOOT)
                        .name("§bБеги, пока можешь")
                        .lore("§eВы можете с некоторым шансом получить скорость")
                        .lore("§eЦена: §c200.0§e монет")
                        .build(),
                null);


    }

    @Override
    public void handle(Player player, Player target, Arena arena) {
        val random = Utils.getRandomNumber(0, 100);
        if (random < 5) {
            val effect = new SpiralEffect(Main.getInstance().getScheduledExecutorService(),
                    player.getLocation(),
                    new ParticlePlayer(Particle.FLAME),
                    20,
                    2,
                    2,
                    15,
                    0.75,
                    true,
                    5
            ).play();
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), effect::stop, 20);
        }
    }
}

