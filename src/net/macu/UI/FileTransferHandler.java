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

    public FileTransferHandler(JTextField textField) {
        target = textField;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return Stream.of(support.getDataFlavors()).anyMatch(flavour -> flavour.isFlavorJavaFileListType() || flavour.isFlavorTextType());
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;
        if (Stream.of(support.getDataFlavors()).anyMatch(DataFlavor::isFlavorTextType)) {
            try {
                target.setText((String) support.getTransferable().getTransferData(DataFlavor.stringFlavor));
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
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
            } else
                return false;
        }
    }
}
