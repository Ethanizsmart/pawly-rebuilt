package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.*;
import dev.pawly.rebuilt.core.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import java.util.*;

public final class SchematicFeature extends Feature {
    private final Navigator navigator=new Navigator();
    private final LinkedHashMap<BlockPos,BlockState> tasks=new LinkedHashMap<>();
    private final Map<BlockPos,Integer> retryAfter=new HashMap<>();
    private int ticks,delay,scanCursor;
    private BlockPos scanOrigin;
    private final boolean manual;
    public SchematicFeature(boolean manual){
        super(manual?"easy_place":"schematic_builder",manual?"Litematica Tweak":"Schematic Builder",manual?"Packets":"World",manual?"Place the schematic block under your crosshair while holding use.":"Build loaded schematic blocks using your inventory and walkable routes.",Option.number("place_interval","Place delay",1,20,2),Option.number("turn_speed","Aim speed",10,90,90),Option.flag("replace_wrong","Break incorrect blocks",false),Option.flag("navigate","Walk to work",true),Option.number("scan_radius","Work radius",4,48,16),Option.number("planner_cpu_ms","Planner budget (ms)",1,50,6));this.manual=manual;
    }
    @Override public String unavailable(){return FabricLoader.getInstance().isModLoaded("litematica")&&FabricLoader.getInstance().isModLoaded("malilib")?"":"Requires Litematica and MaLiLib";}
    @Override public void tick(){
        ticks++;
        if(mc.screen!=null){navigator.reset();status="Close the screen to build";return;}
        if(PawlyClient.active("freecam")||!PawlyClient.instance().actions.acquire(id))return;
        if(manual){if(mc.options.keyUse.isDown()&&delay--<=0){fi.dy.masa.litematica.util.WorldUtils.handleEasyPlace(mc);delay=(int)number("place_interval");}status="Hold Use on schematic blocks";return;}
        if(mc.gameMode.getPlayerMode()!=GameType.SURVIVAL&&mc.gameMode.getPlayerMode()!=GameType.CREATIVE){status="Survival or creative required";return;}
        var placement=fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        var world=fi.dy.masa.litematica.world.SchematicWorldHandler.getSchematicWorld();
        if(placement==null||!placement.isEnabled()||world==null){navigator.reset();status="Select and enable a Litematica placement";return;}
        var box=placement.getEclosingBox();if(box==null||box.getPos1()==null||box.getPos2()==null){status="Placement has no region";return;}
        BlockPos a=box.getPos1(),b=box.getPos2();
        int r=(int)number("scan_radius"),side=2*r+1;
        if(scanOrigin==null||scanOrigin.distSqr(mc.player.blockPosition())>16){scanOrigin=mc.player.blockPosition();scanCursor=0;tasks.clear();}
        long deadline=System.nanoTime()+(long)(number("planner_cpu_ms")*1_000_000);
        for(int count=0;count<4096&&System.nanoTime()<deadline;count++){
            int i=scanCursor++%(side*side*side);BlockPos p=scanOrigin.offset(i%side-r,i/side%side-r,i/(side*side)-r);
            if(p.getX()<Math.min(a.getX(),b.getX())||p.getX()>Math.max(a.getX(),b.getX())||p.getY()<Math.min(a.getY(),b.getY())||p.getY()>Math.max(a.getY(),b.getY())||p.getZ()<Math.min(a.getZ(),b.getZ())||p.getZ()>Math.max(a.getZ(),b.getZ())||!mc.level.hasChunkAt(p))continue;
            var desired=world.getBlockState(p);if(!desired.isAir()&&desired!=mc.level.getBlockState(p))tasks.put(p,desired);else tasks.remove(p);
        }
        tasks.entrySet().removeIf(e->mc.level.getBlockState(e.getKey())==e.getValue()||!mc.level.hasChunkAt(e.getKey()));
        var task=tasks.entrySet().stream().filter(e->retryAfter.getOrDefault(e.getKey(),0)<=ticks).min(Comparator.comparingDouble(e->mc.player.position().distanceToSqr(Vec3.atCenterOf(e.getKey()))+Math.max(0,e.getKey().getY()-mc.player.getY())*20)).orElse(null);
        if(task==null){navigator.reset();status="Scanning "+placement.getName();return;}
        BlockPos p=task.getKey();if(mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(p))>16){if(flag("navigate")){navigator.walkNear(p,(int)(number("planner_cpu_ms")*80));status="Walking to "+p.toShortString();}else status="Move closer to "+p.toShortString();return;}
        navigator.reset();if(delay-->0)return;
        BlockState actual=mc.level.getBlockState(p);
        if(!actual.canBeReplaced()){
            if(flag("replace_wrong")){GameActions.look(Vec3.atCenterOf(p),number("turn_speed"));mc.gameMode.continueDestroyBlock(p,Direction.UP);mc.player.swing(InteractionHand.MAIN_HAND);status="Removing incorrect block";}
            else {retryAfter.put(p,ticks+100);status="Wrong block; replacement is disabled";}
            return;
        }
        var item=task.getValue().getBlock().asItem();
        if(!GameActions.equip(item)){status="Need "+item.getName().getString();return;}
        GameActions.look(Vec3.atCenterOf(p),number("turn_speed"));
        if(GameActions.place(p)){delay=(int)number("place_interval");retryAfter.put(p,ticks+10);status="Awaiting block confirmation";}else{retryAfter.put(p,ticks+40);status="No placement face";}
    }
    @Override public void stop(){navigator.reset();tasks.clear();retryAfter.clear();scanOrigin=null;if(mc.gameMode!=null)mc.gameMode.stopDestroyBlock();}
}
