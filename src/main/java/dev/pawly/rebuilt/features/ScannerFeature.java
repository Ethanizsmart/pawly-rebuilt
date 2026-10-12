package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.GameActions;
import dev.pawly.rebuilt.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.util.*;

public final class ScannerFeature extends Feature {
    public final Map<BlockPos,String> hits=new LinkedHashMap<>();
    private BlockPos origin;
    private int cursor,cooldown;
    private Set<String> ids=Set.of();
    private final boolean debris;
    public ScannerFeature(boolean debris){
        super(debris?"netherite_finder":"block_esp",debris?"Netherite Finder":"Block ESP",debris?"World":"Render","Scan loaded blocks and mark matches in the world view.",Option.text("blocks","Block IDs, comma separated",debris?"minecraft:ancient_debris":"minecraft:spawner"),Option.number("radius","Scan radius",4,64,24),Option.number("budget","Blocks per tick",128,32768,4096),Option.number("scan_interval","Pause between passes",1,200,20),Option.flag("notify_exact","Alert on new match",debris));
        this.debris=debris;
    }
    @Override public void start(){rescan();}
    public void rescan(){hits.clear();origin=null;cursor=0;cooldown=0;ids=new HashSet<>(Arrays.stream(value("blocks").split(",")).map(String::strip).filter(s->!s.isEmpty()).toList());}
    @Override public void changed(String option){if(running)rescan();}
    @Override public void tick(){
        if(debris&&!mc.level.dimension().equals(net.minecraft.world.level.Level.NETHER)){status="Enter the Nether";hits.clear();return;}
        int r=(int)number("radius"),width=2*r+1,total=width*width*width;
        if(origin==null||origin.distSqr(mc.player.blockPosition())>64){origin=mc.player.blockPosition();cursor=0;cooldown=0;}
        hits.keySet().removeIf(p->p.distSqr(mc.player.blockPosition())>r*r*3||!mc.level.hasChunkAt(p)||!ids.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(p).getBlock()).toString()));
        if(cooldown-->0)return;
        int end=Math.min(total,cursor+(int)number("budget"));
        for(;cursor<end;cursor++){
            BlockPos p=origin.offset(cursor%width-r,cursor/width%width-r,cursor/(width*width)-r);
            if(p.getY()<mc.level.getMinY()||p.getY()>=mc.level.getMaxY()||!mc.level.hasChunkAt(p))continue;
            var state=mc.level.getBlockState(p);String id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            if(ids.contains(id)&&hits.size()<4096){boolean fresh=!hits.containsKey(p);hits.put(p,state.getBlock().getName().getString());if(fresh&&flag("notify_exact"))GameActions.message(hits.get(p)+" at "+p.toShortString());}
        }
        if(cursor>=total){cursor=0;cooldown=(int)number("scan_interval");}
        status=hits.size()+" matches; loaded blocks only";
    }
    @Override public void stop(){hits.clear();origin=null;}
}
