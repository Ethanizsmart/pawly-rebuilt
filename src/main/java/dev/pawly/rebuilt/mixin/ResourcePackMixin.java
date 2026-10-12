package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ResourcePackMixin {
    @Shadow @Final protected Connection connection;
    @Inject(method="handleResourcePackPush",at=@At("HEAD"),cancellable=true)
    private void pawly$pack(ClientboundResourcePackPushPacket packet,CallbackInfo ci){
        if(PawlyClient.instance()==null||!PawlyClient.instance().feature("exploit_preventer").enabled()||PawlyClient.instance().unloaded)return;
        boolean allowed=false;try{var uri=java.net.URI.create(packet.url());allowed=("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))&&uri.getHost()!=null&&uri.getUserInfo()==null;}catch(IllegalArgumentException ignored){}
        if(!allowed||PawlyClient.instance().feature("exploit_preventer").flag("block_packs")){connection.send(new ServerboundResourcePackPacket(packet.id(),ServerboundResourcePackPacket.Action.DECLINED));ci.cancel();}
    }
}
