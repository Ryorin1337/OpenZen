package shit.zen.utils.animation;

import lombok.Getter;
import lombok.Setter;

public class SpringAnimation {
    private final float stiffness;
    private final float mass;
    private final float damping;
    @Getter @Setter
    private float targetValue;
    private float currentValue;
    private float velocity;

    public SpringAnimation(float f, float f2, float f3, float f4) {
        this.stiffness = f;
        this.mass = f2;
        this.damping = f3;
        this.currentValue = f4;
        this.targetValue = f4;
    }

    public void reset(float f) {
        this.currentValue = f;
        this.targetValue = f;
        this.velocity = 0.0f;
    }

    public void update(float f) {
        if (f <= 0.0f) {
            return;
        }
        float f2 = -this.stiffness * (this.currentValue - this.targetValue) - this.damping * this.velocity;
        float f3 = f2 / this.mass;
        this.velocity += f3 * f;
        this.currentValue += this.velocity * f;
    }

    public float getValue() {
        return this.currentValue;
    }

    public void setValue(float f) {
        this.currentValue = f;
    }

    }