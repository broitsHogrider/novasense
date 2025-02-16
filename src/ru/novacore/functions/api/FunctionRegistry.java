package ru.novacore.functions.api;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.lwjgl.system.CallbackI;
import ru.novacore.NovaCore;
import ru.novacore.events.EventKey;
import ru.novacore.functions.impl.combat.*;
import ru.novacore.functions.impl.misc.*;
import ru.novacore.functions.impl.movement.*;
import ru.novacore.functions.impl.player.*;
import ru.novacore.functions.impl.render.*;
import ru.novacore.utils.render.font.Font;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class FunctionRegistry {
    private final List<Function> functions = new CopyOnWriteArrayList<>();

    private SwingAnimation swingAnimation;
    private AutoGapple autoGapple;
    private AutoSprint autoSprint;
    private Velocity velocity;
    private NoRender noRender;
    private Timer timer;
    private AutoTool autoTool;
    private xCarry xcarry;
    private ElytraHelper elytrahelper;
    private Phase phase;
    private ItemSwapFix itemswapfix;
    private AutoPotion autopotion;
    private TriggerBot triggerbot;
    private NoJumpDelay nojumpdelay;
    private ClickFriend clickfriend;
    private GuiMove guiMove;
    private ESP esp;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwap autoSwap;
    private AutoArmor autoArmor;
    private Hitbox hitbox;
    private AntiPush antiPush;
    private FreeCam freeCam;
    private ChestStealer chestStealer;
    private AutoLeave autoLeave;
    private AutoAccept autoAccept;
    private Fly fly;
    private TargetStrafe targetStrafe;
    private ClientSounds clientSounds;
    private AutoTotem autoTotem;
    private NoSlow noSlow;
    private Pointers pointers;
    private AutoExplosion autoExplosion;
    private NoRotate noRotate;
    private AttackAura attackAura;
    private AntiBot antiBot;
    private Trails trails;
    private Crosshair crosshair;
    private Strafe strafe;
    private World world;
    private ViewModel viewModel;
    private ElytraFly elytraFly;
    private ChinaHat chinaHat;
    private Particles particles;
    private TargetESP targetESP;
    private JumpCircle jumpCircle;
    private ItemPhysic itemPhysic;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private NoClip noClip;
    private ItemScroller itemScroller;
    private StorageESP storageESP;
    private Spider spider;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private Tracers tracers;
    private SelfDestruct selfDestruct;
    private FreeLook freeLook;
    private BetterMinecraft betterMinecraft;
    private HudNew hudNew;
    private ElytraBoost elytraBoost;
    private Item360 item360;
    private GlassHand glassHand;
    private AntiTarget antiTarget;
    private NoFriendDamage noFriendDamage;
    private DragonFly dragonFly;
    //private RWJoiner rwJoiner;

    public void init() {
        registerAll( item360 = new Item360(), noFriendDamage = new NoFriendDamage(), dragonFly = new DragonFly(),  glassHand = new GlassHand(), /*rwJoiner = new RWJoiner(),*/   freeLook = new FreeLook(),   antiTarget = new AntiTarget(),  hudNew = new HudNew(),elytraBoost = new ElytraBoost(), autoGapple = new AutoGapple(), autoSprint = new AutoSprint(), velocity = new Velocity(), noRender = new NoRender(), autoTool = new AutoTool(), xcarry = new xCarry(),  elytrahelper = new ElytraHelper(), phase = new Phase(), itemswapfix = new ItemSwapFix(), autopotion = new AutoPotion(), noClip = new NoClip(), triggerbot = new TriggerBot(), nojumpdelay = new NoJumpDelay(), clickfriend = new ClickFriend(), guiMove = new GuiMove(), esp = new ESP(),   autoArmor = new AutoArmor(), hitbox = new Hitbox(), antiPush = new AntiPush(), freeCam = new FreeCam(), chestStealer = new ChestStealer(), autoLeave = new AutoLeave(), autoAccept = new AutoAccept(), fly = new Fly(), clientSounds = new ClientSounds(), noSlow = new NoSlow(), pointers = new Pointers(), autoExplosion = new AutoExplosion(), noRotate = new NoRotate(), antiBot = new AntiBot(), trails = new Trails(), crosshair = new Crosshair(), autoTotem = new AutoTotem(), itemCooldown = new ItemCooldown(), attackAura = new AttackAura(autopotion), clickPearl = new ClickPearl(), autoSwap = new AutoSwap(autoTotem), targetStrafe = new TargetStrafe(attackAura), strafe = new Strafe(targetStrafe, attackAura), swingAnimation = new SwingAnimation(attackAura), targetESP = new TargetESP(attackAura), world = new World(), viewModel = new ViewModel(), elytraFly = new ElytraFly(), chinaHat = new ChinaHat(),  particles = new Particles(), jumpCircle = new JumpCircle(), itemPhysic = new ItemPhysic(), predictions = new Predictions(), noEntityTrace = new NoEntityTrace(), itemScroller = new ItemScroller(), storageESP = new StorageESP(), spider = new Spider(), timer = new Timer(), nameProtect = new NameProtect(), noInteract = new NoInteract(),  tracers = new Tracers(), selfDestruct = new SelfDestruct(),    betterMinecraft = new BetterMinecraft(),  new RWHelper());

        NovaCore.getInstance().getEventBus().register(this);
    }

    private void registerAll(Function... Functions) {
        Arrays.sort(Functions, Comparator.comparing(Function::getName));

        functions.addAll(List.of(Functions));
    }

    public List<Function> getSorted(Font font, float size) {
        return functions.stream().sorted((f1, f2) -> Float.compare(font.getWidth(f2.getName(), size), font.getWidth(f1.getName(), size))).toList();
    }


    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        for (Function Function : functions) {
            if (Function.getBind() == e.getKey()) {
                Function.toggle();
            }
        }
    }
}
