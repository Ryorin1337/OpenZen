package shit.zen.modules.impl.movement;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import shit.zen.event.impl.MotionEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.game.MovementUtil;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.rotation.Rotation;
import shit.zen.event.EventTarget;

public class CollisionSpeed
extends Module {
    public CollisionSpeed() {
        super("CollisionSpeed", Category.MOVEMENT);
    }

    @EventTarget
    public void onMotion(MotionEvent motionEvent) {
        if (motionEvent.isPost() && MovementUtil.isInputActive() && mc.player != null && mc.level != null) {
            AABB aABB = mc.player.getBoundingBox().expandTowards(0.25, 0.25, 0.25);
            int n = 0;
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof LivingEntity) && !(entity instanceof Boat) && !(entity instanceof Minecart) && !(entity instanceof FishingHook) || entity instanceof ArmorStand || entity.getId() == mc.player.getId() || !entity.isAlive() || !aABB.intersects(entity.getBoundingBox()) || entity.getId() == -8 || entity.getId() == -1337 || mc.player.getTeam() != null && entity.getTeam() != null && !entity.getTeam().getCollisionRule().equals(Team.CollisionRule.ALWAYS) && (mc.player.getTeam().isAlliedTo(entity.getTeam()) ? !entity.getTeam().getCollisionRule().equals(Team.CollisionRule.PUSH_OWN_TEAM) : !entity.getTeam().getCollisionRule().equals(Team.CollisionRule.PUSH_OTHER_TEAMS))) continue;
                ++n;
            }
            float f = mc.player.getYRot();
            if (mc.options.keyDown.isDown()) {
                f += 180.0f;
                if (mc.options.keyLeft.isDown()) {
                    f += 45.0f;
                } else if (mc.options.keyRight.isDown()) {
                    f -= 45.0f;
                }
            } else if (mc.options.keyUp.isDown()) {
                if (mc.options.keyLeft.isDown()) {
                    f -= 45.0f;
                } else if (mc.options.keyRight.isDown()) {
                    f += 45.0f;
                }
            } else if (mc.options.keyRight.isDown()) {
                f += 90.0f;
            } else if (mc.options.keyLeft.isDown()) {
                f -= 90.0f;
            }
            double d = Math.toRadians(f);
            double d2 = 0.065f * (float)Math.min(4, n);
            if (TargetStrafe.strafeTarget != null && TargetStrafe.INSTANCE.isEnabled() && (!TargetStrafe.isSmartStrafe() || mc.options.keyJump.isDown())) {
                float f2 = (float)(d2 / ((double)TargetStrafe.getRange() * Math.PI * 2.0) * 360.0) * (float)TargetStrafe.strafeDirectionSign;
                Rotation rotation = RotationUtil.rotationToForBow(new Vec3(TargetStrafe.strafeTarget.getX(), TargetStrafe.strafeTarget.getY(), TargetStrafe.strafeTarget.getZ()), new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
                rotation.setYaw(rotation.getYaw() + f2);
                float f3 = rotation.getYaw() * ((float)Math.PI / 180);
                double d3 = TargetStrafe.strafeTarget.getX() - Math.sin(f3) * (double)TargetStrafe.getRange();
                double d4 = TargetStrafe.strafeTarget.getZ() + Math.cos(f3) * (double)TargetStrafe.getRange();
                d = RotationUtil.rotationToForBow(new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()), new Vec3(d3, TargetStrafe.strafeTarget.getY(), d4)).getYaw() * ((float)Math.PI / 180);
            }
            MovementUtil.strafeWithYaw(d, d2);
            MovementUtil.strafeForward(1.0E-4f);
        }
    }
}