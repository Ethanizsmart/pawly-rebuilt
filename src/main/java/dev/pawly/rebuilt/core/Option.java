package dev.pawly.rebuilt.core;

import java.math.BigDecimal;
import java.util.List;

public record Option(String id, String label, Kind kind, String initial, double min, double max, List<String> choices) {
    public enum Kind { BOOLEAN, NUMBER, TEXT, CHOICE }
    public static Option flag(String id, String label, boolean initial) { return new Option(id,label,Kind.BOOLEAN,""+initial,0,0,List.of()); }
    public static Option number(String id,String label,double min,double max,double initial) { return new Option(id,label,Kind.NUMBER,format(initial),min,max,List.of()); }
    public static Option text(String id,String label,String initial) { return new Option(id,label,Kind.TEXT,initial,0,4096,List.of()); }
    public static Option choice(String id,String label,String... values) { return new Option(id,label,Kind.CHOICE,values[0],0,0,List.of(values)); }
    public String validate(String value) {
        if(value==null)throw new IllegalArgumentException("Missing value");
        return switch(kind) {
            case BOOLEAN -> { if(!value.equals("true")&&!value.equals("false"))throw new IllegalArgumentException("Use true or false"); yield value; }
            case CHOICE -> { if(!choices.contains(value))throw new IllegalArgumentException("Unknown choice"); yield value; }
            case NUMBER -> { double n; try{n=Double.parseDouble(value);}catch(NumberFormatException e){throw new IllegalArgumentException("Enter a number");} if(!Double.isFinite(n)||n<min||n>max)throw new IllegalArgumentException("Range "+format(min)+" to "+format(max)); yield format(n); }
            case TEXT -> { if(value.length()>max||value.indexOf('\0')>=0)throw new IllegalArgumentException("Text too long or invalid"); yield value; }
        };
    }
    private static String format(double n){ return BigDecimal.valueOf(n).stripTrailingZeros().toPlainString(); }
}
