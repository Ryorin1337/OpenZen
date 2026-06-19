package shit.zen.utils.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.level.Level;


import shit.zen.utils.rotation.RotationHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityUtil {

    private static final Map<Integer, Entity> FAKE_ENTITY_CACHE = new ConcurrentHashMap<>();
    private static Level lastLevel = null;


    public static Entity getOrCreateSyncedEntity(Entity original, String targetStr) {
        Level currentLevel = original.level();

        if (lastLevel != currentLevel) {
            FAKE_ENTITY_CACHE.clear();
            lastLevel = currentLevel;
        }

        EntityType<?> targetType = switch (targetStr) {
            case "Cow" -> EntityType.COW;
            case "Zombie" -> EntityType.ZOMBIE;
            case "Creeper" -> EntityType.CREEPER;
            default -> EntityType.PIG;
        };

        Entity fake = FAKE_ENTITY_CACHE.computeIfAbsent(original.getId(), id -> targetType.create(currentLevel));
        if (fake == null) return null;

        if (fake.getType() != targetType) {
            fake = targetType.create(currentLevel);
            if (fake == null) return null;
            FAKE_ENTITY_CACHE.put(original.getId(), fake);
        }

        // === 基础数据同步 ===
        fake.copyPosition(original);
        fake.setBoundingBox(original.getBoundingBox());
        fake.tickCount = original.tickCount;
        fake.setOnGround(original.onGround());

        if (fake instanceof LivingEntity fakeLiving && original instanceof LivingEntity origLiving) {
            fakeLiving.setPos(origLiving.getX(), origLiving.getY(), origLiving.getZ());
            fakeLiving.xo = origLiving.xo;
            fakeLiving.yo = origLiving.yo;
            fakeLiving.zo = origLiving.zo;


            if (origLiving == net.minecraft.client.Minecraft.getInstance().player) {
                fakeLiving.setYRot(origLiving.getYRot());
                fakeLiving.setXRot(RotationHandler.prevSentRotation.getPitch());
                fakeLiving.yRotO = origLiving.yRotO;
                fakeLiving.xRotO = RotationHandler.prevSentRotation.getPitch();

                fakeLiving.yBodyRot = origLiving.yBodyRot;
                fakeLiving.yBodyRotO = origLiving.yBodyRotO;
                fakeLiving.yHeadRot = RotationHandler.sentRotation.getYaw();
                fakeLiving.yHeadRotO = RotationHandler.prevSentRotation.getYaw();
                fakeLiving.setYHeadRot(RotationHandler.sentRotation.getYaw());
            }else {
                fakeLiving.setYRot(origLiving.getYRot());
                fakeLiving.setXRot(origLiving.getXRot());
                fakeLiving.yRotO = origLiving.yRotO;
                fakeLiving.xRotO = origLiving.xRotO;

                fakeLiving.yBodyRot = origLiving.yBodyRot;
                fakeLiving.yBodyRotO = origLiving.yBodyRotO;
                fakeLiving.yHeadRot = origLiving.yHeadRot;
                fakeLiving.yHeadRotO = origLiving.yHeadRotO;
                fakeLiving.setYHeadRot(origLiving.getYHeadRot());
            }
            fakeLiving.swingTime = origLiving.swingTime;
            fakeLiving.attackAnim = origLiving.attackAnim;     // 当前帧挥手进度
            fakeLiving.oAttackAnim = origLiving.oAttackAnim;   // 上一帧挥手进度
            fakeLiving.hurtTime = origLiving.hurtTime;
            fakeLiving.hurtDuration = origLiving.hurtDuration;
            fakeLiving.deathTime = origLiving.deathTime;

            WalkAnimationState origAnim = origLiving.walkAnimation;
            WalkAnimationState fakeAnim = fakeLiving.walkAnimation;
            float origSpeed = origAnim.speed();
            float origSpeedOld = 2.0F * origAnim.speed(0.5F) - origSpeed;
            float origPosition = origAnim.position();
            fakeAnim.setSpeed(origSpeedOld);
            fakeAnim.update(origSpeed, 1.0F);
            float currentFakePos = fakeAnim.position();
            float deltaPos = origPosition - currentFakePos;
            fakeAnim.setSpeed(fakeAnim.speed() - deltaPos);
            fakeAnim.update(origSpeed, 1.0F);
        }

        return fake;
    }

    public static void clearCache() {
        FAKE_ENTITY_CACHE.clear();
    }
}