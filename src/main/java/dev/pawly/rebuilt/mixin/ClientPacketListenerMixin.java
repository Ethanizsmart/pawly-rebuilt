package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.features.FakePayFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method="sendCommand",at=@At("HEAD"),cancellable=true)
    private void pawly$pay(String command,CallbackInfo ci){if(PawlyClient.instance()!=null&&((FakePayFeature)PawlyClient.instance().feature("fake_pay")).intercept(command))ci.cancel();}
    @Inject(method="handleOpenSignEditor",at=@At("HEAD"),cancellable=true)
    private void pawly$sign(ClientboundOpenSignEditorPacket packet,CallbackInfo ci){var mc=Minecraft.getInstance();if(PawlyClient.active("exploit_preventer")&&PawlyClient.instance().feature("exploit_preventer").flag("block_signs")&&mc.player!=null&&mc.player.blockPosition().distSqr(packet.getPos())>64)ci.cancel();}
}
