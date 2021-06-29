package net.macu.settings;


import net.macu.UI.ViewManager;
import net.macu.core.JobManager;

import java.io.IOException;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {

    public static final StringSetting CertificateAuthority_RootCA = new StringSetting("browser.proxy.cert.CertificateAuthority.root_ca");
    public static final BooleanSetting ManualCutterFrame_ScrollInversion = new BooleanSetting("UI.ManualCutterFrame.scroll_inversion");
    public static final IntSetting ManualCutterFrame_ScrollSpeed = new IntSetting("UI.ManualCutterFrame.additional_scroll_speed");
    public static final StringSetting IOManager_UserAgent = new StringSetting("core.IOManager.user_agent");
    public static final IntSetting ImageColorStream_BufferHeight = new IntSetting("cutter.pasta.ImageColorStream.buffer_height");
    public static final IntSetting PastaCutter_MinHeight = new IntSetting("cutter.pasta.PastaCutter.min_height");
    public static final IntSetting PastaCutter_BordersWidth = new IntSetting("cutter.pasta.PastaCutter.borders_width");
    public static final ListSetting L_Lang = new ListSetting("settings.L.LANG", L.SUPPORTED_LANGUAGES, true);
    public static final IntSetting ViewManager_MasterScrollSpeed = new IntSetting("UI.ViewManager.master_scroll_speed");
    public static final BooleanSetting ViewManager_MasterScrollInversion = new BooleanSetting("UI.ViewManager.master_scroll_inversion");
    public static final IntSetting Handler_PoolSize = new IntSetting("browser.proxy.Handler.pool_size");
    public static final ListSetting ViewManager_LookAndFeel = new ListSetting("UI.ViewManager.laf", ViewManager.SUPPORTED_THEMES, true);
    public static final StringSetting PsbForm_LayerName = new StringSetting("UI.imgWriter.PsbForm.layer_name");
    public static final StringSetting PsbForm_LevelsLayerName = new StringSetting("UI.imgWriter.PsbForm.levels_layer_name");
    public static final ListSetting JobManager_MethodPriority = new ListSetting("core.JobManager.method_priority", Arrays.stream(JobManager.Method.values()).map(Enum::toString).toArray(String[]::new), true);
    public static final StringSetting Client_InstallationFolder = new StringSetting("browser.Client.installation_folder");
    public static final IntSetting OffscreenBrowser_Timeout = new IntSetting("browser.OffscreenBrowser.timeout");
    //public static final IntSetting ManualCutterFrame_SmartSelectionThreshold = new IntSetting("UI.ManualCutterFrame.smart_selection_threshold");
    private static final ArrayList<Setting> allSettings = new ArrayList<>();
    static Preferences preferences;
    private static Properties defaults;

    public static void loadSettings() {
        if (preferences != null) return;
        preferences = Preferences.userNodeForPackage(Settings.class);
        defaults = new Properties();
        try {
            defaults.load(Settings.class.getResourceAsStream("defaultSettings.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            ViewManager.showMessageDialog("settings.Settings.collectParameters.get_defaults_exception", null, e.toString());
        }
        defaults.stringPropertyNames().forEach(s -> {
            try {
                if (!Arrays.asList(preferences.keys()).contains(s)) {
                    preferences.put(s, defaults.getProperty(s));
                }
            } catch (BackingStoreException e) {
                e.printStackTrace();
                ViewManager.showMessageDialog("settings.Settings.collectParameters.put_defaults_exception", null, e.toString());
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
        allSettings.add(ViewManager_MasterScrollSpeed);
        allSettings.add(ViewManager_MasterScrollInversion);
        allSettings.add(ViewManager_LookAndFeel);
        allSettings.add(Handler_PoolSize);
        allSettings.add(PsbForm_LayerName);
        allSettings.add(PsbForm_LevelsLayerName);
        allSettings.add(JobManager_MethodPriority);
        allSettings.add(Client_InstallationFolder);
        allSettings.add(OffscreenBrowser_Timeout);
        allSettings.sort(Comparator.comparing(Setting::getName));
        allSettings.forEach(setting -> {
            if (setting instanceof StringSetting)
                setting.setValue(preferences.get(setting.getName(), ""));
            else if (setting instanceof IntSetting)
                setting.setValue(preferences.getInt(setting.getName(), Integer.MAX_VALUE));
            else if (setting instanceof BooleanSetting)
                setting.setValue(preferences.getBoolean(setting.getName(), false));
            else if (setting instanceof ListSetting)
                setting.setValue(preferences.get(setting.getName(), ""));
        });
    }

    public static void flush() throws BackingStoreException {
        preferences.flush();
    }

    public static List<Setting> getAllSettings() {
        return allSettings;
    }

    public static void restoreDefaults() throws BackingStoreException {
        preferences.removeNode();
    }
}
