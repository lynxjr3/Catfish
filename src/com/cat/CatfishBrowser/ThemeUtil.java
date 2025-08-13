/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cat.CatfishBrowser;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.*;
import java.awt.Window;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author LynxJr
 */
public class ThemeUtil {
    private static final Preferences prefs = Preferences.userRoot().node("CatfishBrowser");
    private static final String PREF_KEY = "selectedTheme";

    // Set a theme and save it
    public static void setTheme(String themeName) {
        try {
            

            // --- Set the Look and Feel ---
            switch (themeName) {
    // FlatLaf Themes
    case "Nord": UIManager.setLookAndFeel(new FlatNordIJTheme()); break;
    case "Arc Dark": UIManager.setLookAndFeel(new FlatArcDarkIJTheme()); break;
    case "Arc Dark - Orange": UIManager.setLookAndFeel(new FlatArcDarkOrangeIJTheme()); break;
    case "Carbon": UIManager.setLookAndFeel(new FlatCarbonIJTheme()); break;
    case "Cobalt 2": UIManager.setLookAndFeel(new FlatCobalt2IJTheme()); break;
    case "Dark Flat": UIManager.setLookAndFeel(new FlatDarkFlatIJTheme()); break;
    case "Dark Purple": UIManager.setLookAndFeel(new FlatDarkPurpleIJTheme()); break;
    case "Dracula": UIManager.setLookAndFeel(new FlatDraculaIJTheme()); break;
    case "Gradianto Dark Fuchsia": UIManager.setLookAndFeel(new FlatGradiantoDarkFuchsiaIJTheme()); break;
    case "Gradianto Deep Ocean": UIManager.setLookAndFeel(new FlatGradiantoDeepOceanIJTheme()); break;
    case "Gradianto Midnight Blue": UIManager.setLookAndFeel(new FlatGradiantoMidnightBlueIJTheme()); break;
    case "Gradianto Nature Green": UIManager.setLookAndFeel(new FlatGradiantoNatureGreenIJTheme()); break;
    case "Gruvbox Dark Hard": UIManager.setLookAndFeel(new FlatGruvboxDarkHardIJTheme()); break;
    case "Hiberbee Dark": UIManager.setLookAndFeel(new FlatHiberbeeDarkIJTheme()); break;
    case "High Contrast": UIManager.setLookAndFeel(new FlatHighContrastIJTheme()); break;
    case "Material Design Dark": UIManager.setLookAndFeel(new FlatMaterialDesignDarkIJTheme()); break;
    case "Monocai": UIManager.setLookAndFeel(new FlatMonocaiIJTheme()); break;
    case "Monokai Pro": UIManager.setLookAndFeel(new FlatMonokaiProIJTheme()); break;
    case "One Dark": UIManager.setLookAndFeel(new FlatOneDarkIJTheme()); break;
    case "Solarized Dark": UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme()); break;
    case "Spacegray": UIManager.setLookAndFeel(new FlatSpacegrayIJTheme()); break;
    case "Vuesion": UIManager.setLookAndFeel(new FlatVuesionIJTheme()); break;
    case "Xcode-Dark": UIManager.setLookAndFeel(new FlatXcodeDarkIJTheme()); break;
    case "FlatLaf Dark": UIManager.setLookAndFeel(new FlatDarkLaf()); break;
    case "FlatLaf Darcula": UIManager.setLookAndFeel(new FlatDarculaLaf()); break;

    // Java Defaults
    case "Metal": UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); break;
    case "Nimbus": UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); break;
    case "CDE/Motif": UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); break;
    case "Windows Classic": UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"); break;
    case "System Default": UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); break;

    default:
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
}

// Update all open windows
for (Window window : Window.getWindows()) {
    SwingUtilities.updateComponentTreeUI(window);
    window.pack();
}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void saveTheme(String themeName) {
    prefs.put(PREF_KEY, themeName);
}

    // Get saved theme
    public static String getSavedTheme() {
        return prefs.get(PREF_KEY, "Nord"); // default to Nord
    }

    // Apply saved theme
    public static void applySavedTheme() {
        setTheme(getSavedTheme());
    }
    
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
}
