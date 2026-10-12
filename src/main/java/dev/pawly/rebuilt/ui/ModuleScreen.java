package dev.pawly.rebuilt.ui;

import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.core.Feature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class ModuleScreen extends Screen {
    private final Screen parent;
    private int page;
    public ModuleScreen(Screen parent){super(Component.literal("Pawly Rebuilt"));this.parent=parent;}
    @Override protected void init(){
        var client=PawlyClient.instance();
        int rows=Math.max(1,(height-104)/32),perPage=rows*2;
        List<Feature> list=List.copyOf(client.features());int pages=Math.max(1,(list.size()+perPage-1)/perPage);page=Math.clamp(page,0,pages-1);
        int widthEach=Math.min(235,(width-42)/2),left=(width-widthEach*2-12)/2;
        for(int i=page*perPage;i<Math.min(list.size(),(page+1)*perPage);i++){
            Feature f=list.get(i);int j=i-page*perPage,x=left+j%2*(widthEach+12),y=44+j/2*32;
            var toggle=Button.builder(Component.literal((f.enabled()?"● ":"○ ")+f.name),button->{f.setEnabled(!f.enabled());rebuildWidgets();}).bounds(x,y,widthEach-28,25).build();
            toggle.setTooltip(Tooltip.create(Component.literal(f.category+" · "+f.description+(f.status.isEmpty()?"":"\n"+f.status))));addRenderableWidget(toggle);
            addRenderableWidget(Button.builder(Component.literal("…"),button->minecraft.setScreen(new SettingsScreen(this,f))).bounds(x+widthEach-26,y,26,25).build());
        }
        int y=height-43;
        addRenderableWidget(Button.builder(Component.literal("‹"),b->{page--;rebuildWidgets();}).bounds(left,y,25,20).build()).active=page>0;
        addRenderableWidget(Button.builder(Component.literal("›"),b->{page++;rebuildWidgets();}).bounds(left+30,y,25,20).build()).active=page+1<pages;
        addRenderableWidget(Button.builder(Component.literal(client.unloaded?"Resume":"Stop all"),b->{if(client.unloaded)client.unloaded=false;else client.unload();rebuildWidgets();}).bounds(width/2-50,y,100,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+widthEach*2-58,y,70,20).build());
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        g.fill(0,0,width,height,0xef101520);g.fill(0,0,width,34,0xff192332);g.drawString(font,title,16,12,0xffa8e6d4);
        g.drawString(font,"Minecraft 1.21.11 · source rebuild",Math.max(170,width-235),12,0xff8eabc2);
        super.render(g,x,y,delta);
        g.drawString(font,"Right Shift / /pawly · settings saved locally",16,height-14,0xff91a1b5);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
