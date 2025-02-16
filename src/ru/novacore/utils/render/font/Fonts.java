package ru.novacore.utils.render.font;

public class Fonts {

    public static Font wexside, interMedium, sfbold, sfMedium, sfRegular;

    public static void register() {
        wexside = new Font("wexside.png", "wexside.json");
        interMedium = new Font("Inter-Medium.png", "Inter-Medium.json");
        sfbold = new Font("sf_bold_.png", "sf_bold_.json");
        sfMedium = new Font("sf_medium.png", "sf_medium.json");
        sfRegular = new Font("sf_regular.png", "sf_regular.json");
    }
}
