package me.aris.arievault;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
public class SerializationUtils {
    public static String toBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(os);
            data.writeInt(items.length);
            for (ItemStack item : items) data.writeObject(item);
            data.close();
            return Base64Coder.encodeLines(os.toByteArray());
        } catch (Exception e) { return ""; }
    }
    public static ItemStack[] fromBase64(String data, int size) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataIn = new BukkitObjectInputStream(is);
            ItemStack[] items = new ItemStack[dataIn.readInt()];
            for (int i = 0; i < items.length; i++) items[i] = (ItemStack) dataIn.readObject();
            dataIn.close();
            return items;
        } catch (Exception e) { return new ItemStack[size]; }
    }
}
