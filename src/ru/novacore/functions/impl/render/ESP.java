package ru.novacore.functions.impl.render;

import ru.novacore.events.EventHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;
import ru.novacore.NovaCore;
import ru.novacore.events.EventSystem;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.events.render.EventDisplay;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.combat.AntiBot;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.ui.styles.StyleManager;
import ru.novacore.utils.client.ClientUtil;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.Vector4i;
import ru.novacore.utils.projections.ProjectionUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
// by lapycha and artem
@FunctionInfo(name = "ESP", category = Category.Render)
public class ESP extends Function {
    public ModeListSetting remove = new ModeListSetting("Убрать", new BooleanSetting("Боксы", false), new BooleanSetting("Полоску хп", false), new BooleanSetting("Текст хп", false), new BooleanSetting("Зачарования", false), new BooleanSetting("Список эффектов", false));

    public ESP() {
        toggle();
        addSettings(remove);
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

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    @EventHandler
    public void onDisplay(EventDisplay e) {
        MatrixStack stack = e.getMatrixStack();
        if (mc.world == null) {
            return;
        }

        positions.clear();

        StyleManager styleManager = NovaCore.getInstance().getStyleManager();
        
        Vector4i colors = new Vector4i(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB());
        Vector4i friendColors = new Vector4i(FriendStorage.getColor(), FriendStorage.getColor(), FriendStorage.getColor(), FriendStorage.getColor());


        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity || entity instanceof ItemEntity)) continue;
            if (entity == mc.player && (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)) continue;
            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());


            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;

            for (int i = 0; i < 8; i++) {
                Vector2f vector = ProjectionUtil.project(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            positions.put(entity, position);
        }


        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            if (entry.getKey() instanceof ItemEntity item) {
                if (!remove.getValueByName("Боксы").get()) {
                    RenderUtils.Render2D.drawBox(position.x - 0.5f, position.y - 0.5f, position.z + 0.5f, position.w + 0.5f, 2, ColorUtils.rgba(0, 0, 0, 128));
                    RenderUtils.Render2D.drawBoxTest(position.x, position.y, position.z, position.w, 1, colors);
                }
            }
            if (entry.getKey() instanceof LivingEntity entity) {
                if (!remove.getValueByName("Боксы").get()) {
                    RenderUtils.Render2D.drawBox(position.x - 0.5f, position.y - 0.5f, position.z + 0.5f, position.w + 0.5f, 2, ColorUtils.rgba(0, 0, 0, 128));
                    RenderUtils.Render2D.drawBoxTest(position.x, position.y, position.z, position.w, 1, colors);
                }
                float hpOffset = 3f;
                float out = 0.5f;
                if (!remove.getValueByName("Полоску хп").get()) {
                    String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

                    RenderUtils.Render2D.drawRectBuilding(position.x - hpOffset - out, position.y - out, position.x - hpOffset + 1 + out, position.w + out, ColorUtils.rgba(0, 0, 0, 128));
                    RenderUtils.Render2D.drawRectBuilding(position.x - hpOffset, position.y, position.x - hpOffset + 1, position.w, ColorUtils.rgba(0, 0, 0, 128));

                    Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

                    float hp = ClientUtil.isConnectedToServer("reallyworld") ? getHealthFromScoreboard(entity)[0] : entity.getHealth();
                    float maxHp = entity.getMaxHealth();

                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        hp = score.getScorePoints();
                        maxHp = 20;
                    }

                    RenderUtils.Render2D.drawMCVerticalBuilding(position.x - hpOffset, position.y + (position.w - position.y) * (1 - MathHelper.clamp(hp / maxHp, 0, 1)), position.x - hpOffset + 1, position.w, FriendStorage.isFriend(entity.getName().getString()) ? friendColors.w : colors.w, FriendStorage.isFriend(entity.getName().getString()) ? friendColors.x : colors.x);
                }
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();
            //double nametagWidth = entity.getWidth() / 1.5, nametagHeight = entity.getHeight() + 0.1f - (entity.isSneaking() ? 0.2f : 0.0f);

            if (entity instanceof LivingEntity living) {
                Score score = mc.world.getScoreboard().getOrCreateScore(living.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                float hp = ClientUtil.isConnectedToServer("reallyworld") ? getHealthFromScoreboard(living)[0] : living.getHealth();
                float maxHp = living.getMaxHealth();

                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

                if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                String hpText = (int) hp + "HP";
                float hpWidth = Fonts.sfbold.getWidth(hpText, 6);

                float hpPercent = MathHelper.clamp(hp / maxHp, 0, 1);
                float hpPosY = position.y + (position.w - position.y) * (1 - hpPercent);
                if (!remove.getValueByName("Текст хп").get()) {
                    Fonts.sfbold.drawText(stack, hpText, position.x - hpWidth - 6, hpPosY, -1, 6, 0.05f);
                }

                float length = Fonts.sfMedium.getWidth(entity.getDisplayName(), 7f);
                float hpLength = Fonts.sfMedium.getWidth(String.valueOf(ClientUtil.isConnectedToServer("reallyworld") ? (int) getHealthFromScoreboard((LivingEntity) entity)[0] : (int) ((LivingEntity) entity).getHealth()), 7);

                RenderUtils.Render2D.drawRound(position.x + width / 2f - length / 2 - 2 - hpLength, position.y - 10, length + hpLength + 12, 9, 0, ColorUtils.rgba(20, 20, 20, 100));
                Fonts.sfMedium.drawText(stack, entity.getCustomName() != null ? entity.getCustomName() : entity.getDisplayName(), position.x + width / 2 - length / 2 - hpLength, position.y - 9, 7f, 255);
                Fonts.sfMedium.drawText(stack, "[", position.x + width / 2 - length / 2 + length + 2 - hpLength, position.y - 9.5F, -1, 7);
                Fonts.sfMedium.drawText(stack, String.valueOf(ClientUtil.isConnectedToServer("reallyworld") ? (int) getHealthFromScoreboard((LivingEntity) entity)[0] : (int) ((LivingEntity) entity).getHealth()), position.x - hpLength + width / 2 - length / 2 + length + 5, position.y - 9F, ColorUtils.rgba(255,128,128,255), 7);
                Fonts.sfMedium.drawText(stack, "]", position.x + width / 2 - length / 2 + length + hpLength + 4 - hpLength, position.y - 9.5F, ColorUtils.rgba(191,191,191,255), 7);

                if (!remove.getValueByName("Список эффектов").get()) {
                    drawPotions(stack, living, position.z + 2, position.y);
                }
                drawItems(stack, living, (int) (position.x + width / 2f), (int) (position.y - 20));
            } else if (entity instanceof ItemEntity item) {
                Vector4f position = entry.getValue();
                int count = item.getItem().getCount();
                float width = position.z - position.x;
                float length = Fonts.sfMedium.getWidth(count == 1 ? item.getName().getString() : item.getName().getString() + " x" + count, 6f, 0.15f);

                RenderUtils.Render2D.drawRound(position.x - 1 + width / 2 - length / 2, position.y - 9, length + 3, Fonts.sfMedium.getHeight(6f) + 1, 0, ColorUtils.rgba(25,25,25,199));
                Fonts.sfMedium.drawText(stack, count == 1 ? item.getName().getString() : item.getName().getString() + " x" + count, position.x + width / 2 - length / 2, position.y - 8.5f, -1, 6f, 0.15f);
            }
        }
    }

    public boolean isInView(Entity ent) {

        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for (EffectInstance pot : entity.getActivePotionEffects()) {
            int amp = pot.getAmplifier();

            String ampStr = "";

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + I18n.format("enchantment.level." + (amp + 1));
            }

            String text = I18n.format(pot.getEffectName()) + ampStr + " - " + EffectUtils.getPotionDurationString(pot, 1);

            Fonts.sfMedium.drawText(matrixStack, text, posX, posY, -1, 6);

            posY += Fonts.sfMedium.getHeight(6);
        }
    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;

        float fontHeight = Fonts.sfMedium.getHeight(6);

        List<ItemStack> items = new ArrayList<>();

        ItemStack mainStack = entity.getHeldItemMainhand();

        if (!mainStack.isEmpty()) {
            items.add(mainStack);
        }

        for (ItemStack itemStack : entity.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;
            items.add(itemStack);
        }

        ItemStack offStack = entity.getHeldItemOffhand();

        if (!offStack.isEmpty()) {
            items.add(offStack);
        }

        posX -= (items.size() * (size + padding)) / 2f;

        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;

            GL11.glPushMatrix();

            glCenteredScale(posX, posY, size / 2f, size / 2f, 0.5f);

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY, null);

            GL11.glPopMatrix();

            if (itemStack.isEnchanted() && !remove.getValueByName("Зачарования").get()) {
                int ePosY = (int) (posY - fontHeight);

                Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.getEnchantments(itemStack);

                for (Enchantment enchantment : enchantmentsMap.keySet()) {
                    int level = enchantmentsMap.get(enchantment);

                    if (level < 1 || !enchantment.canApply(itemStack)) continue;

                    IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(enchantment.getName());

                    String enchText = iformattabletextcomponent.getString().substring(0, 2) + level;

                    Fonts.sfMedium.drawText(matrixStack, enchText, posX, ePosY, -1, 6, 0.05f);

                    ePosY -= (int) fontHeight;
                }
            }

            posX += size + padding;
        }
    }

    public boolean isValid(Entity e) {
        if (AntiBot.isBot(e)) return false;

        return isInView(e);
    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }
}