package ru.novacore.utils.font.common;

import lombok.Getter;

@Getter
public enum Lang {

    ENG(new int[]{31, 127, 0, 0}),
    ENG_RU(new int[]{31, 127, 1024, 1106});

    public final int[] charCodes;

    Lang(int[] charCodes) {
        this.charCodes = charCodes;
    }
}