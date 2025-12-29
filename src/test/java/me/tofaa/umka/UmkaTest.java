package me.tofaa.umka;

import me.tofaa.umka.generated.UmkaExternFunc;
import me.tofaa.umka.generated.UmkaStackSlot;
import org.junit.jupiter.api.Test;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UmkaTest {

    @Test
    public void testRunScript() throws Exception {
         String script = """
                fn javaInc(x: int): int;
                
                fn main() {
                   res := javaInc(10)
                   printf("Res: %d\\n", res)
                   if res != 11 {
                       exit(1) // Fail
                   }
                }
                """;
         
         try (Umka umka = Umka.allocate()) {
            umka.init("test.um", script, 4096, new String[]{}, null, Features.ENABLE_IMPL_LIBS);
            
            umka.addFunc("javaInc", (params, result) -> {
                 MemorySegment slot = umka.getParam(params, 0);
                 long val = UmkaStackSlot.intVal(slot);
                 System.out.println("Java received: " + val);
                 
                 MemorySegment resSlot = umka.getResult(params, result);
                 UmkaStackSlot.intVal(resSlot, val + 1);
            });
            
            umka.compile();
            umka.run();
         }
    }
}
