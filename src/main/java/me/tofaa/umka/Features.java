package me.tofaa.umka;

public final class Features {

    private Features() {}

    public static final int ENABLE_FILE_SYSTEM = 0x01;
    public static final int ENABLE_IMPL_LIBS = 0x04;

    public static boolean hasFeature(int a, int f) {
        return (a & f) != 0;
    }

}
