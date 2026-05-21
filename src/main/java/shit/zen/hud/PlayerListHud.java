package shit.zen.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.PacketEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.game.ItemUtil;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.ChatUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;
import shit.zen.event.EventTarget;

public class PlayerListHud
extends HudElement {
    public static final class PlayerEntry {
        public final PlayerListHud parent;
        public final net.minecraft.world.entity.player.Player player;
        public java.util.List<ItemStack> items;
        public final java.util.Map<net.minecraft.world.item.Item, ItemStack> itemStacks = new java.util.HashMap<>();
        public final java.util.Map<net.minecraft.world.item.Item, Integer> cheatItems = new java.util.HashMap<>();
        public final java.util.Set<net.minecraft.world.item.Item> flaggedItems = new java.util.HashSet<>();
        public final SmoothAnimationTimer fadeAnim = new SmoothAnimationTimer();
        public final SmoothAnimationTimer slideAnim = new SmoothAnimationTimer();
        public final SmoothAnimationTimer heightAnim = new SmoothAnimationTimer();
        public final SmoothAnimationTimer alphaAnim = new SmoothAnimationTimer();
        public final SmoothAnimationTimer widthAnim = new SmoothAnimationTimer();
        public String displayName;
        public float nameWidth;
        public boolean visible = true;
        public boolean rightAligned = false;
        public float alpha = 1.0f;
        public float targetY;
        public float currentY;
        public boolean removing = false;

        public PlayerEntry(PlayerListHud parent, net.minecraft.world.entity.player.Player player, java.util.List<ItemStack> items) {
            this.parent = parent;
            this.player = player;
            this.items = items;
            this.displayName = player.getName().getString();
            for (ItemStack s : items) {
                this.itemStacks.put(s.getItem(), s);
            }
        }

        public void startRemove() {
            this.removing = true;
        }

        public void updateItems(java.util.List<ItemStack> newItems) {
            this.items = newItems;
        }

        public boolean isRemoveDone() {
            return this.removing && this.alpha <= 0.01f;
        }

        public void tick() {
            float target = this.removing ? 0.0f : 1.0f;
            this.alpha += (target - this.alpha) * 0.18f;
            this.currentY += (this.targetY - this.currentY) * 0.18f;
            this.fadeAnim.tick();
            this.slideAnim.tick();
            this.heightAnim.tick();
            this.alphaAnim.tick();
        }
    }

    private final List<PlayerListHud.PlayerEntry> playerEntryList = new ArrayList<>();
    private final FontRenderer nameFont = FontPresets.poppinsMedium(15.0f);
    final FontRenderer headerFont = FontPresets.pingfang(15.0f);
    private final FontRenderer subFont = FontPresets.materialIcons(18.0f);
    private final SmoothAnimationTimer slideAnim = new SmoothAnimationTimer();
    private final SmoothAnimationTimer fadeAnim = new SmoothAnimationTimer();
    private final SmoothAnimationTimer rightAlignAnim = new SmoothAnimationTimer();
    boolean wasRightAligned = false;
    private boolean rightAlign = false;
    private final Set<UUID> playerEntries = new HashSet<>();
    private final Set<UUID> removedEntries = new HashSet<>();

    public PlayerListHud() {
        super("PlayerList");
        this.setWidth(150.0f);
        this.setHeight(100.0f);
        this.setEnabled(true);
        this.fadeAnim.setCurrentValue(1.0);
        this.rightAlignAnim.setCurrentValue(1.0);
    }

    @Override
    public void onEnable() {
        if (this.playerEntryList != null) {
            this.playerEntryList.clear();
        }
        if (this.removedEntries != null) {
            this.removedEntries.clear();
        }
    }

    private boolean isCheatItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return ItemUtil.isOtherCheat(itemStack) || ItemUtil.isWeaponItem(itemStack) || ItemUtil.isEnchantedGoldenApple(itemStack) || ItemUtil.isEndCrystal(itemStack) || ItemUtil.isKBSlimeBall(itemStack) || ItemUtil.isKBStick(itemStack) || ItemUtil.getPunchLevel(itemStack) > 2 && itemStack.getItem() instanceof BowItem || ItemUtil.getPowerLevel(itemStack) > 3 && itemStack.getItem() instanceof BowItem;
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null || mc.level == null) {
            if (this.playerEntryList != null && !this.playerEntryList.isEmpty()) {
                this.playerEntryList.clear();
            }
            if (this.removedEntries != null && !this.removedEntries.isEmpty()) {
                this.removedEntries.clear();
            }
            return;
        }
        List<? extends Player> list = mc.level.players();
        ArrayList<Player> playerList = new ArrayList<>(list);
        for (PlayerListHud.PlayerEntry entry : this.playerEntryList) {
            if (!playerList.stream().noneMatch(p -> p.getUUID().equals(entry.player.getUUID()))) continue;
            entry.startRemove();
        }
        for (Player currentPlayer : playerList) {
            List<ItemStack> playerItems;
            boolean alreadyTracked = this.playerEntryList.stream().anyMatch(e -> e.player.getUUID().equals(currentPlayer.getUUID()));
            if (alreadyTracked || (playerItems = this.getPlayerItems(currentPlayer)).isEmpty()) continue;
            this.playerEntryList.add(0, new PlayerListHud.PlayerEntry(this, currentPlayer, playerItems));
        }
        for (PlayerListHud.PlayerEntry entry : this.playerEntryList) {
            playerList.stream().filter(p -> p.getUUID().equals(entry.player.getUUID())).findFirst().ifPresent(p -> {
                if (p == mc.player) {
                    entry.updateItems(this.getPlayerItems(p));
                } else {
                    this.getPlayerItems(p).forEach(itemStack -> entry.itemStacks.putIfAbsent(itemStack.getItem(), itemStack));
                    ItemStack handItem = p.getMainHandItem();
                    if (ItemUtil.isOtherCheat(handItem)) {
                        int dmg = handItem.getDamageValue();
                        Integer prev = entry.cheatItems.get(handItem.getItem());
                        if (prev != null && dmg > prev && !entry.flaggedItems.contains(handItem.getItem())) {
                            ChatUtil.print(String.format("§c[ALERT] §f%s used a God Axe!", p.getName().getString()));
                            entry.flaggedItems.add(handItem.getItem());
                        }
                        entry.cheatItems.put(handItem.getItem(), dmg);
                    }
                    ArrayList<ItemStack> filtered = new ArrayList<>();
                    for (ItemStack itemStack : entry.itemStacks.values()) {
                        if (entry.flaggedItems.contains(itemStack.getItem())) continue;
                        filtered.add(itemStack);
                    }
                    entry.updateItems(filtered);
                }
            });
        }
        this.removedEntries.removeIf(uUID -> {
            Player player = mc.level.getPlayerByUUID(uUID);
            if (player == null) {
                return true;
            }
            MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.ABSORPTION);
            return mobEffectInstance == null || mobEffectInstance.getAmplifier() < 3;
        });
    }

    private List<ItemStack> getPlayerItems(Player player) {
        Stream<ItemStack> stream = player == mc.player
                ? Stream.concat(player.getInventory().items.stream(), Stream.concat(player.getInventory().armor.stream(), player.getInventory().offhand.stream()))
                : Stream.concat(player.getInventory().armor.stream(), Stream.of(player.getMainHandItem(), player.getOffhandItem()));
        return stream.filter(this::isCheatItem).collect(Collectors.toList());
    }

    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        MobEffectInstance mobEffectInstance;
        Player player;
        Entity entity;
        ClientboundEntityEventPacket clientboundEntityEventPacket;
        if (mc.level == null) {
            return;
        }
        Packet<?> packet = packetEvent.getPacket();
        if (packet instanceof ClientboundEntityEventPacket && (clientboundEntityEventPacket = (ClientboundEntityEventPacket)packet).getEventId() == 35 && (entity = clientboundEntityEventPacket.getEntity(mc.level)) instanceof Player) {
            player = (Player)entity;
            ChatUtil.print("§e[提示] §f" + player.getName().getString() + " 触发了不死图腾!");
            this.updatePlayerItem(player, Items.TOTEM_OF_UNDYING);
        }
        if (packet instanceof ClientboundSetEntityDataPacket dataPacket
                && (entity = mc.level.getEntity(dataPacket.id())) instanceof Player
                && (mobEffectInstance = (player = (Player)entity).getEffect(MobEffects.ABSORPTION)) != null
                && mobEffectInstance.getAmplifier() >= 3
                && player.hasEffect(MobEffects.REGENERATION)
                && !this.removedEntries.contains(player.getUUID())) {
            ChatUtil.print("§6[提示] §f" + player.getName().getString() + " 吃下了附魔金苹果!");
            this.updatePlayerItem(player, Items.ENCHANTED_GOLDEN_APPLE);
            this.removedEntries.add(player.getUUID());
        }
    }

    private void updatePlayerItem(Player player, Item item) {
        this.playerEntryList.stream().filter(playerListHud$PlayerEntry -> playerListHud$PlayerEntry.player.getUUID().equals(player.getUUID())).findFirst().ifPresent(playerListHud$PlayerEntry -> {
            playerListHud$PlayerEntry.flaggedItems.add(item);
            playerListHud$PlayerEntry.itemStacks.remove(item);
        });
    }

    @Override
    public void onRender2D(Render2DEvent render2DEvent, float f, float f2) {
    }

    @Override
    public void onGlRender(GlRenderEvent glRenderEvent, float f, float f2) {
        float f3;
        float f4;
        float f5;
        float f6;
        boolean bl;
        this.playerEntryList.removeIf(PlayerListHud.PlayerEntry::isRemoveDone);
        this.playerEntryList.forEach(PlayerListHud.PlayerEntry::tick);
        this.slideAnim.tick();
        this.fadeAnim.tick();
        this.rightAlignAnim.tick();
        if (this.playerEntryList.isEmpty() && this.slideAnim.isDone() && this.fadeAnim.isDone()) {
            return;
        }
        boolean bl2 = bl = f + this.getWidth() / 2.0f > (float)mc.getWindow().getGuiScaledWidth() / 2.0f;
        if (this.wasRightAligned != bl && this.rightAlignAnim.isDone()) {
            this.rightAlign = this.wasRightAligned;
            this.wasRightAligned = bl;
            this.rightAlignAnim.setCurrentValue(0.0);
            this.rightAlignAnim.animate(1.0, 0.3, Easings.EASE_OUT_SINE);
        }
        float f7 = 5.0f;
        float f8 = 3.0f;
        float f9 = 4.5f;
        float f10 = 15.0f;
        float f11 = 0.7f;
        String string = "";
        String string2 = "Playerlist";
        float f12 = GlHelper.getStringWidth(string, this.subFont);
        float f13 = GlHelper.getStringWidth(string2, this.nameFont);
        float f14 = (float)GlHelper.getFontAscent(this.nameFont) + f7 * 2.0f;
        float f15 = f12 + f8 + f13 + f7 * 2.0f;
        float f16 = 20.0f;
        float f17 = 16.0f;
        float f18 = f16 + f7 * 2.0f;
        float f19 = f15;
        if (!this.playerEntryList.isEmpty()) {
            f6 = 0.0f;
            for (PlayerListHud.PlayerEntry playerListHud$PlayerEntry : this.playerEntryList) {
                float f20;
                f5 = GlHelper.getStringWidth(playerListHud$PlayerEntry.displayName, this.headerFont);
                float f21 = f16 + f8 + f5 + f8 + (f20 = (float)playerListHud$PlayerEntry.items.size() * (f17 + f8)) + f7 * 2.0f;
                if (!(f21 > f6)) continue;
                f6 = f21;
            }
            f19 = Math.max(f15, f6);
        }
        if (this.slideAnim.isDone() && this.playerEntryList.isEmpty()) {
            this.slideAnim.setCurrentValue(f19);
        }
        this.slideAnim.animate(f19, 0.2, Easings.EASE_OUT_SINE);
        if (this.playerEntryList.isEmpty()) {
            this.fadeAnim.animate(0.0, 0.2, Easings.EASE_IN_POW3);
        } else {
            this.fadeAnim.animate(1.0, 0.2, Easings.EASE_OUT_POW3);
        }
        f6 = this.slideAnim.getValueF();
        float f22 = f;
        if (this.wasRightAligned) {
            f22 = f + this.getWidth() - f6;
        }
        float f23 = f22;
        f5 = f2 + f14 + f8;
        for (PlayerListHud.PlayerEntry playerListHud$PlayerEntry : this.playerEntryList) {
            if (playerListHud$PlayerEntry.visible) {
                playerListHud$PlayerEntry.visible = false;
                playerListHud$PlayerEntry.fadeAnim.setCurrentValue(f5);
                playerListHud$PlayerEntry.slideAnim.setCurrentValue(playerListHud$PlayerEntry.rightAligned ? 20.0 : -20.0);
                playerListHud$PlayerEntry.heightAnim.setCurrentValue(0.0);
                playerListHud$PlayerEntry.alphaAnim.setCurrentValue(0.0);
                playerListHud$PlayerEntry.slideAnim.animate(0.0, 0.2, Easings.EASE_OUT_POW3);
                playerListHud$PlayerEntry.heightAnim.animate(1.0, 0.2, Easings.EASE_OUT_POW3);
                playerListHud$PlayerEntry.alphaAnim.animate(f18 + f8, 0.2, Easings.EASE_OUT_POW3);
            }
            playerListHud$PlayerEntry.fadeAnim.animate(f5, 0.15, Easings.EASE_OUT_SINE);
            f5 += playerListHud$PlayerEntry.alphaAnim.getValueF();
        }
        GuiGraphics iterator = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        float f24 = this.fadeAnim.getValueF();
        if (!this.rightAlignAnim.isDone()) {
            float f25 = 1.0f - this.rightAlignAnim.getValueF();
            f4 = this.rightAlignAnim.getValueF();
            this.renderLegacy(iterator.pose(), f23, f2, f6, this.rightAlign, f24 * f25, f18, f9, f10, f11, f7);
            this.renderLegacy(iterator.pose(), f23, f2, f6, this.wasRightAligned, f24 * f4, f18, f9, f10, f11, f7);
        } else {
            this.renderLegacy(iterator.pose(), f23, f2, f6, this.wasRightAligned, f24, f18, f9, f10, f11, f7);
        }
        DrawContext drawContext = glRenderEvent.drawContext();
        if (!this.rightAlignAnim.isDone()) {
            f4 = 1.0f - this.rightAlignAnim.getValueF();
            f3 = this.rightAlignAnim.getValueF();
            this.renderEntry(drawContext, f23, f2, f6, this.rightAlign, f24 * f4, string, string2, f12, f13, f14, f7, f8, f9, f16);
            this.renderEntry(drawContext, f23, f2, f6, this.wasRightAligned, f24 * f3, string, string2, f12, f13, f14, f7, f8, f9, f16);
        } else {
            this.renderEntry(drawContext, f23, f2, f6, this.wasRightAligned, f24, string, string2, f12, f13, f14, f7, f8, f9, f16);
        }
        if (!this.rightAlignAnim.isDone()) {
            f4 = 1.0f - this.rightAlignAnim.getValueF();
            f3 = this.rightAlignAnim.getValueF();
            this.renderEntryGui(iterator, f23, f2, f6, this.rightAlign, f24 * f4, f16, f17, f7, f8);
            this.renderEntryGui(iterator, f23, f2, f6, this.wasRightAligned, f24 * f3, f16, f17, f7, f8);
        } else {
            this.renderEntryGui(iterator, f23, f2, f6, this.wasRightAligned, f24, f16, f17, f7, f8);
        }
        iterator.flush();
        if (this.wasRightAligned) {
            this.setX(f22);
        }
        this.setWidth(f6);
        this.setHeight(f5 - f2);
    }

    private void renderLegacy(PoseStack poseStack, float f, float f2, float f3, boolean bl, float f4, float f5, float f6, float f7, float f8, float f9) {
        if (f4 <= 0.01f) {
            return;
        }
        for (PlayerListHud.PlayerEntry playerListHud$PlayerEntry : this.playerEntryList) {
            float f10 = playerListHud$PlayerEntry.heightAnim.getValueF();
            if (f10 <= 0.0f) continue;
            float f11 = bl ? f + f3 - playerListHud$PlayerEntry.widthAnim.getValueF() : f;
            float f12 = f11 + playerListHud$PlayerEntry.slideAnim.getValueF();
            float f13 = playerListHud$PlayerEntry.fadeAnim.getValueF();
            float f14 = f8 * f4 * f10;
            float f15 = playerListHud$PlayerEntry.widthAnim.getValueF();
            float f16 = f12;
            RenderUtil.drawBlurredRect(poseStack, f16, f13, f15, f5, f6, f7, f14, 0);
        }
    }

    private void renderEntry(DrawContext drawContext, float f, float f2, float f3, boolean bl, float f4, String string, String string2, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
        if (f4 <= 0.01f) {
            return;
        }
        float f12 = f2 + f8 + (f7 - f8 * 2.0f - (float)GlHelper.getFontAscent(this.nameFont)) / 2.0f + 1.0f;
        int n = ColorUtil.fromARGB(255, 255, 255, (int)(255.0f * f4));
        Paint paint = GlHelper.toPaint(n);
        try (Paint paint2 = new Paint()){
            paint2.setColor(ColorUtil.fromARGB(0, 0, 0, (int)(190.0f * f4)));
            GlHelper.drawRoundedRect(f, f2, f3, f7, f10, paint2);
        }
        if (bl) {
            float f13 = f + f3 - f8 - f5;
            float f14 = f13 - f9 - f6;
            GlHelper.drawTextWithShadow(string2, f14, f12, this.nameFont, paint);
            GlHelper.drawTextWithShadow(string, f13, f12 + 1.0f, this.subFont, paint);
        } else {
            GlHelper.drawTextWithShadow(string, f + f8, f12 + 1.0f, this.subFont, paint);
            GlHelper.drawTextWithShadow(string2, f + f8 + f5 + f9, f12, this.nameFont, paint);
        }
        for (PlayerListHud.PlayerEntry playerListHud$PlayerEntry : this.playerEntryList) {
            float f15;
            float f16;
            float f17 = playerListHud$PlayerEntry.heightAnim.getValueF();
            if (f17 <= 0.0f || (f16 = f4 * f17) <= 0.0f) continue;
            float f18 = bl ? f + f3 - playerListHud$PlayerEntry.widthAnim.getValueF() : f;
            float f19 = f18 + playerListHud$PlayerEntry.slideAnim.getValueF();
            float f20 = playerListHud$PlayerEntry.fadeAnim.getValueF();
            float f21 = f11 + f8 * 2.0f;
            try (Paint paint3 = new Paint()){
                paint3.setColor(ColorUtil.fromARGB(0, 0, 0, (int)(90.0f * f16)));
                float f22 = playerListHud$PlayerEntry.widthAnim.getValueF();
                float f23 = f19;
                GlHelper.drawRoundedRect(f23, f20, f22, f21, f10, paint3);
            }
            float f24 = f20 + f8;
            int n2 = playerListHud$PlayerEntry.player == mc.player ? ColorUtil.fromRGB(100, 150, 255) : -1;
            int n3 = ColorUtil.withAlpha(n2, (int)f16);
            float f25 = f20 + f8 + (f21 - f8 * 2.0f - (float)GlHelper.getFontAscent(this.headerFont)) / 2.0f;
            if (bl) {
                f15 = f19 + playerListHud$PlayerEntry.widthAnim.getValueF();
                float f26 = f15 - f8 - f11;
                if (playerListHud$PlayerEntry.player instanceof AbstractClientPlayer) {
                    GlHelper.drawPlayerHeadRounded((AbstractClientPlayer)playerListHud$PlayerEntry.player, f26, f24, f11, f11, f16, f10);
                }
                float f27 = f26 - f9 - playerListHud$PlayerEntry.nameWidth;
                GlHelper.drawTextShadowLegacy(playerListHud$PlayerEntry.displayName, f27, f25, this.headerFont, n3);
                continue;
            }
            if (playerListHud$PlayerEntry.player instanceof AbstractClientPlayer) {
                f15 = f19 + f8;
                GlHelper.drawPlayerHeadRounded((AbstractClientPlayer)playerListHud$PlayerEntry.player, f15, f24, f11, f11, f16, f10);
            }
            f15 = f19 + f8 + f11 + f9;
            GlHelper.drawTextShadowLegacy(playerListHud$PlayerEntry.displayName, f15, f25, this.headerFont, n3);
        }
    }

    private void renderEntryGui(GuiGraphics guiGraphics, float f, float f2, float f3, boolean bl, float f4, float f5, float f6, float f7, float f8) {
        if (f4 <= 0.01f) {
            return;
        }
        for (PlayerListHud.PlayerEntry playerListHud$PlayerEntry : this.playerEntryList) {
            float f9;
            float f10 = playerListHud$PlayerEntry.heightAnim.getValueF();
            if (f10 <= 0.0f || (f9 = f4 * f10) <= 0.01f) continue;
            float f11 = bl ? f + f3 - playerListHud$PlayerEntry.widthAnim.getValueF() : f;
            float f12 = f11 + playerListHud$PlayerEntry.slideAnim.getValueF();
            float f13 = playerListHud$PlayerEntry.fadeAnim.getValueF();
            float f14 = f13 + f7 + (f5 - f6) / 2.0f;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f9);
            float rightEdge;
            if (bl) {
                rightEdge = f12 + playerListHud$PlayerEntry.widthAnim.getValueF();
                float f15 = rightEdge - f7 - f5;
                float f16 = f15 - f8 - playerListHud$PlayerEntry.nameWidth;
                float f17 = (float)playerListHud$PlayerEntry.items.size() * (f6 + f8);
                float f18 = f16 - f8 - f17;
                for (ItemStack itemStack : playerListHud$PlayerEntry.items) {
                    PoseStack poseStack = guiGraphics.pose();
                    poseStack.pushPose();
                    poseStack.translate(f18, f14, 0.0f);
                    guiGraphics.renderItem(itemStack, 0, 0);
                    poseStack.popPose();
                    f18 += f6 + f8;
                }
            } else {
                rightEdge = f12 + f7 + f5 + f8 + playerListHud$PlayerEntry.nameWidth + f8;
                for (ItemStack itemStack : playerListHud$PlayerEntry.items) {
                    PoseStack poseStack = guiGraphics.pose();
                    poseStack.pushPose();
                    poseStack.translate(rightEdge, f14, 0.0f);
                    guiGraphics.renderItem(itemStack, 0, 0);
                    poseStack.popPose();
                    rightEdge += f6 + f8;
                }
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
        guiGraphics.flush();
    }

    @Override
    public void onSettings() {
    }
}