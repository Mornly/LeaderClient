package unfair.module.modules.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.*;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.PacketEvent;
import unfair.events.PreMotionEvent;
import unfair.module.Module;
import unfair.property.properties.ModeProperty;
import unfair.util.ChatUtil;
import unfair.util.PacketUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static unfair.event.EventManager.call;

public class Disabler extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"PredictionInventory","Post"});

    private final List<Packet<?>> inventoryPackets = new ArrayList<>();
    public static final CopyOnWriteArrayList<Packet<INetHandlerPlayClient>> storedPackets = new CopyOnWriteArrayList<Packet<INetHandlerPlayClient>>();
    public Disabler() {
        super("Disabler",false,false);
    }

    @Override
    public void onEnabled() {
        String currentMode = mode.getModeString();
        if (currentMode.equals("PredictionInventory")) {
            ChatUtil.sendFormatted(String.format("%s%s: You can use Vanilla-InvWalk & Silent-InvManager now",
                    Unfair.clientName, this.getName()));
        }
        resetStates();
    }
    public boolean checkCompass(){
        boolean compass = false;
        for (int i = 0; i < 9; i++) {
            final ItemStack stackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (stackInSlot != null && stackInSlot.getUnlocalizedName().toLowerCase().contains("compass")) {
                compass = true;
            }
        }
        return compass;
    }

    @Override
    public void onDisabled() {
        if (mode.getValue() == 0) {
            if (!inventoryPackets.isEmpty()) {
                for (Packet<?> p : inventoryPackets) {
                    PacketUtil.sendPacketNoEvent(p);
                }
                inventoryPackets.clear();
            }
            resetStates();
        }
    }

    private void resetStates() {
        inventoryPackets.clear();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mode.getValue() == 0) {
            if (!this.isEnabled()) return;
            if (!this.checkCompass()) {
                if (event.getType() == EventType.SEND) {
                    handlePredictionInventory(event);
                }
            }
        }
    }
    @EventTarget
    public void onPre(PreMotionEvent event){
        if (this.isEnabled())releasePost();
    }
    public static boolean grimPostDelay(final Packet<?> packet) {
        if (mc.thePlayer == null) {
            return false;
        }
        if (mc.currentScreen instanceof GuiDownloadTerrain) {
            return false;
        }
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity s12PacketEntityVelocity = (S12PacketEntityVelocity) packet;
            return s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId();
        }
        return packet instanceof S27PacketExplosion || packet instanceof S32PacketConfirmTransaction || packet instanceof S08PacketPlayerPosLook || packet instanceof S18PacketEntityTeleport || packet instanceof S19PacketEntityStatus || packet instanceof S04PacketEntityEquipment || packet instanceof S23PacketBlockChange || packet instanceof S22PacketMultiBlockChange || packet instanceof S13PacketDestroyEntities || packet instanceof S00PacketKeepAlive || packet instanceof S06PacketUpdateHealth || packet instanceof S14PacketEntity || packet instanceof S0FPacketSpawnMob|| packet instanceof S3FPacketCustomPayload;
    }
    private void handlePredictionInventory(PacketEvent event) {
        if (!mode.getModeString().equals("PredictionInventory")) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof C16PacketClientStatus || packet instanceof C0EPacketClickWindow) {
            event.setCancelled(true);
            inventoryPackets.add(packet);
        } else if (packet instanceof C0DPacketCloseWindow) {
            for (Packet<?> p : inventoryPackets) {
                PacketUtil.sendPacketNoEvent(p);
            }
            inventoryPackets.clear();
        }
    }
    public static void releasePost() {
        if (Unfair.moduleManager.getModule(Disabler.class).isEnabled() && mode.getValue() == 1 && mc.getNetHandler() != null) {
            while (!storedPackets.isEmpty()) {
                PacketEvent packetEvent = new PacketEvent(EventType.RECEIVE, (Packet<?>) storedPackets);
                call(packetEvent);

                if (packetEvent.isCancelled()) {
                    continue;
                }

                Packet<NetHandlerPlayClient> packet = packetEvent.getPacket() instanceof Packet ?
                        (Packet<NetHandlerPlayClient>) packetEvent.getPacket() : null;

                if (packet == null) {
                    continue;
                }

                try {
                    packet.processPacket(mc.getNetHandler());
                } catch (Exception e) {
                    // 记录异常或调试信息
                    e.printStackTrace();
                }
            }
        }
    }
}