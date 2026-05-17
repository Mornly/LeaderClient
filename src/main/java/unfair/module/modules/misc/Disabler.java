package unfair.module.modules.misc;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.util.concurrent.LinkedBlockingQueue;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.PacketEvent;
import unfair.module.Module;
import unfair.property.properties.ModeProperty;
import unfair.util.ChatUtil;
import unfair.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

import java.util.ArrayList;
import java.util.List;

import static unfair.event.EventManager.call;

public class Disabler extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"PredictionInventory"});

    private final List<Packet<?>> inventoryPackets = new ArrayList<>();
    public static LinkedBlockingQueue<Packet<INetHandlerPlayClient>> postPackets;
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



//    public static boolean noPost() {
//        return PacketStoringComponent.blinking || BlinkUtils.isBlinking();
//    }

}