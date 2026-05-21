package shit.zen.event.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import shit.zen.event.EventMarker;

import java.util.Objects;

public class UpdateHeldItemEvent
implements EventMarker {
    @Getter
    private final InteractionHand hand;
    @Getter @Setter
    private ItemStack itemStack;

    @Generated
    public UpdateHeldItemEvent(InteractionHand interactionHand, ItemStack itemStack) {
        this.hand = interactionHand;
        this.itemStack = itemStack;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof UpdateHeldItemEvent updateHeldItemEvent)) {
            return false;
        }
        if (!updateHeldItemEvent.canEqual(this)) {
            return false;
        }
        InteractionHand interactionHand = this.getHand();
        InteractionHand interactionHand2 = updateHeldItemEvent.getHand();
        if (!Objects.equals(interactionHand, interactionHand2)) {
            return false;
        }
        ItemStack itemStack = this.getItemStack();
        ItemStack itemStack2 = updateHeldItemEvent.getItemStack();
        return !(!Objects.equals(itemStack, itemStack2));
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof UpdateHeldItemEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        InteractionHand interactionHand = this.getHand();
        n2 = n2 * 59 + (interactionHand == null ? 43 : interactionHand.hashCode());
        ItemStack itemStack = this.getItemStack();
        n2 = n2 * 59 + (itemStack == null ? 43 : itemStack.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return "UpdateHeldItemEvent(hand=" + this.getHand() + ", item=" + this.getItemStack() + ")";
    }
}