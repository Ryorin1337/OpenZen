package shit.zen.event.impl;

import lombok.Getter;
import lombok.Generated;
import shit.zen.event.Event;

import java.util.Objects;

public class ChatEvent
extends Event {
    @Getter
    private final String message;
    private static final String TO_STRING_PREFIX = "ChatEvent(message=";

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof ChatEvent chatEvent)) {
            return false;
        }
        if (!chatEvent.canEqual(this)) {
            return false;
        }
        String string = this.getMessage();
        String string2 = chatEvent.getMessage();
        return !(!Objects.equals(string, string2));
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof ChatEvent;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        String string = this.getMessage();
        n2 = n2 * 59 + (string == null ? 43 : string.hashCode());
        return n2;
    }

    @Generated
    public String toString() {
        return TO_STRING_PREFIX + this.getMessage() + ")";
    }

    @Generated
    public ChatEvent(String string) {
        this.message = string;
    }
}