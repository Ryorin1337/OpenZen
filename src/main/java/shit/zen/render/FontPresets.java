package shit.zen.render;

import shit.zen.render.FontRenderer;
import shit.zen.render.Fonts;

public final class FontPresets {
    public static FontRenderer pingfang(float f) {
        return Fonts.getRenderer("pingfang_sc_regular.ttf", f);
    }

    public static FontRenderer productSans(float f) {
        return Fonts.getRenderer("product_sans_regular.ttf", f);
    }

    public static FontRenderer astaSans(float f) {
        return Fonts.getRenderer("AstaSans-Medium.ttf", f);
    }

    public static FontRenderer poppinsRegular(float f) {
        return Fonts.getRenderer("Poppins-Regular.ttf", f);
    }

    public static FontRenderer poppinsMedium(float f) {
        return Fonts.getRenderer("Poppins-Medium.ttf", f);
    }

    public static FontRenderer poppinsBold(float f) {
        return Fonts.getRenderer("Poppins-Bold.ttf", f);
    }

    public static FontRenderer zenIcon(float f) {
        return Fonts.getRenderer("zenicon-Regular.ttf", f);
    }

    public static FontRenderer museoSans(float f) {
        return Fonts.getRenderer("MuseoSansCyrl-900.ttf", f);
    }

    public static FontRenderer materialIcons(float f) {
        return Fonts.getRenderer("MaterialIcons-Regular.ttf", f);
    }

    public static FontRenderer axiformaBold(float f) {
        return Fonts.getRenderer("axiforma_bold.ttf", f);
    }

    public static FontRenderer axiformaRegular(float f) {
        return Fonts.getRenderer("axiforma_regular.ttf", f);
    }

    public static FontRenderer axiformaExtraBold(float f) {
        return Fonts.getRenderer("axiforma_extrabold.ttf", f);
    }
}