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
    private static final String FRAME_POS_X_PREFIX = "history_frame_pos_x_";
    private static final String FRAME_POS_Y_PREFIX = "history_frame_pos_y_";
    private static final String FRAME_WIDTH_PREFIX = "history_frame_width_";
    private static final String FRAME_HEIGHT_PREFIX = "history_frame_height_";
    private static final String FILE_CHOOSER_SELECTED_FILE_PREFIX = "history_file_chooser_selected_file_";
    private static final String USER_ALLOWED_TO_SHOW_PREFIX = "user_allowed_to_show_";
    private static final String SAVED_ANSWER_PREFIX = "saved_answer_";
    private static final String USAGE_COUNT_PREFIX = "usage_count_";

    public static JFrame createJFrameFromHistory(String id, int defaultWidth, int defaultHeight) {
        Rectangle defaultBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        int width = Settings.preferences.getInt(FRAME_WIDTH_PREFIX + id, defaultWidth);
        int height = Settings.preferences.getInt(FRAME_HEIGHT_PREFIX + id, defaultHeight);
        int posX = Settings.preferences.getInt(FRAME_POS_X_PREFIX + id, (defaultBounds.x + defaultBounds.width - width) / 2);
        int posY = Settings.preferences.getInt(FRAME_POS_Y_PREFIX + id, (defaultBounds.y + defaultBounds.height - height) / 2);
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
                    Settings.preferences.putInt(FRAME_WIDTH_PREFIX + id, frame.getWidth());
                    Settings.preferences.putInt(FRAME_HEIGHT_PREFIX + id, frame.getHeight());
                }).start();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                new Thread(() -> {
                    Settings.preferences.putInt(FRAME_POS_X_PREFIX + id, frame.getX());
                    Settings.preferences.putInt(FRAME_POS_Y_PREFIX + id, frame.getY());
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
                        fileChooser.getSelectedFile().getParentFile().getAbsolutePath());
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

    public static boolean userAllowedToShowNotification(String id) {
        return Settings.preferences.getBoolean(USER_ALLOWED_TO_SHOW_PREFIX + id, true);
    }

    public static void disallowToShowNotification(String id) {
        Settings.preferences.putBoolean(USER_ALLOWED_TO_SHOW_PREFIX + id, false);
        try {
            Settings.preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void allowToShowNotification(String id) {
        Settings.preferences.putBoolean(USER_ALLOWED_TO_SHOW_PREFIX + id, true);
        try {
            Settings.preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static boolean getSavedAnswer(String id) {
        return Settings.preferences.getBoolean(SAVED_ANSWER_PREFIX + id, false);
    }

    public static void saveAnswer(String id, boolean answer) {
        Settings.preferences.putBoolean(SAVED_ANSWER_PREFIX + id, answer);
        try {
            Settings.preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void clearHistory() {
        try {
            String[] keys = Settings.preferences.keys();
            for (String key : keys) {
                if (key.startsWith(FRAME_POS_X_PREFIX) ||
                        key.startsWith(FRAME_POS_Y_PREFIX) ||
                        key.startsWith(FRAME_WIDTH_PREFIX) ||
                        key.startsWith(FRAME_HEIGHT_PREFIX) ||
                        key.startsWith(FILE_CHOOSER_SELECTED_FILE_PREFIX) ||
                        key.startsWith(USER_ALLOWED_TO_SHOW_PREFIX) ||
                        key.startsWith(SAVED_ANSWER_PREFIX) ||
                        key.startsWith(USAGE_COUNT_PREFIX)) {
                    Settings.preferences.remove(key);
                }
            }
            Settings.preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void incrementUsage(String key) {
        Settings.preferences.putInt(USAGE_COUNT_PREFIX + key, Settings.preferences.getInt(USAGE_COUNT_PREFIX + key, 0) + 1);
    }

    public static int getUsage(String key) {
        return Settings.preferences.getInt(USAGE_COUNT_PREFIX + key, 0);
    }
}
