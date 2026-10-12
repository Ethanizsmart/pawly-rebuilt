package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.DisplayText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @ModifyVariable(method="drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private String pawly$string(String text){return text==null?null:DisplayText.names(text);}
    @ModifyVariable(method="drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private Component pawly$component(Component text){return text==null?null:DisplayText.names(text);}
}
