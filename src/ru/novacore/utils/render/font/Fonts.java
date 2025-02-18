package ru.novacore.utils.render.font;

public class Fonts {

    public static Font wexside, interMedium, sfbold, sfMedium, sfRegular;

    public static void register() {
        wexside = new Font("wexside", "wexside");
        interMedium = new Font("Inter-Medium", "Inter-Medium");
        sfbold = new Font("sf_bold_", "sf_bold_");
        sfMedium = new Font("sf_medium", "sf_medium");
        sfRegular = new Font("sf_regular", "sf_regular");
    }
}
