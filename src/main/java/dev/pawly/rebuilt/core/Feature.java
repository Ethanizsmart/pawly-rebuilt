package dev.pawly.rebuilt.core;

import java.util.*;
import net.minecraft.client.Minecraft;
import dev.pawly.rebuilt.PawlyClient;

public abstract class Feature {
    public final String id,name,category,description;
    public final List<Option> options=new ArrayList<>();
    private SettingsStore.Entry entry;
    public String status="";
    public volatile boolean running;
    protected final Minecraft mc=Minecraft.getInstance();
    protected Feature(String id,String name,String category,String description,Option... options){this.id=id;this.name=name;this.category=category;this.description=description;this.options.addAll(List.of(options));}
    public boolean defaultEnabled(){return false;}
    public void attach(SettingsStore store){
        entry=store.entry(id,defaultEnabled());
        for(Option option:options){try{entry.values.put(option.id(),option.validate(entry.values.getOrDefault(option.id(),option.initial())));}catch(IllegalArgumentException ex){entry.values.put(option.id(),option.initial());PawlyClient.LOGGER.warn("Reset invalid setting {}.{}",id,option.id());}}
    }
    public final boolean enabled(){return entry!=null&&entry.enabled;}
    public final String value(String id){if(!entry.values.containsKey(id))throw new IllegalArgumentException("Unknown setting "+this.id+"."+id);return entry.values.get(id);}
    public final double number(String id){return Double.parseDouble(value(id));}
    public final boolean flag(String id){return Boolean.parseBoolean(value(id));}
    public final void value(String id,String value){Option o=options.stream().filter(v->v.id().equals(id)).findFirst().orElseThrow();entry.values.put(id,o.validate(value));changed(id);PawlyClient.instance().save();}
    public final void setEnabled(boolean enabled){
        entry.enabled=enabled;
        if(!enabled)suspend();
        if(enabled)status="";
        PawlyClient.instance().save();
    }
    public final void reset(){for(Option o:options)entry.values.put(o.id(),o.initial());changed("");PawlyClient.instance().save();}
    public String unavailable(){return "";}
    public void changed(String option){}
    public void start(){}
    public abstract void tick();
    public void stop(){}
    public final void suspend(){if(running){running=false;try{stop();}catch(RuntimeException e){PawlyClient.LOGGER.error("Stopping {}",id,e);}}}
    public final void update(){
        if(!enabled()||mc.player==null||mc.level==null){suspend();return;}
        String reason=unavailable();if(!reason.isEmpty()){status=reason;suspend();return;}
        try {if(!running){running=true;start();}tick();}catch(Exception ex){status=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();setEnabled(false);PawlyClient.LOGGER.error("Feature {} stopped",id,ex);}
    }
}
