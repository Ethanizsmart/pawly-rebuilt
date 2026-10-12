package dev.pawly.rebuilt.features;

import dev.pawly.rebuilt.core.Feature;
import dev.pawly.rebuilt.core.Option;

/** Its implementation is called by the named Minecraft mixins, not a tick loop. */
public final class HookFeature extends Feature {
    private final boolean initial;
    public HookFeature(String id,String name,String category,String description,boolean initial,Option...options){super(id,name,category,description,options);this.initial=initial;}
    @Override public boolean defaultEnabled(){return initial;}
    @Override public void tick(){status="Enabled";}
}
