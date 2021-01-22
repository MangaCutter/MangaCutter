package net.macu.settings;


import net.macu.UI.ViewManager;

import java.io.IOException;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {

    public static final IntSetting ManualCutterFrame_ScrollSpeed = new IntSetting("UI.ManualCutterFrame.additional_scroll_speed");
    public static final BooleanSetting ManualCutterFrame_ScrollInversion = new BooleanSetting("UI.ManualCutterFrame.scroll_inversion");
    public static final StringSetting CertificateAuthority_RootCA = new StringSetting("browser.proxy.cert.CertificateAuthority.root_ca");
    public static final StringSetting IOManager_UserAgent = new StringSetting("core.IOManager.user_agent");
    public static final IntSetting ImageColorStream_BufferHeight = new IntSetting("cutter.pasta.ImageColorStream.buffer_height");
    public static final IntSetting PastaCutter_MinHeight = new IntSetting("cutter.pasta.PastaCutter.min_height");
    public static final IntSetting PastaCutter_BordersWidth = new IntSetting("cutter.pasta.PastaCutter.borders_width");
    public static final StringSetting L_Lang = new StringSetting("settings.L.LANG");
    public static final IntSetting Settings_MasterScrollSpeed = new IntSetting("settings.Settings.master_scroll_speed");
    private static final ArrayList<Setting> allSettings = new ArrayList<>();
    private static Preferences preferences;
    private static Properties defaults;

    public static void collectParameters() {
        if (preferences != null) return;
        preferences = Preferences.userNodeForPackage(Settings.class);
        defaults = new Properties();
        try {
            defaults.load(Settings.class.getResourceAsStream("defaultSettings.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            ViewManager.showMessageDialog(
                    L.get("settings.Settings.collectParameters.get_defaults_exception", e.toString()), null);
        }
        defaults.stringPropertyNames().forEach(s -> {
            try {
                if (!Arrays.asList(preferences.keys()).contains(s)) {
                    preferences.put(s, defaults.getProperty(s));
                }
            } catch (BackingStoreException e) {
                e.printStackTrace();
                ViewManager.showMessageDialog(
                        L.get("settings.Settings.collectParameters.put_defaults_exception", e.toString()), null);
            }
        });
        allSettings.add(ManualCutterFrame_ScrollSpeed);
        allSettings.add(ManualCutterFrame_ScrollInversion);
        allSettings.add(CertificateAuthority_RootCA);
        allSettings.add(IOManager_UserAgent);
        allSettings.add(ImageColorStream_BufferHeight);
        allSettings.add(PastaCutter_MinHeight);
        allSettings.add(PastaCutter_BordersWidth);
        allSettings.add(L_Lang);
        allSettings.add(Settings_MasterScrollSpeed);
        allSettings.sort(Comparator.comparing(Setting::getName));
        allSettings.forEach(setting -> {
            if (setting instanceof StringSetting)
                setting.setValue(preferences.get(setting.getName(), ""));
            else if (setting instanceof IntSetting)
                setting.setValue(preferences.getInt(setting.getName(), Integer.MAX_VALUE));
            else if (setting instanceof BooleanSetting)
                setting.setValue(preferences.getBoolean(setting.getName(), false));
        });
    }

    public static List<Setting> getAllSettings() {
        return allSettings;
    }
}
