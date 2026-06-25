package shit.zen.modules.impl.movement;

import java.util.HashMap;
import net.minecraft.client.KeyMapping;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.player.InventoryManager;
import shit.zen.event.EventTarget;
import shit.zen.event.impl.MotionEvent;

public class Sprint
        extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT);
        this.setEnabled(true);
    }

    @EventTarget
    public void onMotion(MotionEvent e) {
        if (e.pre) {
            if (InventoryManager.INSTANCE != null && InventoryManager.INSTANCE.isSuppressingSprint()) {
                mc.options.keySprint.setDown(false);
                if (mc.player != null) {
                    mc.player.setSprinting(false);
                }
                return;
            }

            mc.options.keySprint.setDown(true);
            mc.options.toggleSprint().set(false);
        }
    }

    @Override
    public void onDisable() {
        mc.options.keySprint.setDown(false);
    }
}