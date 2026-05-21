package shit.zen.event.impl;

import lombok.Getter;
import lombok.Generated;
import shit.zen.event.Event;

public class KeyEvent
extends Event {
    @Getter
    private final int keyCode;
    @Getter
    private final boolean pressed;

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof KeyEvent keyEvent)) {
            return false;
        }
        if (!keyEvent.canEqual(this)) {
            return false;
        }
        if (this.getKeyCode() != keyEvent.getKeyCode()) {
            return false;
        }
        return this.isPressed() == keyEvent.isPressed();
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof KeyEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + this.getKeyCode();
        n2 = n2 * 59 + (this.isPressed() ? 79 : 97);
        return n2;
    }

    @Generated
    public String toString() {
        return "KeyEvent(key=" + this.getKeyCode() + ", state=" + this.isPressed() + ")";
    }

    @Generated
    public KeyEvent(int n, boolean bl) {
        this.keyCode = n;
        this.pressed = bl;
    }
}