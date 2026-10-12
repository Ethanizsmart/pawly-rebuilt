package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.features.PacketDelayFeature;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import io.netty.channel.ChannelFutureListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method="send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",at=@At("HEAD"),cancellable=true)
    private void pawly$delay(Packet<?> packet,ChannelFutureListener listener,boolean flush,CallbackInfo ci){if(PawlyClient.instance()!=null&&((PacketDelayFeature)PawlyClient.instance().feature("fake_lag")).intercept((Connection)(Object)this,packet,listener,flush))ci.cancel();}
}
