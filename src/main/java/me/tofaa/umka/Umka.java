package me.tofaa.umka;


import me.tofaa.umka.error.UmkaError;
import me.tofaa.umka.error.UmkaException;
import me.tofaa.umka.error.UmkaWarningCallback;
import me.tofaa.umka.generated.UnsafeUmka;
import me.tofaa.umka.generated.UmkaExternFunc;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Umka extends AutoCloseable{
    String LIB_NAME = "libumka";

    static Umka allocate() {
        System.loadLibrary(LIB_NAME);
        return new UmkaImpl(UnsafeUmka.umkaAlloc());
    }


    void addModule(String fileName, String fileContent) throws UmkaException;

    default void addModule(
            Path path
    ) throws UmkaException {
        String fileName = path.getFileName().toString();
        String fileContent;
        try {
            fileContent = Files.readString(path);
        } catch (Exception e) {
            throw new UmkaException(e);
        }
        addModule(fileName, fileContent);
    }

    /**
     * Returns the umka compiled assembly of the previously compiled
     * source code.
     */
    String getAsm();

    /**
     * @return The version of the umka library.
     */
    String getVersion();

    /**
     * Gets the size of umkas heap
     */
    long getMemoryUsage();


    void addGlobal(String name, Object value);

    /**
     * Runs the umka code.
     * This expects init and compile to have been called beforehand.
     * @throws UmkaException of any runtime error thrown by umka
     */
    void run() throws UmkaException;

    /**
     * Compiles the umka source code and modules supplied previously either from init or addModule
     * @throws UmkaException of any compilation error
     */
    void compile() throws UmkaException;

    /**
     * @return The last compilation/runtime error thrown by umka, or null if there was no error.
     */
    @Nullable UmkaError getLastError();

    /**
     * Initializes the base umka state with a module
     * @param fileName The file name, this does must be unique and doesnt actually have to be a specific file in the filesystem.
     * @param fileContent The umka source code, or NULL if umka should read the src from the fileName above
     * @param stackSize the default umka VM stack size
     * @param args the program main arguments.
     * @param warningCallback A warning callback that gets triggered whenever a runtime warning occurs
     * @param features features, a bitflag fetched from {@link Features}
     * @throws UmkaException if the initialization failed
     */
    void init(
            String fileName,
            String fileContent,
            int stackSize,
            String[] args,
            @Nullable UmkaWarningCallback warningCallback,
            int features
    ) throws UmkaException;

    /**
     * Gets the underlying FFI arena used to bridge to umka.
     */
    Arena getArena();

    /**
     * @return The pointer to the umka instance.
     */
    MemorySegment getUmkaAddress();

    /**
     * Adds an external function to the Umka runtime.
     * @param name The name of the function to add.
     * @param func The function implementation.
     * @return true if successful, false otherwise.
     */
    boolean addFunc(String name, UmkaExternFunc.Function func);

    /**
     * Retrieves an Umka function context by name.
     * @param moduleName The name of the module (can be null for global).
     * @param funcName The name of the function.
     * @param fn The UmkaFuncContext to populate.
     * @return true if found, false otherwise.
     */
    boolean getFunc(@Nullable String moduleName, String funcName, MemorySegment fn);

    /**
     * Calls an Umka function.
     * @param fn The function context (populated by getFunc).
     * @return The result code (0 for success).
     */
    int call(MemorySegment fn);

    /**
     * Creates a string in Umka's managed memory.
     * @param str The Java string to convert.
     * @return A pointer to the Umka string.
     */
    MemorySegment makeStr(String str);

    /**
     * Finds function parameter slot.
     * @param params Parameter stack slots
     * @param index Parameter position. The leftmost parameter is at position 0
     * @return Pointer to the first stack slot occupied by the parameter, NULL if there is no such parameter.
     */
    MemorySegment getParam(MemorySegment params, int index);

    /**
     * Finds the returned value slot.
     * @param params Parameter stack slots
     * @param result Returned value stack slots
     * @return Pointer to the stack slot allocated for storing the returned value.
     */
    MemorySegment getResult(MemorySegment params, MemorySegment result);

    /**
     * Returns function parameter type.
     * @param params Parameter stack slots
     * @param index Parameter position.
     * @return Parameter type pointer, or NULL.
     */
    MemorySegment getParamType(MemorySegment params, int index);

    /**
     * Returns function result type.
     * @param params Parameter stack slots
     * @param result Returned value stack slots
     * @return Function result type pointer.
     */
    MemorySegment getResultType(MemorySegment params, MemorySegment result);

    /**
     * Creates a structure or array in heap memory.
     * @param type Structure or array type (MemorySegment pointer to UmkaType)
     * @return Pointer to the created structure or array.
     */
    MemorySegment makeStruct(MemorySegment type);

    /**
     * Creates a dynamic array.
     * @param array Pointer to the dynamic array struct
     * @param type Dynamic array type
     * @param len Dynamic array length
     */
    void makeDynArray(MemorySegment array, MemorySegment type, int len);

    /**
     * Returns the length of a dynamic array.
     * @param array Pointer to the dynamic array
     * @return Dynamic array length
     */
    int getDynArrayLen(MemorySegment array);
    
    /**
     * Creates a string from a C string.
     * @param cStr C string pointer (MemorySegment)
     * @return Umka string pointer (MemorySegment)
     */
    MemorySegment makeStr(MemorySegment cStr);

    /**
     * Allocates data in Umka's garbage-collected memory.
     * @param size The size in bytes.
     * @param onFree An optional callback to run when the memory is freed (can be null).
     * @return A pointer to the allocated memory.
     */
    MemorySegment allocData(int size, @Nullable UmkaExternFunc.Function onFree);


    /**
     * Increments the reference count of an Umka object.
     * @param ptr The pointer to the object.
     */
    void incRef(MemorySegment ptr);

    /**
     * Decrements the reference count of an Umka object.
     * @param ptr The pointer to the object.
     */
    void decRef(MemorySegment ptr);

    /**
     * Gets the call stack depth and info.
     * @param depth The depth to query.
     * @param nameSize The buffer size for names.
     * @param offset Output for offset.
     * @param fileName Output buffer for file name.
     * @param funcName Output buffer for function name.
     * @param line Output for line number.
     * @return true if successful.
     */
    boolean getCallStack(int depth, int nameSize, MemorySegment offset, MemorySegment fileName, MemorySegment funcName, MemorySegment line);

    /**
     * Closes this umka instance, freeing the underlying memory and the ffi arena.
     */
    @Override
    void close();
}
