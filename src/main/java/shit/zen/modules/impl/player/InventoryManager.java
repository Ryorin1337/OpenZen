package shit.zen.modules.impl.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import org.apache.commons.lang3.tuple.Pair;
import shit.zen.event.impl.MotionEvent;
import shit.zen.event.impl.PacketEvent;
import shit.zen.event.impl.SprintEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.movement.GuiMove;
import shit.zen.modules.impl.movement.Scaffold;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.Timer;
import shit.zen.utils.game.BlockUtil;
import shit.zen.utils.game.ItemUtil;
import shit.zen.utils.game.MovementUtil;
import shit.zen.utils.misc.PacketUtil;
import shit.zen.event.EventTarget;

public class InventoryManager
extends Module {
    public static InventoryManager INSTANCE;
    private final NumberSetting actionDelaySetting = new NumberSetting("Delay", Integer.valueOf(200), Integer.valueOf(0), Integer.valueOf(500), Integer.valueOf(10));
    private final NumberSetting sprintDelayTicksSetting = new NumberSetting("Open Delay", Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(10), Integer.valueOf(1));
    private final NumberSetting dropDelaySetting = new NumberSetting("Drop Delay", Integer.valueOf(200), Integer.valueOf(0), Integer.valueOf(500), Integer.valueOf(10));
    private final BooleanSetting autoArmorSetting = new BooleanSetting("Auto Armor", true);
    private final BooleanSetting throwItemsSetting = new BooleanSetting("Throw Items", true);
    private final ModeSetting offhandItemSetting = new ModeSetting("Offhand Items", "Golden Apple", "Fishing Rod", "None").withDefault("Projectile");
    private final ModeSetting bowPrioritySetting = new ModeSetting("Bow Priority", "Crossbow", "Punch Bow").withDefault("Crossbow");
    private final BooleanSetting inventoryOnlySetting = new BooleanSetting("Inventory Only", true);
    private final BooleanSetting fastThrowSetting = new BooleanSetting("Fast Throw", false);
    private final NumberSetting maxEggsSnowballsSetting = new NumberSetting("Max Eggs & Snowballs Size", Integer.valueOf(64), Integer.valueOf(16), Integer.valueOf(256), Integer.valueOf(16));
    public final NumberSetting maxBlockSizeSetting = new NumberSetting("Max Block Size", Integer.valueOf(256), Integer.valueOf(64), Integer.valueOf(512), Integer.valueOf(64));
    private final NumberSetting maxFoodSizeSetting = new NumberSetting("Max Food Size", Integer.valueOf(128), Integer.valueOf(32), Integer.valueOf(256), Integer.valueOf(32));
    private final NumberSetting maxRodSizeSetting = new NumberSetting("Max Rod Size", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(16), Integer.valueOf(1));
    private final NumberSetting swordSlotSetting = new NumberSetting("Sword Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting blockSlotSetting = new NumberSetting("Block Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting axeSlotSetting = new NumberSetting("Axe Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting pickaxeSlotSetting = new NumberSetting("Pickaxe Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting bowSlotSetting = new NumberSetting("Bow Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting waterBucketSlotSetting = new NumberSetting("Water Bucket Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting pearlSlotSetting = new NumberSetting("Ender Pearl Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting goldenAppleSlotSetting = new NumberSetting("Golden Apple Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting eggsSnowballsSlotSetting = new NumberSetting("Eggs & Snowballs Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting slimeBallSlotSetting = new NumberSetting("Slime Ball Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private final NumberSetting crystalSlotSetting = new NumberSetting("Crystal Slot", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(9), Integer.valueOf(1));
    private static final Timer actionTimer;
    private boolean didInventoryAction = false;
    private boolean pendingOffhandPlace = false;
    private int idleTicks = 0;
    private int sprintWaitTicks = 0;
    public static boolean isPerformingAction;
    private boolean wasSprinting = false;
    private boolean skipNextTick = false;
    private boolean justClosedInventory = false;
    private final Queue<Packet<ServerGamePacketListener>> pendingPackets = new ConcurrentLinkedQueue<>();
    private int sprintDelayTicks = 0;

    public InventoryManager() {
        super("InventoryManager", Category.PLAYER, 66);
        INSTANCE = this;
    }

    @Override
    protected void onDisable() {
        this.sprintDelayTicks = 0;
        isPerformingAction = false;
        this.skipNextTick = false;
        this.justClosedInventory = false;
        this.wasSprinting = false;
        super.onDisable();
    }

    @EventTarget
    public void onSprint(SprintEvent sprintEvent) {
        if (!this.inventoryOnlySetting.getValue() && GuiMove.INSTANCE.isEnabled() && (!this.pendingPackets.isEmpty() || isPerformingAction) && mc.player != null) {
            mc.options.keySprint.setDown(false);
            mc.player.setSprinting(false);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        Packet<?> packet = packetEvent.getPacket();
        if (!packetEvent.isIncomingRaw()) return;
        if (mc.player == null) return;
        if (mc.getConnection() == null) return;
        if (packetEvent.getPacket() instanceof ServerboundContainerClosePacket) {
            this.didInventoryAction = false;
            this.sprintWaitTicks = 0;
        }
        if (!GuiMove.INSTANCE.isEnabled()) {
            if (!this.didInventoryAction) return;
            if (this.inventoryOnlySetting.getValue() != false) return;
            if (packetEvent.getPacket() instanceof ServerboundMovePlayerPacket) {
                if (!MovementUtil.isInputActive()) return;
                mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
                return;
            } else {
                if (!(packetEvent.getPacket() instanceof ServerboundUseItemOnPacket || packetEvent.getPacket() instanceof ServerboundUseItemPacket || packetEvent.getPacket() instanceof ServerboundInteractPacket)) {
                    if (!(packetEvent.getPacket() instanceof ServerboundPlayerActionPacket)) return;
                }
                mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
            }
        } else {
            Screen screen;
            AbstractContainerScreen abstractContainerScreen;
            ServerboundPlayerCommandPacket serverboundPlayerCommandPacket;
            boolean bl;
            boolean bl2 = bl = !this.pendingPackets.isEmpty();
            if (packet instanceof ServerboundPlayerCommandPacket) {
                serverboundPlayerCommandPacket = (ServerboundPlayerCommandPacket)packet;
                if (serverboundPlayerCommandPacket.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
                    this.wasSprinting = true;
                } else if (serverboundPlayerCommandPacket.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
                    this.wasSprinting = false;
                }
            }
            if (isPerformingAction && packet instanceof ServerboundPlayerInputPacket inputPacket) {
                if (mc.player.isSprinting()) {
                    mc.player.setSprinting(false);
                    packetEvent.setCancelled(true);
                    PacketUtil.sendQueued(new ServerboundPlayerInputPacket(inputPacket.getXxa(), inputPacket.getZza(), inputPacket.isJumping(), inputPacket.isShiftKeyDown()));
                    return;
                }
            }
            if ((screen = mc.screen) instanceof AbstractContainerScreen) {
                abstractContainerScreen = (AbstractContainerScreen)screen;
                if (abstractContainerScreen.getMenu().containerId != mc.player.inventoryMenu.containerId) {
                    return;
                }
            }
            boolean bl3 = false;
            boolean bl4 = bl3;
            if (bl4) return;
            if (!(packet instanceof ServerboundContainerClickPacket)) {
                if (!(packet instanceof ServerboundContainerClosePacket)) return;
            }
            packetEvent.setCancelled(true);
            this.pendingPackets.add((Packet<ServerGamePacketListener>) packet);
        }
    }

    @EventTarget
    public void onMotion(MotionEvent motionEvent) {
        if (motionEvent.isPost() && !this.inventoryOnlySetting.getValue() && GuiMove.INSTANCE.isEnabled() && mc.player != null) {
            boolean bl;
            boolean bl2 = bl = !this.pendingPackets.isEmpty();
            if (bl) {
                if (this.wasSprinting || mc.player.isSprinting()) {
                    this.skipNextTick = true;
                    this.sprintDelayTicks = 2 + this.sprintDelayTicksSetting.getValue().intValue();
                    return;
                }
                if (this.sprintDelayTicks > 0) {
                    --this.sprintDelayTicks;
                    return;
                }
                while (!this.pendingPackets.isEmpty()) {
                    PacketUtil.sendQueued((Packet<ServerGamePacketListener>) this.pendingPackets.poll());
                }
                PacketUtil.sendQueued(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
                boolean bl3 = mc.options.keyJump.isDown();
                boolean bl4 = mc.options.keyShift.isDown();
                this.justClosedInventory = true;
            } else {
                this.sprintDelayTicks = 0;
                if (this.justClosedInventory) {
                    boolean bl5 = mc.options.keyJump.isDown();
                    boolean bl6 = mc.options.keyShift.isDown();
                    this.justClosedInventory = false;
                }
            }
        }
    }

    private boolean validateSlotConfig() {
        ArrayList<Pair<Boolean, NumberSetting>> arrayList = new ArrayList<>();
        arrayList.add(Pair.of(this.swordSlotSetting.getValue().intValue() != 0, this.swordSlotSetting));
        arrayList.add(Pair.of(this.axeSlotSetting.getValue().intValue() != 0, this.axeSlotSetting));
        arrayList.add(Pair.of(this.pickaxeSlotSetting.getValue().intValue() != 0, this.pickaxeSlotSetting));
        arrayList.add(Pair.of(this.bowSlotSetting.getValue().intValue() != 0, this.bowSlotSetting));
        arrayList.add(Pair.of(this.waterBucketSlotSetting.getValue().intValue() != 0, this.waterBucketSlotSetting));
        arrayList.add(Pair.of(this.pearlSlotSetting.getValue().intValue() != 0, this.pearlSlotSetting));
        arrayList.add(Pair.of(this.slimeBallSlotSetting.getValue().intValue() != 0, this.slimeBallSlotSetting));
        arrayList.add(Pair.of(this.crystalSlotSetting.getValue().intValue() != 0, this.crystalSlotSetting));
        arrayList.add(Pair.of(this.eggsSnowballsSlotSetting.getValue().intValue() != 0, this.eggsSnowballsSlotSetting));
        if (!"Golden Apple".equals(this.offhandItemSetting.getValue())) {
            arrayList.add(Pair.of(this.goldenAppleSlotSetting.getValue().intValue() != 0, this.goldenAppleSlotSetting));
        }
        if (!"Block".equals(this.offhandItemSetting.getValue())) {
            arrayList.add(Pair.of(this.blockSlotSetting.getValue().intValue() != 0, this.blockSlotSetting));
        }
        HashSet<Integer> hashSet = new HashSet<>();
        for (Pair<Boolean, NumberSetting> pair : arrayList) {
            if (!pair.getKey()) continue;
            int n = pair.getValue().getValue().intValue() - 1;
            if (!hashSet.contains(n)) {
                hashSet.add(n);
                continue;
            }
            return false;
        }
        return true;
    }

    @EventTarget
    public void onMotionManage(MotionEvent motionEvent) {
        if (motionEvent.isPost() && mc.player != null && mc.getConnection() != null && mc.gameMode != null) {
            ContainerScreen containerScreen;
            if (!this.validateSlotConfig()) {
                isPerformingAction = false;
                this.setEnabled(false);
                this.skipNextTick = true;
                return;
            }
            if (ItemUtil.hasServerItem()) {
                isPerformingAction = false;
                this.skipNextTick = true;
                return;
            }
            this.idleTicks = MovementUtil.isInputActive() ? 0 : ++this.idleTicks;
            boolean bl = false;
            AbstractContainerMenu abstractContainerMenu = mc.player.containerMenu;
            Object object = mc.screen;
            if (object instanceof ContainerScreen) {
                containerScreen = (ContainerScreen)object;
                object = containerScreen.getTitle().getString();
                String string = Component.translatable("container.chest").getString();
                String string2 = Component.translatable("container.chestDouble").getString();
                String string3 = Component.translatable("container.enderchest").getString();
                ChestMenu chestMenu = containerScreen.getMenu();
                if (object.equals(string) || object.equals(string2) || object.equals("Chest")) {
                    bl = true;
                }
            }
            if (abstractContainerMenu instanceof FurnaceMenu || abstractContainerMenu instanceof BrewingStandMenu) {
                bl = true;
            }
            if (bl || ChestStealer.isRateLimited() || Scaffold.INSTANCE.isEnabled() || (this.inventoryOnlySetting.getValue() != false ? !(mc.screen instanceof InventoryScreen) : !GuiMove.INSTANCE.isEnabled() && this.idleTicks <= 1)) {
                this.pendingOffhandPlace = false;
                this.sprintWaitTicks = 0;
                isPerformingAction = false;
                this.skipNextTick = true;
                return;
            }
            object = mc.screen;
            if (object instanceof AbstractContainerScreen acs) {
                if (acs.getMenu().containerId != mc.player.inventoryMenu.containerId) {
                    return;
                }
            }
            if (this.inventoryOnlySetting.getValue() && mc.screen instanceof InventoryScreen) {
                ++this.sprintWaitTicks;
                if (this.sprintWaitTicks < this.sprintDelayTicksSetting.getValue().intValue()) {
                    return;
                }
            }
            if (this.performInventoryAction()) {
                isPerformingAction = true;
            } else {
                isPerformingAction = false;
                this.skipNextTick = true;
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    private boolean performInventoryAction() {
        Integer n;
        ItemStack itemStack;
        ItemStack itemStack2;
        ItemStack itemStack3;
        ItemStack itemStack4;
        ItemStack itemStack5;
        if (this.handleAutoArmor()) {
            return true;
        }
        if (this.pendingOffhandPlace && actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())) {
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, 45, 0, ClickType.PICKUP, mc.player);
            this.didInventoryAction = true;
            this.pendingOffhandPlace = false;
            actionTimer.reset();
        }
        String offhandPreference = this.offhandItemSetting.getValue();
        if ("Golden Apple".equals(offhandPreference)) {
            if (this.handleGoldenAppleOffhand()) {
                return true;
            }
        } else if ("Projectile".equals(offhandPreference)) {
            if (this.handleProjectileOffhand()) {
                return true;
            }
        } else if ("Fishing Rod".equals(offhandPreference)) {
            if (this.handleFishingRodOffhand()) {
                return true;
            }
        } else if ("Block".equals(offhandPreference)) {
            if (this.handleBlockOffhand()) {
                return true;
            }
        }
        if (!this.offhandItemSetting.getValue().equals("Golden Apple") && this.goldenAppleSlotSetting.getValue().intValue() != 0) {
            this.swapItemToSlot(this.goldenAppleSlotSetting.getValue().intValue() - 1, Items.GOLDEN_APPLE);
        }
        if (this.blockSlotSetting.getValue().intValue() != 0) {
            int n7 = this.blockSlotSetting.getValue().intValue() - 1;
            ItemStack itemStack11 = mc.player.getInventory().items.get(n7);
            ItemStack itemStack12 = ItemUtil.getBestBlock();
            if (!(itemStack12 == null || itemStack12.getCount() <= itemStack11.getCount() && BlockUtil.isPlaceable(itemStack11) || this.offhandItemSetting.getValue().equals("Block") || !this.swapToSlot(n7, itemStack12))) {
                return true;
            }
        }
        if (ItemUtil.countBlocks() > this.maxBlockSizeSetting.getValue().intValue() && this.throwItem(itemStack5 = ItemUtil.getWorstBlock())) {
            return true;
        }
        if (ItemUtil.countFood() > this.maxFoodSizeSetting.getValue().intValue() && this.throwItem(itemStack4 = ItemUtil.getBestFoodStack())) {
            return true;
        }
        if (ItemUtil.countFishingRods() > this.maxRodSizeSetting.getValue().intValue() && this.throwItem(itemStack3 = ItemUtil.getFishingRodStack())) {
            return true;
        }
        if (ItemUtil.countItem(Items.EGG) + ItemUtil.countItem(Items.SNOWBALL) > this.maxEggsSnowballsSetting.getValue().intValue() && this.throwItem(itemStack2 = ItemUtil.getWorstProjectile())) {
            return true;
        }
        if (this.swordSlotSetting.getValue().intValue() != 0) {
            ItemStack itemStack13 = ItemUtil.getBestSword();
            int n8 = this.swordSlotSetting.getValue().intValue() - 1;
            ItemStack itemStack14 = mc.player.getInventory().items.get(n8);
            ItemStack itemStack15 = ItemUtil.getBestSharpAxe();
            if (ItemUtil.getAxeDamage(itemStack15) > ItemUtil.getSwordDamage(itemStack13)) {
                itemStack13 = itemStack15;
            }
            if (itemStack13 != null) {
                float f;
                float f2 = itemStack14.getItem() instanceof SwordItem ? ItemUtil.getSwordDamage(itemStack14) : ItemUtil.getAxeDamage(itemStack14);
                float f3 = f = itemStack13.getItem() instanceof SwordItem ? ItemUtil.getSwordDamage(itemStack13) : ItemUtil.getAxeDamage(itemStack13);
                if (f > f2 && this.swapToSlot(n8, itemStack13)) {
                    return true;
                }
            }
        }
        if (this.pickaxeSlotSetting.getValue().intValue() != 0) {
            int n9 = this.pickaxeSlotSetting.getValue().intValue() - 1;
            ItemStack itemStack16 = ItemUtil.getBestPickaxe();
            ItemStack itemStack17 = mc.player.getInventory().items.get(n9);
            if (itemStack16 != null && itemStack16.getItem() instanceof PickaxeItem && (ItemUtil.getDigSpeed(itemStack16) > ItemUtil.getDigSpeed(itemStack17) || !(itemStack17.getItem() instanceof PickaxeItem)) && this.swapToSlot(n9, itemStack16)) {
                return true;
            }
        }
        if (this.bowSlotSetting.getValue().intValue() != 0) {
            ItemStack itemStack18;
            float f;
            float f4;
            ItemStack itemStack19;
            int n10 = this.bowSlotSetting.getValue().intValue() - 1;
            ItemStack itemStack20 = mc.player.getInventory().items.get(n10);
            if (this.bowPrioritySetting.getValue().equals("Crossbow")) {
                itemStack19 = ItemUtil.getBestCrossbow();
                f4 = ItemUtil.getCrossbowScore(itemStack19);
                f = ItemUtil.getCrossbowScore(itemStack20);
            } else if (this.bowPrioritySetting.getValue().equals("Power Bow")) {
                itemStack19 = ItemUtil.getBestBowAlt();
                f4 = ItemUtil.getBowScoreAlt(itemStack19);
                f = ItemUtil.getBowScoreAlt(itemStack20);
            } else {
                itemStack19 = ItemUtil.getBestBow();
                f4 = ItemUtil.getBowScore(itemStack19);
                f = ItemUtil.getBowScore(itemStack20);
            }
            if (itemStack19 == null) {
                itemStack19 = ItemUtil.getBestCrossbow();
                f4 = ItemUtil.getCrossbowScore(itemStack19);
                f = ItemUtil.getCrossbowScore(itemStack20);
            }
            if (itemStack19 == null) {
                itemStack19 = ItemUtil.getBestBowAlt();
                f4 = ItemUtil.getBowScoreAlt(itemStack19);
                f = ItemUtil.getBowScoreAlt(itemStack20);
            }
            if (itemStack19 == null) {
                itemStack19 = ItemUtil.getBestBow();
                f4 = ItemUtil.getBowScore(itemStack19);
                f = ItemUtil.getBowScore(itemStack20);
            }
            if (itemStack19 != null && f4 > f && this.swapToSlot(n10, itemStack19)) {
                return true;
            }
            if (ItemUtil.countItem(Items.ARROW) > 256 && this.throwItem(itemStack18 = ItemUtil.getArrowStack())) {
                return true;
            }
        }
        if (this.axeSlotSetting.getValue().intValue() != 0) {
            ItemStack itemStack21 = ItemUtil.getBestAxe();
            if (this.swapToSlot(this.axeSlotSetting.getValue().intValue() - 1, itemStack21)) {
                return true;
            }
        }
        if (this.eggsSnowballsSlotSetting.getValue().intValue() != 0 && this.swapToSlot(this.eggsSnowballsSlotSetting.getValue().intValue() - 1, ItemUtil.getBestProjectile())) {
            return true;
        }
        if (this.pearlSlotSetting.getValue().intValue() != 0 && this.swapItemToSlot(this.pearlSlotSetting.getValue().intValue() - 1, Items.ENDER_PEARL)) {
            return true;
        }
        if (this.waterBucketSlotSetting.getValue().intValue() != 0 && this.swapItemToSlot(this.waterBucketSlotSetting.getValue().intValue() - 1, Items.WATER_BUCKET)) {
            return true;
        }
        if (this.slimeBallSlotSetting.getValue().intValue() != 0 && this.swapItemToSlot(this.slimeBallSlotSetting.getValue().intValue() - 1, Items.SLIME_BALL)) {
            return true;
        }
        if (this.crystalSlotSetting.getValue().intValue() != 0 && this.swapItemToSlot(this.crystalSlotSetting.getValue().intValue() - 1, Items.END_CRYSTAL)) {
            return true;
        }
        List<Integer> list = IntStream.range(0, mc.player.getInventory().items.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(list);
        Iterator<Integer> iterator = list.iterator();
        do {
            if (iterator.hasNext()) continue;
            return false;
        } while ((itemStack = mc.player.getInventory().items.get((n = iterator.next()).intValue())).isEmpty() || this.isUsefulItem(itemStack));
        this.throwItem(itemStack);
        return true;
    }

    private boolean handleAutoArmor() {
        if (!this.autoArmorSetting.getValue()) {
            return false;
        }
        for (int slot = 0; slot < mc.player.getInventory().armor.size(); ++slot) {
            ItemStack armorStack = mc.player.getInventory().armor.get(slot);
            if (!(armorStack.getItem() instanceof ArmorItem armorItem)) continue;
            if (armorStack.isEmpty()
                    || !actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())
                    || !(ItemUtil.getBestArmorScore(armorItem.getEquipmentSlot()) > ItemUtil.getArmorScore(armorStack))) {
                continue;
            }
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, 4 + (4 - slot), 1, ClickType.THROW, mc.player);
            this.didInventoryAction = true;
            actionTimer.reset();
            return true;
        }
        for (int slot = 0; slot < mc.player.getInventory().items.size(); ++slot) {
            ItemStack candidate = mc.player.getInventory().items.get(slot);
            if (candidate.isEmpty() || !(candidate.getItem() instanceof ArmorItem armorItem)) continue;
            float candidateScore = ItemUtil.getArmorScore(candidate);
            boolean isBestArmor = ItemUtil.getBestArmorScore(armorItem.getEquipmentSlot()) == candidateScore;
            boolean betterThanEquipped = ItemUtil.getEquippedArmorScore(armorItem.getEquipmentSlot()) < candidateScore;
            if (!isBestArmor || !betterThanEquipped
                    || !actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())) {
                continue;
            }
            int targetSlot = slot < 9 ? slot + 36 : slot;
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, targetSlot, 0, ClickType.QUICK_MOVE, mc.player);
            this.didInventoryAction = true;
            actionTimer.reset();
            return true;
        }
        return false;
    }

    private boolean handleGoldenAppleOffhand() {
        ItemStack offhand = mc.player.getInventory().offhand.get(0);
        int slot = ItemUtil.getSlot(Items.GOLDEN_APPLE);
        if (slot == -1 || !actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())) {
            return false;
        }
        if (offhand.getItem() != Items.GOLDEN_APPLE) {
            this.moveToOffhand(slot);
            return true;
        }
        ItemStack invStack = mc.player.getInventory().items.get(slot);
        if (offhand.getCount() + invStack.getCount() > 64) {
            return false;
        }
        int targetSlot = slot < 9 ? slot + 36 : slot;
        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, targetSlot, 0, ClickType.PICKUP, mc.player);
        this.didInventoryAction = true;
        this.pendingOffhandPlace = true;
        actionTimer.reset();
        return true;
    }

    private boolean handleProjectileOffhand() {
        ItemStack offhand = mc.player.getInventory().offhand.get(0);
        ItemStack bestProjectile = ItemUtil.getBestProjectile();
        if (bestProjectile == null) {
            return false;
        }
        int slot = ItemUtil.getSlot(bestProjectile);
        boolean shouldSwap;
        if (offhand.getItem() == Items.EGG || offhand.getItem() == Items.SNOWBALL) {
            shouldSwap = offhand.getCount() < bestProjectile.getCount();
        } else {
            shouldSwap = true;
        }
        if (!shouldSwap || slot == -1 || !actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())) {
            return false;
        }
        this.moveToOffhand(slot);
        return true;
    }

    private boolean handleFishingRodOffhand() {
        ItemStack offhand = mc.player.getInventory().offhand.get(0);
        int slot = ItemUtil.getSlot(Items.FISHING_ROD);
        if (slot == -1 || !actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())
                || offhand.getItem() == Items.FISHING_ROD) {
            return false;
        }
        this.moveToOffhand(slot);
        return true;
    }

    private boolean handleBlockOffhand() {
        ItemStack offhand = mc.player.getInventory().offhand.get(0);
        ItemStack bestBlock = ItemUtil.getBestBlock();
        if (bestBlock == null) {
            return false;
        }
        int slot = ItemUtil.getSlot(bestBlock);
        boolean shouldSwap;
        if (BlockUtil.isPlaceable(offhand)) {
            shouldSwap = offhand.getCount() < bestBlock.getCount();
        } else {
            shouldSwap = true;
        }
        if (!shouldSwap || slot == -1 || !actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue())) {
            return false;
        }
        this.moveToOffhand(slot);
        return true;
    }

    private void moveToOffhand(int n) {
        if (mc.gameMode == null || mc.player == null) {
            return;
        }
        if (n < 9) {
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n + 36, 40, ClickType.SWAP, mc.player);
        } else {
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n, 40, ClickType.SWAP, mc.player);
        }
        this.didInventoryAction = true;
        actionTimer.reset();
    }

    private boolean throwItem(ItemStack itemStack) {
        int n;
        if (mc.gameMode == null || mc.player == null) {
            return false;
        }
        if (this.throwItemsSetting.getValue() && ItemUtil.isUsable(itemStack) && (actionTimer.hasPassed(this.dropDelaySetting.getValue().intValue()) || this.fastThrowSetting.getValue()) && (n = ItemUtil.getSlot(itemStack)) != -1) {
            if (n < 9) {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n + 36, 1, ClickType.THROW, mc.player);
            } else {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n, 1, ClickType.THROW, mc.player);
            }
            this.didInventoryAction = true;
            actionTimer.reset();
            return true;
        }
        return false;
    }

    private boolean swapToSlot(int n, ItemStack itemStack) {
        int n2;
        if (mc.gameMode == null || mc.player == null) {
            return false;
        }
        ItemStack itemStack2 = mc.player.getInventory().items.get(n);
        if (ItemUtil.isUsable(itemStack2) && itemStack != itemStack2 && actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue()) && (n2 = ItemUtil.getSlot(itemStack)) != -1) {
            if (n2 < 9) {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n2 + 36, n, ClickType.SWAP, mc.player);
            } else {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n2, n, ClickType.SWAP, mc.player);
            }
            this.didInventoryAction = true;
            actionTimer.reset();
            return true;
        }
        return false;
    }

    private boolean swapItemToSlot(int n, Item item) {
        int n2;
        if (mc.gameMode == null || mc.player == null) {
            return false;
        }
        ItemStack itemStack = mc.player.getInventory().items.get(n);
        if (ItemUtil.isUsable(itemStack) && actionTimer.hasPassed(this.actionDelaySetting.getValue().intValue()) && (n2 = ItemUtil.getSlot(item)) != -1) {
            ItemStack itemStack2 = mc.player.getInventory().items.get(n2);
            if (itemStack.getItem() != item || itemStack.getItem() == item && itemStack.getCount() < itemStack2.getCount()) {
                if (n2 < 9) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n2 + 36, n, ClickType.SWAP, mc.player);
                } else {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, n2, n, ClickType.SWAP, mc.player);
                }
                this.didInventoryAction = true;
                actionTimer.reset();
                return true;
            }
        }
        return false;
    }

    public static int getMaxBlockSize() {
        return InventoryManager.INSTANCE.maxBlockSizeSetting.getValue().intValue();
    }

    public static int getMaxEggsSnowballsSize() {
        return InventoryManager.INSTANCE.maxEggsSnowballsSetting.getValue().intValue();
    }

    public static int getMaxArrows() {
        return 256;
    }

    public static int getMaxWaterBuckets() {
        return 1;
    }

    public static int getMaxLavaBuckets() {
        return 1;
    }

    public boolean isUsefulItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        if (ItemUtil.isWeaponItem(itemStack)) {
            return true;
        }
        if (itemStack.getDisplayName().getString().contains("点击使用")) {
            return true;
        }
        if (itemStack.getItem() == Items.COBWEB) {
            return true;
        }
        Item item = itemStack.getItem();
        if (item instanceof ArmorItem armorItem) {
            float f = ItemUtil.getArmorScore(itemStack);
            if (ItemUtil.getEquippedArmorScore(armorItem.getEquipmentSlot()) >= f) {
                return false;
            }
            float f2 = ItemUtil.getBestArmorScore(armorItem.getEquipmentSlot());
            return !(f < f2);
        }
        if (itemStack.getItem() instanceof SwordItem) {
            return ItemUtil.getBestSword() == itemStack;
        }
        if (itemStack.getItem() instanceof PickaxeItem) {
            return ItemUtil.getBestPickaxe() == itemStack;
        }
        if (itemStack.getItem() instanceof AxeItem && !ItemUtil.isLegitAxe(itemStack)) {
            return ItemUtil.getBestAxe() == itemStack;
        }
        if (itemStack.getItem() instanceof ShovelItem) {
            return ItemUtil.getBestShovel() == itemStack;
        }
        if (itemStack.getItem() instanceof CrossbowItem) {
            return ItemUtil.getBestCrossbow() == itemStack;
        }
        if (itemStack.getItem() instanceof BowItem && ItemUtil.isGoodBow(itemStack)) {
            return ItemUtil.getBestBow() == itemStack;
        }
        if (itemStack.getItem() instanceof BowItem && ItemUtil.isGoodBowAlt(itemStack)) {
            return ItemUtil.getBestBowAlt() == itemStack;
        }
        if (itemStack.getItem() instanceof BowItem && ItemUtil.countItem(Items.BOW) > 1) {
            return false;
        }
        if (itemStack.getItem() == Items.WATER_BUCKET && ItemUtil.countItem(Items.WATER_BUCKET) > InventoryManager.getMaxWaterBuckets()) {
            return false;
        }
        if (itemStack.getItem() == Items.LAVA_BUCKET && ItemUtil.countItem(Items.LAVA_BUCKET) > InventoryManager.getMaxLavaBuckets()) {
            return false;
        }
        if (itemStack.getItem() instanceof FishingRodItem && ItemUtil.countItem(Items.FISHING_ROD) > 1) {
            return false;
        }
        if (itemStack.getItem() instanceof ItemNameBlockItem) {
            return false;
        }
        return ItemUtil.isUsableItem(itemStack);
    }

    static {
        actionTimer = new Timer();
        isPerformingAction = false;
    }
}