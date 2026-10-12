package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.PawlyClient;
import dev.pawly.rebuilt.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;

public final class OrderDropFeature extends Feature {
    private int wait;
    public OrderDropFeature(){super("auto_order_drop","Auto Order Drop","Inventory","Drop configured stacks while the matching order screen is open.",Option.number("delay_ticks","Tick delay",1,20,2),Option.text("screen_title","Order screen title contains","Order"),Option.text("item","Item ID to drop","minecraft:cobblestone"));}
    @Override public void tick(){
        if(mc.screen==null||value("screen_title").isBlank()||!mc.screen.getTitle().getString().toLowerCase().contains(value("screen_title").toLowerCase())){status="Waiting for order screen";return;}
        if(wait-->0||!PawlyClient.instance().actions.acquire(id))return;
        var menu=mc.player.containerMenu;if(!menu.getCarried().isEmpty())return;
        for(var slot:menu.slots)if(slot.container==mc.player.getInventory()&&slot.hasItem()&&BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).toString().equals(value("item"))){
            mc.gameMode.handleInventoryMouseClick(menu.containerId,slot.index,1,ClickType.THROW,mc.player);wait=(int)number("delay_ticks");status="Dropped matching stack";return;
        }
        status="No matching stacks";
    }
}
