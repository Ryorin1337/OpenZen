package shit.zen.modules.impl.combat;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import shit.zen.event.impl.SprintEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.movement.Scaffold;
import shit.zen.modules.impl.player.Stuck;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.animation.Timer;
import shit.zen.utils.game.PlayerUtil;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.rotation.Rotation;
import shit.zen.utils.rotation.RotationHandler;
import shit.zen.event.EventTarget;

public class AutoThrow
extends Module {
    public static AutoThrow INSTANCE;
    private final NumberSetting minDistance = new NumberSetting("Min Distance", Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(30), Integer.valueOf(1));
    private final NumberSetting maxDistance = new NumberSetting("Max Distance", Integer.valueOf(10), Integer.valueOf(3), Integer.valueOf(30), Integer.valueOf(1));
    private final NumberSetting throwDelay = new NumberSetting("Delay", Integer.valueOf(500), Integer.valueOf(50), Integer.valueOf(2000), Integer.valueOf(50));
    private final Timer throwTimer = new Timer();
    public Rotation targetRotation;
    public int ticksUntilThrow;
    private int savedSlot = -1;

    public AutoThrow() {
        super("AutoThrow", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.targetRotation = null;
        this.ticksUntilThrow = 0;
        this.savedSlot = -1;
        this.throwTimer.reset();
    }

    @Override
    public void onDisable() {
        this.targetRotation = null;
        this.ticksUntilThrow = 0;
        this.savedSlot = -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @EventTarget
    public void onSprint(SprintEvent sprintEvent) {
        int n;
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) {
            return;
        }
        if (Scaffold.INSTANCE != null && Scaffold.INSTANCE.isEnabled() || Stuck.INSTANCE != null && Stuck.INSTANCE.isEnabled() || mc.player.isUsingItem() || mc.screen != null) {
            this.ticksUntilThrow = 0;
            this.targetRotation = null;
            return;
        }
        if (this.ticksUntilThrow <= 0) {
            this.targetRotation = null;
        }
        int n2 = -1;
        for (n = 0; n < 9; ++n) {
            ItemStack itemStack = mc.player.getInventory().getItem(n);
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof EggItem) && !(itemStack.getItem() instanceof SnowballItem)) continue;
            n2 = n;
            break;
        }
        if (mc.player.isUsingItem() || mc.player.getMainHandItem().getItem() instanceof BowItem || mc.player.getMainHandItem().getItem() instanceof CrossbowItem) {
            return;
        }
        if (n2 == -1) return;
        if (--this.ticksUntilThrow == 0) {
            boolean bl;
            n = mc.player.getInventory().selected;
            boolean bl2 = bl = n != n2;
            if (bl) {
                mc.player.getInventory().selected = n2;
                PlayerUtil.sendCarriedItem();
                this.savedSlot = n;
            }
            float f = mc.player.getYRot();
            float f2 = mc.player.getXRot();
            if (RotationHandler.targetRotation != null && RotationHandler.isRotating) {
                mc.player.setYRot(RotationHandler.targetRotation.getYaw());
                mc.player.setXRot(RotationHandler.targetRotation.getPitch());
            }
            try {
                if (!(mc.player.getMainHandItem().getItem() instanceof EggItem) && !(mc.player.getMainHandItem().getItem() instanceof SnowballItem)) return;
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            } finally {
                mc.player.setYRot(f);
                mc.player.setXRot(f2);
            }
        } else {
            if (!this.findTarget().isPresent() || !this.throwTimer.hasPassed((float)((long) this.throwDelay.getValue().doubleValue())) || Stuck.INSTANCE != null && Stuck.INSTANCE.isEnabled()) return;
            this.targetRotation = this.calculateThrowRotation(this.findTarget().get());
            if (this.targetRotation != null) {
                RotationHandler.setTargetRotation(this.targetRotation);
                RotationHandler.isRotating = true;
                this.ticksUntilThrow = 2;
            }
            this.throwTimer.reset();
        }
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null) {
            return;
        }
        if (this.savedSlot != -1) {
            mc.player.getInventory().selected = this.savedSlot;
            this.savedSlot = -1;
            RotationHandler.isRotating = false;
        }
    }

    private Rotation calculateThrowRotation(Entity entity) {
        float f;
        float f2 = 1.5f;
        float f3 = 0.03f;
        double d = entity.getX();
        double d2 = entity.getY() + (double)entity.getBbHeight() * 0.8;
        double d3 = entity.getZ();
        double d4 = entity.getX() - entity.xOld;
        double d5 = entity.getY() - entity.yOld;
        double d6 = entity.getZ() - entity.zOld;
        for (int i = 0; i < 3; ++i) {
            double d7 = d - mc.player.getX();
            double d8 = d2 - (mc.player.getY() + (double)mc.player.getEyeHeight(mc.player.getPose()));
            double d9 = d3 - mc.player.getZ();
            double d10 = Math.sqrt(d7 * d7 + d9 * d9);
            f = (float)(d10 / (double)(f2 * 0.4f));
            d = entity.getX() + d4 * (double)f;
            d2 = entity.getY() + (double)entity.getBbHeight() * 0.8 + d5 * (double)f;
            d3 = entity.getZ() + d6 * (double)f;
        }
        double d11 = d - mc.player.getX();
        double d12 = d2 - (mc.player.getY() + (double)mc.player.getEyeHeight(mc.player.getPose()));
        double d13 = d3 - mc.player.getZ();
        double d14 = Math.sqrt(d11 * d11 + d13 * d13);
        float f4 = (float)(Math.atan2(d13, d11) * 180.0 / Math.PI) - 90.0f;
        f = -RotationUtil.ballisticPitch((float)d14, (float)d12, f2, f3);
        return new Rotation(f4, f);
    }

    private Optional<? extends Player> findTarget() {
        if (mc.player == null || mc.level == null) {
            return Optional.empty();
        }
        return mc.level.players().stream().filter(abstractClientPlayer -> abstractClientPlayer != mc.player).filter(abstractClientPlayer -> KillAura.INSTANCE.isValidTarget(abstractClientPlayer)).filter(abstractClientPlayer -> {
            double d = this.getDistanceTo(abstractClientPlayer);
            return d >= this.minDistance.getValue().doubleValue() && d <= this.maxDistance.getValue().doubleValue();
        }).filter(this::hasLineOfSight).filter(abstractClientPlayer -> !this.isInvisibleAlly(abstractClientPlayer)).min(Comparator.comparingDouble(abstractClientPlayer -> mc.player.distanceTo(abstractClientPlayer)));
    }

    private double getDistanceTo(Entity entity) {
        double d = mc.player.getX() - entity.getX();
        double d2 = mc.player.getZ() - entity.getZ();
        return Math.sqrt(d * d + d2 * d2);
    }

    private boolean hasLineOfSight(Entity entity) {
        Vec3 vec3;
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 vec32 = new Vec3(mc.player.getX(), mc.player.getY() + (double)mc.player.getEyeHeight(), mc.player.getZ());
        BlockHitResult blockHitResult = mc.level.clip(new ClipContext(vec32, vec3 = new Vec3(entity.getX(), entity.getY() + (double)entity.getEyeHeight(), entity.getZ()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return blockHitResult.getType() == HitResult.Type.MISS;
    }

    private boolean isInvisibleAlly(Entity entity) {
        if (!entity.isInvisible()) {
            return false;
        }
        if (mc.player.isSpectator()) {
            return false;
        }
        Team team = entity.getTeam();
        return team == null || mc.player.getTeam() != team || !team.isAlliedTo(mc.player.getTeam());
    }
}