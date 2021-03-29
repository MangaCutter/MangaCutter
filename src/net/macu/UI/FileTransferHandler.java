package net.macu.UI;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class FileTransferHandler extends TransferHandler {
    private final JTextField target;
    private final TransferHandler oldHandler;

    public FileTransferHandler(JTextField textField) {
        target = textField;
        oldHandler = textField.getTransferHandler();
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return Stream.of(support.getDataFlavors()).anyMatch(DataFlavor::isFlavorJavaFileListType) || oldHandler.canImport(support);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (oldHandler.canImport(support)) return oldHandler.importData(support);
        if (!canImport(support)) return false;
        List<File> files;
        try {
            files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();

            return false;
        }
        if (files.size() == 1 && files.get(0).isDirectory()) {
            target.setText(files.get(0).toURI().toString());
            return true;
        } else return false;
    }
}
