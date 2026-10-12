package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.core.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import io.netty.channel.ChannelFutureListener;

public final class PacketDelayFeature extends Feature {
    private record Send(Connection connection,Packet<?> packet,ChannelFutureListener listener,boolean flush){}
    private final OrderedQueue<Send> pending=new OrderedQueue<>(4096);
    private Connection connection;
    private final ThreadLocal<Boolean> sending=ThreadLocal.withInitial(()->false);
    public PacketDelayFeature(){super("fake_lag","Fake Lag","Packets","Delay outgoing gameplay packets without reordering them.",Option.number("delay_ms","Delay in milliseconds",0,1000,50));}
    public synchronized boolean intercept(Connection c,Packet<?> packet,ChannelFutureListener listener,boolean flush){
        if(!running||sending.get()||connection!=c||!packet.getClass().getPackageName().equals("net.minecraft.network.protocol.game"))return false;
        if(!pending.add(System.nanoTime()+(long)(number("delay_ms")*1_000_000),new Send(c,packet,listener,flush))){pending.drain(this::send);return false;}
        return true;
    }
    @Override public synchronized void start(){connection=mc.getConnection().getConnection();pending.clear();}
    @Override public synchronized void tick(){if(connection!=mc.getConnection().getConnection()){pending.clear();connection=mc.getConnection().getConnection();}pending.due(System.nanoTime(),this::send);status=pending.size()+" packets queued";}
    private void send(Send send){if(send.connection().isConnected()){sending.set(true);try{send.connection().send(send.packet(),send.listener(),send.flush());}finally{sending.set(false);}}}
    @Override public synchronized void stop(){if(mc.getConnection()!=null&&mc.getConnection().getConnection()==connection)pending.drain(this::send);else pending.clear();connection=null;}
}
