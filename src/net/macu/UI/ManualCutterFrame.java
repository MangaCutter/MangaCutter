package net.macu.UI;

import net.macu.cutter.pasta.Frame;
import net.macu.settings.L;
import net.macu.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ManualCutterFrame extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private static final Color CUTTER_BOX_COLOR = Color.MAGENTA;
    private static final Color SLIDER_BAR_COLOR = Color.LIGHT_GRAY;
    private static final Color SLIDER_COLOR = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(), Color.DARK_GRAY.getBlue(), 190);
    private static final Color BUTTON_PANEL_COLOR = Color.WHITE;
    private static final Color BUTTON_PANEL_STROKE_COLOR = Color.DARK_GRAY;
    private static final Color BUTTON_PRESSED_COLOR = Color.DARK_GRAY;
    private static final Color BUTTON_RELEASED_COLOR = Color.LIGHT_GRAY;
    private static final Color BUTTON_STROKE_COLOR = Color.BLACK;
    private static final Color ADD_BUTTON_PIC_COLOR = Color.GREEN;
    private static final Color REMOVE_BUTTON_PIC_COLOR = Color.RED;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color LEADER_COLOR = Color.GRAY;
    private static final int SLIDER_WIDTH = 8;
    private static final int LEADER_HEIGHT = 40;
    private static final int LEADER_WIDTH = 3;
    private static final int CUTTER_BOX_HEIGHT = 16;
    private static final int BUTTON_PANEL_HEIGHT = 40;
    private static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, CUTTER_BOX_HEIGHT);
    private static final int ZOOM_IN_SELECTED = 1;
    private static final int ZOOM_OUT_SELECTED = 2;
    private static final int ADD_SELECTED = 4;
    private static final int REMOVE_SELECTED = 8;
    private static final int CONFIRM_SELECTED = 16;
    private static final Stroke nickStroke = new BasicStroke(3);
    private static final Stroke buttonStroke = new BasicStroke(2);
    private static final Stroke cutLineStroke = new BasicStroke(1);
    private static final Stroke arrowStroke = new BasicStroke(2);
    private static final Stroke stringStroke = new BasicStroke(4);
    private final Canvas c;
    private final ArrayList<Integer> cutLines = new ArrayList<>();
    private final int viewportVerticalOffset = BUTTON_PANEL_HEIGHT;
    private final Object locker = new Object();
    private final BufferedImage[] fragments;
    private BufferedImage buffer;
    private Graphics2D g;
    private boolean ctrlPressed = false;
    private boolean altPressed = false;
    private int srcWidth = Integer.MIN_VALUE;
    private int srcHeight = 0;
    private int imageViewportStart = 0;
    private int previewWidth;
    private int sliderStart;
    private int sliderEnd;
    private int sliderDragPoint;
    private int druggingBoxIndex;
    private int druggingBoxStart;
    private int cutterColumnWidth;
    private int cutterBoxWidth;
    private int viewportWidth;
    private int viewportHeight;
    private int buttonRadius;
    private int pressedButton = ADD_SELECTED;
    private int buttonCount;
    private int distanceBetweenCenters;
    private int finishButtonWidth;
    private int srcPos;
    private boolean hasMarkedForRemove = false;
    private Drag dragging = Drag.NOTHING;
    private BufferedImage[] result;

    public ManualCutterFrame(BufferedImage[] images) {
        super(L.get("UI.ManualCutterFrame.frame_title"));
        fragments = images;
        for (BufferedImage fragment : fragments) {
            srcWidth = Math.max(srcWidth, fragment.getWidth());
            srcHeight += fragment.getHeight();
        }
        cutLines.add(0);
        cutLines.add(srcHeight);
        previewWidth = srcWidth / 2;
        c = new Canvas() {
            public void paint(Graphics g2) {
                g.setColor(BACKGROUND_COLOR);
                g.clearRect(0, 0, c.getWidth(), c.getHeight());

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g.setFont(FONT);
                Rectangle labelBounds = g.getFontMetrics().getStringBounds(String.valueOf(srcHeight), g).getBounds();
                cutterBoxWidth = labelBounds.width;
                viewportWidth = getWidth() - SLIDER_WIDTH;
                viewportHeight = c.getHeight() - viewportVerticalOffset;
                cutterColumnWidth = (viewportWidth - previewWidth) / 2;
                float heightToPreviewScale = ((float) previewWidth) / srcWidth;

                net.macu.cutter.pasta.Frame frame = new net.macu.cutter.pasta.Frame(fragments, toSRCCoordinates(imageViewportStart), toSRCCoordinates(imageViewportStart + viewportHeight));
                frame.drawOnImage(buffer, cutterColumnWidth, viewportVerticalOffset, previewWidth, viewportHeight, heightToPreviewScale);

                g.setColor(LEADER_COLOR);
                for (int i = 0; i < viewportHeight; i += LEADER_HEIGHT * 2) {
                    g.fillRect(cutterColumnWidth - LEADER_WIDTH, i + viewportVerticalOffset,
                            LEADER_WIDTH, LEADER_HEIGHT);
                    g.fillRect(cutterColumnWidth + previewWidth, i + viewportVerticalOffset,
                            LEADER_WIDTH, LEADER_HEIGHT);
                }

                g.setColor(CUTTER_BOX_COLOR);
                for (int i = 0; i < cutLines.size(); i++) {
                    int linePos = (int) ((float) (cutLines.get(i)) * heightToPreviewScale - imageViewportStart + viewportVerticalOffset);
                    if (linePos >= -CUTTER_BOX_HEIGHT / 2 - g.getFontMetrics().getDescent() - CUTTER_BOX_HEIGHT + viewportVerticalOffset &&
                            linePos <= viewportHeight + CUTTER_BOX_HEIGHT / 2 + g.getFontMetrics().getHeight() + 4 + CUTTER_BOX_HEIGHT) {
                        boolean fill = true;
                        if ((pressedButton & REMOVE_SELECTED) != 0) {
                            try {
                                int posY = MouseInfo.getPointerInfo().getLocation().y - c.getLocationOnScreen().y;
                                int posX = MouseInfo.getPointerInfo().getLocation().x - c.getLocationOnScreen().x;
                                if ((cutterColumnWidth - LEADER_WIDTH - cutterBoxWidth <= posX && posX <= cutterColumnWidth - LEADER_WIDTH) ||
                                        (viewportWidth - cutterColumnWidth + LEADER_WIDTH <= posX && posX <= viewportWidth - cutterColumnWidth + LEADER_WIDTH + cutterBoxWidth)) {
                                    if (linePos - CUTTER_BOX_HEIGHT / 2 <= posY && posY <= linePos + CUTTER_BOX_HEIGHT / 2) {
                                        fill = false;
                                    }
                                }
                            } catch (IllegalComponentStateException ex) {
                                ex.printStackTrace();
                            }
                        }
                        drawCutLine(linePos, (i != 0) ? String.valueOf(cutLines.get(i) - cutLines.get(i - 1)) : "",
                                (i != cutLines.size() - 1) ? String.valueOf(cutLines.get(i + 1) - cutLines.get(i)) : "", fill);
                    }
                }
                if (dragging == Drag.NOTHING) {
                    try {
                        int posY = MouseInfo.getPointerInfo().getLocation().y - c.getLocationOnScreen().y - viewportVerticalOffset;
                        if ((pressedButton & ADD_SELECTED) != 0 && posY >= 0) {
                            int onImagePos = toSRCCoordinates(imageViewportStart + posY);
                            int prevIndex = 0;
                            for (int i = 0; i < cutLines.size(); i++) {
                                if (cutLines.get(i) < onImagePos) {
                                    prevIndex = i;
                                }
                            }
                            if (toViewportCoordinates(onImagePos - cutLines.get(prevIndex)) >= 5 * CUTTER_BOX_HEIGHT &&
                                    toViewportCoordinates(cutLines.get(prevIndex + 1) - onImagePos) >= 5 * CUTTER_BOX_HEIGHT)
                                drawCutLine(posY + viewportVerticalOffset, String.valueOf(onImagePos - cutLines.get(prevIndex)),
                                        String.valueOf(cutLines.get(prevIndex + 1) - onImagePos), false);
                        }
                    } catch (IllegalComponentStateException ex) {
                        ex.printStackTrace();
                    }
                }

                g.setColor(SLIDER_BAR_COLOR);
                g.fillRect(viewportWidth, viewportVerticalOffset, c.getWidth(), viewportHeight);

                g.setColor(CUTTER_BOX_COLOR);
                Stroke oldStroke = g.getStroke();
                g.setStroke(nickStroke);
                cutLines.forEach(pos ->
                        g.drawLine(viewportWidth, (int) (((float) pos) / srcHeight * viewportHeight) + viewportVerticalOffset,
                                getWidth(), (int) (((float) pos) / srcHeight * viewportHeight) + viewportVerticalOffset));

                g.setStroke(oldStroke);
                g.setColor(SLIDER_COLOR);
                sliderStart = (int) (imageViewportStart * viewportHeight / (srcHeight * heightToPreviewScale)) + viewportVerticalOffset;
                sliderEnd = sliderStart + (int) (viewportHeight * viewportHeight / (srcHeight * heightToPreviewScale));
                if (sliderEnd - sliderStart < 10) {
                    sliderEnd = sliderStart + 10;
                }
                g.fillRect(viewportWidth, sliderStart, SLIDER_WIDTH, sliderEnd - sliderStart + 1);

                g.setColor(BUTTON_PANEL_COLOR);
                g.fillRect(0, 0, c.getWidth(), BUTTON_PANEL_HEIGHT);

                g.setStroke(buttonStroke);
                g.setColor(BUTTON_PANEL_STROKE_COLOR);
                g.drawLine(0, BUTTON_PANEL_HEIGHT, c.getWidth(), BUTTON_PANEL_HEIGHT);

                buttonRadius = (int) (0.4 * BUTTON_PANEL_HEIGHT);
                buttonCount = 5;
                Rectangle buttonBounds = g.getFontMetrics().getStringBounds(L.get("UI.ManualCutterFrame.confirm_text"), g).getBounds();
                distanceBetweenCenters = (c.getWidth() - buttonBounds.width) / (buttonCount + 1);
                int picSize = buttonRadius;
                for (int i = 0; i < buttonCount - 1; i++) {
                    int centerX = distanceBetweenCenters * (i + 1);
                    int centerY = BUTTON_PANEL_HEIGHT / 2;
                    if ((pressedButton & (1 << i)) != 0)
                        g.setColor(BUTTON_PRESSED_COLOR);
                    else
                        g.setColor(BUTTON_RELEASED_COLOR);
                    g.fillRoundRect(centerX - buttonRadius, centerY - buttonRadius,
                            2 * buttonRadius, 2 * buttonRadius, buttonRadius, buttonRadius);
                    g.setColor(BUTTON_STROKE_COLOR);
                    g.drawRoundRect(centerX - buttonRadius, centerY - buttonRadius,
                            2 * buttonRadius, 2 * buttonRadius, buttonRadius, buttonRadius);
                    switch (i) {
                        case 0:
                            g.drawLine(centerX - picSize / 10, centerY + picSize / 10, centerX - picSize / 2, centerY + picSize / 2);
                            int radiusOnMinusPic = (int) (picSize * 0.35 * Math.sqrt(2));
                            g.drawOval(centerX + picSize / 4 - radiusOnMinusPic, centerY - picSize / 4 - radiusOnMinusPic, radiusOnMinusPic * 2, radiusOnMinusPic * 2);
                            g.drawLine(centerX + picSize / 4 - picSize / 8, centerY - picSize / 4,
                                    centerX + picSize / 4 + picSize / 8, centerY - picSize / 4);
                            g.drawLine(centerX + picSize / 4, centerY - picSize / 4 - picSize / 8,
                                    centerX + picSize / 4, centerY - picSize / 4 + picSize / 8);
                            break;
                        case 1:
                            g.drawLine(centerX - picSize / 10, centerY + picSize / 10, centerX - picSize / 2, centerY + picSize / 2);
                            int radiusOnPlusPic = (int) (picSize * 0.35 * Math.sqrt(2));
                            g.drawOval(centerX + picSize / 4 - radiusOnPlusPic, centerY - picSize / 4 - radiusOnPlusPic, radiusOnPlusPic * 2, radiusOnPlusPic * 2);
                            g.drawLine(centerX + picSize / 4 - picSize / 8, centerY - picSize / 4,
                                    centerX + picSize / 4 + picSize / 8, centerY - picSize / 4);
                            break;
                        case 2:
                            g.setColor(ADD_BUTTON_PIC_COLOR);
                            g.drawLine(centerX - picSize / 2, centerY, centerX + picSize / 2, centerY);
                            g.drawLine(centerX, centerY - picSize / 2, centerX, centerY + picSize / 2);
                            break;
                        case 3:
                            g.setColor(REMOVE_BUTTON_PIC_COLOR);
                            g.drawLine(centerX - picSize / 2, centerY, centerX + picSize / 2, centerY);
                            break;
                    }
                }
                if ((pressedButton & CONFIRM_SELECTED) != 0)
                    g.setColor(BUTTON_PRESSED_COLOR);
                else
                    g.setColor(BUTTON_RELEASED_COLOR);
                finishButtonWidth = buttonBounds.width + buttonRadius;
                g.fillRoundRect(distanceBetweenCenters * buttonCount - buttonRadius, BUTTON_PANEL_HEIGHT / 2 - buttonRadius, finishButtonWidth, buttonRadius * 2, buttonRadius, buttonRadius);
                g.setColor(BUTTON_STROKE_COLOR);
                g.drawRoundRect(distanceBetweenCenters * buttonCount - buttonRadius, BUTTON_PANEL_HEIGHT / 2 - buttonRadius, finishButtonWidth, buttonRadius * 2, buttonRadius, buttonRadius);
                g.drawString(L.get("UI.ManualCutterFrame.confirm_text"), distanceBetweenCenters * buttonCount - buttonRadius / 2, BUTTON_PANEL_HEIGHT / 2 + buttonRadius / 2);

                g2.drawImage(buffer, 0, 0, null);

            }

            @Override
            public void update(Graphics g2) {
                paint(g2);
            }
        };
        c.setBackground(Color.black);
        c.addMouseListener(this);
        c.addMouseMotionListener(this);
        c.addMouseWheelListener(this);
        c.addKeyListener(this);
        c.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setViewportStart(toViewportCoordinates(srcPos));
                int bufferWidth = e.getComponent().getWidth();
                if (bufferWidth <= 0) bufferWidth = 1;
                int bufferHeight = e.getComponent().getHeight();
                if (bufferHeight <= 0) bufferHeight = 1;
                buffer = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
                g = buffer.createGraphics();
            }
        });
        add(c);
        buffer = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        g = buffer.createGraphics();
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(IconManager.getBrandIcon());
        ManualCutterFrame mcf = this;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    if (ViewManager.showConfirmDialog(L.get("UI.ManualCutterFrame.cancel"), mcf)) {
                        result = null;
                        dispose();
                        synchronized (locker) {
                            locker.notifyAll();
                        }
                    }
                }).start();
            }
        });
        Thread scrollThread = new Thread(() -> {
            while (isVisible()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isVisible()) {
                    if (dragging == Drag.CUT_BOX) {
                        try {
                            int y = MouseInfo.getPointerInfo().getLocation().y - c.getLocationOnScreen().y - viewportVerticalOffset;
                            if (y < 0) {
                                scroll((Settings.ViewManager_MasterScrollSpeed.getValue() + Settings.ManualCutterFrame_ScrollSpeed.getValue()) * (Settings.ManualCutterFrame_ScrollInversion.getValue() ? 1 : -1));
                            } else if (y >= viewportHeight - 1) {
                                scroll((Settings.ViewManager_MasterScrollSpeed.getValue() + Settings.ManualCutterFrame_ScrollSpeed.getValue()) * (Settings.ManualCutterFrame_ScrollInversion.getValue() ? -1 : 1));
                            }
                            updateDraggedBoxPos();
                            c.repaint();
                        } catch (IllegalComponentStateException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        scrollThread.setDaemon(true);
        scrollThread.start();
        setVisible(true);
    }

    public Object getLocker() {
        return locker;
    }

    public BufferedImage[] waitForResults() {
        synchronized (getLocker()) {
            try {
                getLocker().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void cancel() {
        dispose();
        result = null;
        synchronized (locker) {
            locker.notifyAll();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        new Thread(() -> {
            ctrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
            altPressed = (e.getModifiers() & KeyEvent.ALT_MASK) != 0;
            if (e.getKeyCode() == KeyEvent.VK_SUBTRACT && (ctrlPressed || altPressed)) {
                zoom(-2);
                c.repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_ADD && (ctrlPressed || altPressed)) {
                zoom(2);
                c.repaint();
            }
        }).start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        new Thread(() -> {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_ALT) {
                altPressed = false;
            }
        }).start();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        new Thread(() -> {
            if (dragging == Drag.NOTHING) {
                if ((pressedButton & ADD_SELECTED) != 0 && MouseInfo.getPointerInfo().getLocation().getY() >= viewportVerticalOffset) {
                    c.repaint();
                }
                try {
                    int posY = MouseInfo.getPointerInfo().getLocation().y - c.getLocationOnScreen().y - viewportVerticalOffset;
                    int posX = MouseInfo.getPointerInfo().getLocation().x - c.getLocationOnScreen().x;
                    if ((cutterColumnWidth - LEADER_WIDTH - cutterBoxWidth <= posX && posX <= cutterColumnWidth - LEADER_WIDTH) ||
                            (viewportWidth - cutterColumnWidth + LEADER_WIDTH <= posX && posX <= viewportWidth - cutterColumnWidth + LEADER_WIDTH + cutterBoxWidth)) {
                        int onImagePos = toSRCCoordinates(imageViewportStart + posY);
                        for (int i = 1; i < cutLines.size() - 1; i++) {
                            if (onImagePos - toSRCCoordinates(CUTTER_BOX_HEIGHT / 2) <= cutLines.get(i) &&
                                    cutLines.get(i) <= onImagePos + toSRCCoordinates(CUTTER_BOX_HEIGHT / 2)) {
                                if ((pressedButton & ADD_SELECTED) != 0 && posY >= 0) {
                                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                                } else {
                                    hasMarkedForRemove = true;
                                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                                    c.repaint();
                                }
                                return;
                            }
                        }
                    }
                } catch (IllegalComponentStateException ex) {
                    ex.printStackTrace();
                }
                if (hasMarkedForRemove) {
                    hasMarkedForRemove = false;
                    c.repaint();
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }).start();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        new Thread(() -> {
            switch (dragging) {
                case SLIDER:
                    setViewportStart((int) ((float) (e.getY() - viewportVerticalOffset - sliderDragPoint) / viewportHeight * srcHeight * ((float) previewWidth) / srcWidth));
                    c.repaint();
                    break;
                case CUT_BOX:
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    updateDraggedBoxPos();
                    c.repaint();
                    break;
            }
        }).start();
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        new Thread(() -> {
            dragging = Drag.NOTHING;
            if (e.getY() <= BUTTON_PANEL_HEIGHT / 2 + buttonRadius && e.getY() >= BUTTON_PANEL_HEIGHT / 2 - buttonRadius) {
                for (int i = 0; i < buttonCount - 1; i++) {
                    if (e.getX() <= distanceBetweenCenters * (i + 1) + buttonRadius && e.getX() >= distanceBetweenCenters * (i + 1) - buttonRadius) {
                        if ((pressedButton & (1 << i)) != 0) {
                            switch (i) {
                                case 0:
                                    zoom(2);
                                    pressedButton &= (~ZOOM_IN_SELECTED);
                                    break;
                                case 1:
                                    zoom(-2);
                                    pressedButton &= (~ZOOM_OUT_SELECTED);
                                    break;
                                case 2:
                                    pressedButton &= (~REMOVE_SELECTED);
                                    break;
                                case 3:
                                    pressedButton &= (~ADD_SELECTED);
                                    break;
                            }
                        }
                        break;
                    }
                }
                if (e.getX() <= distanceBetweenCenters * buttonCount - buttonRadius + finishButtonWidth && e.getX() >= distanceBetweenCenters * buttonCount - buttonRadius) {
                    if ((pressedButton & CONFIRM_SELECTED) != 0) {
                        confirm();
                        pressedButton &= (~CONFIRM_SELECTED);
                    }
                }
                pressedButton &= (~ZOOM_OUT_SELECTED) & (~ZOOM_IN_SELECTED) & (~CONFIRM_SELECTED);
                c.repaint();
            }
            if ((pressedButton & ZOOM_IN_SELECTED) != 0 || (pressedButton & ZOOM_OUT_SELECTED) != 0 || (pressedButton & CONFIRM_SELECTED) != 0) {
                pressedButton &= (~ZOOM_OUT_SELECTED) & (~ZOOM_IN_SELECTED) & (~CONFIRM_SELECTED);
                c.repaint();
            }
        }).start();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        new Thread(() -> {
            if (e.getX() >= c.getWidth() - SLIDER_WIDTH && e.getY() <= sliderEnd && e.getY() >= sliderStart) {
                sliderDragPoint = e.getY() - sliderStart;
                dragging = Drag.SLIDER;
            }
            if ((e.getX() <= cutterColumnWidth - LEADER_WIDTH && e.getX() >= cutterColumnWidth - LEADER_WIDTH - cutterBoxWidth) ||
                    (e.getX() <= viewportWidth - cutterBoxWidth && e.getX() >= cutterColumnWidth + previewWidth + LEADER_WIDTH)) {
                int pos = e.getY() - viewportVerticalOffset + imageViewportStart;
                for (int i = 1; i < cutLines.size() - 1; i++) {
                    int cutLinePos = toViewportCoordinates(cutLines.get(i));
                    if (pos >= cutLinePos - CUTTER_BOX_HEIGHT / 2 && pos <= cutLinePos + CUTTER_BOX_HEIGHT / 2) {
                        if ((pressedButton & REMOVE_SELECTED) != 0) {
                            cutLines.remove(i);
                            c.repaint();
                        } else {
                            druggingBoxIndex = i;
                            druggingBoxStart = pos - cutLinePos;
                            dragging = Drag.CUT_BOX;
                            break;
                        }
                    }
                }
            }
            if (e.getY() <= BUTTON_PANEL_HEIGHT / 2 + buttonRadius && e.getY() >= BUTTON_PANEL_HEIGHT / 2 - buttonRadius) {
                for (int i = 0; i < buttonCount - 1; i++) {
                    if (e.getX() <= distanceBetweenCenters * (i + 1) + buttonRadius && e.getX() >= distanceBetweenCenters * (i + 1) - buttonRadius) {
                        pressedButton |= (1 << i);
                        if (i == 2) {
                            pressedButton &= (~REMOVE_SELECTED);
                        }
                        if (i == 3) {
                            pressedButton &= (~ADD_SELECTED);
                        }
                    }
                }
                if (e.getX() <= distanceBetweenCenters * buttonCount - buttonRadius + finishButtonWidth && e.getX() >= distanceBetweenCenters * buttonCount - buttonRadius) {
                    pressedButton |= CONFIRM_SELECTED;
                }
                c.repaint();
            }
            if (dragging == Drag.NOTHING) {
                try {
                    int posY = MouseInfo.getPointerInfo().getLocation().y - c.getLocationOnScreen().y - viewportVerticalOffset;
                    if ((pressedButton & ADD_SELECTED) != 0 && posY >= 0) {
                        int onImagePos = toSRCCoordinates(imageViewportStart + posY);
                        int prevIndex = 0;
                        for (int i = 0; i < cutLines.size(); i++) {
                            if (cutLines.get(i) < onImagePos) {
                                prevIndex = i;
                            }
                        }
                        if (toViewportCoordinates(onImagePos - cutLines.get(prevIndex)) >= 5 * CUTTER_BOX_HEIGHT &&
                                toViewportCoordinates(cutLines.get(prevIndex + 1) - onImagePos) >= 5 * CUTTER_BOX_HEIGHT) {
                            cutLines.add(onImagePos);
                            cutLines.sort(Integer::compareTo);
                            c.repaint();
                        }
                    }
                } catch (IllegalComponentStateException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        new Thread(() -> {
            if (ctrlPressed || altPressed) {
                zoom(-2 * e.getWheelRotation());
            } else {
                scroll(e.getWheelRotation() * (Settings.ViewManager_MasterScrollSpeed.getValue() + Settings.ManualCutterFrame_ScrollSpeed.getValue()) * (Settings.ManualCutterFrame_ScrollInversion.getValue() ? 1 : -1));
            }
            mouseMoved(e);
            c.repaint();
        }).start();
    }

    private void drawCutLine(int linePos, String topLabel, String bottomLabel, boolean fillButtons) {
        g.setFont(FONT);
        g.setStroke(cutLineStroke);
        g.drawLine(cutterColumnWidth - LEADER_WIDTH, linePos,
                cutterColumnWidth + previewWidth + LEADER_WIDTH, linePos);

        if (fillButtons) {
            g.fillRoundRect(cutterColumnWidth - cutterBoxWidth - LEADER_WIDTH, linePos - CUTTER_BOX_HEIGHT / 2,
                    cutterBoxWidth, CUTTER_BOX_HEIGHT, CUTTER_BOX_HEIGHT / 2, CUTTER_BOX_HEIGHT);
            g.fillRoundRect(cutterColumnWidth + previewWidth + LEADER_WIDTH, linePos - CUTTER_BOX_HEIGHT / 2,
                    cutterBoxWidth, CUTTER_BOX_HEIGHT, CUTTER_BOX_HEIGHT / 2, CUTTER_BOX_HEIGHT);
        }

        g.setColor(Color.PINK);
        g.setStroke(buttonStroke);
        g.drawRoundRect(cutterColumnWidth - cutterBoxWidth - LEADER_WIDTH, linePos - CUTTER_BOX_HEIGHT / 2,
                cutterBoxWidth, CUTTER_BOX_HEIGHT, CUTTER_BOX_HEIGHT / 2, CUTTER_BOX_HEIGHT);
        g.drawRoundRect(cutterColumnWidth + previewWidth + LEADER_WIDTH, linePos - CUTTER_BOX_HEIGHT / 2,
                cutterBoxWidth, CUTTER_BOX_HEIGHT, CUTTER_BOX_HEIGHT / 2, CUTTER_BOX_HEIGHT);

        g.setColor(Color.MAGENTA);
        g.setStroke(stringStroke);
        if (!topLabel.isEmpty()) {
            int x = cutterColumnWidth - cutterBoxWidth - LEADER_WIDTH;
            int y = linePos - CUTTER_BOX_HEIGHT / 2 - g.getFontMetrics().getDescent();

            int offset = (cutterBoxWidth - g.getFontMetrics().stringWidth(topLabel)) / 2;
            g.drawString(topLabel, x + offset, y);

            g.setStroke(arrowStroke);
            x += cutterBoxWidth / 2;
            y -= CUTTER_BOX_HEIGHT;
            g.drawLine(x, y,
                    x, y - CUTTER_BOX_HEIGHT);
            g.drawLine(x - 4, y - CUTTER_BOX_HEIGHT + 4,
                    x, y - CUTTER_BOX_HEIGHT);
            g.drawLine(x + 4, y - CUTTER_BOX_HEIGHT + 4,
                    x, y - CUTTER_BOX_HEIGHT);

            g.setStroke(stringStroke);
            x = cutterColumnWidth + previewWidth + LEADER_WIDTH;
            y = linePos - CUTTER_BOX_HEIGHT / 2 - g.getFontMetrics().getDescent();
            g.drawString(topLabel, x + offset, y);

            g.setStroke(arrowStroke);
            x += cutterBoxWidth / 2;
            y -= CUTTER_BOX_HEIGHT;
            g.drawLine(x, y,
                    x, y - CUTTER_BOX_HEIGHT);
            g.drawLine(x - 4, y - CUTTER_BOX_HEIGHT + 4,
                    x, y - CUTTER_BOX_HEIGHT);
            g.drawLine(x + 4, y - CUTTER_BOX_HEIGHT + 4,
                    x, y - CUTTER_BOX_HEIGHT);
        }
        g.setStroke(stringStroke);
        if (!bottomLabel.isEmpty()) {
            int x = cutterColumnWidth - cutterBoxWidth - LEADER_WIDTH;
            int y = linePos + CUTTER_BOX_HEIGHT / 2 + g.getFontMetrics().getHeight();

            int offset = (cutterBoxWidth - g.getFontMetrics().stringWidth(bottomLabel)) / 2;
            g.drawString(bottomLabel, x + offset, y);

            g.setStroke(arrowStroke);
            x += cutterBoxWidth / 2;
            y += 4;
            g.drawLine(x, y,
                    x, y + CUTTER_BOX_HEIGHT);
            g.drawLine(x - 4, y + CUTTER_BOX_HEIGHT - 4,
                    x, y + CUTTER_BOX_HEIGHT);
            g.drawLine(x + 4, y + CUTTER_BOX_HEIGHT - 4,
                    x, y + CUTTER_BOX_HEIGHT);

            g.setStroke(stringStroke);
            x = cutterColumnWidth + previewWidth + LEADER_WIDTH;
            y = linePos + CUTTER_BOX_HEIGHT / 2 + g.getFontMetrics().getHeight();
            g.drawString(bottomLabel, x + offset, y);

            g.setStroke(arrowStroke);
            x += cutterBoxWidth / 2;
            y += 4;
            g.drawLine(x, y,
                    x, y + CUTTER_BOX_HEIGHT);
            g.drawLine(x - 4, y + CUTTER_BOX_HEIGHT - 4,
                    x, y + CUTTER_BOX_HEIGHT);
            g.drawLine(x + 4, y + CUTTER_BOX_HEIGHT - 4,
                    x, y + CUTTER_BOX_HEIGHT);
        }
    }

    private void setViewportStart(int start) {
        imageViewportStart = start;
        fixImageViewportStart();
    }

    private void fixImageViewportStart() {
        if (imageViewportStart <= 0) imageViewportStart = 0;
        else if (imageViewportStart + viewportHeight > toViewportCoordinates(srcHeight)) {
            imageViewportStart = toViewportCoordinates(srcHeight) - viewportHeight;
        }
        srcPos = toSRCCoordinates(imageViewportStart);
    }

    private int toSRCCoordinates(int viewportY) {
        return (int) (viewportY * ((float) srcWidth) / previewWidth);
    }

    private int toViewportCoordinates(int srcY) {
        return (int) (srcY * ((float) previewWidth / srcWidth));
    }

    private void updateDraggedBoxPos() {
        try {
            int srcPos = toSRCCoordinates(MouseInfo.getPointerInfo().getLocation().y - c.getLocationOnScreen().y - viewportVerticalOffset + imageViewportStart - druggingBoxStart);
            if (srcPos < 0) srcPos = 0;
            else if (srcPos > srcHeight) srcPos = srcHeight;
            if (srcPos > cutLines.get(druggingBoxIndex + 1)) {
                int tmp = druggingBoxIndex;
                for (int i = druggingBoxIndex; i < cutLines.size(); i++) {
                    if (cutLines.get(druggingBoxIndex + 1) < srcPos) druggingBoxIndex++;
                }
                if (tmp != druggingBoxIndex) {
                    cutLines.remove(tmp);
                    cutLines.add(druggingBoxIndex, srcPos);
                }
            }
            if (srcPos < cutLines.get(druggingBoxIndex - 1)) {
                int tmp = druggingBoxIndex;
                for (int i = druggingBoxIndex; i < cutLines.size(); i++) {
                    if (cutLines.get(druggingBoxIndex - 1) > srcPos) druggingBoxIndex--;
                }
                if (tmp != druggingBoxIndex) {
                    cutLines.remove(tmp);
                    cutLines.add(druggingBoxIndex, srcPos);
                }
            }
            cutLines.set(druggingBoxIndex, srcPos);
        } catch (IllegalComponentStateException ex) {
            ex.printStackTrace();
        }
    }

    private void scroll(int units) {
        imageViewportStart += units;
        fixImageViewportStart();
        if (dragging == Drag.CUT_BOX)
            updateDraggedBoxPos();
    }

    private void zoom(int amount) {
        previewWidth += 10 * amount;
        if (previewWidth <= 0) previewWidth = 20;
        if (toSRCCoordinates(imageViewportStart + viewportHeight) > srcHeight) {
            previewWidth = (int) ((float) srcWidth / srcHeight * viewportHeight);
            setViewportStart(0);
        } else {
            setViewportStart(toViewportCoordinates(srcPos));
        }
    }

    private enum Drag {
        SLIDER, CUT_BOX, NOTHING
    }

    private void confirm() {
        if (ViewManager.showConfirmDialog(L.get("UI.ManualCutterFrame.accept"), this)) {
            for (int i = 0; i < cutLines.size() - 1; i++) {
                if (cutLines.get(i) == cutLines.get(i + 1)) {
                    cutLines.remove(i);
                    i--;
                }
            }
            result = new BufferedImage[cutLines.size() - 1];
            for (int i = 0; i < cutLines.size() - 1; i++) {
                result[i] = new Frame(fragments, cutLines.get(i), cutLines.get(i + 1) - ((i == cutLines.size() - 1) ? 0 : 1)).createImage();
            }
            dispose();
            synchronized (locker) {
                locker.notifyAll();
            }
        }
    }
}