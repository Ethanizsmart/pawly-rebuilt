package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.core.*;
import dev.pawly.rebuilt.GameActions;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;

public final class MacroFeature extends Feature {
    private final ArrayDeque<String> lines=new ArrayDeque<>();
    private boolean held;
    private int wait;
    public MacroFeature(){super("chat_macro","Chat Macro","Utilities","Send a configured sequence on a key press.",Option.number("key","Trigger key (GLFW code)",-1,348,71),Option.text("messages","Messages separated by |","/help"),Option.number("delay_ticks","Delay between messages",1,200,20),Option.flag("stop_input","Stop movement while sending",false));}
    @Override public void start(){held=down();lines.clear();wait=0;}
    private boolean down(){return number("key")>=0&&GLFW.glfwGetKey(mc.getWindow().handle(),(int)number("key"))==GLFW.GLFW_PRESS;}
    @Override public void tick(){
        boolean pressed=down();
        if(mc.screen==null&&pressed&&!held&&lines.isEmpty())for(String line:value("messages").split("\\|")){if(!line.isBlank())lines.add(line.strip());}
        held=pressed;
        if(!lines.isEmpty()&&mc.screen==null){if(flag("stop_input"))GameActions.releaseMovement();if(wait--<=0){GameActions.chat(lines.remove());wait=(int)number("delay_ticks");}}
        status=lines.isEmpty()?"Ready":lines.size()+" messages pending";
    }
    @Override public void stop(){lines.clear();held=false;}
}
