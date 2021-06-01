package net.macu.writer.psd;

public class UnsupportedColorSpaceTypeException extends Exception {
    public UnsupportedColorSpaceTypeException(int type) {
        super(type + " type isn't supported");
    }
}
