package dev.pawly.rebuilt;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class GameActions {
    private static final Minecraft MC=Minecraft.getInstance();
    private GameActions(){}
    public static void message(String text){if(MC.player!=null)MC.player.displayClientMessage(net.minecraft.network.chat.Component.literal("[Pawly] "+text),false);}
    public static void chat(String text){if(MC.getConnection()==null)return;if(text.startsWith("/"))MC.getConnection().sendCommand(text.substring(1));else MC.getConnection().sendChat(text);}
    public static boolean equip(Item item){
        var inv=MC.player.getInventory();
        for(int i=0;i<9;i++)if(inv.getItem(i).is(item)){inv.setSelectedSlot(i);return true;}
        if(MC.player.containerMenu!=MC.player.inventoryMenu||!MC.player.containerMenu.getCarried().isEmpty())return false;
        for(int i=9;i<36;i++)if(inv.getItem(i).is(item)){MC.gameMode.handleInventoryMouseClick(MC.player.inventoryMenu.containerId,i,inv.getSelectedSlot(),ClickType.SWAP,MC.player);return false;}
        return false;
    }
    public static void look(Vec3 target,double maxStep){
        Vec3 d=target.subtract(MC.player.getEyePosition());
        float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        float pitch=(float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z)));
        MC.player.setYRot(approach(MC.player.getYRot(),yaw,(float)maxStep));
        MC.player.setXRot(approach(MC.player.getXRot(),pitch,(float)maxStep));
    }
    private static float approach(float from,float to,float step){float d=net.minecraft.util.Mth.wrapDegrees(to-from);return from+Math.clamp(d,-step,step);}
    public static boolean place(BlockPos pos){
        for(Direction face:Direction.values()){
            BlockPos neighbor=pos.relative(face);
            if(MC.level.getBlockState(neighbor).isAir()||MC.level.getBlockState(neighbor).getCollisionShape(MC.level,neighbor).isEmpty())continue;
            Vec3 hit=Vec3.atCenterOf(neighbor).add(face.getOpposite().getUnitVec3().scale(.5));
            if(hit.distanceToSqr(MC.player.getEyePosition())>20.25)continue;
            look(hit,90);
            var result=MC.gameMode.useItemOn(MC.player,InteractionHand.MAIN_HAND,new BlockHitResult(hit,face.getOpposite(),neighbor,false));
            if(result.consumesAction()){MC.player.swing(InteractionHand.MAIN_HAND);return true;}
        }
        return false;
    }
    public static void releaseMovement(){MC.options.keyUp.setDown(false);MC.options.keyDown.setDown(false);MC.options.keyLeft.setDown(false);MC.options.keyRight.setDown(false);MC.options.keyJump.setDown(false);MC.options.keyShift.setDown(false);}
}
