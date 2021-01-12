package net.macu.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.macu.browser.plugin.BrowserPlugin;
import net.macu.browser.proxy.CapturedImageProcessor;
import net.macu.browser.proxy.ImageListener;
import net.macu.settings.L;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RequestFrame implements ImageListener {
    //todo add reload counter
    private static final Color SELECTED_PENDING_BACKGROUND = new Color(175, 82, 0);
    private static final Color DESELECTED_PENDING_BACKGROUND = new Color(125, 59, 0);
    private static final Color SELECTED_READY_BACKGROUND = new Color(22, 87, 0);
    private static final Color DESELECTED_READY_BACKGROUND = new Color(0, 69, 0);
    private static final int THUMBNAIL_WIDTH = 50;
    private static final int THUMBNAIL_HEIGHT = 50;
    private static ImageIcon WaitIcon;
    private final JPopupMenu sortModePopup = new JPopupMenu();
    private final ArrayList<Thumbnail> visible = new ArrayList<>();
    private final ArrayList<String> least = new ArrayList<>();
    private final CapturedImageProcessor processor;
    private final JFrame frame;
    private final BrowserPlugin plugin;
    private final String tabId;
    private JButton forceCompleteButton;
    private JButton reloadButton;
    private JButton cancelButton;
    private JScrollPane imagePreviewScrollPane;
    private JProgressBar mainProgressBar;
    private JPanel rootPanel;
    private JTextField filterTextField;
    private JButton selectOnlyVisibleButton;
    private JButton selectAllVisibleButton;
    private JButton deselectAllVisibleButton;
    private JPanel imagesPanel;
    private JButton sortModeButton;
    private JLabel filterLabel;
    private ArrayList<Thumbnail> thumbnails = new ArrayList<>();
    private ArrayList<String> original;
    private String url;
    private boolean downloadingCompleted = false;

    public RequestFrame(ArrayList<String> captureList, CapturedImageProcessor processor, BrowserPlugin plugin, String tabId, String url) {
        WaitIcon = IconManager.getSpinnerIcon(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        this.plugin = plugin;
        this.tabId = tabId;
        this.url = url;
        this.original = captureList;
        $$$setupUI$$$();
        filterLabel.setText(L.get("UI.RequestFrame.filter_label"));
        deselectAllVisibleButton.setText(L.get("UI.RequestFrame.deselect_all_visible_button"));
        selectOnlyVisibleButton.setText(L.get("UI.RequestFrame.select_only_visible_button"));
        selectAllVisibleButton.setText(L.get("UI.RequestFrame.select_all_visible_button"));
        cancelButton.setText(L.get("UI.RequestFrame.cancel_button"));
        reloadButton.setText(L.get("UI.RequestFrame.reload_button"));
        imagePreviewScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        sortModeButton.setIcon(IconManager.getSortIcon());
        for (SortMode mode : SortMode.values()) {
            JMenuItem item = new JMenuItem(L.get("UI.RequestFrame.SortMode." + mode.name()));
            item.addActionListener(e -> sortItems(mode));
            sortModePopup.add(item);
        }
        captureList.forEach((s) -> {
            BufferedImage image = processor.getImage(s);
            Thumbnail t = new Thumbnail(s, image);
            imagesPanel.add(t);
            thumbnails.add(t);
            visible.add(t);
            if (image == null) {
                least.add(s);
            } else {
                updateProgress();
            }
        });
        updateProgress();
        filterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                applyFilter();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                applyFilter();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        deselectAllVisibleButton.addActionListener(e -> visible.forEach(thumbnail -> thumbnail.setSelected(false)));
        selectOnlyVisibleButton.addActionListener(e -> thumbnails.forEach(thumbnail -> thumbnail.setSelected(visible.contains(thumbnail))));
        selectAllVisibleButton.addActionListener(e -> visible.forEach(thumbnail -> thumbnail.setSelected(true)));
        sortModeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                sortModePopup.show(sortModeButton, e.getX(), e.getY());
            }
        });
        cancelButton.addActionListener(e -> cancel());
        reloadButton.addActionListener(e -> plugin.sendReloadMessage(tabId));
        forceCompleteButton.addActionListener(e -> {
            complete();
        });
        frame = new JFrame(L.get("UI.RequestFrame.frame_title", url));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(ViewManager.getFrame());
        frame.setIconImage(IconManager.getBrandIcon());
        frame.setResizable(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });
        frame.setContentPane($$$getRootComponent$$$());
        frame.pack();
        frame.setSize(700, Math.min(Math.max(400, frame.getHeight()), 500));
        this.processor = processor;
        processor.addImageListener(this);
        frame.setVisible(true);
        frame.requestFocus();
    }

    public void reload(ArrayList<String> urls, String tabUrl) {
        thumbnails.clear();
        visible.clear();
        imagesPanel.removeAll();
        original = urls;
        urls.forEach((s) -> {
            BufferedImage image = processor.getImage(s);
            Thumbnail t = new Thumbnail(s, image);
            imagesPanel.add(t);
            thumbnails.add(t);
            if (image == null) {
                least.add(s);
            } else {
                updateProgress();
            }
        });
        updateProgress();
        applyFilter();
        SwingUtilities.updateComponentTreeUI(imagesPanel);
        url = tabUrl;
        frame.setTitle(L.get("UI.RequestFrame.frame_title", url));
    }

    @Override
    public void onImageCaptured(String url, BufferedImage image) {
        Thumbnail t = getThumbnail(url);
        if (t != null) {
            t.setImage(image);
            for (int i = 0; i < least.size(); i++) {
                if (least.get(i).equals(url)) {
                    least.remove(i--);
                    updateProgress();
                }
            }
        }
    }

    public void onCancelRequest() {
        ViewManager.showMessageDialog(L.get("UI.RequestFrame.onCancel.message"));
        if (!downloadingCompleted)
            processor.removeImageListener(this);
        frame.dispose();
    }

    public void onTooManyAttempts() {
        ViewManager.showMessageDialog(L.get("browser.plugin.BrowserPlugin.onMessage.too_many_attempts"));
        onLoadingComplete();
    }

    private void onLoadingComplete() {
        if (!downloadingCompleted) {
            forceCompleteButton.setText(L.get("UI.RequestFrame.complete_button"));
            reloadButton.setEnabled(false);
            plugin.onRequestComplete(tabId);
            processor.removeImageListener(this);
            downloadingCompleted = true;
        }
    }

    private Thumbnail getThumbnail(String url) {
        for (int i = 0; i < thumbnails.size(); i++) {
            if (thumbnails.get(i).url.equals(url)) return thumbnails.get(i);
        }
        return null;
    }

    private int getVisibleThumbnailPos(Thumbnail t) {
        for (int i = 0; i < visible.size(); i++) {
            if (visible.get(i) == t) {
                return i;
            }
        }
        return -1;
    }

    private int getGlobalThumbnailPos(Thumbnail t) {
        for (int i = 0; i < thumbnails.size(); i++) {
            if (thumbnails.get(i) == t) {
                return i;
            }
        }
        return -1;
    }

    private void sortItems(SortMode mode) {
        if (mode == SortMode.ORIGINAL) {
            ArrayList<Thumbnail> tmp = new ArrayList<>();
            for (int i = 0; i < original.size(); i++) {
                tmp.add(getThumbnail(original.get(i)));
            }
            visible.clear();
            thumbnails = tmp;
        } else {
            thumbnails.sort(Comparator.comparing(o -> {
                switch (mode) {
                    case BY_PATH:
                        return URI.create(o.url).getPath();
                    case BY_HOST:
                        return URI.create(o.url).getHost();
                    case BY_FULL_URL:
                        return o.url;
                    case BY_FILENAME:
                        return o.url.substring(o.url.lastIndexOf("/") + 1);
                }
                return "";
            }));
        }
        applyFilter();
    }

    private void updateProgress() {
        int[] states = new int[4];
        thumbnails.forEach(thumbnail -> {
            if (thumbnail.isSelected()) {
                if (thumbnail.ready) states[0]++;
                else states[1]++;
            } else {
                if (thumbnail.ready) states[2]++;
                else states[3]++;
            }
        });
        mainProgressBar.setValue(states[0]);
        mainProgressBar.setMaximum(states[0] + states[1]);
        mainProgressBar.setString(L.get("UI.RequestFrame.progress",
                states[0], states[0] + states[1], states[2] + states[3]));
        if (states[1] == 0) {
            onLoadingComplete();
        } else {
            forceCompleteButton.setText(L.get("UI.RequestFrame.force_complete_button"));
            reloadButton.setEnabled(true);
        }
    }

    private void applyFilter() {
        System.out.println("filter");
        Pattern filter;
        try {
            filter = Pattern.compile(filterTextField.getText());
        } catch (PatternSyntaxException e) {
            filter = Pattern.compile(Pattern.quote(filterTextField.getText()));
        }
        imagesPanel.removeAll();
        visible.clear();
        Pattern finalFilter = filter;
        thumbnails.forEach(thumbnail -> {
            if (finalFilter.matcher(thumbnail.url).find()) {
                visible.add(thumbnail);
                imagesPanel.add(thumbnail);
            }
        });
        SwingUtilities.updateComponentTreeUI(imagesPanel);
    }

    private void cancel() {
        if (!downloadingCompleted) {
            plugin.onRequestCancel(tabId);
            processor.removeImageListener(this);
        }
        frame.dispose();
        downloadingCompleted = true;
    }

    private void complete() {
        if (!downloadingCompleted) onLoadingComplete();
        //todo
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(5, 4, new Insets(4, 4, 4, 4), -1, -1));
        imagePreviewScrollPane = new JScrollPane();
        rootPanel.add(imagePreviewScrollPane, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        imagePreviewScrollPane.setViewportView(panel1);
        panel1.add(imagesPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel2, new GridConstraints(4, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel2.add(cancelButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        forceCompleteButton = new JButton();
        forceCompleteButton.setText("Force complete");
        panel2.add(forceCompleteButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        reloadButton = new JButton();
        reloadButton.setText("Reload");
        panel2.add(reloadButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainProgressBar = new JProgressBar();
        mainProgressBar.setString("0 / 100 + 0 unselected");
        mainProgressBar.setStringPainted(true);
        rootPanel.add(mainProgressBar, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel3, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        filterLabel = new JLabel();
        filterLabel.setText("Filter:");
        panel3.add(filterLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filterTextField = new JTextField();
        panel3.add(filterTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sortModeButton = new JButton();
        sortModeButton.setText("");
        panel3.add(sortModeButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel4, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        selectAllVisibleButton = new JButton();
        selectAllVisibleButton.setText("Select all visible");
        panel4.add(selectAllVisibleButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deselectAllVisibleButton = new JButton();
        deselectAllVisibleButton.setText("Deselect all visible");
        panel4.add(deselectAllVisibleButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        selectOnlyVisibleButton = new JButton();
        selectOnlyVisibleButton.setText("Select only visible");
        panel4.add(selectOnlyVisibleButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    private void createUIComponents() {
        imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));
    }

    private enum SortMode {
        BY_FULL_URL,
        BY_PATH,
        BY_FILENAME,
        BY_HOST,
        ORIGINAL
    }

    private class Thumbnail extends JPanel {
        String url;
        JLabel thumbnail;
        JCheckBox checkBox;
        JPanel content;
        BufferedImage image;
        boolean ready;

        private Thumbnail(String url, BufferedImage capturedImage) {
            this.url = url;
            this.image = capturedImage;
            ready = this.image != null;
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(3, 0, 3, 0));
            content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
            checkBox = new JCheckBox();
            checkBox.addActionListener(e -> onCheckBoxChange());
            checkBox.setSelected(true);
            content.add(checkBox);
            JButton upButton = new JButton(IconManager.getArrowUpIcon());
            upButton.addActionListener(e -> {
                int pos = getVisibleThumbnailPos(this);
                if (pos > 0) {
                    int newPos = getGlobalThumbnailPos(visible.get(pos - 1));
                    thumbnails.remove(getGlobalThumbnailPos(this));
                    thumbnails.add(newPos, this);
                    visible.remove(pos);
                    visible.add(pos - 1, this);
                    imagesPanel.remove(pos);
                    imagesPanel.add(this, pos - 1);
                    SwingUtilities.updateComponentTreeUI(imagesPanel);
                }
            });
            content.add(upButton);
            JButton downButton = new JButton(IconManager.getArrowDownIcon());
            downButton.addActionListener(e -> {
                int pos = getVisibleThumbnailPos(this);
                if (pos < visible.size() - 1) {
                    int newPos = getGlobalThumbnailPos(visible.get(pos + 1));
                    thumbnails.remove(getGlobalThumbnailPos(this));
                    thumbnails.add(newPos, this);
                    visible.remove(pos);
                    visible.add(pos + 1, this);
                    imagesPanel.remove(pos);
                    imagesPanel.add(this, pos + 1);
                    SwingUtilities.updateComponentTreeUI(imagesPanel);
                }
            });
            content.add(downButton);
            thumbnail = new JLabel(url, createThumbnail(capturedImage), JLabel.LEFT);
            thumbnail.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    ViewManager.showPreviewFrame(image, null);
                }
            });
            content.add(thumbnail);
            add(content, BorderLayout.LINE_START);
            updateColor();
        }

        private void onCheckBoxChange() {
            updateColor();
            updateProgress();
        }

        private void setImage(BufferedImage image) {
            this.image = image;
            ready = true;
            updateColor();
            thumbnail.setIcon(createThumbnail(image));
        }

        private void updateColor() {
            if (isSelected()) {
                if (ready) {
                    setBackground(this, SELECTED_READY_BACKGROUND);
                } else {
                    setBackground(this, SELECTED_PENDING_BACKGROUND);
                }
            } else {
                if (ready) {
                    setBackground(this, DESELECTED_READY_BACKGROUND);
                } else {
                    setBackground(this, DESELECTED_PENDING_BACKGROUND);
                }
            }
        }

        private boolean isSelected() {
            return checkBox.isSelected();
        }

        private void setSelected(boolean b) {
            checkBox.setSelected(b);
            onCheckBoxChange();
        }

        private ImageIcon createThumbnail(BufferedImage image) {
            if (image != null) {
                int thumbWidth;
                int thumbHeight;
                if (((float) THUMBNAIL_HEIGHT) / THUMBNAIL_WIDTH < ((float) image.getHeight()) / image.getWidth()) {
                    thumbHeight = THUMBNAIL_HEIGHT;
                    thumbWidth = (int) (((float) image.getWidth()) / image.getHeight() * THUMBNAIL_HEIGHT);
                } else {
                    thumbWidth = THUMBNAIL_WIDTH;
                    thumbHeight = (int) (((float) image.getHeight()) / image.getWidth() * THUMBNAIL_WIDTH);
                }
                BufferedImage thumb = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = thumb.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setColor(new Color(0x000000));
                g.fillRect(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
                g.drawImage(image, (THUMBNAIL_WIDTH - thumbWidth) / 2, (THUMBNAIL_HEIGHT - thumbHeight) / 2, thumbWidth, thumbHeight, null);
                return new ImageIcon(thumb);
            } else {
                return WaitIcon;
            }
        }

        private void setBackground(Container container, Color c) {
            container.setBackground(c);
            synchronized (container.getTreeLock()) {
                for (Component component : container.getComponents()) {
                    if (component instanceof Container) {
                        setBackground((Container) component, c);
                    } else {
                        component.setBackground(c);
                    }
                }
            }
        }
    }
}
