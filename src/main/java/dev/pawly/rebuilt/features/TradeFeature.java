package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.core.*;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.core.registries.BuiltInRegistries;

public final class TradeFeature extends Feature {
    private int cooldown,index,uses=-1,selected=-1;
    private Object menu;
    public TradeFeature(){super("fast_villager_trade","Fast Villager Trade","Inventory","Repeat selected trades through the synchronized merchant menu.",Option.number("delay_ticks","Trade delay",1,40,2),Option.text("result","Result item ID (empty = selected trade)",""),Option.number("trade_index","Trade number",1,100,1),Option.flag("cycle","Cycle matching trades",false));}
    @Override public void tick(){
        if(!(mc.player.containerMenu instanceof MerchantMenu merchant)){status="Open a villager trade";menu=null;return;}
        if(!merchant.getCarried().isEmpty()){status="Clear cursor item";return;}
        if(menu!=merchant){menu=merchant;index=(int)number("trade_index")-1;cooldown=0;uses=-1;selected=-1;}
        if(cooldown-->0||!PawlyClient.instance().actions.acquire(id))return;
        var offers=merchant.getOffers();if(offers.isEmpty())return;
        index=Math.floorMod(index,offers.size());
        for(int n=0;n<offers.size();n++){
            var offer=offers.get(index);
            boolean matches=value("result").isBlank()||BuiltInRegistries.ITEM.getKey(offer.getResult().getItem()).toString().equals(value("result"));
            if(!offer.isOutOfStock()&&matches){
                if(selected==index&&merchant.getSlot(2).hasItem()&&net.minecraft.world.item.ItemStack.isSameItemSameComponents(merchant.getSlot(2).getItem(),offer.getResult())&&offer.getUses()!=uses){mc.gameMode.handleInventoryMouseClick(merchant.containerId,2,0,ClickType.QUICK_MOVE,mc.player);uses=offer.getUses();status="Trading "+offer.getResult().getHoverName().getString();}
                else {merchant.setSelectionHint(index);merchant.tryMoveItems(index);mc.getConnection().send(new ServerboundSelectTradePacket(index));selected=index;status="Waiting for items / server";}
                cooldown=(int)number("delay_ticks");return;
            }
            if(!flag("cycle")&&value("result").isBlank()){status="Selected trade exhausted";return;}
            index=(index+1)%offers.size();uses=-1;
        }
        status="No available matching trades";
    }
    @Override public void changed(String option){menu=null;selected=-1;}
    @Override public void stop(){menu=null;uses=-1;selected=-1;}
}
