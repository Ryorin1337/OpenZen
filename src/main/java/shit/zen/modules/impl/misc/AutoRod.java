package shit.zen.modules.impl.misc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import shit.zen.event.impl.TickEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.player.MidPearl;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.game.PlayerUtil;
import shit.zen.event.EventTarget;

public class AutoRod
extends Module {
    public static AutoRod INSTANCE;
    private final ModeSetting mouseButton = new ModeSetting("Button", "Middle", "Mouse 4", "Mouse 5").withDefault("Mouse 4");
    private final NumberSetting delay = new NumberSetting("Delay", Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(20), Integer.valueOf(1));
    private boolean slotSwitched = false;
    private boolean isActive = false;
    private int previousSlot = -1;
    private int tickDelay = 0;

    public AutoRod() {
        super("AutoRod", Category.MISC);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.resetState();
    }

    @Override
    public void onDisable() {
        this.restoreSlot();
        this.resetState();
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null || mc.level == null) {
            this.resetState();
            return;
        }
        if (this.tickDelay > 0) {
            --this.tickDelay;
            if (this.tickDelay == 0) {
                this.restoreSlot();
                this.slotSwitched = false;
            }
            return;
        }
        boolean bl = this.isMouseButtonDown();
        if (!bl) {
            this.isActive = false;
            return;
        }
        if (this.isActive || this.isMidPearlBlocking()) {
            return;
        }
        this.isActive = true;
        int n = this.findUsableItemSlot();
        if (n == -1) {
            return;
        }
        this.previousSlot = mc.player.getInventory().selected;
        boolean bl2 = mc.player.getInventory().getItem(n).getItem() == Items.FISHING_ROD;
        this.slotSwitched = true;
        this.selectHotbarSlot(n);
        this.useItem();
        if (bl2) {
            this.tickDelay = this.delay.getValue().intValue();
        }
        if (!bl2 || this.tickDelay <= 0) {
            this.restoreSlot();
            this.slotSwitched = false;
        }
    }

    public boolean isCurrentMouseButton(int n) {
        return this.getMouseButtonCode() == n;
    }

    public boolean isActiveOrPending() {
        return this.slotSwitched || this.tickDelay > 0;
    }

    public boolean shouldInterceptButton(int n) {
        return this.isCurrentMouseButton(n) && (this.isActiveOrPending() || this.findUsableItemSlot() != -1);
    }

    private boolean isMidPearlBlocking() {
        MidPearl midPearl = MidPearl.INSTANCE;
        return midPearl != null && midPearl.isEnabled() && midPearl.isInterceptButton(this.getMouseButtonCode());
    }

    private boolean isMouseButtonDown() {
        return GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), this.getMouseButtonCode()) == 1;
    }

    private int getMouseButtonCode() {
        return switch ((String) this.mouseButton.getValue()) {
            case "Mouse 5" -> 4;
            case "Middle" -> 2;
            default -> 3;
        };
    }

    private int findUsableItemSlot() {
        int n = this.findItemInHotbar(Items.FISHING_ROD);
        return n != -1 ? n : this.findThrowableSlot();
    }

    private int findItemInHotbar(Item item) {
        if (mc.player == null) {
            return -1;
        }
        int n = Math.min(9, mc.player.getInventory().items.size());
        for (int i = 0; i < n; ++i) {
            if (mc.player.getInventory().getItem(i).getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private int findThrowableSlot() {
        if (mc.player == null) {
            return -1;
        }
        int n = Math.min(9, mc.player.getInventory().items.size());
        for (int i = 0; i < n; ++i) {
            if (!this.isThrowable(mc.player.getInventory().getItem(i).getItem())) continue;
            return i;
        }
        return -1;
    }

    private void selectHotbarSlot(int n) {
        mc.player.getInventory().selected = n;
        PlayerUtil.sendCarriedItem();
    }

    private void useItem() {
        if (mc.gameMode == null || !this.isUsableItem(mc.player.getMainHandItem().getItem())) {
            return;
        }
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private boolean isUsableItem(Item item) {
        return item == Items.FISHING_ROD || this.isThrowable(item);
    }

    private boolean isThrowable(Item item) {
        return item == Items.EGG || item == Items.SNOWBALL;
    }

    private void restoreSlot() {
        if (mc.player != null && this.previousSlot != -1) {
            mc.player.getInventory().selected = this.previousSlot;
            PlayerUtil.sendCarriedItem();
            this.previousSlot = -1;
        }
    }

    private void resetState() {
        this.slotSwitched = false;
        this.isActive = false;
        this.previousSlot = -1;
        this.tickDelay = 0;
    }
}