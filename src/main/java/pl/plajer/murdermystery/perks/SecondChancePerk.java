package pl.plajer.murdermystery.perks;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import pl.plajer.murdermystery.MurderMystery;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajer.murdermystery.utils.effects.particle.ParticlePlayer;
import pl.plajer.murdermystery.utils.effects.particle.effect.SpiralEffect;
import pl.plajer.murdermystery.utils.items.ItemBuilder;

import java.util.Random;

public class SecondChancePerk extends Perk {

    private boolean success;

    protected SecondChancePerk() {
        super(
                "§cВторой шанс",
                500.0,
                new ItemBuilder(Material.BED)
                        .name("&cВторой шанс")
                        .lore("&eС шансом 25% Вы можете получить второй шанс после смерти")
                        .lore("&7(работает только на удары мечём)")
                        .lore("&eЦена: &c500.0&e монет")
                        .build()
        );
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public void handle(final Player player, final Player target, final Arena arena) {
        val random = Utils.getRandomNumber(0, 100);
        if (random < 25) {
            val effect = new SpiralEffect(MurderMystery.getInstance().getScheduledExecutorService(),
                    player.getLocation(),
                    new ParticlePlayer(Particle.CRIT_MAGIC),
                    3,
                    2,
                    10,
                    10,
                    1,
                    true,
                    3
            ).play();
            Bukkit.getScheduler().runTaskLater(MurderMystery.getInstance(), effect::stop, 20);
            player.teleport(arena.getPlayerSpawnPoints().get(new Random().nextInt(arena.getPlayerSpawnPoints().size())));
            player.sendMessage("§cВот же повезло удрать!");
            success = true;
        } else {
            success = false;
        }
    }
}
