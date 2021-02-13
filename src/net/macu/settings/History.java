package net.macu.settings;

import net.macu.UI.IconifiedFileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.prefs.BackingStoreException;

public class History {
    private static final String FRAME_POS_X_PERFIX = "history_frame_pos_x_";
    private static final String FRAME_POS_Y_PERFIX = "history_frame_pos_y_";
    private static final String FRAME_WIDTH_PERFIX = "history_frame_width_";
    private static final String FRAME_HEIGHT_PERFIX = "history_frame_height_";
    private static final String FILE_CHOOSER_SELECTED_FILE_PREFIX = "history_file_chooser_selected_file_";

    public static JFrame createJFrameFromHistory(String id, int defaultWidth, int defaultHeight) {
        Rectangle defaultBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        int width = Settings.preferences.getInt(FRAME_WIDTH_PERFIX + id, defaultWidth);
        int height = Settings.preferences.getInt(FRAME_HEIGHT_PERFIX + id, defaultHeight);
        int posX = Settings.preferences.getInt(FRAME_POS_X_PERFIX + id, (defaultBounds.x + defaultBounds.width - width) / 2);
        int posY = Settings.preferences.getInt(FRAME_POS_Y_PERFIX + id, (defaultBounds.y + defaultBounds.height - height) / 2);
        JFrame frame = new JFrame();
        frame.setSize(width, height);
        frame.setLocation(posX, posY);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                new Thread(() -> {
                    try {
                        Settings.preferences.flush();
                    } catch (BackingStoreException backingStoreException) {
                        backingStoreException.printStackTrace();
                    }
                }).start();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                new Thread(() -> {
                    Settings.preferences.putInt(FRAME_WIDTH_PERFIX + id, frame.getWidth());
                    Settings.preferences.putInt(FRAME_HEIGHT_PERFIX + id, frame.getHeight());
                }).start();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                new Thread(() -> {
                    Settings.preferences.putInt(FRAME_POS_X_PERFIX + id, frame.getX());
                    Settings.preferences.putInt(FRAME_POS_Y_PERFIX + id, frame.getY());
                }).start();
            }
        });
        return frame;
    }

    public static IconifiedFileChooser createIconifiedFileChooserFromHistory(String id) {
        IconifiedFileChooser fileChooser = new IconifiedFileChooser(null);
        fileChooser.addActionListener(e -> new Thread(() -> {
            if (fileChooser.getSelectedFile() != null && !fileChooser.getSelectedFile().getAbsolutePath().isEmpty()) {
                Settings.preferences.put(FILE_CHOOSER_SELECTED_FILE_PREFIX + id,
                        fileChooser.getSelectedFile().getAbsolutePath());
                try {
                    Settings.preferences.flush();
                } catch (BackingStoreException backingStoreException) {
                    backingStoreException.printStackTrace();
                }
            }
        }).start());
        if (Settings.preferences.get(FILE_CHOOSER_SELECTED_FILE_PREFIX + id, null) != null) {
            File selected = new File(Settings.preferences.get(FILE_CHOOSER_SELECTED_FILE_PREFIX + id, ""));
            if (selected.isFile()) selected = selected.getParentFile();
            fileChooser.setCurrentDirectory(selected);
        }
        return fileChooser;
    }
}
