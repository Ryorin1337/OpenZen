package shit.zen.modules.impl.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.EventTarget;
import shit.zen.event.impl.PacketEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.event.impl.WorldChangeEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.BooleanSetting;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HitEffect extends Module {

    public final BooleanSetting Lighting = new BooleanSetting("Lighting", true);
    public final BooleanSetting YangYongXinMode = new BooleanSetting("YangYongXinMode", false, () -> this.Lighting.getValue());
    public final BooleanSetting Blood = new BooleanSetting("Blood", false);
    public final BooleanSetting MoreCritParticle = new BooleanSetting("More Crit Particle (danger for L PC)",false);

    private final Map<UUID, PlayerData> attackedPlayers = new HashMap<>();
    private final Queue<Vec3> hitPositionsQueue = new ConcurrentLinkedQueue<>();

    public HitEffect() {
        super("HitEffect", Category.RENDER);
    }

    @Override
    public void onDisable() {
        attackedPlayers.clear();
        hitPositionsQueue.clear();
    }

@EventTarget
public void onPacket(PacketEvent e) {
    if (mc.player == null || mc.level == null) return;
    if (!e.isIncoming() && e.getPacket() instanceof ServerboundInteractPacket packet) {
        packet.dispatch(new ServerboundInteractPacket.Handler() {
            @Override
            public void onAttack() {
                if (mc.crosshairPickEntity instanceof Player playerTarget && playerTarget != mc.player) {
                    hitPositionsQueue.add(playerTarget.position());
                    attackedPlayers.put(playerTarget.getUUID(),
                            new PlayerData(playerTarget.getName().getString(), playerTarget.position()));
                }
            }
            @Override public void onInteraction(InteractionHand hand) {}
            @Override public void onInteraction(InteractionHand hand, Vec3 location) {}
        });
    }
}
    @EventTarget
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.level == null) {
            attackedPlayers.clear();
            hitPositionsQueue.clear();
            return;
        }
        while (!hitPositionsQueue.isEmpty()) {
            Vec3 pos = hitPositionsQueue.poll();
            if (pos != null) {
                if (Blood.getValue()) {
                    mc.level.playSound(mc.player, pos.x, pos.y, pos.z, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                    mc.level.levelEvent(2001, BlockPos.containing(pos.add(0, 1, 0)), Block.getId(Blocks.REDSTONE_BLOCK.defaultBlockState())); // love from rise
                }

                if (YangYongXinMode.getValue() && Lighting.getValue()) {
                    spawnClientLightning(pos);
                }
                if (MoreCritParticle.getValue()){

                    double size = 0.6;

                    AABB box = new AABB(
                            pos.x - size / 2.0, pos.y, pos.z - size / 2.0,
                            pos.x + size / 2.0, pos.y + 1.8, pos.z + size / 2.0
                    );
                    for (int i = 0; i < 500; i++) {

                        double x = box.minX + Math.random() * (box.maxX - box.minX);
                        double y = box.minY + Math.random() * (box.maxY - box.minY);
                        double z = box.minZ + Math.random() * (box.maxZ - box.minZ);
                        double dx = (x - pos.x) * 0.3;
                        double dy = (y - pos.y) * 0.3;
                        double dz = (z - pos.z) * 0.3;

                        mc.level.addParticle(
                                ParticleTypes.CRIT,
                                x, y, z,
                                dx, dy, dz
                        );
                    }
                }
            }
        }
        Iterator<Map.Entry<UUID, PlayerData>> it = attackedPlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PlayerData> entry = it.next();
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();

            boolean found = false;
            for (Player player : mc.level.players()) {
                if (player.getUUID().equals(uuid)) {
                    found = true;
                    data.lastPos = player.position();
                    break;
                }
            }

            if (!found) {
                if (!data.name.equals(mc.player.getName().getString()) && Lighting.getValue()) {
                    spawnClientLightning(data.lastPos);
                }
                it.remove();
            }
        }
    }

    @EventTarget
    public void onWorldChange(WorldChangeEvent e) {
        attackedPlayers.clear();
        hitPositionsQueue.clear();
    }

    private void spawnClientLightning(Vec3 pos) {
        if (mc.level == null || pos == null) return;

        LightningBolt fakeLightning = EntityType.LIGHTNING_BOLT.create(mc.level);
        if (fakeLightning != null) {
            fakeLightning.moveTo(pos.x, pos.y, pos.z);
            fakeLightning.setVisualOnly(true);

            mc.level.putNonPlayerEntity(fakeLightning.getId(), fakeLightning);
        }
    }

    private static class PlayerData {
        String name;
        Vec3 lastPos;

        public PlayerData(String name, Vec3 lastPos) {
            this.name = name;
            this.lastPos = lastPos;
        }
    }
}