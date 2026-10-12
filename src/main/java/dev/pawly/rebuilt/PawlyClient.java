package dev.pawly.rebuilt;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.pawly.rebuilt.core.*;
import dev.pawly.rebuilt.features.*;
import dev.pawly.rebuilt.ui.ModuleScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import static dev.pawly.rebuilt.core.Option.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PawlyClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Pawly Rebuilt");
    private static PawlyClient INSTANCE;
    public final ActionGate actions=new ActionGate();
    private final Map<String,Feature> features=new LinkedHashMap<>();
    private SettingsStore store;
    private KeyMapping menuKey;
    private Object world;
    public volatile boolean unloaded;
    public static PawlyClient instance(){return INSTANCE;}
    public static boolean active(String id){return INSTANCE!=null&&!INSTANCE.unloaded&&INSTANCE.features.containsKey(id)&&INSTANCE.features.get(id).running;}
    public Feature feature(String id){return Objects.requireNonNull(features.get(id),id);}
    public Collection<Feature> features(){return Collections.unmodifiableCollection(features.values());}
    private void add(Feature f){f.attach(store);if(features.put(f.id,f)!=null)throw new IllegalStateException("Duplicate feature "+f.id);}
    public void save(){try{store.save();}catch(Exception e){LOGGER.error("Cannot save config",e);GameActions.message(e.getMessage());}}
    public CameraFeature camera(){if(active("freecam"))return (CameraFeature)feature("freecam");if(active("freelook"))return (CameraFeature)feature("freelook");return null;}
    public void unload(){for(Feature f:features.values())f.suspend();unloaded=true;GameActions.message("Pawly Rebuilt stopped. /pawly opens the menu to resume.");}
    @Override public void onInitializeClient() {
        INSTANCE=this;
        store=new SettingsStore(FabricLoader.getInstance().getConfigDir().resolve("pawly-rebuilt/settings.json"));
        try{store.load();}catch(Exception e){LOGGER.error("Config load failed",e);}
        add(new SpawnerFeature());add(new ScannerFeature(true));add(new SchematicFeature(true));add(new TradeFeature());
        add(new SchematicFeature(false));add(new AreaMinerFeature());add(new OrderDropFeature());
        add(new PacketDelayFeature());add(new MacroFeature());add(new CameraFeature(true));add(new CameraFeature(false));add(new ScannerFeature(false));
        add(new FakePayFeature());
        add(new Feature("self_destruct","Self Destruct","Utilities","Stop all Pawly Rebuilt controllers and hooks until resumed from the menu.") {
            @Override public void tick(){setEnabled(false);PawlyClient.this.unload();}
        });
        add(new HookFeature("fullbright","Fullbright","Render","Increase rendered light without changing world lighting.",false,number("brightness","Brightness",1,15,12)));
        add(new HookFeature("swing_speed","Swing Speed","Render","Adjust the duration of the local hand animation.",false,number("speed","Animation speed",.1,3,1)));
        add(new HookFeature("clip_perspective","Clip Perspective","Render","Allow the third-person camera through walls.",false));
        add(new HookFeature("name_display","Name Display","Render","Show your own name in third person.",false));
        add(new HookFeature("scoreboard","Identity Changer","Render","Change rendered names and local display balance.",false,text("alias","Your display name",""),text("nearby_names","Other names",""),text("skin_name","Skin of an online player",""),text("money_value","Local display balance","0")));
        add(new HookFeature("coord_faker","Coord Faker","Utilities","Offset coordinates in the debug display; real position stays unchanged.",false,number("offset_x","X offset",-30000000,30000000,0),number("offset_y","Y offset",-2048,2048,0),number("offset_z","Z offset",-30000000,30000000,0)));
        add(new HookFeature("break_reset","Break Reset","Packets","Restart held mining after a confirmed block break.",false));
        add(new HookFeature("render_tooltips","Render Tooltips","Render","Show container contents and curse warnings in tooltips.",false,flag("shulkers","Container preview",true),flag("curse_marker","Curse warning",true)));
        add(new HookFeature("exploit_preventer","Exploit Preventer","Utilities","Reject invalid resource pack URLs and unsolicited sign edit probes.",true,flag("block_packs","Block all server resource packs",false),flag("block_signs","Block distant sign edit requests",true)));
        add(new HookFeature("region_map","Region Map","Render","Show the 50,000-block region grid around your position.",false,number("pos_x","Horizontal position",0,1920,12),number("pos_y","Vertical position",0,1080,12),number("cell_size","Cell size",8,24,10)));
        menuKey=KeyBindingHelper.registerKeyBinding(new KeyMapping("key.pawly_rebuilt.menu",InputConstants.Type.KEYSYM,344,KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            while(menuKey.consumeClick())mc.setScreen(new ModuleScreen(mc.screen));
            if(unloaded)return;
            if(world!=mc.level){for(Feature f:features.values())f.suspend();world=mc.level;}
            actions.beginTick();for(Feature f:features.values()){if(unloaded)break;f.update();}
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,mc)->{for(Feature f:features.values())f.suspend();world=null;});
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("pawly_rebuilt","overlay"),new Overlay());
        TooltipFeatures.register();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,access)->dispatcher.register(literal("pawly").executes(context->{Minecraft.getInstance().execute(()->Minecraft.getInstance().setScreen(new ModuleScreen(null)));return 1;}).then(literal("stop").executes(context->{unload();return 1;}))));
        LOGGER.info("Pawly Rebuilt initialized");
    }
}
