package ru.novacore.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.events.EventDisplay;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.impl.combat.AntiBot;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.ColorSetting;
import ru.novacore.functions.settings.impl.ModeListSetting;
import ru.novacore.functions.settings.impl.ModeSetting;
import ru.novacore.utils.math.MathUtil;
import ru.novacore.utils.math.Vector4i;
import ru.novacore.utils.projections.ProjectionUtil;
import ru.novacore.utils.render.ColorUtils;
import ru.novacore.utils.render.RenderUtils;
import ru.novacore.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
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

import java.util.*;

import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@FunctionInfo(name = "ESP", category = Category.Render)
public class ESP extends Function {
    public ModeListSetting remove = new ModeListSetting("Включить", new BooleanSetting("Предметы", true), new BooleanSetting("Полоску хп", true), new BooleanSetting("Текст хп", true), new BooleanSetting("Зачарования", false), new BooleanSetting("Список эффектов", true),new BooleanSetting("Индикация Сфер",false),new BooleanSetting("Индикация Талисманов",false));
    private final ModeSetting typeBox = new ModeSetting("Тип ", "Углы", "Боксы", "Углы");
    public ESP() {
        addSettings(remove,typeBox);
    }

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    public ColorSetting color = new ColorSetting("Color", -1);

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }
        positions.clear();
        Vector4i colors = new Vector4i(ColorUtils.getColor(0, 1), ColorUtils.getColor(90, 1), ColorUtils.getColor(180, 1), ColorUtils.getColor(270, 1));
        Vector4i friendColors = new Vector4i(ColorUtils.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 0, 1), ColorUtils.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 90, 1), ColorUtils.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 180, 1), ColorUtils.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 270, 1));
        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity || entity instanceof ItemEntity)) continue;
            if (entity == mc.player && (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)) continue;

            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 1.5f, y, z - size.z / 1.5f, x + size.x / 1.5f, y + size.y + 0.1f, z + size.z / 1.5f);

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
        buffer.endVertex();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            if (entry.getKey() instanceof LivingEntity entity) {
                if (!typeBox.is("Углы")) {
                    RenderUtils.Render2D.drawBoxTest(position.x, position.y, position.z, position.w, 1, FriendStorage.isFriend(entity.getName().getString()) ? friendColors : colors);
                }
                if (!typeBox.is("Боксы")) {
                    double x = position.x;
                    double y = position.y;
                    double endX = position.z;
                    double endY = position.w;
                    int getColor = ColorUtils.getColor(90);
                    int outlineColor = ColorUtils.rgb(26,26,26);

                    double percentageX = 0.2;
                    double percentageY = 0.15;

                    double distanceX = endX - x;
                    double distanceY = endY - y;

                    double calcSectX = distanceX * percentageX;
                    double calcSectY = distanceY * percentageY;

                    drawMcRect(x - 1, y - 1, x + calcSectX + 0.5, y + 1, outlineColor);
                    drawMcRect(endX - calcSectX - 0.5, y - 1, endX + 1, y + 1, outlineColor);
                    drawMcRect(x - 1, endY - 1, x + calcSectX + 0.5, endY + 1, outlineColor);
                    drawMcRect(endX - calcSectX - 0.5, endY - 1, endX + 1, endY + 1, outlineColor);
                    drawMcRect(x - 1, y + 0.5, x + 1, y + calcSectY + 1, outlineColor);
                    drawMcRect(x - 1, endY - calcSectY - 1, x + 1, endY + 0.5, outlineColor);
                    drawMcRect(endX - 1, y + 0.5, endX + 1, y + calcSectY + 1, outlineColor);
                    drawMcRect(endX - 1, endY - calcSectY - 1, endX + 1, endY + 0.5, outlineColor);

                    drawMcRect(x - 0.5, y - 0.5, x + calcSectX, y + 0.5, getColor);
                    drawMcRect(endX - calcSectX, y - 0.5, endX + 0.5, y + 0.5, getColor);
                    drawMcRect(x - 0.5, endY - 0.5, x + calcSectX, endY + 0.5, getColor);
                    drawMcRect(endX - calcSectX, endY - 0.5, endX + 0.5, endY + 0.5, getColor);
                    drawMcRect(x - 0.5, y + 0.5, x + 0.5, y + calcSectY, getColor);
                    drawMcRect(x - 0.5, endY - calcSectY, x + 0.5, endY, getColor);
                    drawMcRect(endX - 0.5, y + 0.5, endX + 0.5, y + calcSectY, getColor);
                    drawMcRect(endX - 0.5, endY - calcSectY, endX + 0.5, endY, getColor);
                }

                float hpOffset = 3f;
                float out = 0.5f;
                if (remove.getValueByName("Полоску хп").get()) {
                    String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
                    Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                    float hp = entity.getHealth();
                    float maxHp = entity.getMaxHealth();
                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        hp = score.getScorePoints();
                        maxHp = 20;
                    }
                    if (FriendStorage.isFriend(entity.getName().getString())){
                        RenderUtils.Render2D.drawMCVerticalBuilding(position.x - hpOffset, position.y + (position.w - position.y) * (1 - MathHelper.clamp(hp / maxHp, 0, 1)), position.x - hpOffset + 1, position.w, getHealthColor(entity, new java.awt.Color(0, 128, 6).getRGB(), new java.awt.Color(0, 128, 6).getRGB()), getHealthColor(entity, new java.awt.Color(0, 128, 6).getRGB(), new java.awt.Color(0, 128, 6).getRGB()));
                    }else {
                        RenderUtils.Render2D.drawMCVerticalBuilding(position.x - hpOffset, position.y + (position.w - position.y) * (1 - MathHelper.clamp(hp / maxHp, 0, 1)), position.x - hpOffset + 1, position.w, ColorUtils.rgb(255, 0, 0),ColorUtils.rgb(113, 247, 106));
                    }
                }
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                Score score = mc.world.getScoreboard().getOrCreateScore(living.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();

                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();


                if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                String hpText = (int) hp + "HP";
                float hpWidth = Fonts.interMedium.getWidth(hpText, 5);

                float hpPercent = MathHelper.clamp(hp / maxHp, 0, 1);
                float hpPosY = position.y + (position.w - position.y) * (1 - hpPercent);
                if (remove.getValueByName("Текст хп").get()) {
                    Fonts.interMedium.drawText(e.getMatrixStack(), hpText, position.x - hpWidth - 6, hpPosY, -1, 5, 0.05f);
                }
                String hptext1 =  (int) hp + "";
                String friendPrefix = FriendStorage.isFriend(entity.getName().getString()) ? TextFormatting.GREEN + "[F] " : "";
                ITextComponent text = entity.getDisplayName();
                TextComponent name = (TextComponent)text;
                name.append(new StringTextComponent(" - " + TextFormatting.RED + (int) hp + TextFormatting.RED  + ""));
                float length = mc.fontRenderer.getStringPropertyWidth(name);
                GL11.glPushMatrix();
                glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, 0.5f);
                if(FriendStorage.isFriend(entity.getName().getString())) {
                    RenderUtils.Render2D.drawTexture(position.x - 2 + width / 2.0F - length / 1.9f, position.y - 16, length + 8, 14, 2, ColorUtils.rgba(0, 255, 0, 88));
                }else {
                    RenderUtils.Render2D.drawTexture(position.x - 2 + width / 2.0F - length / 1.9f , position.y - 16, length + 8, 14, 2, ColorUtils.rgba(21, 21, 21, 150));
                }
                mc.fontRenderer.func_243246_a(e.getMatrixStack(), name, position.x + width/2.0f - length / 2f , position.y - 13, -1);
                if(entity instanceof PlayerEntity player) {
                    if (remove.getValueByName("Индикация Сфер").get()) {
                        ItemStack stack = player.getHeldItemOffhand();
                        String nameS = "";

                        String itemName = stack.getDisplayName().getString();
                        if (stack.getItem() == Items.PLAYER_HEAD) {
                            CompoundNBT tag = stack.getTag();

                            if (tag != null && tag.contains("display", 10)) {
                                CompoundNBT display = tag.getCompound("display");

                                if (display.contains("Lore", 9)) {
                                    ListNBT lore = display.getList("Lore", 8);

                                    if (!lore.isEmpty()) {
                                        String firstLore = lore.getString(0);

                                        int levelIndex = firstLore.indexOf("Уровень");
                                        if (levelIndex != -1) {
                                            String levelString = firstLore.substring(levelIndex + "Уровень".length()).trim();
                                            String gat = levelString;
                                            if (gat.contains("1/3")) {
                                                nameS = "- 1/3]";
                                            } else if (gat.contains("2/3")) {
                                                nameS = "- 2/3]";
                                            } else if (gat.contains("MAX")) {
                                                nameS = "- MAX]";
                                            } else {
                                                nameS = "";
                                            }
                                        }
                                    }
                                }
                            }
                            if (itemName.contains("Пандо")) {
                                itemName = "[PANDORA ";
                            } else if (itemName.contains("Аполл")) {
                                itemName = "[APOLLON ";
                            } else if (itemName.contains("Тит")) {
                                itemName = "[TITANA ";
                            } else if (itemName.contains("Осир")) {
                                itemName = "[OSIRIS ";
                            } else if (itemName.contains("Андро")) {
                                itemName = "[ANDROMEDA";
                            } else if (itemName.contains("Хим")) {
                                itemName = "[XIMERA ";
                            } else if (itemName.contains("Астр")) {
                                itemName = "[ASTREYA ";
                            }
                            Fonts.interMedium.drawText(e.getMatrixStack(), itemName + nameS, (float) position.x - 15, position.y - 55, ColorUtils.rgb(255, 16, 16), 10.5f, 0.0001f);
                        }
                    }
                }
                mc.fontRenderer.func_243246_a(e.getMatrixStack(), name, position.x + width/2.0f - length / 2f , position.y - 13, -1);
                if(entity instanceof PlayerEntity player){
                    if(remove.getValueByName("Индикация Талисманов").get()) {
                        ItemStack stack = player.getHeldItemOffhand();
                        String nameS = "";

                        String itemName = stack.getDisplayName().getString();
                        if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                            CompoundNBT tag = stack.getTag();

                            if (tag != null && tag.contains("display", 10)) {
                                CompoundNBT display = tag.getCompound("display");

                                if (display.contains("Lore", 9)) {
                                    ListNBT lore = display.getList("Lore", 8);

                                    if (!lore.isEmpty()) {
                                        String firstLore = lore.getString(0);

                                        int levelIndex = firstLore.indexOf("Уровень");
                                        if (levelIndex != -1) {
                                            String levelString = firstLore.substring(levelIndex + "Уровень".length()).trim();
                                            String gat = levelString;
                                            if (gat.contains("1/3")) {
                                                nameS = "- 1/3]";
                                            } else if (gat.contains("2/3")) {
                                                nameS = "- 2/3]";
                                            } else if (gat.contains("MAX")) {
                                                nameS = "- MAX]";
                                            } else {
                                                nameS = "";
                                            }
                                        }
                                    }
                                }
                            }
                            if (itemName.contains("Талисман Крушителя")) {
                                itemName = "[KRUSH ";
                            } else if (itemName.contains("Талисман Дедала")) {
                                itemName = "[DEDALA ";
                            } else if (itemName.contains("Талисман Гармонии")) {
                                itemName = "[GARMONIYA ";
                            } else if (itemName.contains("Талисман Карателя")) {
                                itemName = "[KARATELYA ";
                            } else if (itemName.contains("Талисман Грани")) {
                                itemName = "[GRANI";
                            } else if (itemName.contains("Талисман Феникса")) {
                                itemName = "[FENIKSA ";
                            } else if (itemName.contains("Талисман Ехидны")) {
                                itemName = "[EXIDNA ";
                            }
                            Fonts.interMedium.drawText(e.getMatrixStack(), itemName + nameS, (float) position.x - 15, position.y - 55, ColorUtils.rgb(255, 16, 16), 10.5f, 0.0001f);
                        }
                    }
                }GL11.glPopMatrix();
                if (remove.getValueByName("Список эффектов").get()) {

                    drawPotions(e.getMatrixStack(), living, position.z + 2, position.y);
                }
                drawItems(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - 20));
            } else if (entity instanceof ItemEntity item) {
                MatrixStack stack = new MatrixStack();
                if (remove.getValueByName("Предметы").get()) {
                    GL11.glPushMatrix();
                    Vector4f position = entry.getValue();


//                    ITextComponent text = entity.getDisplayName();
//                    TextComponent name = (TextComponent)text;
//                    name.append(new StringTextComponent(""));
//                    float length = Fonts.interMedium.getWidth(name,11);


                    float width = position.z - position.x;

                    ITextComponent textComponent = item.getItem().getDisplayName();
                    TextComponent tag = (TextComponent)textComponent;
                    tag.append(new StringTextComponent(item.getItem().getCount() < 1 ? "" : " x" + item.getItem().getCount()));
                    float length = mc.fontRenderer.getStringPropertyWidth(tag);
                    glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, 0.5f);
                    RenderUtils.Render2D.drawRound(position.x - 5 + width / 2f - length / 2f, position.y - 16, length + 10, 16, 2, ColorUtils.rgba(10, 10, 10, 150));
                    mc.fontRenderer.func_243246_a(e.getMatrixStack(), tag, position.x + width/2.0f - length / 2f, position.y - 12, -1);
                    //Fonts.interMedium.drawText(e.getMatrixStack(),tag,position.x + width/2.0f - length / 2f, position.y - 14,13,255);
                    GL11.glPopMatrix();
                }
            }
        }
    }
    public static int getHealthColor(final LivingEntity entity, final int c1, final int c2) {
        final float health = entity.getHealth();
        final float maxHealth = entity.getMaxHealth();
        final float hpPercentage = health / maxHealth;
        final int red = (int) ((c2 >> 16 & 0xFF) * hpPercentage + (c1 >> 16 & 0xFF) * (1.0f - hpPercentage));
        final int green = (int) ((c2 >> 8 & 0xFF) * hpPercentage + (c1 >> 8 & 0xFF) * (1.0f - hpPercentage));
        final int yellow = (int) ((c2 >> 8 & 0xFF) * hpPercentage + (c1 >> 8 & 0xFF) * (1.0f - hpPercentage));
        final int blue = (int) ((c2 & 0xFF) * hpPercentage + (c1 & 0xFF) * (1.0f - hpPercentage));
        return new java.awt.Color(red, green, yellow, blue).getRGB();
    }
    public ModeListSetting getRemove() {
        return remove;
    }

    public HashMap<Entity, Vector4f> getPositions() {
        return positions;
    }

    public ColorSetting getColor() {
        return color;
    }

    public boolean isInView(Entity ent) {

        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for (EffectInstance ef : entity.getActivePotionEffects()) {
            int amp = ef.getAmplifier();

            String ampStr = "";

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + I18n.format("" + (amp + 1));
            }

            String text = I18n.format(ef.getEffectName()) + ampStr + " - " + EffectUtils.getPotionDurationString(ef, 1);

            Fonts.interMedium.drawText(matrixStack, text, posX + 10, posY, -1, 5, 0.05f);

            Effect effect = ef.getPotion();
            PotionSpriteUploader potionspriteuploader = Minecraft.getInstance().getPotionSpriteUploader();
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
            Minecraft.getInstance().getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            DisplayEffectsScreen.blit(matrixStack, (int) (posX), (int) (posY - 1), 11, 10, 10, textureatlassprite);

            posY += Fonts.interMedium.getHeight(7) + 3.5f;
        }
    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;

        float fontHeight = Fonts.interMedium.getHeight(6);

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
            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX -2, posY-5);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX -2, posY-5, null);
            GL11.glPopMatrix();
            if (itemStack.isEnchanted() && remove.getValueByName("Зачарования").get()) {
                int ePosY = (int) (posY - fontHeight);

                Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.getEnchantments(itemStack);

                for (Enchantment enchantment : enchantmentsMap.keySet()) {
                    int level = enchantmentsMap.get(enchantment);

                    if (level < 1 || !enchantment.canApply(itemStack)) continue;

                    IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(enchantment.getName());

                    String enchText = iformattabletextcomponent.getString().substring(0, 2) + level;

                    Fonts.interMedium.drawText(matrixStack, enchText, posX, ePosY, -1, 6, 0.05f);

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
    public static void drawMcRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.pos(left, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 1.0F).color(f, f1, f2, f3).endVertex();

    }
}