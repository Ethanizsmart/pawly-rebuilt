package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method="shouldShowName",at=@At("HEAD"),cancellable=true)
    private void pawly$name(Entity entity,double distance,CallbackInfoReturnable<Boolean> cir){if(entity==Minecraft.getInstance().player&&PawlyClient.active("name_display")&&!Minecraft.getInstance().options.getCameraType().isFirstPerson())cir.setReturnValue(true);}
}
