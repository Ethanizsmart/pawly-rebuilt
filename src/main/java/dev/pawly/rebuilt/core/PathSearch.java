package dev.pawly.rebuilt.core;

import java.util.*;
import java.util.function.Predicate;

/** Incremental A*: one-block climbs, bounded falls, no diagonal corner cutting. */
public final class PathSearch {
    public record Cell(int x,int y,int z){public Cell offset(int x,int y,int z){return new Cell(this.x+x,this.y+y,this.z+z);}public double distance(Cell b){return Math.abs(x-b.x)+Math.abs(z-b.z)+Math.abs(y-b.y)*1.2;}}
    private record Node(Cell cell,double score){}
    private final PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingDouble(Node::score));
    private final Map<Cell,Double> cost=new HashMap<>();
    private final Map<Cell,Cell> parents=new HashMap<>();
    private final Set<Cell> closed=new HashSet<>();
    private final Predicate<Cell> passable,supported,goal;
    private final Cell target,start;
    private List<Cell> result;
    private boolean done;
    public PathSearch(Cell start,Cell target,Predicate<Cell> passable,Predicate<Cell> supported,Predicate<Cell> goal){this.start=start;this.target=target;this.passable=passable;this.supported=supported;this.goal=goal;cost.put(start,0d);open.add(new Node(start,start.distance(target)));}
    public void step(int budget){
        while(!done&&budget-->0){
            Node node=open.poll();if(node==null||closed.size()>30000){done=true;result=List.of();return;}
            Cell at=node.cell();if(!closed.add(at))continue;
            if(goal.test(at)){List<Cell> path=new ArrayList<>();for(Cell c=at;c!=null;c=parents.get(c))path.add(c);Collections.reverse(path);result=List.copyOf(path);done=true;return;}
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
                Cell next=at.offset(d[0],0,d[1]);
                if(!clear(next)){next=next.offset(0,1,0);if(!clear(at.offset(0,1,0))||!clear(next))continue;}
                int fall=0;while(!supported.test(next)&&fall<3){next=next.offset(0,-1,0);fall++;if(!clear(next))break;}
                if(!clear(next)||!supported.test(next)||closed.contains(next)||Math.abs(next.x-start.x)>96||Math.abs(next.z-start.z)>96)continue;
                double score=cost.get(at)+1+Math.abs(next.y-at.y)*.4;
                if(score<cost.getOrDefault(next,Double.POSITIVE_INFINITY)){cost.put(next,score);parents.put(next,at);open.add(new Node(next,score+next.distance(target)));}
            }
        }
    }
    private boolean clear(Cell p){return passable.test(p)&&passable.test(p.offset(0,1,0));}
    public boolean done(){return done;}
    public List<Cell> result(){if(!done)throw new IllegalStateException("Search pending");return result;}
}
