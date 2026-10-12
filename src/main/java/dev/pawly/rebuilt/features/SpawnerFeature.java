package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.GameActions;
import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionHand;

public final class SpawnerFeature extends Feature {
    private BlockPos target;
    private int alertCooldown;
    public SpawnerFeature(){super("spawner_protect","Spawner Protect","World","Detect nearby untrusted players and recover nearby spawners.",Option.number("threat_radius","Player threat radius",4,128,24),Option.text("friends","Trusted player names, comma separated",""),Option.choice("response","Threat response","Alert","Mine spawners","Command"),Option.text("command","Recovery command","/home"),Option.number("response_speed","Speed (%)",75,125,100));}
    @Override public void tick(){
        boolean threat=mc.level.players().stream().anyMatch(p->p!=mc.player&&!p.isSpectator()&&p.distanceToSqr(mc.player)<number("threat_radius")*number("threat_radius")&&!java.util.Arrays.stream(value("friends").split(",")).anyMatch(n->n.strip().equalsIgnoreCase(p.getName().getString())));
        if(!threat){stop();status="Watching for threats";return;}
        if(alertCooldown--<=0){GameActions.message("Spawner Protect: untrusted player nearby");alertCooldown=100;if(value("response").equals("Command")&&!value("command").isBlank())GameActions.chat(value("command"));}
        if(!value("response").equals("Mine spawners")||mc.screen!=null){stop();return;}
        if(!PawlyClient.instance().actions.acquire(id))return;
        if(target==null||!mc.level.getBlockState(target).is(Blocks.SPAWNER)){
            target=null;BlockPos center=mc.player.blockPosition();for(BlockPos p:BlockPos.betweenClosed(center.offset(-4,-4,-4),center.offset(4,4,4)))if(mc.level.getBlockState(p).is(Blocks.SPAWNER)&&mc.player.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p))<20.25){target=p.immutable();break;}
        }
        if(target!=null){GameActions.look(net.minecraft.world.phys.Vec3.atCenterOf(target),90*number("response_speed")/100);mc.gameMode.continueDestroyBlock(target,Direction.UP);mc.player.swing(InteractionHand.MAIN_HAND);status="Recovering "+target.toShortString();}else status="No reachable spawners";
    }
    @Override public void stop(){if(target!=null&&mc.gameMode!=null)mc.gameMode.stopDestroyBlock();target=null;}
}
