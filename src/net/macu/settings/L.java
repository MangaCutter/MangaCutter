package net.macu.settings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

public class L implements Parametrized {
    private static final Parameter LANG = new Parameter(Parameter.Type.STRING_TYPE, "settings.L.LANG");
    private static final String[] SUPPORTED_LANGUAGES = new String[]{
            "en", "ru"
    };
    private static Properties langBundle;

    public static void loadLanguageData() {
        String lang = LANG.getString();
        langBundle = new Properties();
        if (Arrays.asList(SUPPORTED_LANGUAGES).contains(lang) && !lang.equals("en")) {
            try {
                langBundle.load(new InputStreamReader(L.class.getResourceAsStream("label_" + lang + ".properties"), StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();//todo
            }
        } else {
            try {
                langBundle.load(L.class.getResourceAsStream("label.properties"));
            } catch (IOException e) {
                e.printStackTrace();//todo
            }
        }
    }

    public static String get(String labelName, Object... args) {
        return String.format(langBundle.getProperty(labelName, "not_yet_implemented"), args);
    }

    public static Parameters getParameters() {
        return new Parameters("settings.L", LANG);
    }
}
