package net.macu.imgWriter.psd;

public class UnsupportedColorSpaceTypeException extends Exception {
    public UnsupportedColorSpaceTypeException(int type) {
        super(type + " type isn't supported");
    }
}
