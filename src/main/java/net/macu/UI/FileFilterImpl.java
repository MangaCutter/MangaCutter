package net.macu.UI;

import net.macu.settings.L;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileFilterImpl extends FileFilter {
    private final String ext;

    public FileFilterImpl(String ext) {
        this.ext = ext.toLowerCase();
    }

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith("." + ext);
    }

    @Override
    public String getDescription() {
        return L.get("UI.FileFilterImpl.file_format_description", ext.toUpperCase());
    }
}
