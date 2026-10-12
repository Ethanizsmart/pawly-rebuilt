package dev.pawly.rebuilt;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import java.util.Optional;

public final class DisplayText {
    private DisplayText(){}
    public static String names(String input){
        if(!PawlyClient.active("scoreboard"))return input;
        var mc=Minecraft.getInstance();if(mc.player==null)return input;
        var feature=PawlyClient.instance().feature("scoreboard");String alias=feature.value("alias");
        if(!alias.isBlank())input=input.replace(mc.player.getName().getString(),alias);
        if(!feature.value("nearby_names").isBlank()&&mc.level!=null)for(var player:mc.level.players())if(player!=mc.player)input=input.replace(player.getName().getString(),feature.value("nearby_names"));
        return input;
    }
    public static Component names(Component text){
        if(!PawlyClient.active("scoreboard"))return text;
        MutableComponent result=Component.empty();text.visit((style,value)->{result.append(Component.literal(names(value)).setStyle(style));return Optional.empty();},Style.EMPTY);return result;
    }
}
