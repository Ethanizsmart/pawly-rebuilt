package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.GameActions;
import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.core.*;
import java.math.BigDecimal;
import net.minecraft.network.chat.Component;

public final class FakePayFeature extends Feature {
    private final OrderedQueue<String> messages=new OrderedQueue<>(100);
    public FakePayFeature(){super("fake_pay","Fake Pay","Utilities","Local /pay feedback only; matching commands never reach the server.",Option.text("pay_command","Intercept command","pay"),Option.choice("pay_feedback","Feedback","Both","Chat","Action bar"),Option.number("pay_ping_ms","Feedback delay (ms)",0,2000,260));}
    public boolean intercept(String command){
        if(!running)return false;
        String[] args=command.strip().split("\\s+");
        if(value("pay_command").isBlank()||!args[0].equalsIgnoreCase(value("pay_command").replaceFirst("^/","")))return false;
        if(args.length!=3){GameActions.message("Usage: /"+value("pay_command")+" <player> <amount>");return true;}
        try{
            BigDecimal amount=parseAmount(args[2]);if(amount.signum()<=0)throw new NumberFormatException();
            Feature identity=PawlyClient.instance().feature("scoreboard");
            BigDecimal balance=parseAmount(identity.value("money_value"));
            String message;
            if(amount.compareTo(balance)>0)message="Insufficient local display balance";
            else {identity.value("money_value",balance.subtract(amount).toPlainString());message="Paid $"+amount.stripTrailingZeros().toPlainString()+" to "+args[1]+" (local display)";}
            messages.add(System.nanoTime()+(long)(number("pay_ping_ms")*1_000_000),message);
        }catch(IllegalArgumentException ex){GameActions.message("Invalid amount");}
        return true;
    }
    public static BigDecimal parseAmount(String s){s=s.strip().replace(",","").replace("$","").toLowerCase();BigDecimal multiplier=BigDecimal.ONE;if(s.endsWith("k")||s.endsWith("m")||s.endsWith("b")){multiplier=BigDecimal.TEN.pow(s.endsWith("k")?3:s.endsWith("m")?6:9);s=s.substring(0,s.length()-1);}BigDecimal result=new BigDecimal(s).multiply(multiplier);if(result.precision()>30||Math.abs(result.scale())>20)throw new NumberFormatException();return result;}
    @Override public void tick(){messages.due(System.nanoTime(),s->{if(!value("pay_feedback").equals("Action bar"))mc.player.displayClientMessage(Component.literal(s),false);if(!value("pay_feedback").equals("Chat"))mc.player.displayClientMessage(Component.literal(s),true);});status="Local feedback only";}
    @Override public void stop(){messages.clear();}
}
