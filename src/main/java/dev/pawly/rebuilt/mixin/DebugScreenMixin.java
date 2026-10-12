package dev.pawly.rebuilt.mixin;
import dev.pawly.rebuilt.PawlyClient;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import java.util.*;
import java.util.regex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenMixin {
    private static final Pattern PAWLY_COORDS=Pattern.compile("^(XYZ: |Block: )(-?[\\d.]+)( / | )(-?[\\d.]+)( / | )(-?[\\d.]+)(.*)$");
    @ModifyVariable(method="renderLines",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private List<String> pawly$coordinates(List<String> lines){
        if(!PawlyClient.active("coord_faker"))return lines;var f=PawlyClient.instance().feature("coord_faker");var result=new ArrayList<String>();
        for(String line:lines){Matcher m=PAWLY_COORDS.matcher(line);if(m.matches()){boolean integer=m.group(1).startsWith("Block");double x=Double.parseDouble(m.group(2))+f.number("offset_x"),y=Double.parseDouble(m.group(4))+f.number("offset_y"),z=Double.parseDouble(m.group(6))+f.number("offset_z");line=m.group(1)+(integer?""+(int)Math.floor(x):String.format(Locale.ROOT,"%.3f",x))+m.group(3)+(integer?""+(int)Math.floor(y):String.format(Locale.ROOT,"%.3f",y))+m.group(5)+(integer?""+(int)Math.floor(z):String.format(Locale.ROOT,"%.3f",z))+m.group(7);}result.add(line);}return result;
    }
}
