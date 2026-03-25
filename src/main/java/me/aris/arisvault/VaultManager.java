package me.aris.arisvault;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;

public class VaultManager {
    private final ArisVaults plugin;
    public VaultManager(ArisVaults plugin) { this.plugin = plugin; }

    public void save(Player p, int n, Inventory inv) {
        ItemStack[] contents = inv.getContents();
        String base64 = SerializationUtils.toBase64(contents);
        Bukkit.getAsyncScheduler().runNow(plugin, (t) -> {
            File folder = new File(plugin.getDataFolder(), "data");
            if (!folder.exists()) folder.mkdirs();
            File f = new File(folder, p.getUniqueId() + ".yml");
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            c.set("v." + n, base64);
            try { c.save(f); } catch (IOException ignored) {}
        });
    }

    public Inventory get(Player p, int n) {
        int r = plugin.getConfig().getInt("settings.vault.rows") * 9;
        String t = ColorUtils.color(plugin.getConfig().getString("settings.vault.title").replace("%number%", String.valueOf(n)));
        Inventory inv = Bukkit.createInventory(null, r, t);
        File f = new File(plugin.getDataFolder() + "/data", p.getUniqueId() + ".yml");
        if (f.exists()) {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            String d = c.getString("v." + n);
            if (d != null && !d.isEmpty()) {
                inv.setContents(SerializationUtils.fromBase64(d, r));
            }
        }
        return inv;
    }
            }
