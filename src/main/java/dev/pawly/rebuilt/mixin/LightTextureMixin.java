package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Redirect(method="updateLightTexture",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/dimension/DimensionType;ambientLight()F"))
    private float pawly$lightmap(net.minecraft.world.level.dimension.DimensionType dimension){float original=dimension.ambientLight();return PawlyClient.active("fullbright")?Math.max(original,(float)(PawlyClient.instance().feature("fullbright").number("brightness")/15)):original;}
    @Inject(method="getBrightness(FI)F",at=@At("RETURN"),cancellable=true)
    private static void pawly$bright(float ambient,int level,CallbackInfoReturnable<Float> cir){if(PawlyClient.active("fullbright"))cir.setReturnValue(Math.max(cir.getReturnValue(),(float)Math.min(1,PawlyClient.instance().feature("fullbright").number("brightness")/15)));}
}
