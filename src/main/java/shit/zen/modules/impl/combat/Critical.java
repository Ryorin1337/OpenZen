package shit.zen.modules.impl.combat;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.EventPriority;
import shit.zen.event.EventTarget;
import shit.zen.event.impl.GameTickEvent;
import shit.zen.event.impl.PacketEvent;
import shit.zen.event.impl.PreMotionEvent;
import shit.zen.utils.misc.ChatUtil;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.misc.PacketUtil;
public class Critical extends Module {

    public static Critical instance;

    private float   prevFallDistance;
    private boolean isFalling;
    private boolean serverSprinting;
    private int     sprintRestoreTick;

    public Critical() {
        super("Critical", Category.COMBAT);
        instance = this;
    }


    @Override
    public void onEnable() {
        prevFallDistance  = mc.player != null ? mc.player.fallDistance : 0f;
        isFalling         = false;
        serverSprinting   = mc.player != null && mc.player.isSprinting();
        sprintRestoreTick = 0;
        ChatUtil.print("Warn: This module is still WIP");
    }

    @Override
    public void onDisable() {
        sprintRestoreTick = 0;
    }


    @EventTarget
    public void onGameTick(GameTickEvent event) {
        if (mc.player == null) return;

        final float currentFallDistance = mc.player.fallDistance;
        if (currentFallDistance > prevFallDistance && currentFallDistance > 0f) {
            isFalling = true;
        }
        if (currentFallDistance <= 0f) {
            isFalling = false;
        }
        prevFallDistance = currentFallDistance;

        if (sprintRestoreTick > 0) {
            sprintRestoreTick--;
            if (sprintRestoreTick == 0) {
                sendSprint(true);
            }
        }
    }

    @EventTarget(value = EventPriority.HIGH)
    public void onPreMotion(PreMotionEvent event) {
        if (mc.player == null) return;
        if (!isFalling) return;
        if (!isKillAuraAboutToAttack()) return;

        if (serverSprinting) {
            sendSprint(false);
            sprintRestoreTick = 1;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null) return;
        if (event.isIncoming()) return;

        if (event.getPacket() instanceof ServerboundInteractPacket interact) {
            interact.dispatch(new ServerboundInteractPacket.Handler() {
                @Override
                public void onAttack() {
                    if (!isFalling) return;
                    if (!serverSprinting) return;

                    event.setCancelled(true);
                    sendSprint(false);
                    PacketUtil.sendQueued(
                            (Packet<ServerGamePacketListener>) event.getPacket());
                    sprintRestoreTick = 2;
                }

                @Override
                public void onInteraction(InteractionHand hand) { }

                @Override
                public void onInteraction(InteractionHand hand, Vec3 location) { }
            });
        }
    }
    public void onKaSprintTick() {
        if (mc.player == null || mc.getConnection() == null) return;
        if (!serverSprinting) return;

        sendSprint(false);
        sprintRestoreTick = 2;
    }

    private boolean isKillAuraAboutToAttack() {
        if (KillAura.INSTANCE == null || !KillAura.INSTANCE.isEnabled()) {
            return false;
        }
        if (KillAura.target == null) {
            return false;
        }
        if (KillAura.INSTANCE.keepSprint.getValue()) {
            return KillAura.INSTANCE.sprintTickCounter % 2 == 0;
        }
        return true;
    }

    private void sendSprint(boolean start) {
        if (mc.getConnection() == null || mc.player == null) return;
        final ServerboundPlayerCommandPacket.Action action = start
                ? ServerboundPlayerCommandPacket.Action.START_SPRINTING
                : ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
        PacketUtil.sendQueued(
                new ServerboundPlayerCommandPacket(mc.player, action));
        serverSprinting = start;
    }
}
