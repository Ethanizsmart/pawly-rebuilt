package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.core.*;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class CameraFeature extends Feature {
    public final boolean free;
    public Vec3 position=Vec3.ZERO,previousPosition=Vec3.ZERO;
    public float yaw,pitch;
    private float targetYaw,targetPitch,health;
    private CameraType previousType;
    public Input held=Input.EMPTY;
    public CameraFeature(boolean free){
        super(free?"freecam":"freelook",free?"Freecam":"Freelook","Render",free?"Move the camera independently from the player.":"Look around while your movement and aim stay fixed.",Option.number("flight_speed","Camera speed",.1,10,1),Option.number("camera_smoothing","Smoothing",0,.95,.3),Option.flag("continue_movement","Continue held movement",false),Option.flag("return_on_damage","Return on damage",true));this.free=free;
    }
    @Override public void start(){
        var other=PawlyClient.instance().feature(free?"freelook":"freecam");if(other.enabled())other.setEnabled(false);
        previousType=mc.options.getCameraType();position=previousPosition=mc.player.getEyePosition();yaw=targetYaw=mc.player.getYRot();pitch=targetPitch=mc.player.getXRot();health=mc.player.getHealth();held=mc.player.input.keyPresses;mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }
    public void turn(double dx,double dy){targetYaw+=(float)(dx*.15);targetPitch=Math.clamp(targetPitch+(float)(dy*.15),-90,90);}
    @Override public void tick(){
        if(flag("return_on_damage")&&mc.player.getHealth()<health){setEnabled(false);return;}health=mc.player.getHealth();
        float factor=(float)(1-number("camera_smoothing"));yaw+=net.minecraft.util.Mth.wrapDegrees(targetYaw-yaw)*factor;pitch+=(targetPitch-pitch)*factor;
        previousPosition=position;
        if(free&&mc.screen==null){
            double x=key(GLFW.GLFW_KEY_D)-key(GLFW.GLFW_KEY_A),z=key(GLFW.GLFW_KEY_W)-key(GLFW.GLFW_KEY_S),y=key(GLFW.GLFW_KEY_SPACE)-key(GLFW.GLFW_KEY_LEFT_SHIFT);
            double angle=Math.toRadians(yaw),speed=number("flight_speed")*(key(GLFW.GLFW_KEY_LEFT_CONTROL)>0?2:1);
            Vec3 direction=new Vec3(x*Math.cos(angle)-z*Math.sin(angle),y,z*Math.cos(angle)+x*Math.sin(angle));if(direction.lengthSqr()>1)direction=direction.normalize();position=position.add(direction.scale(speed));
        }
        if(!free){Vec3 back=Vec3.directionFromRotation(pitch,yaw).scale(-4);position=mc.player.getEyePosition().add(back);}
        status=free?"Camera detached":"Look detached";
    }
    private int key(int k){return GLFW.glfwGetKey(mc.getWindow().handle(),k)==GLFW.GLFW_PRESS?1:0;}
    @Override public void stop(){if(previousType!=null)mc.options.setCameraType(previousType);held=Input.EMPTY;}
}
