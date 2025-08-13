/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cat.CatfishBrowser;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefDisplayHandler;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefMessageRouterHandler;
import org.cef.handler.CefMessageRouterHandlerAdapter;

/**
 *
 * @author Saliya
 */
public class Catfish extends javax.swing.JFrame implements WindowListener {

    // Tab management
    private JTabbedPane tabbedPane;
    private Map<Integer, CefBrowser> browsers = new HashMap<>();
    private Map<Integer, Component> browserUIs = new HashMap<>();
    private Map<Integer, JLabel> tabLabels = new HashMap<>();
    private int currentTabIndex = 0;
    private String currentPageTitle = "";
    
    
    // CEF components
    private CefSettings settings;
    private CefApp cefApp;
    private CefClient defaultClient;
    
    // Other fields
    private double zoomLevel_ = 0;
    private String errorMsg_ = "";
    
    private int dragTabIndex = -1;
private Point dragStartPoint;
private final int DRAG_THRESHOLD = 15; // Minimum pixels to move before dragging starts
private Color originalTabColor;

    /**
     * Creates new form webframe
     *
     * @param startURL
     * @param useOSR
     * @param isTransparent
     */
    public Catfish(String startURL, boolean useOSR, boolean isTransparent) {
            initComponents();
        
        // Load saved bookmarks
    loadBookmarksFromPreferences();
    
    // Set default URLs for bookmarks (only used if no preference is saved)
    handleBookmarkClick(bm1, "https://www.google.com");
    handleBookmarkClick(bm2, "https://mail.google.com");
    handleBookmarkClick(bm3, "https://www.youtube.com");
    handleBookmarkClick(bm4, "https://www.reddit.com");
    handleBookmarkClick(bm5, "https://sites.google.com/view/thecatco");
    handleBookmarkClick(bm6, "https://discord.com/app");
    handleBookmarkClick(bm7, null);
    handleBookmarkClick(bm8, null);
    handleBookmarkClick(bm9, null);
        
        setupAddressBarPopup();
        addWindowListener(this);
        
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
    .addKeyEventDispatcher(e -> {
        if (jTextField1.hasFocus()) {
            // If user types when address bar has focus, force focus back to it
            if (e.getID() == KeyEvent.KEY_TYPED) {
                SwingUtilities.invokeLater(() -> {
                    if (!jTextField1.hasFocus()) {
                        jTextField1.requestFocusInWindow();
                    }
                });
            }
        }
        return false; // let other listeners handle events normally
    });
        
        // Setup tabbed interface
        tabbedPane = new JTabbedPane();
        jPanel1.setLayout(new BorderLayout());
        jPanel1.add(tabbedPane, BorderLayout.CENTER);
        
        setupTabDragging();
        
        // Initialize CEF
        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            @Override
            public void stateHasChanged(CefApp.CefAppState state) {
                if (state == CefApp.CefAppState.TERMINATED) {
                    System.exit(0);
                }
            }
        });
        
        settings = new CefSettings();
        settings.windowless_rendering_enabled = useOSR;
        String[] disableWebSecurity = {"--disable-web-security", "--allow-file-access-from-files"};

        cefApp = CefApp.getInstance(settings);
        CefApp.getInstance(disableWebSecurity, settings);
        defaultClient = cefApp.createClient();
        
        cefApp.registerSchemeHandlerFactory("catfish", "", (CefSchemeHandlerFactory) new CustomSchemeHandlerFactory());
        
        // Add first tab
        addNewTab(startURL, useOSR, isTransparent);
        
        back.setEnabled(false);
        forward.setEnabled(false);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        fixTransparentButtons(this.getContentPane());
    }
    

    private void addNewTab(String url, boolean useOSR, boolean isTransparent) {
    // Create a new client for each tab
    CefClient tabClient = cefApp.createClient();
    CefBrowser newBrowser = tabClient.createBrowser(url, useOSR, isTransparent);
    Component newBrowserUI = newBrowser.getUIComponent();
    newBrowserUI.setFocusable(true);

    // Create a container panel for this browser
    JPanel browserContainer = new JPanel(new BorderLayout());
    browserContainer.add(newBrowserUI, BorderLayout.CENTER);

    int tabIndex = tabbedPane.getTabCount();
    browsers.put(tabIndex, newBrowser);
    browserUIs.put(tabIndex, browserContainer);

    // Create tab with close button
    JPanel tabPanel = new JPanel(new BorderLayout());
    tabPanel.setOpaque(false);

    JLabel tabLabel = new JLabel("Loading...");
    tabLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    JButton closeButton = new JButton("×");
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setPreferredSize(new Dimension(16, 16));
    closeButton.addActionListener(e -> {
        int indexToClose = tabbedPane.indexOfComponent(browserContainer);
        if (indexToClose >= 0) {
            closeTab(indexToClose);
        }
    });
    
    setupMiddleClickHandling(newBrowser);
    
    // Add mouse listener as fallback
    newBrowserUI.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            handleMiddleClick(newBrowser, e);
        }
    });
    
    tabLabel.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        int i = tabbedPane.indexOfTabComponent(tabPanel);
        if (i != -1) {
            tabbedPane.setSelectedIndex(i);
        }
    }
});

    tabPanel.add(tabLabel, BorderLayout.CENTER);
tabPanel.add(closeButton, BorderLayout.EAST);
    tabLabels.put(tabIndex, tabLabel);

    tabbedPane.addTab(null, browserContainer);
    
tabbedPane.setTabComponentAt(tabIndex, tabPanel);

    // Add display handler for this tab
    tabClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
    @Override
    public void onTitleChange(CefBrowser browser, String title) {
        SwingUtilities.invokeLater(() -> {
            String displayTitle = title.isEmpty() ? "New Tab" : shortenTitle(title);
            tabLabel.setText(displayTitle);
            tabLabel.setToolTipText(title.isEmpty() ? "New Tab" : title);
            
            // Update the current title if this is the active browser
            if (browser == getCurrentBrowser()) {
                currentPageTitle = title;
            }
        });
    }

        @Override
        public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
            if (browser == getCurrentBrowser()) {
                SwingUtilities.invokeLater(() -> jTextField1.setText(url));
            }
        }
    });

    // Add load handler for this tab
    tabClient.addLoadHandler(new CefLoadHandlerAdapter() {
    @Override
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                  boolean canGoBack, boolean canGoForward) {
        if (browser == getCurrentBrowser()) {
            update(browser, isLoading, canGoBack, canGoForward);
            Color grayColor = new Color(204,204,204);
            Color lightBlueColor = new Color(105,204,255);
            tabLabel.setForeground(isLoading ? lightBlueColor : grayColor);
            
            SwingUtilities.invokeLater(() -> {
                if (isLoading) {
                    // Start with indeterminate state while connecting
                    loadingBar.setIndeterminate(true);
                    loadingBar.setVisible(true);
                } else {
                    // Loading complete
                    loadingBar.setIndeterminate(false);
                    loadingBar.setValue(100);
                    // Hide after a short delay to show completion
                    new Timer(500, e -> {
                        loadingBar.setVisible(false);
                        ((Timer)e.getSource()).stop();
                    }).start();
                }
            });
        }
    }
    
    public void onLoadProgress(CefBrowser browser, double progress) {
        if (browser == getCurrentBrowser()) {
            SwingUtilities.invokeLater(() -> {
                // When we get progress updates, switch to determinate mode
                if (loadingBar.isIndeterminate()) {
                    loadingBar.setIndeterminate(false);
                }
                int progressValue = (int) (progress * 100);
                loadingBar.setValue(progressValue);
                
                // Optional: Show percentage text
                loadingBar.setString(progressValue + "%");
                loadingBar.setStringPainted(true);
            });
        }
    }
    
    public void onLoadError(CefBrowser browser, int frameIdentifer,
                         CefLoadHandler.ErrorCode errorCode,
                         String errorText, String failedUrl) {
        if (errorCode != CefLoadHandler.ErrorCode.ERR_NONE &&
            errorCode != CefLoadHandler.ErrorCode.ERR_ABORTED) {
            errorMsg_ = "<html><head>";
            errorMsg_ += "<title>Error while loading</title>";
            errorMsg_ += "</head><body>";
            errorMsg_ += "<h1>" + errorCode + "</h1>";
            errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
            errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
            errorMsg_ += "</body></html>";
            browser.stopLoad();
            
            SwingUtilities.invokeLater(() -> {
                tabLabel.setText("Error: " + shortenUrl(failedUrl));
                tabLabel.setToolTipText(errorText);
                loadingBar.setIndeterminate(false);
                loadingBar.setValue(0);
                loadingBar.setVisible(false);
            });
        }
    }
});

    // Focus listeners
    jTextField1.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if (tabbedPane.getSelectedIndex() == tabIndex) {
                newBrowserUI.setFocusable(false);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (tabbedPane.getSelectedIndex() == tabIndex) {
                newBrowserUI.setFocusable(true);
                SwingUtilities.invokeLater(() -> newBrowserUI.requestFocusInWindow());
            }
        }
    });

    // Tab change listener
    tabbedPane.addChangeListener(e -> {
        int selected = tabbedPane.getSelectedIndex();
        if (selected >= 0) {  // Check if there is a selected tab
            CefBrowser selectedBrowser = browsers.get(selected);
            if (selectedBrowser != null) {
                SwingUtilities.invokeLater(() -> {
                    jTextField1.setText(selectedBrowser.getURL());
                    if (!jTextField1.hasFocus()) {
                        browserUIs.get(selected).requestFocusInWindow();
                    }
                });
            }
        }
    });

    tabbedPane.setSelectedIndex(tabIndex);

    if (!jTextField1.hasFocus()) {
        SwingUtilities.invokeLater(() -> newBrowserUI.requestFocusInWindow());
    }
}
    
    private void setupTabDragging() {
    if (tabbedPane == null) return;

    tabbedPane.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            dragTabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            if (dragTabIndex >= 0) {
                dragStartPoint = e.getPoint();
                Component tabComp = tabbedPane.getTabComponentAt(dragTabIndex);
                if (tabComp != null) {
                    originalTabColor = tabComp.getBackground();
                    tabComp.setBackground(new Color(220, 220, 220));
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragTabIndex >= 0) {
                Component tabComp = tabbedPane.getTabComponentAt(dragTabIndex);
                if (tabComp != null) {
                    tabComp.setBackground(originalTabColor);
                }
            }
            dragTabIndex = -1;
            dragStartPoint = null;
        }
    });

    tabbedPane.addMouseMotionListener(new MouseAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragTabIndex < 0 || dragStartPoint == null) return;

            // Check if we've moved enough to start dragging
            if (dragStartPoint.distance(e.getPoint()) < DRAG_THRESHOLD) {
                return;
            }

            if (dragTabIndex >= 0) {
    Component tabComp = tabbedPane.getTabComponentAt(dragTabIndex);
    if (tabComp != null) {
        tabComp.setBackground(new Color(220, 220, 220, 150)); // Semi-transparent
    }
}
            int newIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            if (newIndex >= 0 && newIndex != dragTabIndex) {
                moveTab(dragTabIndex, newIndex);
                dragTabIndex = newIndex;
                dragStartPoint = e.getPoint();
            }
        }
    });
}
    
    private void moveTab(int fromIndex, int toIndex) {
    if (fromIndex == toIndex) return;
    
    // Get all components and properties
    Component comp = tabbedPane.getComponentAt(fromIndex);
    Component tabComp = tabbedPane.getTabComponentAt(fromIndex);
    String title = tabbedPane.getTitleAt(fromIndex);
    Icon icon = tabbedPane.getIconAt(fromIndex);
    String tip = tabbedPane.getToolTipTextAt(fromIndex);
    boolean isSelected = tabbedPane.getSelectedIndex() == fromIndex;

    // Remove from original position
    tabbedPane.remove(fromIndex);

    // Insert at new position
    tabbedPane.insertTab(title, icon, comp, tip, toIndex);
    tabbedPane.setTabComponentAt(toIndex, tabComp);

    // Update internal maps
    CefBrowser browser = browsers.remove(fromIndex);
    Component browserUI = browserUIs.remove(fromIndex);
    JLabel tabLabel = tabLabels.remove(fromIndex);

    // Shift indices in the maps
    if (toIndex > fromIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            browsers.put(i, browsers.get(i + 1));
            browserUIs.put(i, browserUIs.get(i + 1));
            tabLabels.put(i, tabLabels.get(i + 1));
        }
    } else {
        for (int i = fromIndex; i > toIndex; i--) {
            browsers.put(i, browsers.get(i - 1));
            browserUIs.put(i, browserUIs.get(i - 1));
            tabLabels.put(i, tabLabels.get(i - 1));
        }
    }

    // Put back the moved tab
    browsers.put(toIndex, browser);
    browserUIs.put(toIndex, browserUI);
    tabLabels.put(toIndex, tabLabel);

    // Restore selection
    if (isSelected) {
        tabbedPane.setSelectedIndex(toIndex);
    }
}
    
    private void swapTabs(int srcIndex, int destIndex) {
    if (tabbedPane == null || srcIndex < 0 || destIndex < 0 || 
        srcIndex >= tabbedPane.getTabCount() || destIndex >= tabbedPane.getTabCount()) {
        return;
    }

    // Don't swap if source and destination are same
    if (srcIndex == destIndex) {
        return;
    }

    try {
        // Store all tab information
        Component srcComp = tabbedPane.getComponentAt(srcIndex);
        Component srcTabComp = tabbedPane.getTabComponentAt(srcIndex);
        String srcTitle = tabbedPane.getTitleAt(srcIndex);
        Icon srcIcon = tabbedPane.getIconAt(srcIndex);
        String srcTip = tabbedPane.getToolTipTextAt(srcIndex);
        
        // Remove the source tab
        tabbedPane.remove(srcIndex);
        
        // Insert at destination position
        tabbedPane.insertTab(srcTitle, srcIcon, srcComp, srcTip, destIndex);
        tabbedPane.setTabComponentAt(destIndex, srcTabComp);
        
        // Update our internal maps
        CefBrowser srcBrowser = browsers.remove(srcIndex);
        Component srcBrowserUI = browserUIs.remove(srcIndex);
        JLabel srcLabel = tabLabels.remove(srcIndex);
        
        // Shift all elements between src and dest
        if (destIndex > srcIndex) {
            for (int i = srcIndex; i < destIndex; i++) {
                browsers.put(i, browsers.get(i + 1));
                browserUIs.put(i, browserUIs.get(i + 1));
                tabLabels.put(i, tabLabels.get(i + 1));
            }
        } else {
            for (int i = srcIndex; i > destIndex; i--) {
                browsers.put(i, browsers.get(i - 1));
                browserUIs.put(i, browserUIs.get(i - 1));
                tabLabels.put(i, tabLabels.get(i - 1));
            }
        }
        
        // Put back the moved tab
        browsers.put(destIndex, srcBrowser);
        browserUIs.put(destIndex, srcBrowserUI);
        tabLabels.put(destIndex, srcLabel);
        
        // Select the moved tab
        tabbedPane.setSelectedIndex(destIndex);
    } catch (Exception e) {
        System.err.println("Error swapping tabs: " + e.getMessage());
        e.printStackTrace();
        // Attempt to restore state if possible
        restoreTabState();
    }
}
    
    private void restoreTabState() {
    // This is a safety method to restore tab state if something goes wrong
    // You might want to implement a more sophisticated recovery
    tabbedPane.removeAll();
    for (int i = 0; i < browsers.size(); i++) {
        if (browsers.get(i) != null && browserUIs.get(i) != null) {
            tabbedPane.addTab(null, browserUIs.get(i));
            if (tabLabels.get(i) != null) {
                tabbedPane.setTabComponentAt(i, createTabComponent(tabLabels.get(i).getText()));
            }
        }
    }
}
    
    private JPanel createTabComponent(String title) {
    JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tabPanel.setOpaque(false);
    
    JLabel tabLabel = new JLabel(title);
    JButton closeButton = new JButton("×");
    closeButton.setMargin(new Insets(0, 5, 0, 0));
    closeButton.addActionListener(e -> {
        int indexToClose = tabbedPane.indexOfTabComponent(tabPanel);
        if (indexToClose >= 0) {
            closeTab(indexToClose);
        }
    });
    
    tabPanel.add(tabLabel);
    tabPanel.add(closeButton);
    return tabPanel;
}
    
    private String shortenUrl(String url) {
    if (url == null) return "";
    if (url.length() > 30) {
        return url.substring(0, 15) + "..." + url.substring(url.length() - 10);
    }
    return url;
}


    private void setupAddressBarPopup() {
    jTextField1.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            showAddressInputDialog();
        }
    });
}
    
    private String shortenTitle(String title) {
    if (title == null) return "New Tab";
    if (title.length() > 100) {
        return title.substring(0, 97) + "...";
    }
    return title;
}
    
    private void showAddressInputDialog() {
    // Create a JDialog (modal or non-modal)
    JDialog dialog = new JDialog(this, "Enter URL", Dialog.ModalityType.APPLICATION_MODAL); // 'this' should be your JFrame
    
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    JTextField urlField = new JTextField(jTextField1.getText(), 30);
    JButton goButton = new JButton("Go");
    
    panel.add(urlField, BorderLayout.CENTER);
    panel.add(goButton, BorderLayout.EAST);
    dialog.getContentPane().add(panel);
    dialog.pack();
    
    // Position dialog near the main address bar
    Point loc = jTextField1.getLocationOnScreen();
    dialog.setLocation(loc.x, loc.y + jTextField1.getHeight());
    
    // Load URL on button click or Enter key press
    ActionListener goAction = ev -> {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            // Load the URL in the current browser tab
            CefBrowser currentBrowser = getCurrentBrowser();
            if (currentBrowser != null) {
                currentBrowser.loadURL(url);
                jTextField1.setText(url);
            }
        }
        dialog.dispose();
        // Optionally focus back to browser UI
        Component browserUI = browserUIs.get(tabbedPane.getSelectedIndex());
        if (browserUI != null) {
            browserUI.requestFocusInWindow();
        }
    };
    
    goButton.addActionListener(goAction);
    urlField.addActionListener(goAction);
    
    dialog.setVisible(true);
    
    // Focus the text field immediately
    SwingUtilities.invokeLater(urlField::requestFocusInWindow);
}

    private CefBrowser getCurrentBrowser() {
    int selectedIndex = tabbedPane.getSelectedIndex();
    if (selectedIndex >= 0) {
        return browsers.get(selectedIndex);
    }
    return null;
}

    private Component getCurrentBrowserUI() {
        return browserUIs.get(currentTabIndex);
    }

    private void updateUIForCurrentTab() {
        CefBrowser currentBrowser = getCurrentBrowser();
        if (currentBrowser != null) {
            String currentUrl = currentBrowser.getURL();
            jTextField1.setText(currentUrl != null ? currentUrl : "");
            back.setEnabled(currentBrowser.canGoBack());
            forward.setEnabled(currentBrowser.canGoForward());
        }
    }

    private void closeTab(int tabIndex) {
    if (tabbedPane.getTabCount() <= 1) {
        // Show warning dialog for last tab
        int option = JOptionPane.showOptionDialog(
            this,
            "This is the last tab. Closing it will exit the browser.",
            "Close Last Tab",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            new Object[]{"Close Browser", "Stay"}, // Button options
            "Stay" // Default option
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // User chose to close the browser
            dispose();
            System.exit(0);
        } else {
            // User chose to stay, do nothing
            return;
        }
    }
    
    // Proceed with normal tab closing if not last tab
    CefBrowser browserToClose = browsers.get(tabIndex);
    if (browserToClose != null) {
        browserToClose.close(true);
    }
    
    // Remove all references
    browsers.remove(tabIndex);
    browserUIs.remove(tabIndex);
    tabLabels.remove(tabIndex);
    
    // Remove the tab
    tabbedPane.remove(tabIndex);
    
    // Reindex remaining tabs
    Map<Integer, CefBrowser> newBrowsers = new HashMap<>();
    Map<Integer, Component> newBrowserUIs = new HashMap<>();
    Map<Integer, JLabel> newTabLabels = new HashMap<>();
    
    // Rebuild the maps with correct indices
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        Component comp = tabbedPane.getComponentAt(i);
        for (Map.Entry<Integer, Component> entry : browserUIs.entrySet()) {
            if (entry.getValue() == comp) {
                newBrowsers.put(i, browsers.get(entry.getKey()));
                newBrowserUIs.put(i, entry.getValue());
                newTabLabels.put(i, tabLabels.get(entry.getKey()));
                break;
            }
        }
    }
    
    // Update the maps
    browsers.clear();
    browsers.putAll(newBrowsers);
    browserUIs.clear();
    browserUIs.putAll(newBrowserUIs);
    tabLabels.clear();
    tabLabels.putAll(newTabLabels);
    
    // Update current tab index
    currentTabIndex = tabbedPane.getSelectedIndex();
}
    
    URL reloadIconURL = getClass().getResource("/com/cat/CatfishBrowser/assets/rotate.png");
URL abortIconURL  = getClass().getResource("/com/cat/CatfishBrowser/assets/cross-circle.png");

ImageIcon reloadIcon = new ImageIcon(reloadIconURL);
ImageIcon abortIcon  = new ImageIcon(abortIconURL);

    public void update(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
        if (browser == getCurrentBrowser()) {
            back.setEnabled(canGoBack);
            forward.setEnabled(canGoForward);
            reload.setIcon(isLoading ? abortIcon : reloadIcon);
        }
    }
    
    public static void fixTransparentButtons(Container container) {
    for (Component c : container.getComponents()) {
        if (c instanceof JButton) {
            JButton btn = (JButton) c;  // old-style cast
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBackground(null);
        } else if (c instanceof Container) {
            fixTransparentButtons((Container) c); // recurse
        }
    }
}
    
    public static void makeButtonTransparent(JButton button) {
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorder(null);
    button.setBackground(null);
}
    
    private void handleBookmarkClick(JButton bookmark, String defaultUrl) {
    // Left click - navigate to URL
    bookmark.addActionListener(e -> {
        String url = bookmark.getToolTipText();
        if (url != null && !url.isEmpty()) {
            getCurrentBrowser().loadURL(url);
        } else if (defaultUrl != null) {
            getCurrentBrowser().loadURL(defaultUrl);
        }
    });
    
    // Right click - configure bookmark
    bookmark.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                configureBookmark(bookmark);
            }
        }
    });
}
    
    private void configureBookmark(JButton bookmark) {
    JDialog configDialog = new JDialog(this, "Configure Bookmark", true);
    configDialog.setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new GridLayout(3, 2));
    JLabel nameLabel = new JLabel("Name:");
    JTextField nameField = new JTextField(bookmark.getText());
    JLabel urlLabel = new JLabel("URL:");
    JTextField urlField = new JTextField(bookmark.getToolTipText());
    
    panel.add(nameLabel);
    panel.add(nameField);
    panel.add(urlLabel);
    panel.add(urlField);
    
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> {
        String url = urlField.getText().trim();
        
        // Check for default bookmarks
        if (isDefaultBookmark(url)) {
            JOptionPane.showMessageDialog(configDialog, 
                "This links bookmark already exsists.",
                null,
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        bookmark.setText(nameField.getText());
        bookmark.setToolTipText(urlField.getText());
        saveBookmarksToPreferences();
        configDialog.dispose();
    });
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> configDialog.dispose());
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    
    configDialog.add(panel, BorderLayout.CENTER);
    configDialog.add(buttonPanel, BorderLayout.SOUTH);
    configDialog.pack();
    configDialog.setLocationRelativeTo(this);
    configDialog.setVisible(true);
}
    
    private void saveBookmarksToPreferences() {
    Preferences prefs = Preferences.userNodeForPackage(Catfish.class);
    
    // Save each bookmark
    prefs.put("bookmark_1_name", bm1.getText());
    prefs.put("bookmark_1_url", bm1.getToolTipText());
    prefs.put("bookmark_2_name", bm2.getText());
    prefs.put("bookmark_2_url", bm2.getToolTipText());
    prefs.put("bookmark_3_name", bm3.getText());
    prefs.put("bookmark_3_url", bm3.getToolTipText());
    prefs.put("bookmark_4_name", bm4.getText());
    prefs.put("bookmark_4_url", bm4.getToolTipText());
    prefs.put("bookmark_5_name", bm5.getText());
    prefs.put("bookmark_5_url", bm5.getToolTipText());
    prefs.put("bookmark_6_name", bm7.getText());
    prefs.put("bookmark_6_url", bm7.getToolTipText());
    prefs.put("bookmark_7_name", bm6.getText());
    prefs.put("bookmark_7_url", bm6.getToolTipText());
    prefs.put("bookmark_8_name", bm8.getText());
    prefs.put("bookmark_8_url", bm8.getToolTipText());
    prefs.put("bookmark_9_name", bm9.getText());
    prefs.put("bookmark_9_url", bm9.getToolTipText());
}

private void loadBookmarksFromPreferences() {
    Preferences prefs = Preferences.userNodeForPackage(Catfish.class);
    
    // Load each bookmark
    bm1.setText(prefs.get("bookmark_1_name", "Google"));
    bm1.setToolTipText(prefs.get("bookmark_1_url", "https://www.google.com"));
    bm2.setText(prefs.get("bookmark_2_name", "Gmail"));
    bm2.setToolTipText(prefs.get("bookmark_2_url", "https://mail.google.com"));
    bm3.setText(prefs.get("bookmark_3_name", "YouTube"));
    bm3.setToolTipText(prefs.get("bookmark_3_url", "https://www.youtube.com"));
    bm4.setText(prefs.get("bookmark_4_name", "Reddit"));
    bm4.setToolTipText(prefs.get("bookmark_4_url", "https://www.reddit.com"));
    bm5.setText(prefs.get("bookmark_5_name", "CatCo"));
    bm5.setToolTipText(prefs.get("bookmark_5_url", "https://sites.google.com/view/thecatco"));
    bm6.setText(prefs.get("bookmark_6_name", "Discord"));
    bm6.setToolTipText(prefs.get("bookmark_6_url", "https://discord.com/app"));
    bm7.setText(prefs.get("bookmark_7_name", "7"));
    bm7.setToolTipText(prefs.get("bookmark_7_url", ""));
    bm8.setText(prefs.get("bookmark_8_name", "8"));
    bm8.setToolTipText(prefs.get("bookmark_8_url", ""));
    bm9.setText(prefs.get("bookmark_9_name", "9"));
    bm9.setToolTipText(prefs.get("bookmark_9_url", ""));
}

    private void showBookmarkReplaceDialog(String url, String title) {
    JDialog replaceDialog = new JDialog(this, "Replace Bookmark", true);
    replaceDialog.setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Create combo box with bookmark labels
    JComboBox<String> bookmarkCombo = new JComboBox<>();
    bookmarkCombo.addItem("1. " + bm1.getText());
    bookmarkCombo.addItem("2. " + bm2.getText());
    bookmarkCombo.addItem("3. " + bm3.getText());
    bookmarkCombo.addItem("4. " + bm4.getText());
    bookmarkCombo.addItem("5. " + bm5.getText());
    bookmarkCombo.addItem("6. " + bm6.getText());
    bookmarkCombo.addItem("7. " + bm7.getText());
    bookmarkCombo.addItem("8. " + bm8.getText());
    bookmarkCombo.addItem("9. " + bm9.getText());
    
    // Show current URL and title
    JLabel infoLabel = new JLabel("<html>Replace which bookmark with:<br>" +
                                 "URL: " + url + "<br>" +
                                 "Title: " + title + "</html>");
    
    panel.add(infoLabel, BorderLayout.NORTH);
    panel.add(bookmarkCombo, BorderLayout.CENTER);
    
    JButton replaceButton = new JButton("Replace");
    replaceButton.addActionListener(e -> {
        int selectedIndex = bookmarkCombo.getSelectedIndex();
        if (selectedIndex >= 0) {
            JButton selectedBookmark = getBookmarkByIndex(selectedIndex + 1);
            selectedBookmark.setText(title);
            selectedBookmark.setToolTipText(url);
            saveBookmarksToPreferences();
            
            // Visual feedback
            selectedBookmark.setBackground(new Color(200, 255, 200)); // Light green
            Timer timer = new Timer(1000, ev -> {
                selectedBookmark.setBackground(null);
            });
            timer.setRepeats(false);
            timer.start();
            
            replaceDialog.dispose();
            
            JOptionPane.showMessageDialog(this, 
                "Bookmark " + (selectedIndex + 1) + " updated successfully!",
                "Bookmark Saved", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    });
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> replaceDialog.dispose());
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(replaceButton);
    buttonPanel.add(cancelButton);
    
    replaceDialog.add(panel, BorderLayout.CENTER);
    replaceDialog.add(buttonPanel, BorderLayout.SOUTH);
    replaceDialog.pack();
    replaceDialog.setLocationRelativeTo(this);
    replaceDialog.setVisible(true);
}

private JButton getBookmarkByIndex(int index) {
    switch(index) {
        case 1: return bm1;
        case 2: return bm2;
        case 3: return bm3;
        case 4: return bm4;
        case 5: return bm5;
        case 6: return bm6;
        case 7: return bm7;
        case 8: return bm8;
        case 9: return bm9;
        default: return null;
    }
}

private boolean isDefaultBookmark(String url) {
    return url.equals("catfish://newtab") || url.equals("catfish://cats");
}

    
private class MiddleClickHandler extends CefMessageRouterHandlerAdapter {
    public boolean onQuery(CefBrowser browser, long queryId, String request, 
                         boolean persistent, CefQueryCallback callback) {
        if (request.startsWith("MIDDLECLICK:")) {
            String url = request.substring("MIDDLECLICK:".length());
            SwingUtilities.invokeLater(() -> {
                // Open in new tab
                addNewTab(url, false, false);
            });
            callback.success("");
            return true;
        }
        return false;
    }
    
    public void onQueryCanceled(CefBrowser browser, long queryId) {
        // Clean up any pending requests
    }
}

private void setupMiddleClickHandling(CefBrowser browser) {
    // Create message router
    CefMessageRouter messageRouter = CefMessageRouter.create();
    messageRouter.addHandler(new MiddleClickHandler(), true);
    browser.getClient().addMessageRouter(messageRouter);
    
    // Updated JavaScript with proper event cancellation
    String jsCode = 
        "document.addEventListener('auxclick', function(e) {" +
        "   if (e.button === 1) {" +  // Middle mouse button
        "       e.preventDefault();" +
        "       e.stopPropagation();" +
        "       if (e.target.tagName === 'A' && e.target.href) {" +
        "           window.mcefQuery({" +
        "               request: 'MIDDLECLICK:' + e.target.href," +
        "               onSuccess: function(response) {}," +
        "               onFailure: function(error_code, error_message) {}" +
        "           });" +
        "           return false;" +  // Prevent default browser behavior
        "       }" +
        "   }" +
        "}, true);";  // Use capturing phase
    
    browser.executeJavaScript(jsCode, browser.getURL(), 0);
}

private void handleMiddleClick(CefBrowser browser, MouseEvent e) {
    if (SwingUtilities.isMiddleMouseButton(e)) {
        e.consume();  // Prevent further processing
        
        // Get coordinates relative to browser component
        Point clickPoint = e.getPoint();
        SwingUtilities.convertPointToScreen(clickPoint, (Component)e.getSource());
        SwingUtilities.convertPointFromScreen(clickPoint, browser.getUIComponent());
        
        // JavaScript to find and handle the link
        String jsCode = String.format(
            "(function(x, y) {" +
            "   var elem = document.elementFromPoint(x, y);" +
            "   while (elem && elem.tagName !== 'A') {" +
            "       elem = elem.parentElement;" +
            "   }" +
            "   if (elem && elem.href) {" +
            "       window.mcefQuery({" +
            "           request: 'MIDDLECLICK:' + elem.href," +
            "           onSuccess: function() {}," +
            "           onFailure: function() {}" +
            "       });" +
            "   }" +
            "   return false;" +  // Prevent default
            "})(%d, %d);", clickPoint.x, clickPoint.y);
        
        browser.executeJavaScript(jsCode, browser.getURL(), 0);
    }
}
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        back = new javax.swing.JButton();
        forward = new javax.swing.JButton();
        reload = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        loadingBar = new javax.swing.JProgressBar();
        jButton9 = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        bm1 = new javax.swing.JButton();
        bm2 = new javax.swing.JButton();
        bm3 = new javax.swing.JButton();
        bm4 = new javax.swing.JButton();
        bm5 = new javax.swing.JButton();
        bm7 = new javax.swing.JButton();
        bm6 = new javax.swing.JButton();
        bm8 = new javax.swing.JButton();
        bm9 = new javax.swing.JButton();
        bmACSTB = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLocation(new java.awt.Point(0, 0));
        setSize(new java.awt.Dimension(0, 0));

        jButton1.setBackground(null);
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/home (1).png"))); // NOI18N
        jButton1.setToolTipText("Go home");
        jButton1.setBorder(null);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel1.setForeground(null);
        jPanel1.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 688, Short.MAX_VALUE)
        );

        back.setBackground(null);
        back.setForeground(new java.awt.Color(255, 255, 255));
        back.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/arrow-small-left (1).png"))); // NOI18N
        back.setToolTipText("Go back");
        back.setBorder(null);
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });

        forward.setBackground(null);
        forward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/arrow-small-left (2).png"))); // NOI18N
        forward.setToolTipText("Go forward");
        forward.setBorder(null);
        forward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardActionPerformed(evt);
            }
        });

        reload.setBackground(null);
        reload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/rotate.png"))); // NOI18N
        reload.setToolTipText("Reload");
        reload.setBorder(null);
        reload.setPreferredSize(new java.awt.Dimension(32, 32));
        reload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadActionPerformed(evt);
            }
        });

        jButton2.setBackground(null);
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/zoom-in (1).png"))); // NOI18N
        jButton2.setToolTipText("Zoom in");
        jButton2.setBorder(null);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(null);
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/zoom-out.png"))); // NOI18N
        jButton3.setToolTipText("Zoom out");
        jButton3.setBorder(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jTextField1.setToolTipText("Enter address");
        jTextField1.setFocusCycleRoot(true);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/logo-small.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Consolas", 0, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 255));
        jLabel2.setText("Catfish");

        jButton5.setBackground(null);
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/info.png"))); // NOI18N
        jButton5.setToolTipText("Info");
        jButton5.setBorder(null);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setBackground(null);
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/book-alt.png"))); // NOI18N
        jButton6.setToolTipText("Licence");
        jButton6.setBorder(null);
        jButton6.setPreferredSize(new java.awt.Dimension(32, 32));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton4.setBackground(null);
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/plus-small.png"))); // NOI18N
        jButton4.setToolTipText("Add new tab");
        jButton4.setBorder(null);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton9.setBackground(null);
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/cat.png"))); // NOI18N
        jButton9.setToolTipText("CATSSS!!!1!");
        jButton9.setBorder(null);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        settingsButton.setBackground(null);
        settingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/settings (4).png"))); // NOI18N
        settingsButton.setToolTipText("Settings");
        settingsButton.setBorder(null);
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });

        bm1.setBackground(null);
        bm1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm1.setText("Google");
        bm1.setToolTipText("Right click to change the bookmark settings");
        bm1.setBorder(null);
        bm1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm1ActionPerformed(evt);
            }
        });

        bm2.setBackground(null);
        bm2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm2.setText("Gmail");
        bm2.setToolTipText("Right click to change the bookmark settings");
        bm2.setBorder(null);
        bm2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm2ActionPerformed(evt);
            }
        });

        bm3.setBackground(null);
        bm3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm3.setText("Youtube");
        bm3.setToolTipText("Right click to change the bookmark settings");
        bm3.setBorder(null);
        bm3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm3ActionPerformed(evt);
            }
        });

        bm4.setBackground(null);
        bm4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm4.setText("Reddit");
        bm4.setToolTipText("Right click to change the bookmark settings");
        bm4.setBorder(null);
        bm4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm4ActionPerformed(evt);
            }
        });

        bm5.setBackground(null);
        bm5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm5.setText("CatCo");
        bm5.setToolTipText("Right click to change the bookmark settings");
        bm5.setBorder(null);
        bm5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm5ActionPerformed(evt);
            }
        });

        bm7.setBackground(null);
        bm7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm7.setText("7");
        bm7.setToolTipText("Right click to change the bookmark settings");
        bm7.setBorder(null);
        bm7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm7ActionPerformed(evt);
            }
        });

        bm6.setBackground(null);
        bm6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm6.setText("Discord");
        bm6.setToolTipText("Right click to change the bookmark settings");
        bm6.setBorder(null);
        bm6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm6ActionPerformed(evt);
            }
        });

        bm8.setBackground(null);
        bm8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm8.setText("8");
        bm8.setToolTipText("Right click to change the bookmark settings");
        bm8.setBorder(null);
        bm8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm8ActionPerformed(evt);
            }
        });

        bm9.setBackground(null);
        bm9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/bookmark.png"))); // NOI18N
        bm9.setText("9");
        bm9.setToolTipText("Right click to change the bookmark settings");
        bm9.setBorder(null);
        bm9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bm9ActionPerformed(evt);
            }
        });

        bmACSTB.setBackground(null);
        bmACSTB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/cat/CatfishBrowser/assets/wishlist-star.png"))); // NOI18N
        bmACSTB.setText("Add current site to bookmark");
        bmACSTB.setToolTipText("");
        bmACSTB.setBorder(null);
        bmACSTB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bmACSTBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2)))
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(back, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(forward, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(reload, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(settingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(bm1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bm2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bm3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bm4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bm5))
                                    .addComponent(bmACSTB))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(loadingBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(bm6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bm7)
                                        .addGap(5, 5, 5)
                                        .addComponent(bm8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bm9)))))))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton7))
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(back, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(forward, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reload, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loadingBar, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bmACSTB, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(settingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(bm1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bm9, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>                        

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        getCurrentBrowser().loadURL("catfish://newtab");
    }                                        

    private void backActionPerformed(java.awt.event.ActionEvent evt) {                                     
        getCurrentBrowser().goBack();
    }                                    

    private void forwardActionPerformed(java.awt.event.ActionEvent evt) {                                        
        getCurrentBrowser().goForward();
    }                                       

    private void reloadActionPerformed(java.awt.event.ActionEvent evt) {                                       
        getCurrentBrowser().reload();
    }                                      

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        CefBrowser browser = getCurrentBrowser();
        browser.setZoomLevel(++zoomLevel_);
    }                                        

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        CefBrowser browser = getCurrentBrowser();
        browser.setZoomLevel(--zoomLevel_);
    }                                        

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        JOptionPane.showMessageDialog(
            this,
            "<html>" +
                "<center>" +
                "<h1>Catfish Browser Project</h1>" +
                "<h2>Version 0.4</h2>" +
                "Created by LynxJr<br>CAT 2025<br>" +
                "Runs on Chromium (CEF)<br>" +
                "<br>" +
                "<i>JCEF ca49ada + CEF 135.0.20+ge7de5c3+chromium-135.0.7049.85<i><br>" +
                "<h2>Protocols</h2>" +
                "catfish://newtab opens a custom new tab page.<br>" +
                "catfish://cats displays random cat images every reload. (cataas.com/cat)" +
                "</center>" +
            "</html>",
            "About",
            JOptionPane.PLAIN_MESSAGE
        );
    }                                        

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        String licenseText = "The Catfish Browser Project is a browser based of Chromium.\nCopyright (C) 2025  CAT\n\nThis program is free software: you can redistribute it and/or modify\nit under the terms of the GNU General Public License as published by\nthe Free Software Foundation, either version 3 of the License, or\n(at your option) any later version.\n\nThis program is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\nGNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License\nalong with this program.  If not, see <https://www.gnu.org/licenses/>.\n\nThis program comes with ABSOLUTELY NO WARRANTY.\nThis is free software, and you are welcome to redistribute it\nunder certain conditions.";
        JTextArea textArea = new JTextArea(licenseText);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "License", 1);
    }                                        

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        getCurrentBrowser().loadURL("catfish://cats");
    }                                        

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {                                            
        // TODO add your handling code here:
    }                                           

    private void jTextField1MouseClicked(java.awt.event.MouseEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        addNewTab("catfish://newtab", false, false);
    }                                        

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        Settings st = new Settings();
        st.setVisible(true);
    }                                              

    private void bm1ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm2ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm3ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm4ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm5ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm7ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm6ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm8ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bm9ActionPerformed(java.awt.event.ActionEvent evt) {                                    
        // TODO add your handling code here:
    }                                   

    private void bmACSTBActionPerformed(java.awt.event.ActionEvent evt) {                                        
    // Get current URL from address bar
    String currentUrl = jTextField1.getText().trim();
    if (currentUrl.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No URL in address bar", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Check if trying to bookmark default pages
    if (isDefaultBookmark(currentUrl)) {
        JOptionPane.showMessageDialog(this, 
            "This links bookmark already exsists.",
                null,
                JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Get current page title (or use URL as fallback)
    String titleToUse = currentPageTitle;
    if (titleToUse == null || titleToUse.isEmpty()) {
        titleToUse = currentUrl;
    }
    
    // Shorten the title if it's too long
    titleToUse = shortenTitle(titleToUse);
    
    // Show the replace dialog
    showBookmarkReplaceDialog(currentUrl, titleToUse);
    }                                       

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws javax.swing.UnsupportedLookAndFeelException
     */
    public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        ThemeUtil.applySavedTheme();
        
        //FlatNordIJTheme.setup();
        
        
        //</editor-fold>
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            String loadpath = "catfish://newtab";
            new Catfish(loadpath, false, false).setVisible(true);
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton back;
    private javax.swing.JButton bm1;
    private javax.swing.JButton bm2;
    private javax.swing.JButton bm3;
    private javax.swing.JButton bm4;
    private javax.swing.JButton bm5;
    private javax.swing.JButton bm6;
    private javax.swing.JButton bm7;
    private javax.swing.JButton bm8;
    private javax.swing.JButton bm9;
    private javax.swing.JButton bmACSTB;
    private javax.swing.JButton forward;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JProgressBar loadingBar;
    private javax.swing.JButton reload;
    private javax.swing.JButton settingsButton;
    // End of variables declaration                   

    @Override
    public void windowOpened(WindowEvent we) {
    }

    @Override
    public void windowClosing(WindowEvent we) {
        
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }
}
