package shit.zen.utils.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import shit.zen.utils.game.ItemUtil;
import shit.zen.utils.misc.ChatUtil;

public class ItemAlertTracker {
    private static final Map<UUID, Integer> trackedItems;
    private static final Map<UUID, Set<Item>> alertedItems;
    private static final ConcurrentHashMap<Object, Set<ItemStack>> entityItems;
    private static final String ALERT_FORMAT;

    public static boolean isNewItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return ItemUtil.isOtherCheat(itemStack) || ItemUtil.isEnchantedGoldenApple(itemStack) || ItemUtil.isEndCrystal(itemStack) || ItemUtil.isKBSlimeBall(itemStack) || ItemUtil.isKBStick(itemStack) || ItemUtil.getPunchLevel(itemStack) > 2 && itemStack.getItem() instanceof BowItem || ItemUtil.getPowerLevel(itemStack) > 3 && itemStack.getItem() instanceof BowItem;
    }

    public static void trackPlayerItem(Player player, ItemStack itemStack) {
        if (!ItemUtil.isOtherCheat(itemStack)) {
            return;
        }
        int n = itemStack.getDamageValue();
        UUID uUID2 = player.getUUID();
        Integer n2 = trackedItems.get(uUID2);
        if (n2 != null && n > n2) {
            ChatUtil.print(String.format(ALERT_FORMAT, new Object[]{player.getName().getString()}));
            alertedItems.computeIfAbsent(uUID2, uUID -> new HashSet<>()).add(itemStack.getItem());
        }
        trackedItems.put(uUID2, n);
    }

    public static void trackEntityItem(Object object2, ItemStack itemStack) {
        if (!ItemAlertTracker.isNewItem(itemStack)) {
            return;
        }
        Set<ItemStack> set = entityItems.computeIfAbsent(object2, object -> new CopyOnWriteArraySet<>());
        if (set.stream().noneMatch(itemStack2 -> ItemStack.matches(itemStack2, itemStack))) {
            set.add(itemStack);
        }
    }

    public static Set<ItemStack> getEntityItems(Object object) {
        return entityItems.getOrDefault(object, Collections.emptySet());
    }

    public static boolean hasItem(UUID uUID, Item item) {
        Set<Item> set = alertedItems.getOrDefault(uUID, Collections.emptySet());
        return set.contains(item);
    }

    public static void clear() {
        trackedItems.clear();
        alertedItems.clear();
        entityItems.clear();
    }

    public static void removeEntity(Object object) {
        entityItems.remove(object);
    }

    public static void updateItems(Set<?> set) {
        entityItems.keySet().removeIf(object -> !set.contains(object));
    }

    public static Integer getItemCount(UUID uUID) {
        return trackedItems.get(uUID);
    }

    public static void setItemCount(UUID uUID, int n) {
        trackedItems.put(uUID, n);
    }

    static {
        ALERT_FORMAT = "§c[ALERT] §f%s used a God Axe!";
        trackedItems = new ConcurrentHashMap<>();
        alertedItems = new ConcurrentHashMap<>();
        entityItems = new ConcurrentHashMap<>();
    }
}