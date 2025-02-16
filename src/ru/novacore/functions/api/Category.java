package ru.novacore.functions.api;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Category {

    Combat("b", "Aura, AutoTotem. Other...", 0),
    Movement("g", "Strafe, Speed, Other...", 0),
    Render("c", "Tracers, ESP, Other...", 0),
    Player("d", "NoClip, AutoPotion, Other...", 0),
    Misc("f", "MCF, MCP, Other...", 0),
    Theme("m", "Green, Blue, Other...", 0);

    public final String icon;
    public final String info;
    public float anim;

}
