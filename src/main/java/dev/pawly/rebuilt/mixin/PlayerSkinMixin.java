package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractClientPlayer.class)
public abstract class PlayerSkinMixin {
    @Inject(method="getSkin",at=@At("RETURN"),cancellable=true)
    private void pawly$skin(CallbackInfoReturnable<PlayerSkin> cir){var mc=Minecraft.getInstance();if((Object)this==mc.player&&PawlyClient.active("scoreboard")&&mc.getConnection()!=null){String name=PawlyClient.instance().feature("scoreboard").value("skin_name");if(!name.isBlank()){var profile=mc.getConnection().getPlayerInfoIgnoreCase(name);if(profile!=null)cir.setReturnValue(profile.getSkin());}}}
}
