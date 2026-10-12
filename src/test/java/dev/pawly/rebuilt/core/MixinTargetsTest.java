package dev.pawly.rebuilt.core;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Inspects class bytes only. Does not load or initialize Minecraft/mod classes. */
class MixinTargetsTest {
    @Test void injectionSelectorsAndShadowsExistInPinnedMinecraft() throws Exception {
        Path root=Path.of(System.getProperty("pawly.classes"),"dev/pawly/rebuilt/mixin");
        int injections=0;
        try(var files=Files.list(root)) {
            for(Path file:files.filter(p->p.toString().endsWith(".class")).toList()) {
                ClassNode mixin=read(Files.readAllBytes(file));
                var annotation=annotations(mixin.visibleAnnotations,mixin.invisibleAnnotations).stream().filter(a->a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
                @SuppressWarnings("unchecked") var targets=(List<Type>)value(annotation,"value");
                assertEquals(1,targets.size());
                String resource=targets.getFirst().getInternalName()+".class";
                ClassNode target;
                try(var bytes=getClass().getClassLoader().getResourceAsStream(resource)){assertNotNull(bytes,resource);target=read(bytes.readAllBytes());}
                for(MethodNode method:mixin.methods) for(AnnotationNode a:annotations(method.visibleAnnotations,method.invisibleAnnotations)) {
                    if(a.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;"))
                        assertTrue(target.methods.stream().anyMatch(m->m.name.equals(method.name)&&m.desc.equals(method.desc)),mixin.name+" shadow "+method.name+method.desc);
                    if(!a.desc.startsWith("Lorg/spongepowered/asm/mixin/injection/"))continue;
                    Object selectors=value(a,"method");if(!(selectors instanceof List<?> list))continue;
                    for(Object item:list){String selector=item.toString();int split=selector.indexOf('(');String name=split<0?selector:selector.substring(0,split),desc=split<0?null:selector.substring(split);
                        var matches=target.methods.stream().filter(m->m.name.equals(name)&&(desc==null||m.desc.equals(desc))).toList();
                        assertEquals(1,matches.size(),mixin.name+" -> "+target.name+"."+selector);injections++;
                        Object points=value(a,"at");List<?> atList=points instanceof List<?> l?l:points instanceof AnnotationNode?List.of(points):List.of();
                        for(Object point:atList)if(point instanceof AnnotationNode at&&"INVOKE".equals(value(at,"value"))){
                            String invocation=(String)value(at,"target");boolean found=false;
                            for(var insn:matches.getFirst().instructions)if(insn instanceof MethodInsnNode call&&("L"+call.owner+";"+call.name+call.desc).equals(invocation)){found=true;break;}
                            assertTrue(found,mixin.name+" missing invocation "+invocation);
                        }
                    }
                }
                for(FieldNode field:mixin.fields) if(annotations(field.visibleAnnotations,field.invisibleAnnotations).stream().anyMatch(a->a.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")))
                    assertTrue(target.fields.stream().anyMatch(f->f.name.equals(field.name)&&f.desc.equals(field.desc)),mixin.name+" field "+field.name);
            }
        }
        assertTrue(injections>=16,"Expected all registered injection selectors");
    }
    private static ClassNode read(byte[] bytes){var node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return node;}
    private static List<AnnotationNode> annotations(List<AnnotationNode> a,List<AnnotationNode> b){var result=new ArrayList<AnnotationNode>();if(a!=null)result.addAll(a);if(b!=null)result.addAll(b);return result;}
    private static Object value(AnnotationNode node,String key){if(node.values!=null)for(int i=0;i<node.values.size();i+=2)if(node.values.get(i).equals(key))return node.values.get(i+1);return null;}
}
