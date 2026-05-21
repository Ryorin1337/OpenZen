package shit.zen.modules.impl.movement;

import java.util.ArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import shit.zen.event.impl.MotionEvent;
import shit.zen.event.impl.SneakEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.combat.KillAura;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.Timer;
import shit.zen.utils.game.MovementUtil;
import shit.zen.event.EventTarget;

public class TargetStrafe
extends Module {
    public static TargetStrafe INSTANCE;
    private final Timer collisionTimer = new Timer();
    private final BooleanSetting smartStrafe = new BooleanSetting("Jump Key Only", true);
    private final NumberSetting range = new NumberSetting("Range", Float.valueOf(0.5f), Float.valueOf(0.1f), Float.valueOf(2.0f), Float.valueOf(0.1f));
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", Integer.valueOf(1000), Integer.valueOf(100), Integer.valueOf(5000), Integer.valueOf(100));
    public static int strafeDirectionSign;
    public static Entity strafeTarget;
    private final Timer switchTimer = new Timer();

    public TargetStrafe() {
        super("TargetStrafe", Category.MOVEMENT);
        INSTANCE = this;
    }

    public static float getRange() {
        return TargetStrafe.INSTANCE.range.getValue().floatValue();
    }

    public static boolean isSmartStrafe() {
        return TargetStrafe.INSTANCE.smartStrafe.getValue();
    }

    @EventTarget
    public void onMotion(MotionEvent motionEvent) {
        if (motionEvent.isPost() && mc.player != null) {
            boolean bl;
            AABB aABB;
            if (KillAura.target == null) {
                strafeTarget = null;
            } else if (this.switchTimer.hasPassed(this.switchDelay.getValue().intValue()) || strafeTarget == null) {
                ArrayList<Entity> sortedTargets = new ArrayList<>(KillAura.targetList);
                sortedTargets.sort((entity, entity2) -> {
                    float f = mc.player.distanceTo(entity);
                    float f2 = mc.player.distanceTo(entity2);
                    return Float.compare(f, f2);
                });
                if (!sortedTargets.isEmpty()) {
                    strafeTarget = sortedTargets.get(0);
                    this.switchTimer.reset();
                }
            }
            aABB = mc.player.getBoundingBox();
            boolean bl2 = bl = MovementUtil.isAboveVoid(aABB.minX, aABB.minY, aABB.minZ) || MovementUtil.isAboveVoid(aABB.minX, aABB.minY, aABB.maxZ) || MovementUtil.isAboveVoid(aABB.maxX, aABB.minY, aABB.minZ) || MovementUtil.isAboveVoid(aABB.maxX, aABB.minY, aABB.maxZ);
            if ((bl || mc.player.horizontalCollision) && this.collisionTimer.hasPassedDouble(500.0, true)) {
                strafeDirectionSign *= -1;
            }
        }
    }

    @EventTarget
    public void onSneak(SneakEvent sneakEvent) {
        if (!sneakEvent.isCancelled() && !FireballBlink.INSTANCE.isEnabled()) {
            sneakEvent.setCancelled(true);
        }
    }

    static {
        strafeDirectionSign = 1;
    }
}