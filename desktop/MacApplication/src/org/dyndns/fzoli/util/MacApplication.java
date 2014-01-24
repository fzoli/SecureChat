package org.dyndns.fzoli.util;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Window;
import javax.swing.JMenuBar;

/**
 * The <code>MacApplication</code> class allows you to integrate your Java application with the native OS X environment.
 * You can provide your OS X users a greatly enhanced experience by implementing a few basic handlers for standard system events.
 * @since 1.4
 */
public class MacApplication {
    
    private static MacApplication MAC_APP;

    private final com.apple.eawt.Application APP = com.apple.eawt.Application.getApplication();

    static {
        try {
            MAC_APP = new MacApplication();
        }
        catch (Throwable t) {
            ;
        }
    }

    /**
     * Creates an Application instance.
     * Should only be used in JavaBean environments.
     * @deprecated use {@link MacApplication#getApplication()}
     * @since 1.4
     */
    @Deprecated
    public MacApplication() {
        super();
    }
    
    /**
     * Returns whether using {@link MacApplication#getApplication()} is safe.
     * @return true if the <code>MacApplication</code> class is supported;
     * otherwise false
     */
    public static boolean isSupported() {
        return MAC_APP != null;
    }
    
    /**
     * @return the singleton representing this OS X Application
     * or <code>null</code> if the OS is not Mac
     * @throws UnsupportedOperationException if the class is not supported
     * @see MacApplication#isSupported()
     */
    public static MacApplication getApplication() {
        if (!isSupported()) throw new UnsupportedOperationException();
        return MAC_APP;
    }
    
    /**
     * Changes this application's Dock icon to the provided image.
     * If the application is not running on Mac, it does nothing.
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public static void setDockIcon(Image image) {
        if (isSupported()) MAC_APP.setDockIconImage(image);
    }
    
    /**
     * Enables this application to be suddenly terminated.
     * Call this method to indicate your application's state is saved, and requires no notification to be terminated.
     * Letting your application remain terminatable improves the user experience by avoiding re-paging in your application when it's asked to quit.
     * <b>Note: enabling sudden termination will allow your application to be quit without notifying your QuitHandler, or running any shutdown hooks.</b>
     * User initiated Cmd-Q, logout, restart, or shutdown requests will effectively "kill -KILL" your application.
     * This call has no effect on OS X versions prior to 10.6.
     * @since Java for OS X v10.6 Update 3, Java for OS X v10.5 Update 8
     * @see #disableSuddenTermination()
     */
    public void enableSuddenTermination() {
        APP.enableSuddenTermination();
    }

    /**
     * Prevents this application from being suddenly terminated.
     * Call this method to indicate that your application has unsaved state,
     * and may not be terminated without notification.
     * This call has no effect on OS X versions prior to 10.6.
     * @since Java for OS X v10.6 Update 3, Java for OS X v10.5 Update 8
     * @see #enableSuddenTermination()
     */
    public void disableSuddenTermination() {
        APP.disableSuddenTermination();
    }

    /**
     * Requests this application to move to the foreground.
     * @param allWindows - if all windows of this application should be moved to the foreground, or only the foremost one
     * @since Java for OS X v10.6 Update 1, Java for OS X v10.5 Update 6 - 1.6, 1.5
     */
    public void requestForeground(boolean allWindows) {
        APP.requestForeground(allWindows);
    }

    /**
     * Requests user attention to this application (usually through bouncing the Dock icon).
     * Critical requests will continue to bounce the Dock icon until the app is activated.
     * An already active application requesting attention does nothing.
     * @param critical - if this is an important request
     * @since Java for OS X v10.6 Update 1, Java for OS X v10.5 Update 6 - 1.6, 1.5
     */
    public void requestUserAttention(boolean critical) {
        APP.requestUserAttention(critical);
    }

    /**
     * Opens the native help viewer application if a Help Book has been added to the
     * application bundler and registered in the Info.plist with CFBundleHelpBookFolder.
     * See http://developer.apple.com/qa/qa2001/qa1022.html for more information.
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public void openHelpViewer() {
        APP.openHelpViewer();
    }

    /**
     * Attaches the contents of the provided PopupMenu to the application's Dock icon.
     * @param menu - the PopupMenu to attach to this application's Dock icon
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public void setDockMenu(PopupMenu menu) {
        APP.setDockMenu(menu);
    }

    /**
     * @return the PopupMenu used to add items to this application's Dock icon
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public PopupMenu getDockMenu() {
        return APP.getDockMenu();
    }

    /**
     * Changes this application's Dock icon to the provided image.
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public void setDockIconImage(Image image) {
        APP.setDockIconImage(image);
    }

    /**
     * Obtains an image of this application's Dock icon.
     * @return an image of this application's Dock icon
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public Image getDockIconImage() {
        return APP.getDockIconImage();
    }

    /**
     * Affixes a small system provided badge to this application's Dock icon. Usually a number.
     * @param badge - textual label to affix to the Dock icon
     * @since Java for OS X v10.5 - 1.5, Java for OS X v10.5 Update 1 - 1.6
     */
    public void setDockIconBadge(String badge) {
        APP.setDockIconBadge(badge);
    }

    /**
     * Sets the default menu bar to use when there are no active frames.
     * Only used when the system property "apple.laf.useScreenMenuBar" is "true", and the Aqua Look and Feel is active.
     * @param menuBar - to use when no other frames are active
     * @since Java for OS X v10.6 Update 1, Java for OS X v10.5 Update 6 - 1.6, 1.5
     */
    public void setDefaultMenuBar(JMenuBar menuBar) {
        APP.setDefaultMenuBar(menuBar);
    }

    public void requestToggleFullScreen(Window window) {
        APP.requestToggleFullScreen(window);
    }
    
    /**
     * Enables the Preferences item in the application menu.
     * The ApplicationListener receives a callback for selection of the Preferences item in the application menu only if this is set to true.
     * If a Preferences item isn't present, this method adds and enables it.
     * @param enable - specifies whether the Preferences item in the application menu should be enabled (true) or not (false)
     * @deprecated no replacement
     * @since 1.4
     */
    @Deprecated
    public void setEnabledPreferencesMenu(boolean enable) {
        APP.setEnabledPreferencesMenu(enable);
    }

    /**
     * Enables the About item in the application menu.
     * The ApplicationListener receives a callback for selection of the About item in the application menu only if this is set to true.
     * Because AWT supplies a standard About window when an application may not, by default this is set to true.
     * If the About item isn't present, this method adds and enables it.
     * @param enable - specifies whether the About item in the application menu should be enabled (true) or not (false)
     * @deprecated no replacement
     * @since 1.4
     */
    @Deprecated
    public void setEnabledAboutMenu(boolean enable) {
        APP.setEnabledAboutMenu(enable);
    }

    /**
     * Determines if the Preferences item of the application menu is enabled.
     * @deprecated no replacement
     * @since 1.4
     */
    @Deprecated
    public boolean getEnabledPreferencesMenu() {
        return APP.getEnabledPreferencesMenu();
    }

    /**
     * Determines if the About item of the application menu is enabled. 
     * @deprecated no replacement
     * @since 1.4
     */
    @Deprecated
    public boolean getEnabledAboutMenu() {
        return APP.getEnabledAboutMenu();
    }

    /**
     * Determines if the About item of the application menu is present.
     * @deprecated no replacement
     * @since 1.4
     */
    @Deprecated
    public boolean isAboutMenuItemPresent() {
        return APP.isAboutMenuItemPresent();
    }

    /**
     * Adds the About item to the application menu if the item is not already present.
     * @deprecated no replacement here (original: use setAboutHandler(AboutHandler) with a non-null AboutHandler parameter)
     * @since 1.4
     */
    @Deprecated
    public void addAboutMenuItem() {
        APP.addAboutMenuItem();
    }

    /**
     * Removes the About item from the application menu if the item is present. 
     * @deprecated no replacement here (original: use setAboutHandler(AboutHandler) with a null parameter)
     * @since 1.4
     */
    @Deprecated
    public void removeAboutMenuItem() {
        APP.removeAboutMenuItem();
    }

    /**
     * Determines if the About Preferences of the application menu is present.
     * By default there is no Preferences menu item.
     * @deprecated no replacement
     * @since 1.4
     */
    @Deprecated
    public boolean isPreferencesMenuItemPresent() {
        return APP.isPreferencesMenuItemPresent();
    }

    /**
     * Adds the Preferences item to the application menu if the item is not already present.
     * @deprecated no replacement here (original: use setPreferencesHandler(PreferencesHandler) with a non-null PreferencesHandler parameter)
     * @since 1.4
     */
    @Deprecated
    public void addPreferencesMenuItem() {
        APP.addPreferencesMenuItem();
    }

    /**
     * Removes the Preferences item from the application menu if that item is present.
     * @deprecated no replacement here (original: use setPreferencesHandler(PreferencesHandler) with a null parameter)
     * @since 1.4
     */
    @Deprecated
    public void removePreferencesMenuItem() {
        APP.removePreferencesMenuItem();
    }
    
}
