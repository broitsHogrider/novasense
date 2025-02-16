package ru.novacore.utils.font;

import lombok.SneakyThrows;
import ru.novacore.utils.font.common.Lang;
import ru.novacore.utils.font.styled.StyledFont;

public class FontManager {

    public static final String FONT_DIR = "/assets/minecraft/novacore/font/";
    public static volatile StyledFont[] minecraft = new StyledFont[9];
    public static volatile StyledFont[] font = new StyledFont[33];
    public static volatile StyledFont[] sfBold = new StyledFont[24];
    public static volatile StyledFont[] sfMedium = new StyledFont[24];
    public static volatile StyledFont[] sfRegular = new StyledFont[24];
    public static volatile StyledFont[] montserrat = new StyledFont[33];
    public static volatile StyledFont[] fontBold = new StyledFont[81];
    public static volatile StyledFont[] sfBold1 = new StyledFont[81];
    public static volatile StyledFont[] icon1 = new StyledFont[23];
    public static volatile StyledFont[] icon2 = new StyledFont[33];
    public static volatile StyledFont[] wex = new StyledFont[131];
    public static volatile StyledFont[] nurik = new StyledFont[131];
    public static volatile StyledFont[] relake = new StyledFont[24];

    @SneakyThrows
    public static void init() {
        minecraft[8] = new StyledFont("minecraft.ttf", 8, 0, 0, false, Lang.ENG_RU);

        fontBold[80] = new StyledFont("font-bold.ttf", 80, 0, 0, true, Lang.ENG_RU);
        sfBold1[80] = new StyledFont("sf_bold.ttf", 80, 0, 0, true, Lang.ENG_RU);
        for (int i = 8; i < 33; i++) {
            font[i] = new StyledFont("font.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 33; i++) {
            montserrat[i] = new StyledFont("Montserrat-SemiBold.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
        for (int i = 3; i < 24; i++) {
            sfBold[i] = new StyledFont("sf_bold.ttf", i, 0, 0,true, Lang.ENG_RU);
        }
        for (int i = 3; i < 24; i++) {
            sfMedium[i] = new StyledFont("sf_medium.ttf", i, 0, 0,true, Lang.ENG_RU);
        }
        for (int i = 3; i < 24; i++) {
            relake[i] = new StyledFont("relake.ttf", i, 0, 0,true, Lang.ENG_RU);
        }
        for (int i = 3; i < 24; i++) {
            sfRegular[i] = new StyledFont("sf_regular.ttf", i, 0, 0,true, Lang.ENG_RU);
        }
        for (int i = 8; i < 33; i++) {
            fontBold[i] = new StyledFont("font-bold.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 23; i++) {
            icon1[i] = new StyledFont("icon-1.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 33; i++) {
            icon2[i] = new StyledFont("icon-2.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
        for (int i = 3; i < 131; i++) {
            wex[i] = new StyledFont("wexside.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
        for (int i = 3; i < 131; i++) {
            nurik[i] = new StyledFont("iconz.ttf", i, 0, 0, true, Lang.ENG_RU);
        }
    }
}