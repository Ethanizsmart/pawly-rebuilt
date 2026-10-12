package dev.pawly.rebuilt.core;

import java.util.ArrayDeque;
import java.util.function.Consumer;

/** FIFO queue: changing the delay cannot reorder already accepted work. */
public final class OrderedQueue<T> {
    private record Pending<T>(long at,T value){}
    private final ArrayDeque<Pending<T>> queue=new ArrayDeque<>();
    private final int limit;
    public OrderedQueue(int limit){if(limit<1)throw new IllegalArgumentException();this.limit=limit;}
    public boolean add(long at,T value){if(queue.size()>=limit)return false;long order=queue.isEmpty()?at:Math.max(at,queue.getLast().at());queue.add(new Pending<>(order,value));return true;}
    public void due(long now,Consumer<T> accept){while(!queue.isEmpty()&&queue.getFirst().at()<=now)accept.accept(queue.remove().value());}
    public void drain(Consumer<T> accept){while(!queue.isEmpty())accept.accept(queue.remove().value());}
    public int size(){return queue.size();}
    public void clear(){queue.clear();}
}
