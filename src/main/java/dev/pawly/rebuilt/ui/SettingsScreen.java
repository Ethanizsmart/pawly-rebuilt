package dev.pawly.rebuilt.ui;

import dev.pawly.rebuilt.core.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
    private final Screen parent;
    private final Feature feature;
    private int page;
    private String error="";
    public SettingsScreen(Screen parent,Feature feature){super(Component.literal(feature.name));this.parent=parent;this.feature=feature;}
    @Override protected void init(){
        int rows=Math.max(1,(height-100)/31),pages=Math.max(1,(feature.options.size()+rows-1)/rows);page=Math.clamp(page,0,pages-1);
        int x=Math.max(15,width/2-180),inputX=width/2+10,w=Math.min(170,width-inputX-15);
        for(int i=page*rows;i<Math.min(feature.options.size(),(page+1)*rows);i++){
            Option o=feature.options.get(i);int y=48+(i-page*rows)*31;
            if(o.kind()==Option.Kind.BOOLEAN||o.kind()==Option.Kind.CHOICE){
                addRenderableWidget(Button.builder(Component.literal(feature.value(o.id())),b->{String next=o.kind()==Option.Kind.BOOLEAN?""+!feature.flag(o.id()):o.choices().get((o.choices().indexOf(feature.value(o.id()))+1)%o.choices().size());feature.value(o.id(),next);b.setMessage(Component.literal(next));}).bounds(inputX,y,w,22).build());
            }else{
                EditBox box=new EditBox(font,inputX,y,w,22,Component.literal(o.label()));box.setMaxLength(4096);box.setValue(feature.value(o.id()));
                box.setResponder(value->{try{feature.value(o.id(),value);error="";box.setTextColor(0xffd0eadf);}catch(IllegalArgumentException e){error=o.label()+": "+e.getMessage();box.setTextColor(0xffff7777);}});addRenderableWidget(box);
            }
        }
        addRenderableWidget(Button.builder(Component.literal("‹"),b->{page--;rebuildWidgets();}).bounds(x,height-40,25,20).build()).active=page>0;
        addRenderableWidget(Button.builder(Component.literal("›"),b->{page++;rebuildWidgets();}).bounds(x+30,height-40,25,20).build()).active=page+1<pages;
        addRenderableWidget(Button.builder(Component.literal("Reset"),b->{feature.reset();rebuildWidgets();}).bounds(width/2-35,height-40,70,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(inputX+w-70,height-40,70,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xf0101520);g.drawCenteredString(font,title,width/2,14,0xffa8e6d4);
        int rows=Math.max(1,(height-100)/31),x=Math.max(15,width/2-180);
        for(int i=page*rows;i<Math.min(feature.options.size(),(page+1)*rows);i++)g.drawString(font,feature.options.get(i).label(),x,55+(i-page*rows)*31,0xffd1dae6);
        super.render(g,mx,my,delta);if(!error.isEmpty())g.drawString(font,error,15,height-15,0xffff8888);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
