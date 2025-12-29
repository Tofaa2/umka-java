package me.tofaa.umka.error;

import java.lang.foreign.MemorySegment;

public record UmkaError(
        String fileName,
        String fnName,
        int line,
        int position,
        int code,
        String message
) {

    public UmkaError(MemorySegment segment) {
        var fileNamePtr = me.tofaa.umka.generated.UmkaError.fileName(segment);
        var fnNamePtr = me.tofaa.umka.generated.UmkaError.fnName(segment);
        var msgPtr = me.tofaa.umka.generated.UmkaError.msg(segment);

        // getString(0) now works on ADDRESS-typed segments in recent Java versions
        var fileName = fileNamePtr.address() == 0 ? null : fileNamePtr.getString(0);
        var fnName = fnNamePtr.address() == 0 ? null : fnNamePtr.getString(0);
        var message = msgPtr.address() == 0 ? null : msgPtr.getString(0);

        var line = me.tofaa.umka.generated.UmkaError.line(segment);
        var position = me.tofaa.umka.generated.UmkaError.pos(segment);
        var code = me.tofaa.umka.generated.UmkaError.code(segment);

        this(fileName, fnName, line, position, code, message);
    }

}
