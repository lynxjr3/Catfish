package catfish;

import catfish.dialog.DownloadDialog;
import catfish.handler.*;
import catfish.ui.BrowserFrame;
import catfish.ui.ControlPanel;
import catfish.ui.MenuBar;
import catfish.ui.StatusPanel;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import org.cef.CefApp;
import org.cef.CefApp.CefVersion;
import org.cef.CefBrowserSettings;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefCookieManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.*;

public class Catfish extends BrowserFrame {
    private static final long serialVersionUID = -2295538706810864538L;
    public static void main(String[] args) {
        FlatCarbonIJTheme.setup();
        // Perform startup initialization on platforms that require it.
        if (!CefApp.startup(args)) {
            System.out.println("Startup initialization failed!");
            return;
        }

        // OSR mode is enabled by default on Linux.
        // and disabled by default on Windows and Mac OS X.
        boolean osrEnabledArg = false;
        boolean transparentPaintingEnabledArg = false;
        boolean createImmediately = false;
        int windowless_frame_rate = 0;
        for (String arg : args) {
            arg = arg.toLowerCase();
            if (arg.equals("--off-screen-rendering-enabled")) {
                osrEnabledArg = true;
            } else if (arg.equals("--transparent-painting-enabled")) {
                transparentPaintingEnabledArg = true;
            } else if (arg.equals("--create-immediately")) {
                createImmediately = true;
            } else if (arg.equals("--windowless-frame-rate-60")) {
                windowless_frame_rate = 60;
            }
        }

        System.out.println("Offscreen rendering " + (osrEnabledArg ? "enabled" : "disabled"));

        // MainFrame keeps all the knowledge to display the embedded browser
        // frame.
        final Catfish frame = new Catfish(osrEnabledArg, transparentPaintingEnabledArg,
                createImmediately, windowless_frame_rate, args);
        frame.setSize(800, 600);
        frame.setVisible(true);

        if (osrEnabledArg && windowless_frame_rate != 0) {
            frame.getBrowser().getWindowlessFrameRate().thenAccept(
                    framerate -> System.out.println("Framerate is:" + framerate));

            frame.getBrowser().setWindowlessFrameRate(2);
            frame.getBrowser().getWindowlessFrameRate().thenAccept(
                    framerate -> System.out.println("Framerate is:" + framerate));

            frame.getBrowser().setWindowlessFrameRate(windowless_frame_rate);
        }
    }

    private final CefClient client_;
    private String errorMsg_ = "";
    private ControlPanel control_pane_;
    private StatusPanel status_panel_;
    private boolean browserFocus_ = true;
    private boolean osr_enabled_;
    private boolean transparent_painting_enabled_;
    private JPanel contentPanel_;
    private JFrame fullscreenFrame_;

    public Catfish(boolean osrEnabled, boolean transparentPaintingEnabled,
                   boolean createImmediately, int windowless_frame_rate, String[] args) {
        this.osr_enabled_ = osrEnabled;
        this.transparent_painting_enabled_ = transparentPaintingEnabled;

        CefApp myApp;
        if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {
            // 1) CefApp is the entry point for JCEF. You can pass
            //    application arguments to it, if you want to handle any
            //    chromium or CEF related switches/attributes in
            //    the native world.
            CefSettings settings = new CefSettings();
            settings.windowless_rendering_enabled = osrEnabled;
            // try to load URL "about:blank" to see the background color
            settings.background_color = settings.new ColorType(100, 255, 242, 211);
            myApp = CefApp.getInstance(args, settings);

            CefVersion version = myApp.getVersion();
            System.out.println("Using:\n" + version);

            //    We're registering our own AppHandler because we want to
            //    add an own schemes (search:// and client://) and its corresponding
            //    protocol handlers. So if you enter "search:something on the web", your
            //    search request "something on the web" is forwarded to www.google.com
            CefApp.addAppHandler(new AppHandler(args));
        } else {
            myApp = CefApp.getInstance();
        }

        //    By calling the method createClient() the native part
        //    of JCEF/CEF will be initialized and an  instance of
        //    CefClient will be created. You can create one to many
        //    instances of CefClient.
        client_ = myApp.createClient();

        // 2) You have the ability to pass different handlers to your
        //    instance of CefClient. Each handler is responsible to
        //    deal with different informations (e.g. keyboard input).
        //
        //    For each handler (with more than one method) adapter
        //    classes exists. So you don't need to override methods
        //    you're not interested in.
        DownloadDialog downloadDialog = new DownloadDialog(this);
        client_.addContextMenuHandler(new ContextMenuHandler(this));
        client_.addDownloadHandler(downloadDialog);
        client_.addDragHandler(new DragHandler());
        client_.addJSDialogHandler(new JSDialogHandler());
        client_.addKeyboardHandler(new KeyboardHandler());
        client_.addRequestHandler(new RequestHandler(this));

        //    Beside the normal handler instances, we're registering a MessageRouter
        //    as well. That gives us the opportunity to reply to JavaScript method
        //    calls (JavaScript binding). We're using the default configuration, so
        //    that the JavaScript binding methods "cefQuery" and "cefQueryCancel"
        //    are used.
        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new MessageRouterHandler(), true);
        msgRouter.addHandler(new MessageRouterHandlerEx(client_), false);
        client_.addMessageRouter(msgRouter);

        // 2.1) We're overriding CefDisplayHandler as nested anonymous class
        //      to update our address-field, the title of the panel as well
        //      as for updating the status-bar on the bottom of the browser
        client_.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                control_pane_.setAddress(browser, url);
            }
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                setTitle("Catfish - " + title);
            }
            @Override
            public void onStatusMessage(CefBrowser browser, String value) {
                status_panel_.setStatusText(value);
            }
            @Override
            public void onFullscreenModeChange(CefBrowser browser, boolean fullscreen) {
                setBrowserFullscreen(fullscreen);
            }
        });

        // 2.2) To disable/enable navigation buttons and to display a prgress bar
        //      which indicates the load state of our website, we're overloading
        //      the CefLoadHandler as nested anonymous class. Beside this, the
        //      load handler is responsible to deal with (load) errors as well.
        //      For example if you navigate to a URL which does not exist, the
        //      browser will show up an error message.
        client_.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                             boolean canGoBack, boolean canGoForward) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        control_pane_.update(browser, isLoading, canGoBack, canGoForward);
                        status_panel_.setIsInProgress(isLoading);

                        if (!isLoading && !errorMsg_.isEmpty()) {
                            browser.loadURL(DataUri.create("text/html", errorMsg_));
                            errorMsg_ = "";
                        }
                    }
                });
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode,
                                    String errorText, String failedUrl) {
                if (errorCode != ErrorCode.ERR_NONE && errorCode != ErrorCode.ERR_ABORTED
                        && frame == browser.getMainFrame()) {
                    errorMsg_ = "<html><head>";
                    errorMsg_ += "<title>Error while loading</title>";
                    errorMsg_ += "</head><body>";
                    errorMsg_ += "<h1>" + errorCode + "</h1>";
                    errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
                    errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
                    errorMsg_ += "</body></html>";
                    browser.stopLoad();
                }
            }
        });

        CefBrowserSettings browserSettings = new CefBrowserSettings();
        browserSettings.windowless_frame_rate = windowless_frame_rate;

        // Create the browser.
        CefBrowser browser = client_.createBrowser("http://www.google.com", osrEnabled,
                transparentPaintingEnabled, null, browserSettings);
        setBrowser(browser);

        // Set up the UI for this example implementation.
        contentPanel_ = createContentPanel();
        getContentPane().add(contentPanel_, BorderLayout.CENTER);

        // Clear focus from the browser when the address field gains focus.
        control_pane_.getAddressField().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!browserFocus_) return;
                browserFocus_ = false;
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                control_pane_.getAddressField().requestFocus();
            }
        });

        // Clear focus from the address field when the browser gains focus.
        client_.addFocusHandler(new CefFocusHandlerAdapter() {
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (browserFocus_) return;
                browserFocus_ = true;
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                browser.setFocus(true);
            }

            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                browserFocus_ = false;
            }
        });

        if (createImmediately) browser.createImmediately();

        // Add the browser to the UI.
        contentPanel_.add(getBrowser().getUIComponent(), BorderLayout.CENTER);

        MenuBar menuBar = new MenuBar(
                this, browser, control_pane_, downloadDialog, CefCookieManager.getGlobalManager());

        menuBar.addBookmark("Google", "https://google.com");
        setJMenuBar(menuBar);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        control_pane_ = new ControlPanel(getBrowser());
        status_panel_ = new StatusPanel();
        contentPanel.add(control_pane_, BorderLayout.NORTH);
        contentPanel.add(status_panel_, BorderLayout.SOUTH);
        return contentPanel;
    }

    public boolean isOsrEnabled() {
        return osr_enabled_;
    }

    public boolean isTransparentPaintingEnabled() {
        return transparent_painting_enabled_;
    }

    public void setBrowserFullscreen(boolean fullscreen) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Component browserUI = getBrowser().getUIComponent();
                if (fullscreen) {
                    if (fullscreenFrame_ == null) {
                        fullscreenFrame_ = new JFrame();
                        fullscreenFrame_.setUndecorated(true);
                        fullscreenFrame_.setResizable(true);
                    }
                    GraphicsConfiguration gc = Catfish.this.getGraphicsConfiguration();
                    fullscreenFrame_.setBounds(gc.getBounds());
                    gc.getDevice().setFullScreenWindow(fullscreenFrame_);

                    contentPanel_.remove(browserUI);
                    fullscreenFrame_.add(browserUI);
                    fullscreenFrame_.setVisible(true);
                    fullscreenFrame_.validate();
                } else {
                    fullscreenFrame_.remove(browserUI);
                    fullscreenFrame_.setVisible(false);
                    contentPanel_.add(browserUI, BorderLayout.CENTER);
                    contentPanel_.validate();
                }
            }
        });
    }
}