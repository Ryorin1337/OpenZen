package shit.zen.render;

import net.minecraft.resources.ResourceLocation;

public final class Texture {
    private final int glId;
    private final int width;
    private final int height;
    private final ResourceLocation resourceLocation;

    public Texture(int n, int n2, int n3) {
        this.glId = n;
        this.width = n2;
        this.height = n3;
        this.resourceLocation = null;
    }

    public Texture(ResourceLocation resourceLocation, int n, int n2) {
        this.glId = 0;
        this.width = n;
        this.height = n2;
        this.resourceLocation = resourceLocation;
    }

    public Texture(int n, ResourceLocation resourceLocation, int n2, int n3) {
        this.glId = n;
        this.width = n2;
        this.height = n3;
        this.resourceLocation = resourceLocation;
    }

    public int getGlId() {
        return this.glId;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }
}