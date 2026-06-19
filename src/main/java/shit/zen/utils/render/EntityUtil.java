package shit.zen.utils.render;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityUtil {

    public static Entity create(Entity original, String target) {

        EntityType<?> type = switch (target) {
            case "Cow" -> EntityType.COW;
            case "Zombie" -> EntityType.ZOMBIE;
            case "Creeper" -> EntityType.CREEPER;
            case "Pig" -> EntityType.PIG;
            default -> EntityType.PIG;
        };

        Entity e = type.create(original.level());
        if (e == null) return null;

        e.copyPosition(original);
        e.setYRot(original.getYRot());
        e.setXRot(original.getXRot());

        return e;
    }
}