package me.tofaa.umka;

import me.tofaa.umka.error.UmkaError;
import me.tofaa.umka.error.UmkaException;
import me.tofaa.umka.error.UmkaWarningCallback;
import me.tofaa.umka.generated.UnsafeUmka;
import me.tofaa.umka.generated.UmkaExternFunc;
import me.tofaa.umka.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

final class UmkaImpl implements Umka {

    private final MemorySegment umka;
    private final Arena arena;

    UmkaImpl(MemorySegment umka) {
        this.umka = umka;
        this.arena = Arena.ofConfined();
    }

    @Override
    public void addModule(String fileName, String fileContent) throws UmkaException {
        boolean res = UnsafeUmka.umkaAddModule(
                umka,
                arena.allocateFrom(fileName),
                arena.allocateFrom(fileContent)
        );
        if (!res) {
            throw new UmkaException("Failed to add module: " + fileName);
        }
    }

    @Override
    public String getAsm() {
        var constCharPtr = UnsafeUmka.umkaAsm(umka);
        if (constCharPtr.address() == 0) return "";
        return constCharPtr.getString(0);
    }

    @Override
    public long getMemoryUsage() {
        return UnsafeUmka.umkaGetMemUsage(umka);
    }

    @Override
    public void addGlobal(String name, Object value) {
    }

    @Override
    public void run() throws UmkaException {
        int code = UnsafeUmka.umkaRun(umka);
        if (code != 0) {
            throw new UmkaException("Umka Runtime Exit With Status Code: " + code);
        }
    }

    @Override
    public void compile() throws UmkaException {
        boolean suc = UnsafeUmka.umkaCompile(umka);
        if (!suc) {
            var err = getLastError();
            throw new UmkaException("Umka Compilation Failed: " + err);
        }
    }

    @Override
    public @Nullable UmkaError getLastError() {
        var seg = UnsafeUmka.umkaGetError(umka);
        if (seg.address() == 0) return null;
        return new UmkaError(seg);
    }

    @Override
    public void init(String fileName, String fileContent, int stackSize, String[] args, @Nullable UmkaWarningCallback warningCallback, int features) throws UmkaException {
        boolean res = UnsafeUmka.umkaInit(
                umka,
                arena.allocateFrom(fileName),
                arena.allocateFrom(fileContent),
                stackSize,
                MemorySegment.NULL,
                args.length,
                Utils.toArgvNullTerminated(args, arena),
                Features.hasFeature(features, Features.ENABLE_FILE_SYSTEM),
                Features.hasFeature(features, Features.ENABLE_IMPL_LIBS),
                warningCallback == null ? MemorySegment.NULL : warningCallback.toNative(arena)
        );
        if (!res) {
            throw new UmkaException();
        }
    }

    @Override
    public String getVersion() {
        return UnsafeUmka.umkaGetVersion().getString(0);
    }

    @Override
    public void close() {
        UnsafeUmka.umkaFree(umka);
        arena.close();
    }

    public MemorySegment getUmkaAddress() {
        return umka;
    }

    @Override
    public boolean addFunc(String name, UmkaExternFunc.Function func) {
        return UnsafeUmka.umkaAddFunc(
                umka,
                arena.allocateFrom(name),
                UmkaExternFunc.allocate(func, arena)
        );
    }

    @Override
    public boolean getFunc(@Nullable String moduleName, String funcName, MemorySegment fn) {
        return UnsafeUmka.umkaGetFunc(
                umka,
                moduleName == null ? MemorySegment.NULL : arena.allocateFrom(moduleName),
                arena.allocateFrom(funcName),
                fn
        );
    }

    @Override
    public int call(MemorySegment fn) {
        return UnsafeUmka.umkaCall(umka, fn);
    }

    @Override
    public MemorySegment makeStr(String str) {
        return UnsafeUmka.umkaMakeStr(umka, arena.allocateFrom(str));
    }

    @Override
    public MemorySegment getParam(MemorySegment params, int index) {
        return UnsafeUmka.umkaGetParam(params, index);
    }

    @Override
    public MemorySegment getResult(MemorySegment params, MemorySegment result) {
        return UnsafeUmka.umkaGetResult(params, result);
    }

    @Override
    public MemorySegment getParamType(MemorySegment params, int index) {
        return UnsafeUmka.umkaGetParamType(params, index);
    }

    @Override
    public MemorySegment getResultType(MemorySegment params, MemorySegment result) {
        return UnsafeUmka.umkaGetResultType(params, result);
    }

    @Override
    public MemorySegment makeStruct(MemorySegment type) {
        return UnsafeUmka.umkaMakeStruct(umka, type);
    }

    @Override
    public void makeDynArray(MemorySegment array, MemorySegment type, int len) {
        UnsafeUmka.umkaMakeDynArray(umka, array, type, len);
    }

    @Override
    public int getDynArrayLen(MemorySegment array) {
        return UnsafeUmka.umkaGetDynArrayLen(array);
    }

    @Override
    public MemorySegment makeStr(MemorySegment cStr) {
        return UnsafeUmka.umkaMakeStr(umka, cStr);
    }

    @Override
    public MemorySegment allocData(int size, @Nullable UmkaExternFunc.Function onFree) {
        return UnsafeUmka.umkaAllocData(
                umka,
                size,
                onFree == null ? MemorySegment.NULL : UmkaExternFunc.allocate(onFree, arena)
        );
    }

    @Override
    public void incRef(MemorySegment ptr) {
        UnsafeUmka.umkaIncRef(umka, ptr);
    }

    @Override
    public void decRef(MemorySegment ptr) {
        UnsafeUmka.umkaDecRef(umka, ptr);
    }

    @Override
    public boolean getCallStack(int depth, int nameSize, MemorySegment offset, MemorySegment fileName, MemorySegment funcName, MemorySegment line) {
        return UnsafeUmka.umkaGetCallStack(
                umka,
                depth,
                nameSize,
                offset,
                fileName,
                funcName,
                line
        );
    }

    @Override
    public Arena getArena() {
        return arena;
    }
}
