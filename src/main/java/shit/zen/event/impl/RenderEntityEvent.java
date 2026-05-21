package shit.zen.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Generated;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import shit.zen.event.Event;

public class RenderEntityEvent
extends Event {
    @Getter
    private final EntityRenderer<?> entityRenderer;
    @Getter
    private final Entity entity;
    @Getter
    private final PoseStack poseStack;
    @Getter
    private final MultiBufferSource bufferSource;
    @Getter
    private final float partialTick;
    @Getter
    private final int packedLight;
    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean bl) {
        this.cancelled = bl;
    }

    @Generated
    public RenderEntityEvent(EntityRenderer<?> entityRenderer, Entity entity, PoseStack poseStack, MultiBufferSource multiBufferSource, float f, int n, boolean bl) {
        this.entityRenderer = entityRenderer;
        this.entity = entity;
        this.poseStack = poseStack;
        this.bufferSource = multiBufferSource;
        this.partialTick = f;
        this.packedLight = n;
        this.cancelled = bl;
    }

    public static class Pre extends RenderEntityEvent {
        public Pre(EntityRenderer<?> renderer, Entity entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight) {
            super(renderer, entity, poseStack, bufferSource, partialTick, packedLight, false);
        }
    }

    public static class Post extends RenderEntityEvent {
        public Post(EntityRenderer<?> renderer, Entity entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight) {
            super(renderer, entity, poseStack, bufferSource, partialTick, packedLight, false);
        }
    }
}