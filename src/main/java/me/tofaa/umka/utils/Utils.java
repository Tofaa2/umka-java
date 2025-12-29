package me.tofaa.umka.utils;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class Utils {

    private Utils() {}

    public static MemorySegment toArgvNullTerminated(String[] args, Arena arena) {
        if (args == null || args.length == 0) {
            return MemorySegment.NULL;
        }
        MemorySegment argv = arena.allocate(ValueLayout.ADDRESS, args.length + 1);
        for (int i = 0; i < args.length; i++) {
            MemorySegment cString = arena.allocateFrom(args[i]);
            argv.setAtIndex(ValueLayout.ADDRESS, i, cString);
        }
        argv.setAtIndex(ValueLayout.ADDRESS, args.length, MemorySegment.NULL);
        return argv;
    }

}
