package me.aris.arisvault;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArisVaults extends JavaPlugin implements Listener, CommandExecutor {
    private VaultManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        new File(getDataFolder(), "data").mkdirs();
        this.manager = new VaultManager(this);
        getCommand("pv").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;
        if (a.length == 0) { openMenu(p); return true; }
        try {
            int n = Integer.parseInt(a[0]);
            if (n >= 1 && n <= 20) openVault(p, n);
        } catch (NumberFormatException ignored) {}
        return true;
    }

    public void openMenu(Player p) {
        int r = getConfig().getInt("settings.menu.rows") * 9;
        Inventory i = Bukkit.createInventory(null, r, ColorUtils.color(getConfig().getString("settings.menu.title")));
        Material m = Material.valueOf(getConfig().getString("settings.menu.display-material"));
        for (int v = 1; v <= 20; v++) {
            if (p.hasPermission("arisvaults.vaults." + v)) {
                ItemStack it = new ItemStack(m);
                ItemMeta mt = it.getItemMeta();
                mt.setDisplayName(ColorUtils.color(getConfig().getString("settings.menu.item-name").replace("%number%", String.valueOf(v))));
                List<String> lr = new ArrayList<>();
                for (String st : getConfig().getStringList("settings.menu.item-lore")) lr.add(ColorUtils.color(st));
                mt.setLore(lr);
                it.setItemMeta(mt);
                i.addItem(it);
            }
        }
        p.openInventory(i);
        play(p, "menu-open");
    }

    public void openVault(Player p, int n) {
        if (!p.hasPermission("arisvaults.vaults." + n)) {
            sendNotification(p, getConfig().getString("messages.no-permission").replace("%number%", String.valueOf(n)));
            return;
        }
        p.openInventory(manager.get(p, n));
        play(p, "vault-open");
    }

    private void sendNotification(Player p, String msg) {
        String formatted = ColorUtils.color(msg);
        if (getConfig().getBoolean("messages.mode.chat")) p.sendMessage(formatted);
        if (getConfig().getBoolean("messages.mode.actionbar")) p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formatted));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        String menuTitle = ColorUtils.color(getConfig().getString("settings.menu.title"));
        String vaultPrefix = ColorUtils.color(getConfig().getString("settings.vault.title").split("%number%")[0]);

        if (title.equals(menuTitle)) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;
            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
            
            String rawName = net.md_5.bungee.api.ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
            try {
                int v = Integer.parseInt(rawName.replaceAll("[^0-9]", ""));
                p.getScheduler().run(this, (task) -> openVault(p, v), null);
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e) {
        String title = e.getView().getTitle();
        String vaultPrefix = ColorUtils.color(getConfig().getString("settings.vault.title").split("%number%")[0]);
        
        if (title.startsWith(vaultPrefix)) {
            try {
                int v = Integer.parseInt(title.replaceAll("[^0-9]", ""));
                manager.save((Player) e.getPlayer(), v, e.getInventory());
                play((Player) e.getPlayer(), "vault-close");
            } catch (Exception ignored) {}
        }
    }

    private void play(Player p, String k) {
        try {
            p.playSound(p.getLocation(), Sound.valueOf(getConfig().getString("settings.sounds." + k)), 1f, 1f);
        } catch (Exception ignored) {}
    }
    }
