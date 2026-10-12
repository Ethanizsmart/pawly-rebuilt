package dev.pawly.rebuilt.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CoreTest {
    @TempDir Path temporary;
    @Test void numericSettingsRejectNonFiniteAndOutOfRange() {
        var option=Option.number("delay","Delay",0,20,2);
        for(String value:List.of("NaN","Infinity","-1","21","text"))
            assertThrows(IllegalArgumentException.class,()->option.validate(value));
        assertEquals("2.5",option.validate("2.500"));
    }
    @Test void typedSettingsRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class,()->Option.flag("a","a",false).validate("yes"));
        assertThrows(IllegalArgumentException.class,()->Option.choice("a","a","One","Two").validate("Three"));
        assertThrows(IllegalArgumentException.class,()->Option.text("a","a","").validate("a\0b"));
    }
    @Test void settingsRoundTrip() throws Exception {
        Path file=temporary.resolve("nested/settings.json");var first=new SettingsStore(file);
        first.entry("camera",true).values.put("speed","1.5");first.save();
        var second=new SettingsStore(file);second.load();
        assertTrue(second.entry("camera",false).enabled);
        assertEquals("1.5",second.entry("camera",false).values.get("speed"));
        assertFalse(Files.exists(file.resolveSibling("settings.json.tmp")));
    }
    @Test void corruptSettingsArePreserved() throws Exception {
        Path file=temporary.resolve("settings.json");Files.writeString(file,"{broken");
        var store=new SettingsStore(file);
        assertThrows(java.io.IOException.class,store::load);
        assertThrows(java.io.IOException.class,store::save);
        assertEquals("{broken",Files.readString(file));
    }
    @Test void futureConfigIsPreserved() throws Exception {
        Path file=temporary.resolve("settings.json");String original="{\"version\":2,\"modules\":{}}";Files.writeString(file,original);
        var store=new SettingsStore(file);assertThrows(java.io.IOException.class,store::load);
        assertThrows(java.io.IOException.class,store::save);assertEquals(original,Files.readString(file));
    }
    @Test void queuePreservesOrderWhenDelayShrinks() {
        var queue=new OrderedQueue<String>(3);var output=new ArrayList<String>();
        assertTrue(queue.add(100,"first"));assertTrue(queue.add(10,"second"));assertTrue(queue.add(200,"third"));assertFalse(queue.add(300,"overflow"));
        queue.due(99,output::add);assertTrue(output.isEmpty());
        queue.due(100,output::add);assertEquals(List.of("first","second"),output);
        queue.drain(output::add);assertEquals(List.of("first","second","third"),output);assertEquals(0,queue.size());
    }
    @Test void queueClearDiscardsOldConnectionWork() {
        var queue=new OrderedQueue<Integer>(2);queue.add(1,1);queue.clear();
        queue.drain(v->fail("Stale work executed"));assertEquals(0,queue.size());
    }
    @Test void actionsHaveOneOwnerPerTick() {
        var gate=new ActionGate();gate.beginTick();assertTrue(gate.acquire("builder"));assertTrue(gate.acquire("builder"));assertFalse(gate.acquire("trade"));gate.beginTick();assertTrue(gate.acquire("trade"));
    }
    @Test void pathDetoursAroundTwoBlockWall() {
        var start=new PathSearch.Cell(0,1,0);var target=new PathSearch.Cell(3,1,0);
        var search=new PathSearch(start,target,c->c.y()>=1&&!(c.x()==1&&c.z()==0&&c.y()<4),c->c.y()==1,target::equals);
        finish(search);assertFalse(search.result().isEmpty());assertEquals(target,search.result().getLast());
        assertTrue(search.result().stream().noneMatch(c->c.x()==1&&c.z()==0));
    }
    @Test void pathRejectsUnsupportedVoid() {
        var start=new PathSearch.Cell(0,1,0);var target=new PathSearch.Cell(2,1,0);
        var search=new PathSearch(start,target,c->true,c->c.equals(start),target::equals);
        finish(search);assertTrue(search.result().isEmpty());
    }
    @Test void pendingPathCannotBeRead() {
        var start=new PathSearch.Cell(0,1,0);var search=new PathSearch(start,start,c->true,c->true,start::equals);
        assertThrows(IllegalStateException.class,search::result);search.step(1);assertEquals(List.of(start),search.result());
    }
    private void finish(PathSearch search) {for(int i=0;i<1000&&!search.done();i++)search.step(100);assertTrue(search.done());}
}
