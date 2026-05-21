package shit.zen.modules.impl.player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import shit.zen.event.impl.DisconnectEvent;
import shit.zen.event.impl.GameTickEvent;
import shit.zen.event.impl.MotionEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.combat.KillAura;
import shit.zen.modules.impl.movement.Scaffold;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.Timer;
import shit.zen.utils.game.BlockUtil;
import shit.zen.utils.game.ItemUtil;
import shit.zen.utils.misc.ReflectionUtil;
import shit.zen.event.EventTarget;

public class ChestStealer
extends Module {

    public record StealTarget(int slotIndex, ItemStack itemStack, int priority, double score) {
    }

    public static ChestStealer INSTANCE;
    private static final Timer actionTimer;
    private final NumberSetting clickDelaySetting = new NumberSetting("Delay", Integer.valueOf(200), Integer.valueOf(0), Integer.valueOf(1000), Integer.valueOf(10));
    private final NumberSetting openDelaySetting = new NumberSetting("Open Delay", Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(10), Integer.valueOf(1));
    private final BooleanSetting chestSetting = new BooleanSetting("Chest", true);
    private final BooleanSetting enderChestSetting = new BooleanSetting("Ender Chest", false);
    private final BooleanSetting furnaceSetting = new BooleanSetting("Furnace", true);
    private final BooleanSetting brewingStandSetting = new BooleanSetting("BrewingStand", true);
    private final BooleanSetting pickTrashSetting = new BooleanSetting("PickTrash", false);
    private final BooleanSetting onlyBestSetting = new BooleanSetting("Only Best", true);
    private final BooleanSetting randomClickSetting = new BooleanSetting("Random Click", false);
    private final BooleanSetting smartStealingSetting = new BooleanSetting("Smart Stealing", true);
    private static final Timer stealTimer;
    private static final Timer openTimer;
    private final Random random = new Random();
    private AbstractContainerMenu pendingMenu = null;
    private boolean hasPendingClick = false;
    private int totalBlockCount = 0;
    private int pendingSlot = -1;
    private int ticksSinceMenu = 0;
    private static long clickDelayMs;
    private int accessCount;
    private Screen lastScreen;
    private int openDelayTicks = 0;
    private final List<ChestStealer.StealTarget> stealTargetQueue = new ArrayList<>();
    private int stealIndex = 0;
    private boolean queueBuilt = false;

    public ChestStealer() {
        super("ChestStealer", Category.PLAYER);
        INSTANCE = this;
    }

    public static boolean isRateLimited() {
        return !stealTimer.hasPassed(100L) && !openTimer.hasPassed((int)clickDelayMs);
    }

    @Override
    public void onDisable() {
        this.resetAll();
    }

    @EventTarget
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        this.resetAll();
    }

    @EventTarget
    public void onGameTick(GameTickEvent gameTickEvent) {
        if (this.hasPendingClick && this.pendingMenu != null && this.pendingSlot >= 0) {
            ++this.ticksSinceMenu;
            if (this.ticksSinceMenu >= 1) {
                this.executePendingClick();
                this.resetState();
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent motionEvent) {
        if (mc == null || mc.player == null || mc.level == null || mc.gameMode == null
                || mc.getConnection() == null || KillAura.target != null || Scaffold.INSTANCE.isEnabled()) {
            return;
        }
        if (!openTimer.hasPassed((int) clickDelayMs)
                || !mc.player.isAlive() || mc.player.isDeadOrDying()
                || mc.player.isSpectator() || motionEvent.isPre()) {
            return;
        }
        Screen screen = mc.screen;
        AbstractContainerMenu abstractContainerMenu = mc.player.containerMenu;
        this.countBlocks();
        if (screen instanceof ContainerScreen containerScreen) {
            if (screen != this.lastScreen) {
                actionTimer.reset();
                this.openDelayTicks = 0;
                this.queueBuilt = false;
                this.stealTargetQueue.clear();
                this.stealIndex = 0;
            } else {
                ++this.openDelayTicks;
                if (this.openDelayTicks < this.openDelaySetting.getValue().intValue()) {
                    return;
                }
                String string = containerScreen.getTitle().getString();
                String string2 = Component.translatable("container.chest").getString();
                String string3 = Component.translatable("container.chestDouble").getString();
                String string4 = Component.translatable("container.enderchest").getString();
                ChestMenu chestMenu = containerScreen.getMenu();
                if (this.chestSetting.getValue() && (string.equals(string2) || string.equals(string3) || string.equals("Chest"))) {
                    if (this.shouldCloseChest(chestMenu)) {
                        this.stealFromChest(chestMenu);
                    }
                } else if (this.enderChestSetting.getValue() && string.equals(string4) && this.shouldCloseChest(chestMenu)) {
                    this.stealFromChest(chestMenu);
                }
            }
        } else {
            this.openDelayTicks = 0;
            this.queueBuilt = false;
            this.stealTargetQueue.clear();
            this.stealIndex = 0;
        }
        if (abstractContainerMenu instanceof FurnaceMenu furnaceMenu) {
            if (this.furnaceSetting.getValue()) {
                this.stealFromFurnace(furnaceMenu);
            }
        }
        if (abstractContainerMenu instanceof BrewingStandMenu brewingMenu) {
            if (this.brewingStandSetting.getValue()) {
                this.stealFromBrewing(brewingMenu);
            }
        }
        this.lastScreen = screen;
    }

    private boolean shouldCloseChest(ChestMenu chestMenu) {
        if (this.isChestDone(chestMenu) && stealTimer.hasPassed(100L)) {
            mc.player.closeContainer();
            return false;
        }
        return true;
    }

    private void stealFromChest(ChestMenu chestMenu) {
        ++this.accessCount;
        if (this.smartStealingSetting.getValue() && this.accessCount > 1) {
            this.stealSmartMode(chestMenu);
        } else {
            this.stealRandomMode(chestMenu);
        }
    }

    private void stealSmartMode(ChestMenu chestMenu) {
        if (!this.queueBuilt) {
            this.buildStealQueue(chestMenu);
            this.queueBuilt = true;
            this.stealIndex = 0;
        }
        if (this.stealIndex < this.stealTargetQueue.size()) {
            ChestStealer.StealTarget chestStealer$StealTarget = this.stealTargetQueue.get(this.stealIndex);
            if (!chestMenu.getSlot(chestStealer$StealTarget.slotIndex).getItem().isEmpty()) {
                this.schedulePendingClick(chestMenu, chestStealer$StealTarget.slotIndex);
                ++this.stealIndex;
            } else {
                ++this.stealIndex;
            }
        } else if (this.isChestComplete(chestMenu) && stealTimer.hasPassed(100L)) {
            mc.player.closeContainer();
        }
    }

    private void buildStealQueue(ChestMenu chestMenu) {
        ArrayList<ChestStealer.StealTarget> arrayList = new ArrayList<>();
        for (int i = 0; i < chestMenu.getRowCount() * 9; ++i) {
            ItemStack itemStack = chestMenu.getSlot(i).getItem();
            if (itemStack.isEmpty() || !this.shouldStealItem(itemStack)) continue;
            int n = this.getItemPriority(itemStack);
            double d = this.getItemScore(itemStack);
            arrayList.add(new ChestStealer.StealTarget(i, itemStack, n, d));
        }
        Map<String, List<ChestStealer.StealTarget>> map = this.categorizeItems(arrayList);
        this.stealTargetQueue.clear();
        List<String> categories = Arrays.asList("god", "helmet", "chestplate", "leggings", "boots", "sword", "bow", "crossbow", "golden_apple", "pickaxe", "axe", "shovel", "special", "utility", "other");
        for (String string : categories) {
            if (!map.containsKey(string)) continue;
            List<ChestStealer.StealTarget> list = map.get(string);
            boolean bl = string.equals("god") || string.equals("helmet") || string.equals("chestplate") || string.equals("leggings") || string.equals("boots") || string.equals("sword") || string.equals("bow") || string.equals("crossbow") || string.equals("pickaxe") || string.equals("axe") || string.equals("shovel");
            if (this.onlyBestSetting.getValue() && bl) {
                ChestStealer.StealTarget best = list.stream().max(Comparator.comparingDouble(t -> t.score)).orElse(null);
                if (best == null) continue;
                this.stealTargetQueue.add(best);
                continue;
            }
            list.sort((a, b) -> Double.compare(b.score, a.score));
            this.stealTargetQueue.addAll(list);
        }
    }

    private Map<String, List<ChestStealer.StealTarget>> categorizeItems(List<ChestStealer.StealTarget> list) {
        HashMap<String, List<ChestStealer.StealTarget>> hashMap = new HashMap<>();
        for (ChestStealer.StealTarget chestStealer$StealTarget : list) {
            String string2 = this.getItemCategory(chestStealer$StealTarget.itemStack);
            hashMap.computeIfAbsent(string2, string -> new ArrayList<>()).add(chestStealer$StealTarget);
        }
        return hashMap;
    }

    private String getItemCategory(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (ItemUtil.isWeaponItem(itemStack) || ItemUtil.isOtherCheat(itemStack)) {
            return "god";
        }
        if (item instanceof ArmorItem armorItem) {
            return switch (armorItem.getEquipmentSlot()) {
                case HEAD -> "helmet";
                case CHEST -> "chestplate";
                case LEGS -> "leggings";
                case FEET -> "boots";
                default -> "other";
            };
        }
        if (item instanceof SwordItem) {
            return "sword";
        }
        if (item instanceof BowItem) {
            return "bow";
        }
        if (item instanceof CrossbowItem) {
            return "crossbow";
        }
        if (item instanceof PickaxeItem) {
            return "pickaxe";
        }
        if (item instanceof AxeItem) {
            return "axe";
        }
        if (item instanceof ShovelItem) {
            return "shovel";
        }
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
            return "golden_apple";
        }
        if (item == Items.COMPASS || item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) {
            return "special";
        }
        if (item == Items.COBWEB) {
            return "utility";
        }
        if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG || item == Items.ARROW || item instanceof FishingRodItem || item instanceof BlockItem) {
            return "utility";
        }
        return "other";
    }

    private int getItemPriority(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (ItemUtil.isWeaponItem(itemStack) || ItemUtil.isOtherCheat(itemStack)) {
            return 150;
        }
        if (item instanceof ArmorItem armorItem) {
            return switch (armorItem.getEquipmentSlot()) {
                case HEAD -> 100;
                case CHEST -> 99;
                case LEGS -> 98;
                case FEET -> 97;
                default -> 50;
            };
        }
        if (item instanceof SwordItem) {
            return 95;
        }
        if (item instanceof BowItem) {
            return 93;
        }
        if (item instanceof CrossbowItem) {
            return 92;
        }
        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            return 91;
        }
        if (item == Items.GOLDEN_APPLE) {
            return 90;
        }
        if (item instanceof PickaxeItem) {
            return 89;
        }
        if (item instanceof AxeItem) {
            return 88;
        }
        if (item instanceof ShovelItem) {
            return 87;
        }
        if (item == Items.COMPASS) {
            return 85;
        }
        if (item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) {
            return 83;
        }
        if (item == Items.ENDER_PEARL) {
            return 80;
        }
        if (item == Items.ARROW) {
            return 75;
        }
        if (item == Items.COBWEB) {
            return 72;
        }
        if (item == Items.SNOWBALL || item == Items.EGG) {
            return 70;
        }
        if (item instanceof FishingRodItem) {
            return 65;
        }
        if (item instanceof BlockItem) {
            return 60;
        }
        return 50;
    }

    private double getItemScore(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (ItemUtil.isWeaponItem(itemStack) || ItemUtil.isOtherCheat(itemStack)) {
            return 10000.0;
        }
        if (item instanceof ArmorItem) {
            return ItemUtil.getArmorScore(itemStack);
        }
        if (item instanceof SwordItem) {
            return ItemUtil.getSwordDamage(itemStack);
        }
        if (item instanceof AxeItem && ItemUtil.isLegitAxe(itemStack)) {
            return ItemUtil.getAxeDamage(itemStack);
        }
        if (item instanceof DiggerItem) {
            return ItemUtil.getDigSpeed(itemStack);
        }
        if (item instanceof BowItem) {
            if (ItemUtil.isGoodBow(itemStack)) {
                return ItemUtil.getBowScore(itemStack);
            }
            if (ItemUtil.isGoodBowAlt(itemStack)) {
                return ItemUtil.getBowScoreAlt(itemStack);
            }
            return 1.0;
        }
        if (item instanceof CrossbowItem) {
            return ItemUtil.getCrossbowScore(itemStack);
        }
        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            return 50.0 + (double)itemStack.getCount();
        }
        if (item == Items.GOLDEN_APPLE) {
            return 30.0 + (double)itemStack.getCount();
        }
        if (item == Items.ENDER_PEARL) {
            return 10.0 + (double)itemStack.getCount();
        }
        if (item == Items.ARROW) {
            return 5.0 + (double)itemStack.getCount() * 0.1;
        }
        if (item == Items.COBWEB) {
            return 4.0 + (double)itemStack.getCount() * 0.1;
        }
        if (item == Items.SNOWBALL || item == Items.EGG) {
            return 3.0 + (double)itemStack.getCount() * 0.1;
        }
        if (item instanceof FishingRodItem) {
            return ItemUtil.getDigSpeed(itemStack);
        }
        if (item instanceof BlockItem) {
            return 2.0 + (double)itemStack.getCount() * 0.05;
        }
        return 1.0;
    }

    private void stealRandomMode(ChestMenu chestMenu) {
        List<Integer> list = this.getStealableChestSlots(chestMenu);
        if (this.randomClickSetting.getValue() && !list.isEmpty() && this.accessCount > 1) {
            int n = list.get(this.random.nextInt(list.size()));
            this.schedulePendingClick(chestMenu, n);
        } else {
            for (int i = 0; i < chestMenu.getRowCount() * 9; ++i) {
                ItemStack itemStack = chestMenu.getSlot(i).getItem();
                if (itemStack.isEmpty() || this.accessCount <= 1 || !this.tryStealSlot(chestMenu, i)) continue;
                return;
            }
        }
    }

    private void stealFromFurnace(FurnaceMenu furnaceMenu) {
        ++this.accessCount;
        try {
            Container container = this.getFurnaceContainer(furnaceMenu);
            if (container == null) {
                return;
            }
            if (this.isFurnaceDone(furnaceMenu) && stealTimer.hasPassed(100L)) {
                mc.player.closeContainer();
                return;
            }
            List<Integer> list = this.getStealableContainerSlots(container);
            if (this.randomClickSetting.getValue() && !list.isEmpty() && this.accessCount > 1) {
                int n = list.get(this.random.nextInt(list.size()));
                this.schedulePendingClick(furnaceMenu, n);
            } else {
                for (int i = 0; i < container.getContainerSize(); ++i) {
                    ItemStack itemStack = container.getItem(i);
                    if (itemStack.isEmpty() || this.accessCount <= 1 || !this.shouldStealItem(itemStack)) continue;
                    this.schedulePendingClick(furnaceMenu, i);
                    return;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void stealFromBrewing(BrewingStandMenu brewingStandMenu) {
        ++this.accessCount;
        Container container = ReflectionUtil.getBrewingStand(brewingStandMenu);
        if (container == null) {
            return;
        }
        if (this.isBrewingDone(brewingStandMenu) && stealTimer.hasPassed(100L)) {
            mc.player.closeContainer();
            return;
        }
        List<Integer> list = this.getStealableContainerSlots(container);
        if (this.randomClickSetting.getValue() && !list.isEmpty() && this.accessCount > 1) {
            int n = list.get(this.random.nextInt(list.size()));
            this.schedulePendingClick(brewingStandMenu, n);
        } else {
            for (int i = 0; i < container.getContainerSize(); ++i) {
                ItemStack itemStack = container.getItem(i);
                if (itemStack.isEmpty() || this.accessCount <= 1 || !this.shouldStealItem(itemStack)) continue;
                this.schedulePendingClick(brewingStandMenu, i);
                return;
            }
        }
    }

    private List<Integer> getStealableChestSlots(ChestMenu chestMenu) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < chestMenu.getRowCount() * 9; ++i) {
            ItemStack itemStack = chestMenu.getSlot(i).getItem();
            if (itemStack.isEmpty() || !ChestStealer.isWorthStealing(itemStack) && !this.pickTrashSetting.getValue() || !this.shouldStealItem(itemStack)) continue;
            arrayList.add(i);
        }
        return arrayList;
    }

    private List<Integer> getStealableContainerSlots(Container container) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty() || !this.shouldStealItem(itemStack)) continue;
            arrayList.add(i);
        }
        return arrayList;
    }

    private Container getFurnaceContainer(AbstractFurnaceMenu abstractFurnaceMenu) throws Exception {
        Field[] fieldArray;
        for (Field field : fieldArray = AbstractFurnaceMenu.class.getDeclaredFields()) {
            if (!Container.class.isAssignableFrom(field.getType())) continue;
            field.setAccessible(true);
            return (Container)field.get(abstractFurnaceMenu);
        }
        return null;
    }

    private void schedulePendingClick(AbstractContainerMenu abstractContainerMenu, int n) {
        if (!this.hasPendingClick) {
            this.pendingMenu = abstractContainerMenu;
            this.pendingSlot = n;
            this.hasPendingClick = true;
            this.ticksSinceMenu = 0;
        }
    }

    private void executePendingClick() {
        if (this.pendingMenu != null && this.pendingSlot >= 0) {
            clickDelayMs = this.clickDelaySetting.getValue().longValue();
            mc.gameMode.handleInventoryMouseClick(this.pendingMenu.containerId, this.pendingSlot, 0, ClickType.QUICK_MOVE, mc.player);
            openTimer.reset();
            stealTimer.reset();
            actionTimer.reset();
        }
    }

    private boolean tryStealSlot(ChestMenu chestMenu, int n) {
        ItemStack itemStack = chestMenu.getSlot(n).getItem();
        if ((ChestStealer.isWorthStealing(itemStack) || this.pickTrashSetting.getValue()) && this.shouldStealItem(itemStack)) {
            this.schedulePendingClick(chestMenu, n);
            return true;
        }
        return false;
    }

    private void resetAll() {
        this.resetState();
        this.openDelayTicks = 0;
    }

    private void resetState() {
        this.hasPendingClick = false;
        this.pendingSlot = -1;
        this.pendingMenu = null;
        this.ticksSinceMenu = 0;
    }

    private void countBlocks() {
        this.totalBlockCount = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem)) continue;
            this.totalBlockCount += itemStack.getCount();
        }
    }

    private boolean shouldStealItem(ItemStack itemStack) {
        int n;
        Item item = itemStack.getItem();
        if (item instanceof FishingRodItem && (n = ItemUtil.countItem(Items.FISHING_ROD)) > 0) {
            return false;
        }
        if (item instanceof BlockItem && item != Items.COBWEB) {
            n = InventoryManager.getMaxBlockSize();
            if (this.totalBlockCount + itemStack.getCount() > n) {
                return false;
            }
        }
        if (this.onlyBestSetting.getValue()) {
            if (ItemUtil.isWeaponItem(itemStack) || ItemUtil.isOtherCheat(itemStack)) {
                return true;
            }
            if (item instanceof SwordItem) {
                return this.isBetterThanCurrent(itemStack);
            }
            if (item instanceof DiggerItem) {
                return this.isBetterThanCurrent(itemStack);
            }
            if (item instanceof ArmorItem) {
                return this.isBetterThanCurrent(itemStack);
            }
            if (item instanceof BowItem) {
                return this.isBetterThanCurrent(itemStack);
            }
            if (item instanceof CrossbowItem) {
                return this.isBetterThanCurrent(itemStack);
            }
            if (!(item instanceof SwordItem || item instanceof DiggerItem || item instanceof ArmorItem || item instanceof BowItem || item instanceof CrossbowItem)) {
                return ChestStealer.isWorthStealing(itemStack) || this.pickTrashSetting.getValue() != false;
            }
        }
        return true;
    }

    private boolean isBetterThanCurrent(ItemStack itemStack) {
        if (itemStack.getItem() instanceof SwordItem) {
            float f;
            float f2 = ItemUtil.getSwordDamage(itemStack);
            return f2 > (f = ItemUtil.getBestSwordDamage());
        }
        if (itemStack.getItem() instanceof DiggerItem) {
            if (itemStack.getItem() instanceof PickaxeItem) {
                float f;
                float f3 = ItemUtil.getDigSpeed(itemStack);
                return f3 > (f = ItemUtil.getBestPickaxeScore());
            }
            if (itemStack.getItem() instanceof AxeItem) {
                float f;
                if (ItemUtil.isLegitAxe(itemStack)) {
                    float f4 = ItemUtil.getAxeDamage(itemStack);
                    ItemStack itemStack2 = ItemUtil.getBestSharpAxe();
                    float f5 = itemStack2 != null ? ItemUtil.getAxeDamage(itemStack2) : 0.0f;
                    return f4 > f5;
                }
                float f6 = ItemUtil.getDigSpeed(itemStack);
                return f6 > (f = ItemUtil.getBestAxeScore());
            }
            if (itemStack.getItem() instanceof ShovelItem) {
                float f;
                float f7 = ItemUtil.getDigSpeed(itemStack);
                return f7 > (f = ItemUtil.getBestShovelScore());
            }
        } else {
            Item item = itemStack.getItem();
            if (item instanceof ArmorItem armorItem) {
                float f;
                float f8 = ItemUtil.getArmorScore(itemStack);
                return f8 > (f = ItemUtil.getEquippedArmorScore(armorItem.getEquipmentSlot())) + 0.1f;
            }
            if (itemStack.getItem() instanceof BowItem) {
                if (ItemUtil.isGoodBow(itemStack)) {
                    float f;
                    float f9 = ItemUtil.getBowScore(itemStack);
                    return f9 > (f = ItemUtil.getBestBowScore());
                }
                if (ItemUtil.isGoodBowAlt(itemStack)) {
                    float f;
                    float f10 = ItemUtil.getBowScoreAlt(itemStack);
                    return f10 > (f = ItemUtil.getBestBowScoreAlt());
                }
            } else if (itemStack.getItem() instanceof CrossbowItem) {
                float f;
                float f11 = ItemUtil.getCrossbowScore(itemStack);
                return f11 > (f = ItemUtil.getBestCrossbowScore());
            }
        }
        return true;
    }

    private boolean isChestDone(ChestMenu chestMenu) {
        for (int i = 0; i < chestMenu.getRowCount() * 9; ++i) {
            ItemStack itemStack = chestMenu.getSlot(i).getItem();
            if (itemStack.isEmpty() || !ChestStealer.isWorthStealing(itemStack) && !this.pickTrashSetting.getValue() || !this.shouldStealItem(itemStack)) continue;
            return false;
        }
        return true;
    }

    private boolean isChestComplete(ChestMenu chestMenu) {
        for (int i = 0; i < chestMenu.getRowCount() * 9; ++i) {
            ItemStack itemStack = chestMenu.getSlot(i).getItem();
            if (itemStack.isEmpty() || !ChestStealer.isWorthStealing(itemStack) && !this.pickTrashSetting.getValue() || !this.shouldStealItem(itemStack)) continue;
            return false;
        }
        return true;
    }

    private boolean isFurnaceDone(FurnaceMenu furnaceMenu) {
        try {
            Container container = this.getFurnaceContainer(furnaceMenu);
            if (container == null) {
                return false;
            }
            for (int i = 0; i < container.getContainerSize(); ++i) {
                ItemStack itemStack = container.getItem(i);
                if (itemStack.isEmpty() || !this.shouldStealItem(itemStack)) continue;
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isBrewingDone(BrewingStandMenu brewingStandMenu) {
        Container container = ReflectionUtil.getBrewingStand(brewingStandMenu);
        if (container == null) {
            return true;
        }
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty() || !this.shouldStealItem(itemStack)) continue;
            return false;
        }
        return true;
    }

    public static boolean isWorthStealing(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        if (ItemUtil.isWeaponItem(itemStack) || ItemUtil.isOtherCheat(itemStack) || ItemUtil.isLegitAxe(itemStack)) {
            return true;
        }
        Item item = itemStack.getItem();
        if (item instanceof ArmorItem armorItem) {
            float f;
            float f2 = ItemUtil.getArmorScore(itemStack);
            return !(f2 <= (f = ItemUtil.getBestArmorScore(armorItem.getEquipmentSlot())));
        }
        if (itemStack.getItem() instanceof SwordItem) {
            float f;
            float f3 = ItemUtil.getSwordDamage(itemStack);
            return !(f3 <= (f = ItemUtil.getBestSwordDamage()));
        }
        if (itemStack.getItem() instanceof PickaxeItem) {
            float f;
            float f4 = ItemUtil.getDigSpeed(itemStack);
            return !(f4 <= (f = ItemUtil.getBestPickaxeScore()));
        }
        if (itemStack.getItem() instanceof AxeItem) {
            float f;
            float f5 = ItemUtil.getDigSpeed(itemStack);
            return !(f5 <= (f = ItemUtil.getBestAxeScore()));
        }
        if (itemStack.getItem() instanceof ShovelItem) {
            float f;
            float f6 = ItemUtil.getDigSpeed(itemStack);
            return !(f6 <= (f = ItemUtil.getBestShovelScore()));
        }
        if (itemStack.getItem() instanceof CrossbowItem) {
            float f;
            float f7 = ItemUtil.getCrossbowScore(itemStack);
            return !(f7 <= (f = ItemUtil.getBestCrossbowScore()));
        }
        if (itemStack.getItem() instanceof BowItem && ItemUtil.isGoodBow(itemStack)) {
            float f;
            float f8 = ItemUtil.getBowScore(itemStack);
            return !(f8 <= (f = ItemUtil.getBestBowScore()));
        }
        if (itemStack.getItem() instanceof BowItem && ItemUtil.isGoodBowAlt(itemStack)) {
            float f;
            float f9 = ItemUtil.getBowScoreAlt(itemStack);
            return !(f9 <= (f = ItemUtil.getBestBowScoreAlt()));
        }
        if (itemStack.getItem() == Items.GOLDEN_APPLE || itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return true;
        }
        if (itemStack.getItem() == Items.COBWEB) {
            return true;
        }
        if (itemStack.getItem() == Items.COMPASS) {
            return !ItemUtil.hasItem(itemStack.getItem());
        }
        if (itemStack.getItem() == Items.WATER_BUCKET && ItemUtil.countItem(Items.WATER_BUCKET) >= InventoryManager.getMaxWaterBuckets()) {
            return false;
        }
        if (itemStack.getItem() == Items.LAVA_BUCKET && ItemUtil.countItem(Items.LAVA_BUCKET) >= InventoryManager.getMaxLavaBuckets()) {
            return false;
        }
        if (itemStack.getItem() instanceof BlockItem && BlockUtil.isPlaceable(itemStack) && ItemUtil.countBlocks() + itemStack.getCount() >= InventoryManager.getMaxBlockSize()) {
            return false;
        }
        if (itemStack.getItem() == Items.ARROW && ItemUtil.countItem(Items.ARROW) + itemStack.getCount() >= InventoryManager.getMaxArrows()) {
            return false;
        }
        if (itemStack.getItem() instanceof FishingRodItem && ItemUtil.countItem(Items.FISHING_ROD) >= 1) {
            return false;
        }
        if ((itemStack.getItem() == Items.SNOWBALL || itemStack.getItem() == Items.EGG) && ItemUtil.countItem(Items.SNOWBALL) + ItemUtil.countItem(Items.EGG) + itemStack.getCount() >= InventoryManager.getMaxEggsSnowballsSize()) {
            return false;
        }
        if (itemStack.getItem() instanceof ItemNameBlockItem) {
            return false;
        }
        return ItemUtil.isUsableItem(itemStack);
    }

    static {
        actionTimer = new Timer();
        stealTimer = new Timer();
        openTimer = new Timer();
    }
}