package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method="getCurrentSwingDuration",at=@At("RETURN"),cancellable=true)
    private void pawly$swing(CallbackInfoReturnable<Integer> cir){if((Object)this==Minecraft.getInstance().player&&PawlyClient.active("swing_speed"))cir.setReturnValue(Math.max(1,(int)Math.round(cir.getReturnValue()/PawlyClient.instance().feature("swing_speed").number("speed"))));}
}
