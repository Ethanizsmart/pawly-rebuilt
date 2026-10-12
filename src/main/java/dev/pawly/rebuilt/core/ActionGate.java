package dev.pawly.rebuilt.core;

/** One controller may mutate inventory/world input during a client tick. */
public final class ActionGate {
    private String owner;
    public void beginTick(){owner=null;}
    public boolean acquire(String requested){if(owner==null||owner.equals(requested)){owner=requested;return true;}return false;}
    public String owner(){return owner;}
}
