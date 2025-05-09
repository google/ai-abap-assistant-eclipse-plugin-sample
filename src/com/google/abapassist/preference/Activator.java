package com.google.abapassist.preference;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import com.google.abapassist.preference.PreferenceConstants;

public class Activator extends AbstractUIPlugin {
	   // The plug-in ID
    public static final String PLUGIN_ID = "com.google.abapassist"; 

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        setDefaultPreferences();
    }

    private void setDefaultPreferences() {
        IEclipsePreferences defaultPrefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
        defaultPrefs.putBoolean(PreferenceConstants.P_ENABLE_QUICK_ASSIST, true); 
        defaultPrefs.put(PreferenceConstants.P_AI_MODEL, "Gemini Pro"); 
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }
}
