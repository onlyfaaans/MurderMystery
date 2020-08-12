package pl.plajer.murdermystery.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.murdermystery.MurderMystery;
import pl.plajer.murdermystery.utils.config.ConfigUtils;

import java.util.ArrayList;
import java.util.List;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Rank {
    String name;
    int xp;

    @Getter
    private static FileConfiguration rankConfig = ConfigUtils.getConfig(JavaPlugin.getPlugin(MurderMystery.class), "ranks");
    @Getter
    private static List<Rank> ranks = new ArrayList<>();
}


