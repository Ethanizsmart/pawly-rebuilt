package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.GameActions;
import dev.pawly.rebuilt.core.PathSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class Navigator {
    private final Minecraft mc=Minecraft.getInstance();
    private PathSearch search;
    private BlockPos goal;
    private List<PathSearch.Cell> path=List.of();
    private int next,stalled;
    private Vec3 last=Vec3.ZERO;
    public boolean walkNear(BlockPos target,int budget){
        if(goal==null||!goal.equals(target)){reset();goal=target;var from=cell(mc.player.blockPosition());var to=cell(target);search=new PathSearch(from,to,this::passable,this::supported,c->new BlockPos(c.x(),c.y(),c.z()).distSqr(target)<=8);}
        if(search!=null){search.step(budget);if(!search.done())return false;path=search.result();next=1;search=null;}
        if(next>=path.size()){GameActions.releaseMovement();if(path.isEmpty()){goal=null;return false;}return true;}
        var p=path.get(next);Vec3 center=new Vec3(p.x()+.5,p.y(),p.z()+.5);
        if(mc.player.position().distanceToSqr(center)<.35){next++;stalled=0;return false;}
        if(mc.player.position().distanceToSqr(last)<.0005)stalled++;else stalled=0;last=mc.player.position();
        if(stalled>60){reset();return false;}
        if(!passable(p)||!supported(p)){reset();return false;}
        GameActions.look(center.add(0,mc.player.getEyeHeight(),0),40);mc.options.keyUp.setDown(true);mc.options.keyJump.setDown(p.y()>mc.player.getY()+.3||mc.player.horizontalCollision);return false;
    }
    private boolean passable(PathSearch.Cell c){BlockPos p=new BlockPos(c.x(),c.y(),c.z());return mc.level.hasChunkAt(p)&&mc.level.getBlockState(p).getCollisionShape(mc.level,p).isEmpty()&&mc.level.getFluidState(p).isEmpty();}
    private boolean supported(PathSearch.Cell c){BlockPos p=new BlockPos(c.x(),c.y()-1,c.z());return mc.level.hasChunkAt(p)&&mc.level.getBlockState(p).isCollisionShapeFullBlock(mc.level,p)&&mc.level.getFluidState(p).isEmpty();}
    private static PathSearch.Cell cell(BlockPos p){return new PathSearch.Cell(p.getX(),p.getY(),p.getZ());}
    public void reset(){search=null;goal=null;path=List.of();next=0;stalled=0;GameActions.releaseMovement();}
}
