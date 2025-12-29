package me.tofaa.umka.error;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface UmkaWarningCallback {

    void handle(UmkaError error);

    /**
     * Converts this Java callback to a native function pointer (MemorySegment)
     * that can be passed to native code.
     *
     * @param arena The arena that manages the lifetime of the upcall stub
     * @return A MemorySegment representing the native function pointer
     */
    default MemorySegment toNative(Arena arena) {
        // Create an adapter that converts the native MemorySegment to UmkaError
        me.tofaa.umka.generated.UmkaWarningCallback.Function nativeCallback =
                (MemorySegment errorSegment) -> {
                    // Convert the native struct pointer to our Java record
                    UmkaError error = new UmkaError(errorSegment);
                    // Call the Java callback
                    this.handle(error);
                };

        // Allocate the upcall stub using jextract's generated code
        return me.tofaa.umka.generated.UmkaWarningCallback.allocate(nativeCallback, arena);
    }

}
