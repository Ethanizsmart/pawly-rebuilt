package dev.pawly.rebuilt.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SettingsStore {
    public static final class Entry { public boolean enabled; public Map<String,String> values=new LinkedHashMap<>(); }
    public static final class Data { public int version=1; public Map<String,Entry> modules=new LinkedHashMap<>(); }
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private Data data=new Data();
    private boolean writable=true;
    public SettingsStore(Path file){this.file=file;}
    public void load() throws IOException {
        if(!Files.exists(file))return;
        try {
            Data parsed=JSON.fromJson(Files.readString(file,StandardCharsets.UTF_8),Data.class);
            if(parsed==null||parsed.version!=1||parsed.modules==null)throw new IllegalArgumentException("Unsupported config");
            for(var e:parsed.modules.entrySet()) if(e.getKey()==null||e.getValue()==null||e.getValue().values==null)throw new IllegalArgumentException("Invalid module config");
            data=parsed;
        } catch(IOException e) { writable=false; throw e; }
        catch(RuntimeException e) { writable=false; throw new IOException("Config is invalid; original file preserved: "+file,e); }
    }
    public Entry entry(String id,boolean initial){return data.modules.computeIfAbsent(id,k->{Entry e=new Entry();e.enabled=initial;return e;});}
    public void save() throws IOException {
        if(!writable)throw new IOException("Saving disabled because existing config could not be read");
        Files.createDirectories(file.getParent());
        Path temporary=file.resolveSibling(file.getFileName()+".tmp");
        Files.writeString(temporary,JSON.toJson(data),StandardCharsets.UTF_8);
        try { Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
        catch(AtomicMoveNotSupportedException e){Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);}
    }
}
