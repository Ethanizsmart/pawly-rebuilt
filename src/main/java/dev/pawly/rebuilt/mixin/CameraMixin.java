package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.Camera;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void setRotation(float yaw,float pitch);
    @Inject(method="setup",at=@At("TAIL"))
    private void pawly$camera(Level level,Entity entity,boolean detached,boolean mirrored,float delta,CallbackInfo ci){if(PawlyClient.instance()==null)return;var feature=PawlyClient.instance().camera();if(feature!=null){setPosition(feature.previousPosition.lerp(feature.position,delta));setRotation(feature.yaw,feature.pitch);}}
    @Inject(method="getMaxZoom",at=@At("HEAD"),cancellable=true)
    private void pawly$clip(float distance,CallbackInfoReturnable<Float> cir){if(PawlyClient.active("clip_perspective")||PawlyClient.active("freelook"))cir.setReturnValue(distance);}
}
