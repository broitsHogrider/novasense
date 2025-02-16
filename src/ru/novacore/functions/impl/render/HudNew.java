package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import ru.novacore.NovaCore;
import ru.novacore.command.staffs.StaffStorage;
import ru.novacore.events.EventDisplay;
import ru.novacore.events.EventUpdate;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.ui.styles.StyleManager;
import ru.novacore.utils.animations.Animation;
import ru.novacore.utils.animations.Direction;
import ru.novacore.utils.animations.impl.DecelerateAnimation;
import ru.novacore.utils.animations.impl.EaseBackIn;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.client.KeyStorage;
import ru.novacore.utils.drag.Dragging;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.StopWatch;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.text.GradientUtil;

import java.util.*;
import java.util.regex.Pattern;

@FunctionInfo(name = "Interface", category = Category.Render)
public class HudNew extends Function {

    private final ModeListSetting modeListSetting = new ModeListSetting("ХУД",
            new BooleanSetting("КЛАВИШИ", true),
            new BooleanSetting("СПИСОК ПЕРСОНАЛА", true),
            new BooleanSetting("СПИСОК ЭФФЕКТОВ", true),
            new BooleanSetting("ВОДЯНОЙ ЗНАК", true),
            new BooleanSetting("ИНФОРМАЦИЯ О МИРЕ", true),
            new BooleanSetting("ТАРГЕТ-ХУД", true),
            new BooleanSetting("БРОНЯ", true),
            new BooleanSetting("УВЕДОМЛЕНИЕ", true));

    public HudNew() {
        addSettings(modeListSetting);
    }

    private Dragging keybinds = NovaCore.getInstance().createDrag(this, "keybinds", 100, 100);
    private Dragging staffOnlineDraggable = NovaCore.getInstance().createDrag(this, "staff list", 200, 100);
    private Dragging activePotionsDraggable = NovaCore.getInstance().createDrag(this, "potions", 300, 100);

    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");

    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE("", -1),
        VANISHED("V", ColorUtils.rgb(254, 68, 68));
        public final String string;
        public final int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }

    @Subscribe
    public void onDisplay(EventDisplay eventDisplay) {
        if (mc.gameSettings.showDebugInfo || eventDisplay.getType() != EventDisplay.Type.POST) return;
        MatrixStack matrixStack = eventDisplay.getMatrixStack();

        if (modeListSetting.get(0).get()) onKeyBindsRenderer(matrixStack);
        if (modeListSetting.get(1).get()) onStaffListRenderer(matrixStack);
        if (modeListSetting.get(2).get()) onPotionRenderer(matrixStack);
        if (modeListSetting.get(3).get()) onWaterMarkRenderer(matrixStack);
        if (modeListSetting.get(4).get()) onWorldInformationRenderer(matrixStack);
        if (modeListSetting.get(5).get()) onTargetInfoRenderer(matrixStack);
        if (modeListSetting.get(6).get()) onArmorRenderer(matrixStack);
        if (modeListSetting.get(7).get()) NovaCore.getInstance().getNotificationManager().draw(matrixStack);

    }

    private void onWaterMarkRenderer(MatrixStack matrixStack) {
        float x = 5, y = 5;
        float width = 10, height = 12;

        String title = "novacore";
        String fpsText = mc.debugFPS + " fps";
        String pingText = calculatePing() + " ping";
        float titleWidth = FontManager.sfBold[16].getWidth(title);
        float separatorWidth = FontManager.sfBold[16].getWidth(" . ");
        float fpsWidth = FontManager.sfBold[16].getWidth(fpsText);
        float pingWidth = FontManager.sfBold[16].getWidth(pingText);
        width += titleWidth + separatorWidth * 1.5f + fpsWidth + pingWidth;

        RenderUtils.Render2D.drawShadow(x, y, width, height, 8, ColorUtils.getColor(0), ColorUtils.getColor(90), ColorUtils.getColor(180), ColorUtils.getColor(270));
        RenderUtils.Render2D.drawRound(x, y, width, height, 3, ColorUtils.rgb(18, 18, 18));

        float textX = x + 2.5f;
        FontManager.sfBold[16].drawString(matrixStack, GradientUtil.gradient(title), textX, y + 3.5f, -1);

        textX += titleWidth;
        //FontManager.sfBold[16].drawString(matrixStack, " . ", textX, y + 3.5f, ColorUtils.rgb(200, 200, 200));
        RenderUtils.Render2D.drawRoundCircle(textX + 3, y + 6f, 2, ColorUtils.rgb(200, 200, 200));

        textX += separatorWidth;
        FontManager.sfBold[16].drawString(matrixStack, fpsText, textX, y + 3.5f, ColorUtils.rgb(225, 225, 225));

        textX += fpsWidth;
        //FontManager.sfBold[16].drawString(matrixStack, " . ", textX, y + 3.5f, ColorUtils.rgb(200, 200, 200));
        RenderUtils.Render2D.drawRoundCircle(textX + 3, y + 6f, 2, ColorUtils.rgb(200, 200, 200));

        textX += separatorWidth;
        FontManager.sfBold[16].drawString(matrixStack, pingText, textX, y + 3.5f, ColorUtils.rgb(225, 225, 225));
    }
    private float maxWid;
    float maxHeg;
    private int activeHotKeys = 0;
    float animateW = 0, animateH = 0;
    private void onKeyBindsRenderer(MatrixStack matrixStack) {
        float posX = keybinds.getX(), posY = keybinds.getY(), offset = 11;
        float width = Math.max(maxWid, 80), height = activeHotKeys * offset;

        animateW = MathUtil.lerp(animateW, width, 15);
        animateH = MathUtil.lerp(animateH, height, 15);

        RenderUtils.Render2D.drawShadow(posX, posY, animateW, animateH + 20, 8, ColorUtils.getColor(0), ColorUtils.getColor(90), ColorUtils.getColor(180), ColorUtils.getColor(270));
        RenderUtils.Render2D.drawRound(posX, posY, animateW, animateH + 20, 5, ColorUtils.rgb(18, 18, 18));
        RenderUtils.Render2D.drawCornerRound(posX, posY, animateW, 16.5f, 5, ColorUtils.rgb(25, 25, 25), RenderUtils.Render2D.Corner.TOP);
        FontManager.sfBold[18].drawCenteredString(matrixStack, GradientUtil.gradient("Keybinds"), posX + animateW / 2f, posY + 6, -1);

        int index = 0;
        maxWid = 0;
        for (Function module : NovaCore.getInstance().getFunctionRegistry().getFunctions()) {
            module.getAnimation().update();
            if (module.bind != 0 && module.isState()) {
                float offsetY = posY + (index * offset) + 20.5f;

                String text = "[" + KeyStorage.getKey(module.bind).toUpperCase() + "]";
                float nameWidth = FontManager.sfBold[16].getWidth(module.getName());
                float bindWidth = FontManager.sfBold[16].getWidth(text);

                FontManager.sfBold[16].drawString(matrixStack, module.getName(), posX + 4, offsetY,ColorUtils.rgba(200, 200, 200, (int) (255 * module.getAnimation().getValue())));
                FontManager.sfBold[16].drawString(matrixStack, text, posX + animateW - bindWidth - 3, offsetY,ColorUtils.rgba(200, 200, 200, (int) (255 * module.getAnimation().getValue())));

                maxWid = (float) Math.max(maxWid, nameWidth + bindWidth + 12);
                index++;
            }
        }
        activeHotKeys = index;
        keybinds.setWidth(animateW);
        keybinds.setHeight(animateH + 20);
    }
    private int activeStaffs = 0;
    float slAnimateWidth = 0, slAnimateHeight = 0;
    float maxWidth;
    private void onStaffListRenderer(MatrixStack matrixStack) {
        float posX = staffOnlineDraggable.getX(), posY = staffOnlineDraggable.getY(), offset = 11;
        float width = Math.max(maxWidth, 80), height = activeStaffs * offset;
        slAnimateWidth = MathUtil.lerp(slAnimateWidth, width, 15);
        slAnimateHeight = MathUtil.lerp(slAnimateHeight, height, 15);

        RenderUtils.Render2D.drawShadow(posX, posY, slAnimateWidth, slAnimateHeight + 20, 8, ColorUtils.getColor(0), ColorUtils.getColor(90), ColorUtils.getColor(180), ColorUtils.getColor(270));
        RenderUtils.Render2D.drawRound(posX, posY, slAnimateWidth, slAnimateHeight + 20, 5, ColorUtils.rgb(18, 18, 18));
        RenderUtils.Render2D.drawCornerRound(posX, posY, slAnimateWidth, 16.5f, 5, ColorUtils.rgb(25, 25, 25), RenderUtils.Render2D.Corner.TOP);
        FontManager.sfBold[18].drawCenteredString(matrixStack, GradientUtil.gradient("Staff Statistics"), posX + slAnimateWidth / 2f, posY + 6, -1);

        int index = 0;
        maxWidth = 0;
        for (Staff staffPlayer : staffPlayers) {
            float offsetY = posY + (index * offset) + 20.5f;

            ITextComponent prefix = staffPlayer.getPrefix();
            String name = (prefix.getString().isEmpty() ? "" : " ") + staffPlayer.getName();
            float prefixWidth = FontManager.sfBold[16].getWidth(prefix);
            float nameWidth = FontManager.sfBold[16].getWidth(name);
            float statusWidth = FontManager.sfBold[16].getWidth(staffPlayer.getStatus().string);

            boolean isReallyWorld = ClientUtil.isConnectedToServer("reallyworld");

            FontManager.sfBold[16].drawPrefix(matrixStack, prefix, !isReallyWorld ? posX + 4 : posX + 2, offsetY, 255);
            FontManager.sfBold[16].drawString(matrixStack, name, !isReallyWorld ? posX + prefixWidth + 4 : posX + prefixWidth, offsetY, -1);
            FontManager.sfBold[16].drawString(matrixStack, staffPlayer.getStatus().string, posX + slAnimateWidth - statusWidth - 4, offsetY, staffPlayer.getStatus().color);

            maxWidth = Math.max(maxWidth, prefixWidth + nameWidth + statusWidth + 12);
            index++;
        }
        activeStaffs = index;
        staffOnlineDraggable.setWidth(slAnimateWidth);
        staffOnlineDraggable.setHeight(slAnimateHeight + 17);
    }
    private void onWorldInformationRenderer(MatrixStack matrixStack) {
        boolean chatScreen = mc.currentScreen instanceof ChatScreen;
        float posY = window.getScaledHeight() - FontManager.sfBold[15].getFontHeight() - (chatScreen ? 6 : 4) * 5;

        String pos = (int) mc.player.getPosX() + ", " + (int) mc.player.getPosY() + ", " + (int) mc.player.getPosZ();
        String serverTPS = String.format("%.1f", NovaCore.getInstance().getServerTPS().getTPS());

        int index = 0;
        FontManager.sfBold[15].drawStringWithOutline(matrixStack, "TPS: ", 5, posY, ColorUtils.getColor(index));
        FontManager.sfBold[15].drawStringWithOutline(matrixStack, serverTPS, 5 + FontManager.sfBold[15].getWidth("TPS: "), posY, -1);
        index += 30;
        FontManager.sfBold[15].drawStringWithOutline(matrixStack, "BPS: ", 5, posY + 9, ColorUtils.getColor(index));
        FontManager.sfBold[15].drawStringWithOutline(matrixStack, calculateBPS(), 5 + FontManager.sfBold[15].getWidth("BPS: "), posY + 9, -1);
        index += 30;
        FontManager.sfBold[15].drawStringWithOutline(matrixStack, "XYZ: ", 5, posY + 18, ColorUtils.getColor(index));
        FontManager.sfBold[15].drawStringWithOutline(matrixStack, pos, 5 + FontManager.sfBold[15].getWidth("XYZ: "), posY + 18, -1);
    }

    float mWidth;

    private int activePotions = 0;

    float potWidthAnim = 0, potHeigthAnim = 0;

    private Map<String, DecelerateAnimation> animations = new HashMap<>();
    Animation animation = new EaseBackIn(250, 1f, 1f);
    Animation alpha = new EaseBackIn(220, 1f, 1f);
    private void onPotionRenderer(MatrixStack matrixStack) {
        float posX = activePotionsDraggable.getX(), posY = activePotionsDraggable.getY(), offset = 11;

        float width = Math.max(mWidth, 80), height = activePotions * offset;

        potWidthAnim = MathUtil.lerp(potWidthAnim, width, 15);
        potHeigthAnim = MathUtil.lerp(potHeigthAnim, height, 15);
        RenderUtils.Render2D.drawShadow(posX, posY, potWidthAnim, potHeigthAnim + 20, 8, ColorUtils.getColor(0), ColorUtils.getColor(90), ColorUtils.getColor(180), ColorUtils.getColor(270));
        RenderUtils.Render2D.drawRound(posX, posY, potWidthAnim, potHeigthAnim + 20, 5, ColorUtils.rgb(18, 18, 18));
        RenderUtils.Render2D.drawCornerRound(posX, posY, potWidthAnim, 16.5f, 5, ColorUtils.rgb(25, 25, 25), RenderUtils.Render2D.Corner.TOP);
        FontManager.sfBold[18].drawCenteredString(matrixStack, GradientUtil.gradient("Potions"), posX + potWidthAnim / 2f, posY + 6, -1);

        int index = 0;
        mWidth = 0;
        for (EffectInstance effectInstance : mc.player.getActivePotionEffects()) {
            if (effectInstance.isShowIcon()) {
                float offsetY = posY + (index * offset) + 20.5f;

                String durationText = EffectUtils.getPotionDurationString(effectInstance, 1);
                float nameWidth = FontManager.sfBold[16].getWidth(I18n.format(effectInstance.getEffectName()) + " " + getPotionAmplifer(effectInstance));
                float durationWidth = FontManager.sfBold[16].getWidth(durationText);
                DecelerateAnimation efAnimation = animations.getOrDefault(effectInstance.getEffectName(), null);
                if (efAnimation == null) {
                    efAnimation = new DecelerateAnimation(250, 1, Direction.FORWARDS);
                    animations.put(effectInstance.getEffectName(), efAnimation);
                }

                boolean potionActive = effectInstance.getDuration() > 5;
                efAnimation.setDirection(potionActive ? Direction.FORWARDS : Direction.BACKWARDS);
                TextureAtlasSprite textureAtlasSprite = mc.getPotionSpriteUploader().getSprite(effectInstance.getPotion());
                mc.getTextureManager().bindTexture(textureAtlasSprite.getAtlasTexture().getTextureLocation());
                AbstractGui.blit(matrixStack, (int) (posX + 4), (int) (offsetY - 2), 0, 9, 9, textureAtlasSprite);

                int duration = effectInstance.getDuration();
                float alphaFactor = 1.0f;

                if (duration <= 200) { // Если осталось меньше 10 секунд
                    int phase = 10 - duration / 20;
                    alphaFactor = MathHelper.clamp(duration / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f)
                            + (float) Math.cos(duration * Math.PI / 5.0f)
                            * MathHelper.clamp(phase / 10.0f * 0.25f, 0.0f, 0.25f);
                }

                int textAlpha = (int) (255 * efAnimation.getOutput() * alphaFactor);

                FontManager.sfBold[16].drawString(matrixStack, I18n.format(effectInstance.getEffectName()) + " " + getPotionAmplifer(effectInstance), posX + 16, offsetY, ColorUtils.rgba(200, 200, 200, textAlpha));
                FontManager.sfBold[16].drawString(matrixStack, durationText, posX + potWidthAnim - durationWidth - 4, offsetY, ColorUtils.rgba(200, 200, 200, textAlpha));

                mWidth = Math.max(mWidth, nameWidth + durationWidth + 24);
                index++;
            }
        }

        if (animations != null) animations.keySet().removeIf((type) -> mc.player.getActivePotionEffects().stream().map((effect) -> effect.getPotion().getName()).noneMatch((type2) -> type2.equals(type)));
        if (index > 0) {
            animation.setDirection(Direction.FORWARDS);
            alpha.setDirection(Direction.FORWARDS);
        } else {
            animation.setDirection(Direction.BACKWARDS);
            alpha.setDirection(Direction.BACKWARDS);
        }
        activePotions = index;
        activePotionsDraggable.setWidth(potWidthAnim);
        activePotionsDraggable.setHeight(potHeigthAnim + 17);
    }
    //dsad

    public String getPotionAmplifer(EffectInstance instance) {
        if (instance.getAmplifier() == 1) {
            return "2";
        } else if (instance.getAmplifier() == 2) {
            return "3";
        } else if (instance.getAmplifier() == 3) {
            return "4";
        } else if (instance.getAmplifier() == 4) {
            return "5";
        } else if (instance.getAmplifier() == 5) {
            return "6";
        } else if (instance.getAmplifier() == 6) {
            return "7";
        } else if (instance.getAmplifier() == 7) {
            return "8";
        } else if (instance.getAmplifier() == 8) {
            return "9";
        } else if (instance.getAmplifier() == 9) {
            return "10";
        } else {
            return "";
        }
    }

    private void onArmorRenderer(MatrixStack matrixStack) {

    }
    final Animation targetHudAnimation = new EaseBackIn(400, 1, 1);
    LivingEntity entity = null;
    Dragging dragging = NovaCore.getInstance().createDrag(this, "targetHud", 300, 300);

    private float hpAnim = 0, absorptionAnimation = 0.0f;
    private void onTargetInfoRenderer(MatrixStack matrixStack) {
        float x = dragging.getX();
        float y = dragging.getY();

        float width = 100;
        float height = 37;

        boolean out = !allow || stopWatch.isReached(1000);
        targetHudAnimation.setDuration(out ? 400 : 300);
        targetHudAnimation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        StyleManager styleManager = NovaCore.getInstance().getStyleManager();

        entity = getTarget(entity);

        if (entity == null) return;
        float hp = (ClientUtil.isConnectedToServer("reallyworld") ? getHealthFromScoreboard(entity)[0] : entity.getHealth());
        float maxHp = entity.getMaxHealth();
        float healthAnimation = MathHelper.clamp(hp / maxHp, 0, 1);
        hpAnim = MathUtil.fast(hpAnim, healthAnimation, 10);
        absorptionAnimation = MathUtil.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);

        GlStateManager.pushMatrix();
        sizeAnimation(x + (width / 2), y + (height / 2), targetHudAnimation.getOutput());
        RenderUtils.Render2D.drawShadow(x, y, width, height, 8, ColorUtils.getColor(0), ColorUtils.getColor(90), ColorUtils.getColor(180), ColorUtils.getColor(270));
        RenderUtils.Render2D.drawRound(x, y, width, height, 5, ColorUtils.rgb(25, 25, 25));
        if (entity instanceof PlayerEntity) RenderUtils.Render2D.drawRoundFace(x + 4.5f, y + 4.5f,28, 28, 5, 1, (AbstractClientPlayerEntity) entity);
        RenderUtils.Render2D.drawGradientRound(x + 34.5F, y + 25.5f, (width - 40), 6, new Vector4f(3, 3, 3, 3), ColorUtils.rgb(14, 14, 14), ColorUtils.rgb(14, 14, 14), ColorUtils.rgb(14, 14, 14), ColorUtils.rgb(14, 14, 14));
        RenderUtils.Render2D.drawGradientRound(x + 34.5F, y + 25.5f, (width - 40) * hpAnim, 6, new Vector4f(3, 3, 3, 3), styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB());
        RenderUtils.Render2D.drawGradientRound(x + 34.5F, y + 25.5f, (width - 40) * absorptionAnimation, 6, new Vector4f(3, 3, 3, 3), ColorUtils.rgba(255, 200, 50, 255), ColorUtils.rgba(255, 200, 50, 255), ColorUtils.rgba(255, 150, 0, 255), ColorUtils.rgba(255, 150, 0, 255));
        FontManager.sfBold[16].drawScissorString(matrixStack, entity.getName().getString(), x + 34.5F, y + 8.5f, -1, 50);
        String formattedHp = String.format("%.1f", hp); // �������������� �� ������ ����� ����� �������
        FontManager.sfBold[14].drawString(matrixStack, "HP: " + formattedHp, x + 34.5F, y + 19.0f, -1);
        GlStateManager.popMatrix();

        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private void onSecondTypeTargetHud(MatrixStack matrixStack) {
//        float x = drag.getX();
//        float y = drag.getY();
//
//        float width = 115;
//        float height = 40;
//        entity = getTarget(entity);
//
//        boolean out = !allow || stopWatch.isReached(1000);
//        targetHudAnimation.setDuration(out ? 400 : 300);
//        targetHudAnimation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);
//        if (entity == null) return;
//
//        GlStateManager.pushMatrix();
//        sizeAnimation(x + (width / 2), y + (height / 2), targetHudAnimation.getOutput());
//        RenderUtils.Render2D.drawRound(x, y, width, height, 5, ColorUtils.rgb(20, 20, 20));
//        GlStateManager.popMatrix();
//
//        drag.setWidth(width);
//        drag.setHeight(height);
    }

    public float[] getHealthFromScoreboard(LivingEntity target) {
        var ref = new Object() {
            float hp = target.getHealth();
            float maxHp = target.getMaxHealth();
        };
        if (mc.world.getScoreboard().getObjectiveInDisplaySlot(2) != null) {
            mc.world.getScoreboard().getObjectivesForEntity(target.getScoreboardName()).entrySet().stream().findAny().ifPresent(x -> {
                ref.hp = x.getValue().getScorePoints();
                ref.maxHp = 20;
            });
        }
        return new float[]{ref.hp, ref.maxHp};
    }
    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }
    public static String calculateBPS() {
        return String.format("%.1f", Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ) * (double) mc.timer.timerSpeed * 20.0D);
    }
    public static int calculatePing() {
        return mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) != null ? mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
    }
    final StopWatch stopWatch = new StopWatch();
    boolean allow;
    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = NovaCore.getInstance().getFunctionRegistry().getAttackAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            stopWatch.reset();
            allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            stopWatch.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }
}
