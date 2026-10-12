package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.*;
import dev.pawly.rebuilt.core.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;

public final class AreaMinerFeature extends Feature {
    private final Navigator navigation=new Navigator();
    private BlockPos target;
    private int scan;
    public AreaMinerFeature(){super("auto_area_miner","Auto Area Miner","World","Clear a configured cuboid from top to bottom.",Option.text("corner1","First corner: x,y,z","0,0,0"),Option.text("corner2","Second corner: x,y,z","0,0,0"),Option.number("turn_speed","Aim speed",10,90,55),Option.flag("navigate","Walk to reachable blocks",true));}
    private BlockPos parse(String text){String[] p=text.split(",");if(p.length!=3)throw new IllegalArgumentException("Set both corners as x,y,z");int x=Integer.parseInt(p[0].strip()),y=Integer.parseInt(p[1].strip()),z=Integer.parseInt(p[2].strip());if(Math.abs((long)x)>30000000||Math.abs((long)z)>30000000||y<mc.level.getMinY()||y>mc.level.getMaxY())throw new IllegalArgumentException("Corners must be inside world bounds");return new BlockPos(x,y,z);}
    @Override public void start(){scan=0;target=null;}
    @Override public void tick(){
        if(mc.screen!=null){navigation.reset();return;}
        if(PawlyClient.active("schematic_builder")||PawlyClient.active("freecam")){status="Paused by another controller";navigation.reset();return;}
        if(!PawlyClient.instance().actions.acquire(id))return;
        BlockPos a=parse(value("corner1")),b=parse(value("corner2"));
        int sx=Math.abs(a.getX()-b.getX())+1,sy=Math.abs(a.getY()-b.getY())+1,sz=Math.abs(a.getZ()-b.getZ())+1;
        long volume=(long)sx*sy*sz;if(volume>1_000_000)throw new IllegalArgumentException("Selection exceeds 1,000,000 blocks");
        if(a.equals(BlockPos.ZERO)&&b.equals(BlockPos.ZERO)){status="Set selection corners";return;}
        if(target!=null&&mc.level.getBlockState(target).isAir()){target=null;scan++;}
        if(target==null)for(int n=0;n<256&&scan<volume;n++,scan++){
            int layer=scan/(sx*sz),row=scan/sx%sz,x=scan%sx;if((row&1)!=0)x=sx-1-x;
            BlockPos p=new BlockPos(Math.min(a.getX(),b.getX())+x,Math.max(a.getY(),b.getY())-layer,Math.min(a.getZ(),b.getZ())+row);
            if(!mc.level.hasChunkAt(p)){status="Waiting for selected chunks";return;}
            var state=mc.level.getBlockState(p);if(!state.isAir()&&state.getDestroySpeed(mc.level,p)>=0){target=p;break;}
        }
        if(target==null){if(scan>=volume){status="Selection complete";setEnabled(false);}return;}
        if(target.equals(mc.player.blockPosition().below())){scan++;target=null;return;}
        if(mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(target))>16){if(flag("navigate"))navigation.walkNear(target,400);status="Moving toward selection";return;}
        navigation.reset();GameActions.look(Vec3.atCenterOf(target),number("turn_speed"));mc.gameMode.continueDestroyBlock(target,Direction.UP);mc.player.swing(InteractionHand.MAIN_HAND);status="Mining "+target.toShortString();
    }
    @Override public void stop(){navigation.reset();target=null;if(mc.gameMode!=null)mc.gameMode.stopDestroyBlock();}
}
