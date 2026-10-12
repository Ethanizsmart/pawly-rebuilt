package dev.pawly.rebuilt;

import dev.pawly.rebuilt.features.ScannerFeature;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Quaternionf;

public final class Overlay implements HudElement {
    @Override public void render(GuiGraphics graphics,DeltaTracker tracker){
        var app=PawlyClient.instance();var mc=Minecraft.getInstance();if(app==null||app.unloaded||mc.player==null||mc.level==null||mc.options.hideGui)return;
        int y=12;for(var f:app.features())if(f.running&&!f.status.isBlank()){String label=f.name+": "+f.status;int x=Math.max(8,graphics.guiWidth()-mc.font.width(label)-8);graphics.drawString(mc.font,label,x,y,0xffa9dbc9);y+=11;if(y>graphics.guiHeight()/2)break;}
        for(String id:new String[]{"block_esp","netherite_finder"})if(PawlyClient.active(id)){
            ScannerFeature scan=(ScannerFeature)app.feature(id);int count=0;for(var hit:scan.hits.entrySet()){
                if(count++>256)break;Vec3 pos=Vec3.atCenterOf(hit.getKey());int[] point=project(pos,graphics);if(point==null)continue;
                int color=id.equals("netherite_finder")?0xffffbb68:0xff73d9cf,x=point[0],sy=point[1];
                graphics.fill(x-4,sy-4,x+5,sy-3,color);graphics.fill(x-4,sy+4,x+5,sy+5,color);graphics.fill(x-4,sy-3,x-3,sy+4,color);graphics.fill(x+4,sy-3,x+5,sy+4,color);
                graphics.drawCenteredString(mc.font,hit.getValue()+" "+(int)mc.player.position().distanceTo(pos)+"m",x,sy-15,color);
            }
        }
        if(PawlyClient.active("region_map"))region(graphics);
    }
    private int[] project(Vec3 pos,GuiGraphics g){
        var mc=Minecraft.getInstance();var camera=mc.gameRenderer.getMainCamera();Vec3 d=pos.subtract(camera.position());
        Vector3f v=new Vector3f((float)d.x,(float)d.y,(float)d.z).rotate(new Quaternionf(camera.rotation()).conjugate());if(v.z>=-.05)return null;
        double focal=g.guiHeight()/(2*Math.tan(Math.toRadians(mc.options.fov().get())/2));
        int x=(int)(g.guiWidth()/2.0-v.x/v.z*focal),y=(int)(g.guiHeight()/2.0+v.y/v.z*focal);return x<0||x>=g.guiWidth()||y<0||y>=g.guiHeight()?null:new int[]{x,y};
    }
    private void region(GuiGraphics g){
        var f=PawlyClient.instance().feature("region_map");var mc=Minecraft.getInstance();int size=(int)f.number("cell_size"),x=(int)f.number("pos_x"),y=(int)f.number("pos_y");
        int gx=(int)Math.floor((mc.player.getX()+225000)/50000),gz=(int)Math.floor((mc.player.getZ()+225000)/50000);
        g.fill(x-3,y-3,x+size*9+3,y+size*9+16,0xc0162230);
        for(int row=0;row<9;row++)for(int col=0;col<9;col++)g.fill(x+col*size,y+row*size,x+(col+1)*size-1,y+(row+1)*size-1,col==gx&&row==gz?0xffb7ebd5:0xa0446076);
        g.drawString(mc.font,"Region "+gx+", "+gz,x,y+size*9+3,0xffe2eaf0);
    }
}
