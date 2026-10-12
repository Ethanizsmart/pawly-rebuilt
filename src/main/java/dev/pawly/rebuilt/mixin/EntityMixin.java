package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method="turn",at=@At("HEAD"),cancellable=true)
    private void pawly$look(double x,double y,CallbackInfo ci){if((Object)this!=Minecraft.getInstance().player||PawlyClient.instance()==null)return;var camera=PawlyClient.instance().camera();if(camera!=null){camera.turn(x,y);ci.cancel();}}
}
