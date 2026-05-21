package shit.zen.utils.math;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Vector2f {
    @Getter @Setter
    public float x;
    @Getter @Setter
    public float y;

    public Vector2f(Vector2f vector2f) {
        this(vector2f.x, vector2f.y);
    }

    public Vector2f add(float f, float f2) {
        return new Vector2f(this.x + f, this.y + f2);
    }

    public Vector2f fill(float f) {
        this.x = f;
        this.y = f;
        return this;
    }

    public Vector2f set(float f, float f2) {
        this.x = f;
        this.y = f2;
        return this;
    }

    public Vector2f fillD(double d) {
        this.x = (float)d;
        this.y = (float)d;
        return this;
    }

    public Vector2f setD(double d, double d2) {
        this.x = (float)d;
        this.y = (float)d2;
        return this;
    }

    public Vector2f fromArray(float[] fArray) {
        this.x = fArray[0];
        this.y = fArray[1];
        return this;
    }

}