package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MultiPlayerGameMode.class)
public abstract class GameModeMixin {
    @Shadow private boolean isDestroying;
    @Inject(method="destroyBlock",at=@At("RETURN"))
    private void pawly$restart(BlockPos pos,CallbackInfoReturnable<Boolean> cir){if(PawlyClient.active("break_reset")&&cir.getReturnValue())isDestroying=false;}
}
