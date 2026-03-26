package me.aris.arisvault;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class VaultHolder implements InventoryHolder {
    private final int number;
    public VaultHolder(int number) { this.number = number; }
    public int getNumber() { return number; }
    @Override
    public Inventory getInventory() { return null; }
}
