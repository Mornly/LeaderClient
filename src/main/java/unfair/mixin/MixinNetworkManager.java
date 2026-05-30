package unfair.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
<<<<<<< HEAD
=======
import net.minecraft.client.Minecraft;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
import net.minecraft.network.*;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
<<<<<<< HEAD
=======
import org.spongepowered.asm.mixin.Shadow;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unfair.Unfair;
import unfair.event.EventManager;
import unfair.event.types.EventType;
import unfair.events.PacketEvent;
import unfair.module.modules.misc.Disabler;
import unfair.util.PacketUtil;

import java.util.concurrent.Future;

@SideOnly(Side.CLIENT)
@Mixin({NetworkManager.class})
public abstract class MixinNetworkManager {
    @Inject(
            method = {"channelRead0*"},
            at = {@At("HEAD")},
            cancellable = true
    )
    @SuppressWarnings("unchecked")
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callbackInfo) {
        if (!packet.getClass().getName().startsWith("net.minecraft.network.play.client")) {
<<<<<<< HEAD
            if (Unfair.delayManager != null && Unfair.delayManager.shouldDelay((Packet<INetHandlerPlayClient>) packet)) {
                callbackInfo.cancel();
            }
            else if (Disabler.mode.getValue() == 1 && Unfair.moduleManager.getModule(Disabler.class).isEnabled() && Disabler.grimPostDelay(packet)){
                Disabler.storedPackets.add((Packet<INetHandlerPlayClient>) packet);
                callbackInfo.cancel();
            }
            else {
=======
            if (Disabler.mode.getValue() == 1 && Unfair.moduleManager.getModule(Disabler.class).isEnabled() && packet == Minecraft.getMinecraft().getNetHandler()){
                Disabler.postPackets.add((Packet<INetHandlerPlayClient>)packet);
                callbackInfo.cancel();
                return;
            }
            if (Unfair.delayManager != null && Unfair.delayManager.shouldDelay((Packet<INetHandlerPlayClient>) packet)) {
                callbackInfo.cancel();
            } else {
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                PacketEvent event = new PacketEvent(EventType.RECEIVE, packet);
                EventManager.call(event);
                if (event.isCancelled()) {
                    callbackInfo.cancel();
                }
            }
        }
    }

    @Inject(
            method = {"sendPacket(Lnet/minecraft/network/Packet;)V"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void sendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        if (!packet.getClass().getName().startsWith("net.minecraft.network.play.server")) {
            PacketEvent event = new PacketEvent(EventType.SEND, packet);
            EventManager.call(event);
            if (event.isCancelled()) {
                callbackInfo.cancel();
            } else if (Unfair.playerStateManager != null && Unfair.blinkManager != null && Unfair.lagManager != null) {
                if (!Unfair.lagManager.isFlushing()) {
                    Unfair.playerStateManager.handlePacket(packet);
                    if (Unfair.blinkManager.isBlinking()) {
                        if (Unfair.blinkManager.offerPacket(packet)) {
                            callbackInfo.cancel();
                            return;
                        }
                    }
                    if (Unfair.lagManager.handlePacket(packet)) {
                        callbackInfo.cancel();
                    }
                }
            }
        }
    }


    @Inject(
            method = {"sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void sendPacket2(
            Packet<?> packet,
            GenericFutureListener<? extends Future<? super Void>> genericFutureListener,
            GenericFutureListener<? extends Future<? super Void>>[] arr,
            CallbackInfo callbackInfo
    ) {
        if (!packet.getClass().getName().startsWith("net.minecraft.network.play.server")) {
            if (Unfair.playerStateManager != null && Unfair.blinkManager != null && Unfair.lagManager != null) {
                if (!Unfair.lagManager.isFlushing()) {
                    Unfair.playerStateManager.handlePacket(packet);
                    if (Unfair.blinkManager.isBlinking()) {
                        if (Unfair.blinkManager.offerPacket(packet)) {
                            callbackInfo.cancel();
                            return;
                        }
                    }
                    if (Unfair.lagManager.handlePacket(packet)) {
                        callbackInfo.cancel();
                    }
                }
            }
        }
    }
<<<<<<< HEAD

=======
    @SuppressWarnings("unchecked")
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    public void receivePacket(ChannelHandlerContext p_channelRead0_1_, Packet<?> packet, CallbackInfo ci) {
        if (packet != null) {
            if (PacketUtil.skipReceiveEvent.contains(packet)) {
                PacketUtil.skipReceiveEvent.remove(packet);
                return;
            }
        }
    }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
}
