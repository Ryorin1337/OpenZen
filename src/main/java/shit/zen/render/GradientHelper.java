package shit.zen.render;

import shit.zen.render.Paint.GradientCoords;

public final class GradientHelper {
    public static Paint.GradientCoords createLinearGradientEx(float f, float f2, float f3, float f4, int[] nArray, float[] fArray, Object object) {
        int n = nArray.length > 0 ? nArray[0] : 0;
        int n2 = nArray.length > 1 ? nArray[nArray.length - 1] : n;
        return new Paint.GradientCoords(f, f2, f3, f4, n, n2);
    }

    public static Paint.GradientCoords createLinearGradient(float f, float f2, float f3, float f4, int[] nArray) {
        return GradientHelper.createLinearGradientEx(f, f2, f3, f4, nArray, null, null);
    }
}