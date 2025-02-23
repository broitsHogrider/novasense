package ru.novacore.functions.impl.player;

import ru.novacore.events.EventHandler;
import ru.novacore.events.player.EventLivingUpdate;
import ru.novacore.events.input.EventMotion;
import ru.novacore.events.server.EventPacket;
import ru.novacore.functions.api.Category;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionInfo;
import ru.novacore.functions.settings.impl.BooleanSetting;
import ru.novacore.functions.settings.impl.SliderSetting;
import ru.novacore.utils.player.MoveUtils;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.vector.Vector3d;

@FunctionInfo(name = "FreeCam", category = Category.Player)
public class FreeCam extends Function {

    private final SliderSetting speed = new SliderSetting("Скорость по XZ", 1.0f, 0.1f, 5.0f, 0.05f);
    private final SliderSetting motionY = new SliderSetting("Скорость по Y", 0.5f, 0.1f, 1.0f, 0.05f);
    private final BooleanSetting mode = new BooleanSetting("RW",false);
    public FreeCam() {
        addSettings(speed, motionY,mode);
    }

    private Vector3d clientPosition = null;

    private RemoteClientPlayerEntity fakePlayer;

    @EventHandler
    public void onLivingUpdate(EventLivingUpdate e) {
        if (mc.player != null) {
            mc.player.noClip = true;
            mc.player.setOnGround(false);
            MoveUtils.setMotion(speed.get());
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motion.y = motionY.get();
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y = -motionY.get();
            }
            mc.player.abilities.isFlying = true;
        }
    }

    @EventHandler
    public void onMotion(EventMotion e) {

        if (mc.player.ticksExisted % 10 == 0) {
            mc.player.connection.sendPacket(new CPlayerPacket(mc.player.isOnGround()));
        }

        if (mc.player != null) {
            e.cancel();
        }
    }

    @EventHandler
    public void onPacket(EventPacket e) {
        if (mode.get()) {
            if (mc.player != null && mc.world != null && !(mc.currentScreen instanceof DownloadTerrainScreen)) {

                if (e.isReceive()) {
                    if (e.getPacket() instanceof SConfirmTransactionPacket
                            || e.getPacket() instanceof SEntityVelocityPacket sEntityVelocityPacket
                            && sEntityVelocityPacket.getEntityID() == mc.player.getEntityId()) {
                        e.cancel();
                    } else if (e.getPacket() instanceof SPlayerPositionLookPacket packet) {
                        if (fakePlayer != null) {
                            fakePlayer.setPosition(packet.getX(), packet.getY(), packet.getZ());
                        }
                        e.cancel();
                    }
                    if (e.getPacket() instanceof SRespawnPacket) {
                        mc.player.abilities.isFlying = false;
                        if (clientPosition != null) {
                            mc.player.setPositionAndRotation(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.rotationYaw, mc.player.rotationPitch);
                        }
                        removeFakePlayer();
                        mc.player.motion = Vector3d.ZERO;
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
        clientPosition = mc.player.getPositionVec();
        spawnFakePlayer();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        mc.player.abilities.isFlying = false;
        if (clientPosition != null) {
            mc.player.setPositionAndRotation(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.rotationYaw, mc.player.rotationPitch);
        }
        removeFakePlayer();
        mc.player.motion = Vector3d.ZERO;
        super.onDisable();
    }

    private void spawnFakePlayer() {
        fakePlayer = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.renderYawOffset = mc.player.renderYawOffset;
        fakePlayer.rotationPitchHead = mc.player.rotationPitchHead;
        fakePlayer.container = mc.player.container;
        fakePlayer.inventory = mc.player.inventory;
        mc.world.addEntity(1337, fakePlayer);
    }

    private void removeFakePlayer() {
        mc.world.removeEntityFromWorld(1337);
    }
}
