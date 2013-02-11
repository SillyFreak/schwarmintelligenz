/**
 * L10n.java
 * 
 * Created on 11.02.2013
 */

package cbcserver;


import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;


/**
 * <p>
 * The class L10n.
 * </p>
 * 
 * @version V0.0 11.02.2013
 * @author SillyFreak
 */
public class L10n {
    private final Properties bundle;
    private final Locale     locale;
    
    public L10n(Locale locale) {
        this.locale = locale;
        bundle = new Properties();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "res/" + locale.getLanguage() + ".properties");
            if(is == null) throw new IOException("no such bundle");
            bundle.load(is);
        } catch(IOException ex) {
            System.err.println("bundle not loaded: " + locale.getLanguage());
        }
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public String get(String key) {
        return bundle.getProperty(key);
    }
    
    public String format(String key, Object... args) {
        return String.format(locale, bundle.getProperty(key, "KEY NOT FOUND"), args);
    }
    
    public static interface Localizable {
        public void setL10n(L10n l10n);
    }
}
