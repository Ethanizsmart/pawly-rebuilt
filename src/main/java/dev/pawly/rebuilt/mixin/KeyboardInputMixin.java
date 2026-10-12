package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.features.CameraFeature;
import net.minecraft.client.player.*;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method="tick",at=@At("TAIL"))
    private void pawly$input(CallbackInfo ci){if(PawlyClient.active("freecam")){CameraFeature camera=(CameraFeature)PawlyClient.instance().feature("freecam");keyPresses=camera.flag("continue_movement")?camera.held:Input.EMPTY;moveVector=new Vec2((keyPresses.left()?1:0)-(keyPresses.right()?1:0),(keyPresses.forward()?1:0)-(keyPresses.backward()?1:0));}}
}
