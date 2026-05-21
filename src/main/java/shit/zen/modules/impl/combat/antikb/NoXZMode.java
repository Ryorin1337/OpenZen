package shit.zen.modules.impl.combat.antikb;

import java.util.concurrent.LinkedBlockingDeque;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.ZenClient;
import shit.zen.event.impl.DisconnectEvent;
import shit.zen.event.impl.GameTickEvent;
import shit.zen.event.impl.MotionEvent;
import shit.zen.event.impl.PreMotionEvent;
import shit.zen.event.impl.ReceivePacketEvent;
import shit.zen.event.impl.RotationEvent;
import shit.zen.event.impl.SprintEvent;
import shit.zen.event.impl.StrafeEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.modules.impl.combat.AntiKB;
import shit.zen.modules.impl.combat.KillAura;
import shit.zen.modules.impl.combat.antikb.AntiKBMode;
import shit.zen.modules.impl.player.Stuck;
import shit.zen.utils.misc.ChatUtil;

public class NoXZMode
extends AntiKBMode {
    public static NoXZMode INSTANCE;
    public static boolean isAttacking;
    public static int attackCount;
    private int attackCooldown = 0;
    private Entity attackTarget = null;
    private int attacksRemaining = 0;
    private int flagCooldown = 0;
    private boolean shouldJump = false;
    private int sprintBoostCounter = 0;
    private int hitCounter = 0;
    private boolean isSuspending = false;
    private int suspendTicks = 0;
    private ClientboundSetEntityMotionPacket knockbackPacket = null;
    private final LinkedBlockingDeque<Packet<?>> packetQueue = new LinkedBlockingDeque();
    private final LinkedBlockingDeque<Packet<?>> movePacketQueue = new LinkedBlockingDeque();
    private volatile boolean isFlushing = false;
    private float instantAttackProgress = 0.0f;
    private boolean isInstantAttacking = false;
    private boolean shouldFlushMotion;

    @Override
    public boolean isActive() {
        return this.isSuspending;
    }

    public NoXZMode() {
        super("NoXZ");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.resetAll();
    }

    @Override
    public void onDisable() {
        this.resetAll();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void onRotation(RotationEvent rotationEvent) {
    }

    @Override
    public void onMotion(MotionEvent motionEvent) {
        if (motionEvent.isPre() && this.shouldFlushMotion) {
            while (!this.packetQueue.isEmpty()) {
                Packet packet = this.packetQueue.poll();
                if (packet == null) continue;
                try {
                    packet.handle(mc.getConnection());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            this.shouldFlushMotion = false;
        }
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
        if (mc.player == null) {
            return;
        }
        if (this.isFlushing) {
            return;
        }
        if (this.shouldIgnore()) {
            return;
        }
        Packet<?> packet = receivePacketEvent.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket && this.isSuspending) {
            this.movePacketQueue.add(packet);
            receivePacketEvent.setCancelled(true);
            return;
        }
        if (packet instanceof ClientboundPlayerPositionPacket) {
            if (this.isSuspending) {
                this.release();
            }
            this.resetSuspension();
            ChatUtil.print("Flag Detected");
            this.flagCooldown = 2;
        }
        if (this.flagCooldown != 0) {
            return;
        }
        if (this.isSuspending) {
            if (!this.isAllowedPacket(packet)) {
                this.packetQueue.add(packet);
                receivePacketEvent.setCancelled(true);
            }
            return;
        }
        if (packet instanceof ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) {
            if (clientboundSetEntityMotionPacket.getId() != mc.player.getId()) {
                return;
            }
            double d = -clientboundSetEntityMotionPacket.getXa();
            double d2 = -clientboundSetEntityMotionPacket.getZa();
            if (Math.abs(d) > 0.01 || Math.abs(d2) > 0.01) {
                this.hitCounter = 1;
            }
            if (clientboundSetEntityMotionPacket.getYa() > 0) {
                Entity entity;
                boolean bl;
                this.sprintBoostCounter = this.sprintBoostCounter % 100 + 100;
                if (this.sprintBoostCounter >= 100) {
                    this.shouldJump = true;
                }
                boolean bl2 = bl = this.isValidTarget(entity = this.getAttackTarget()) && mc.player.isSprinting();
                if (!mc.player.onGround()) {
                    this.isSuspending = true;
                    this.suspendTicks = 0;
                    this.knockbackPacket = clientboundSetEntityMotionPacket;
                    receivePacketEvent.setCancelled(true);
                } else if (bl) {
                    this.attackTarget = entity;
                    this.attacksRemaining = AntiKB.INSTANCE.attackAmount.getValue().intValue();
                } else {
                    this.isSuspending = true;
                    this.suspendTicks = 0;
                    this.knockbackPacket = clientboundSetEntityMotionPacket;
                    receivePacketEvent.setCancelled(true);
                    ChatUtil.print("Alink Wait");
                }
            }
        }
    }

    @Override
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        this.resetAll();
    }

    @Override
    public void onPreMotion(PreMotionEvent preMotionEvent) {
    }

    @Override
    public void onGameTick(GameTickEvent gameTickEvent) {
    }

    @Override
    public void onSprint(SprintEvent sprintEvent) {
    }

    private void resetAll() {
        this.clearTarget();
        this.flagCooldown = 0;
        this.shouldJump = false;
        this.sprintBoostCounter = 0;
        this.hitCounter = 0;
        this.resetSuspension();
    }

    private void clearTarget() {
        this.attackTarget = null;
        this.attacksRemaining = 0;
    }

    private void resetSuspension() {
        this.isSuspending = false;
        this.suspendTicks = 0;
        this.knockbackPacket = null;
        this.packetQueue.clear();
        this.movePacketQueue.clear();
        this.isFlushing = false;
        this.instantAttackProgress = 0.0f;
        this.isInstantAttacking = false;
        ZenClient.serverTickRate = 1.0f;
    }

    private boolean shouldIgnore() {
        if (mc.player == null || mc.level == null) {
            return true;
        }
        if (mc.player.isDeadOrDying() || !mc.player.isAlive() || mc.player.getHealth() <= 0.0f) {
            return true;
        }
        if (mc.player.isSpectator() || mc.player.getAbilities().flying) {
            return true;
        }
        if (mc.player.isInLava() || mc.player.isOnFire() || mc.player.isInWater() || mc.player.onClimbable() || mc.player.isSleeping()) {
            return true;
        }
        if (mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.COBWEB)) {
            return true;
        }
        Stuck stuck = Stuck.INSTANCE;
        return stuck != null && stuck.isEnabled();
    }

    private double getAABBDistance(Entity entity) {
        if (mc.player == null) {
            return Double.MAX_VALUE;
        }
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        AABB aABB = entity.getBoundingBox();
        double d = Math.max(aABB.minX, Math.min(vec3.x, aABB.maxX));
        double d2 = Math.max(aABB.minY, Math.min(vec3.y, aABB.maxY));
        double d3 = Math.max(aABB.minZ, Math.min(vec3.z, aABB.maxZ));
        return vec3.distanceTo(new Vec3(d, d2, d3));
    }

    private Entity getHitResultEntity() {
        Entity entity;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY && (entity = ((EntityHitResult)mc.hitResult).getEntity()) instanceof LivingEntity && entity != mc.player && entity.isAlive() && !entity.isSpectator()) {
            return entity;
        }
        return null;
    }

    private Entity getAttackTarget() {
        if (KillAura.target != null) {
            return KillAura.target;
        }
        return this.getHitResultEntity();
    }

    private boolean isValidTarget(Entity entity) {
        LivingEntity livingEntity;
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof LivingEntity && ((livingEntity = (LivingEntity)entity).isDeadOrDying() || livingEntity.getHealth() <= 0.0f)) {
            return false;
        }
        double d = 3.7f;
        return !(this.getAABBDistance(entity) > d);
    }

    @Override
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null) {
            return;
        }
        if (this.attackCooldown > 0) {
            --this.attackCooldown;
            if (this.attackCooldown <= 0) {
                isAttacking = false;
                attackCount = 0;
            }
        }
        if (this.hitCounter > 0) {
            ++this.hitCounter;
            if (this.hitCounter > 2) {
                this.hitCounter = 0;
            }
        }
        if (mc.player.isDeadOrDying() || !mc.player.isAlive() || this.shouldIgnore()) {
            this.clearTarget();
            if (this.isSuspending) {
                this.release();
            }
            if (this.isInstantAttacking) {
                this.isInstantAttacking = false;
                this.instantAttackProgress = 0.0f;
                ZenClient.serverTickRate = 1.0f;
            }
            return;
        }
        if (this.flagCooldown > 0) {
            --this.flagCooldown;
            this.clearTarget();
        }
        if (this.isSuspending) {
            boolean bl;
            ++this.suspendTicks;
            boolean bl2 = AntiKB.INSTANCE.instantAttack.getValue();
            if (bl2 && this.instantAttackProgress < 3.0f) {
                float f;
                ZenClient.serverTickRate = f = 0.5f;
                this.instantAttackProgress += 1.0f - f;
                this.instantAttackProgress = Math.min(this.instantAttackProgress, 3.0f);
            }
            boolean bl3 = mc.player.onGround();
            boolean bl4 = bl = this.suspendTicks >= 12;
            if (bl3 || bl) {
                ChatUtil.print(bl ? "Alink Timeout" : "ground");
                if (bl2) {
                    ZenClient.serverTickRate = 1.0f;
                }
                Entity entity = this.getAttackTarget();
                boolean bl5 = this.isValidTarget(entity);
                boolean bl6 = mc.player.isSprinting();
                if (bl3 && bl5 && bl6) {
                    this.isFlushing = true;
                    this.attackTarget = entity;
                    this.attacksRemaining = AntiKB.INSTANCE.attackAmount.getValue().intValue();
                    this.sendMovePackets();
                    this.applyKnockbackPacket();
                    if (bl2 && this.instantAttackProgress > 0.0f) {
                        this.attacksRemaining = (int)this.instantAttackProgress;
                        this.scheduleMotionFlush();
                        this.isSuspending = false;
                        this.suspendTicks = 0;
                        this.isFlushing = false;
                        this.isInstantAttacking = true;
                        ZenClient.serverTickRate = 4.0f;
                    } else {
                        this.doAttackSequence(tickEvent);
                        this.scheduleMotionFlush();
                        this.isSuspending = false;
                        this.suspendTicks = 0;
                        this.isFlushing = false;
                    }
                } else {
                    this.release();
                    if (bl2) {
                        this.instantAttackProgress = 0.0f;
                    }
                    if (bl3 && mc.player.isSprinting()) {
                        mc.player.setSprinting(false);
                    }
                }
                return;
            }
            return;
        }
        if (this.isInstantAttacking) {
            this.instantAttackProgress -= 1.0f;
            if (this.instantAttackProgress <= 0.0f) {
                this.instantAttackProgress = 0.0f;
                this.isInstantAttacking = false;
                ZenClient.serverTickRate = 1.0f;
                ChatUtil.print("done");
            }
        }
        if (this.attacksRemaining > 0 && this.attackTarget != null) {
            this.doAttackSequence(tickEvent);
        }
    }

    @Override
    public void onStrafe(StrafeEvent strafeEvent) {
        if (mc.player == null) {
            return;
        }
        if (this.hitCounter > 0) {
            strafeEvent.setForward(1.0f);
        }
        if (this.shouldJump) {
            this.shouldJump = false;
            if (mc.player.onGround() && mc.player.isSprinting() && !mc.player.hasEffect(MobEffects.JUMP) && !this.shouldIgnore()) {
                strafeEvent.setSprinting(true);
            }
        }
    }

    private void doAttackSequence(TickEvent tickEvent) {
        if (this.attackTarget == null || !this.attackTarget.isAlive()) {
            this.clearTarget();
            return;
        }
        double d = 3.7f;
        if (this.getAABBDistance(this.attackTarget) > d) {
            this.clearTarget();
            return;
        }
        isAttacking = true;
        attackCount = this.attacksRemaining--;
        this.attackCooldown = 2;
        this.doAttack(this.attackTarget);
        if (this.attacksRemaining <= 0) {
            this.clearTarget();
            if (AntiKB.INSTANCE.instantAttack.getValue()) {
                ChatUtil.print("Attack (" + AntiKB.INSTANCE.attackAmount.getValue().intValue() + ")");
            }
        }
    }

    private boolean doAttack(Entity entity) {
        if (mc.player == null || mc.gameMode == null) {
            return false;
        }
        if (AntiKB.INSTANCE.sprintStateCheck.getValue() && !mc.player.isSprinting()) {
            ChatUtil.print("not sprinting");
            return false;
        }
        boolean bl = mc.player.isSprinting();
        if (bl) {
            mc.player.setSprinting(false);
        }
        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);
        if (bl) {
            Vec3 vec3 = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(vec3.x * 0.6, vec3.y, vec3.z * 0.6);
        }
        if (!AntiKB.INSTANCE.instantAttack.getValue()) {
            ChatUtil.print("Attack (" + this.attacksRemaining + ")");
        }
        return true;
    }

    private void sendMovePackets() {
        if (mc.getConnection() == null) {
            return;
        }
        while (!this.movePacketQueue.isEmpty()) {
            Packet packet = this.movePacketQueue.poll();
            if (packet == null) continue;
            try {
                mc.getConnection().send(packet);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private void applyKnockbackPacket() {
        if (this.knockbackPacket != null && mc.getConnection() != null) {
            try {
                this.knockbackPacket.handle(mc.getConnection());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            this.knockbackPacket = null;
        }
    }

    private void scheduleMotionFlush() {
        if (mc.getConnection() == null) {
            return;
        }
        this.shouldFlushMotion = true;
    }

    private boolean isAllowedPacket(Packet<?> packet) {
        return packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundSetHealthPacket || packet instanceof ClientboundPlayerPositionPacket || packet instanceof ClientboundSoundPacket || packet instanceof ClientboundPlayerChatPacket || packet instanceof ClientboundPlayerCombatKillPacket || packet instanceof ClientboundContainerClosePacket || packet instanceof ClientboundHurtAnimationPacket || packet instanceof ClientboundSetTitleTextPacket || packet instanceof ClientboundSetPlayerTeamPacket || packet instanceof ClientboundSystemChatPacket || packet instanceof ClientboundDisconnectPacket || packet instanceof ClientboundAnimatePacket && ((ClientboundAnimatePacket)packet).getId() != mc.player.getId();
    }

    private void release() {
        this.isFlushing = true;
        this.sendMovePackets();
        this.applyKnockbackPacket();
        this.scheduleMotionFlush();
        this.isFlushing = false;
        this.isSuspending = false;
        this.suspendTicks = 0;
        this.instantAttackProgress = 0.0f;
        this.isInstantAttacking = false;
        ZenClient.serverTickRate = 1.0f;
    }

    static {
        isAttacking = false;
        attackCount = 0;
    }
}