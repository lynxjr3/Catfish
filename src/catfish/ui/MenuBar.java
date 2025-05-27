/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish.ui;

import catfish.Catfish;
import catfish.dialog.*;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefDevToolsClient;
import org.cef.callback.CefPdfPrintCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.misc.CefPdfPrintSettings;
import org.cef.network.CefCookieManager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

import javax.swing.*;

/**
 *
 * @author lynxjr
 */
@SuppressWarnings("serial")
public class MenuBar extends JMenuBar{
    class SaveAs implements CefStringVisitor {
        private PrintWriter fileWriter_;

        public SaveAs(String fName) throws FileNotFoundException, UnsupportedEncodingException {
            fileWriter_ = new PrintWriter(fName, "UTF-8");
        }

        @Override
        public void visit(String string) {
            fileWriter_.write(string);
            fileWriter_.close();
        }
    }

    private final Catfish owner_;
    private final CefBrowser browser_;
    private String last_selected_file_ = "";
    private final JMenu bookmarkMenu_;
    private final ControlPanel control_pane_;
    private final DownloadDialog downloadDialog_;
    private final CefCookieManager cookieManager_;
    private boolean reparentPending_ = false;
    private CefDevToolsClient devToolsClient_;

    public MenuBar(Catfish owner, CefBrowser browser, ControlPanel control_pane,
            DownloadDialog downloadDialog, CefCookieManager cookieManager) {
        owner_ = owner;
        browser_ = browser;
        control_pane_ = control_pane;
        downloadDialog_ = downloadDialog;
        cookieManager_ = cookieManager;

        setEnabled(browser_ != null);

        JMenu fileMenu = new JMenu("File");

//        JMenuItem openFileItem = new JMenuItem("Open file...");
//        openFileItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent arg0) {
//                JFileChooser fc = new JFileChooser(new File(last_selected_file_));
//                // Show open dialog; this method does not return until the dialog is closed.
//                fc.showOpenDialog(owner_);
//                File selectedFile = fc.getSelectedFile();
//                if (selectedFile != null) {
//                    last_selected_file_ = selectedFile.getAbsolutePath();
//                    browser_.loadURL("file:///" + selectedFile.getAbsolutePath());
//                }
//            }
//        });
//        fileMenu.add(openFileItem);
//
//        JMenuItem openFileDialog = new JMenuItem("Save as...");
//        openFileDialog.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                CefRunFileDialogCallback callback = new CefRunFileDialogCallback() {
//                    @Override
//                    public void onFileDialogDismissed(Vector<String> filePaths) {
//                        if (!filePaths.isEmpty()) {
//                            try {
//                                SaveAs saveContent = new SaveAs(filePaths.get(0));
//                                browser_.getSource(saveContent);
//                            } catch (FileNotFoundException | UnsupportedEncodingException e) {
//                                browser_.executeJavaScript("alert(\"Can't save file\");",
//                                        control_pane_.getAddress(), 0);
//                            }
//                        }
//                    }
//                };
//                browser_.runFileDialog(FileDialogMode.FILE_DIALOG_SAVE, owner_.getTitle(),
//                        "index.html", null, 0, callback);
//            }
//        });
//        fileMenu.add(openFileDialog);

        JMenuItem printItem = new JMenuItem("Print...");
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.print();
            }
        });
        fileMenu.add(printItem);

        JMenuItem printToPdfItem = new JMenuItem("Print to PDF");
        printToPdfItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.showSaveDialog(owner_);
                File selectedFile = fc.getSelectedFile();
                if (selectedFile != null) {
                    CefPdfPrintSettings pdfSettings = new CefPdfPrintSettings();
                    pdfSettings.display_header_footer = true;
                    // letter page size
                    pdfSettings.paper_width = 8.5;
                    pdfSettings.paper_height = 11;
                    browser.printToPDF(
                            selectedFile.getAbsolutePath(), pdfSettings, new CefPdfPrintCallback() {
                                @Override
                                public void onPdfPrintFinished(String path, boolean ok) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (ok) {
                                                JOptionPane.showMessageDialog(owner_,
                                                        "PDF saved to " + path, "Success",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                JOptionPane.showMessageDialog(owner_, "PDF failed",
                                                        "Failed", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    });
                                }
                            });
                }
            }
        });
        fileMenu.add(printToPdfItem);

        JMenuItem searchItem = new JMenuItem("Search...");
        searchItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SearchDialog(owner_, browser_).setVisible(true);
            }
        });
        fileMenu.add(searchItem);

        fileMenu.addSeparator();

        JMenuItem viewSource = new JMenuItem("View source");
        viewSource.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.viewSource();
            }
        });
        fileMenu.add(viewSource);

        JMenuItem getSource = new JMenuItem("Get source...");
        getSource.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ShowTextDialog visitor = new ShowTextDialog(
                        owner_, "Source of \"" + control_pane_.getAddress() + "\"");
                browser_.getSource(visitor);
            }
        });
        fileMenu.add(getSource);

        JMenuItem getText = new JMenuItem("Get text...");
        getText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ShowTextDialog visitor = new ShowTextDialog(
                        owner_, "Content of \"" + control_pane_.getAddress() + "\"");
                browser_.getText(visitor);
            }
        });
        fileMenu.add(getText);

        fileMenu.addSeparator();

        JMenuItem showDownloads = new JMenuItem("Show Downloads");
        showDownloads.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadDialog_.setVisible(true);
            }
        });
        fileMenu.add(showDownloads);

        JMenuItem showCookies = new JMenuItem("Show Cookies");
        showCookies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CookieManagerDialog cookieManager =
                        new CookieManagerDialog(owner_, "Cookie Manager", cookieManager_);
                cookieManager.setVisible(true);
            }
        });
        fileMenu.add(showCookies);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner_.dispatchEvent(new WindowEvent(owner_, WindowEvent.WINDOW_CLOSING));
            }
        });
        fileMenu.add(exitItem);

        bookmarkMenu_ = new JMenu("Bookmarks");

        JMenuItem addBookmarkItem = new JMenuItem("Add bookmark");
        addBookmarkItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBookmark(owner_.getTitle(), control_pane_.getAddress());
            }
        });
        bookmarkMenu_.add(addBookmarkItem);
        bookmarkMenu_.addSeparator();

        JMenu testMenu = new JMenu("Tools");
//
//        JMenuItem testJSItem = new JMenuItem("JavaScript alert");
//        testJSItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                browser_.executeJavaScript("alert('Hello World');", control_pane_.getAddress(), 1);
//            }
//        });
//        testMenu.add(testJSItem);
//
//        JMenuItem jsAlertItem = new JMenuItem("JavaScript alert (will be suppressed)");
//        jsAlertItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                browser_.executeJavaScript("alert('Never displayed');", "http://dontshow.me", 1);
//            }
//        });
//        testMenu.add(jsAlertItem);
//
//        JMenuItem testShowText = new JMenuItem("Show Text");
//        testShowText.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                browser_.loadURL(DataUri.create(
//                        "text/html", "<html><body><h1>Hello World</h1></body></html>"));
//            }
//        });
//        testMenu.add(testShowText);
//
//        JMenuItem showForm = new JMenuItem("RequestHandler Test");
//        showForm.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String form = "<html><head><title>RequestHandler test</title></head>";
//                form += "<body><h1>RequestHandler test</h1>";
//                form += "<form action=\"http://www.google.com/\" method=\"post\">";
//                form += "<input type=\"text\" name=\"searchFor\"/>";
//                form += "<input type=\"submit\"/><br/>";
//                form += "<input type=\"checkbox\" name=\"sendAsGet\"> Use GET instead of POST";
//                form += "<p>This form tries to send the content of the text field as HTTP-POST request to http://www.google.com.</p>";
//                form += "<h2>Testcase 1</h2>";
//                form += "Try to enter the word <b>\"ignore\"</b> into the text field and press \"submit\".<br />";
//                form += "The request will be rejected by the application.";
//                form += "<p>See implementation of <u>tests.RequestHandler.onBeforeBrowse(CefBrowser, CefRequest, boolean)</u> for details</p>";
//                form += "<h2>Testcase 2</h2>";
//                form += "Due Google doesn't allow the POST method, the server replies with a 405 error.</br>";
//                form += "If you activate the checkbox \"Use GET instead of POST\", the application will change the POST request into a GET request.";
//                form += "<p>See implementation of <u>tests.RequestHandler.onBeforeResourceLoad(CefBrowser, CefRequest)</u> for details</p>";
//                form += "</form>";
//                form += "</body></html>";
//                browser_.loadURL(DataUri.create("text/html", form));
//            }
//        });
//        testMenu.add(showForm);
//
//        JMenuItem httpRequest = new JMenuItem("Manual HTTP request");
//        httpRequest.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String searchFor = JOptionPane.showInputDialog(owner_, "Search on google:");
//                if (searchFor != null && !searchFor.isEmpty()) {
//                    CefRequest myRequest = CefRequest.create();
//                    myRequest.setMethod("GET");
//                    myRequest.setURL("http://www.google.com/#q=" + searchFor);
//                    myRequest.setFirstPartyForCookies("http://www.google.com/#q=" + searchFor);
//                    browser_.loadRequest(myRequest);
//                }
//            }
//        });
//        testMenu.add(httpRequest);
//
//        JMenuItem showInfo = new JMenuItem("Show Info");
//        showInfo.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String info = "<html><head><title>Browser status</title></head>";
//                info += "<body><h1>Browser status</h1><table border=\"0\">";
//                info += "<tr><td>CanGoBack</td><td>" + browser_.canGoBack() + "</td></tr>";
//                info += "<tr><td>CanGoForward</td><td>" + browser_.canGoForward() + "</td></tr>";
//                info += "<tr><td>IsLoading</td><td>" + browser_.isLoading() + "</td></tr>";
//                info += "<tr><td>isPopup</td><td>" + browser_.isPopup() + "</td></tr>";
//                info += "<tr><td>hasDocument</td><td>" + browser_.hasDocument() + "</td></tr>";
//                info += "<tr><td>Url</td><td>" + browser_.getURL() + "</td></tr>";
//                info += "<tr><td>Zoom-Level</td><td>" + browser_.getZoomLevel() + "</td></tr>";
//                info += "</table></body></html>";
//                String js = "var x=window.open(); x.document.open(); x.document.write('" + info
//                        + "'); x.document.close();";
//                browser_.executeJavaScript(js, "", 0);
//            }
//        });
//        testMenu.add(showInfo);
//
        final JMenuItem showDevTools = new JMenuItem("Show DevTools");
        showDevTools.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DevToolsDialog devToolsDlg = new DevToolsDialog(owner_, "DEV Tools", browser_);
                devToolsDlg.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent e) {
                        showDevTools.setEnabled(true);
                    }
                });
                devToolsDlg.setVisible(true);
                showDevTools.setEnabled(false);
            }
        });
        testMenu.add(showDevTools);
//
//        JMenu devToolsProtocolMenu = new JMenu("DevTools Protocol");
//        JMenuItem autoDarkMode = devToolsProtocolMenu.add(new JCheckBoxMenuItem("Auto Dark Mode"));
//        autoDarkMode.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Toggle the auto dark mode override
//                String params = String.format("{ \"enabled\": %s }", autoDarkMode.isSelected());
//                executeDevToolsMethod("Emulation.setAutoDarkModeOverride", params);
//            }
//        });
//        JMenuItem checkContrast = devToolsProtocolMenu.add(new JMenuItem("Check Contrast"));
//        checkContrast.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Check contrast, which usually triggers a series of Audits.issueAdded events
//                executeDevToolsMethod("Audits.checkContrast");
//            }
//        });
//        JMenuItem enableCSS = devToolsProtocolMenu.add(new JMenuItem("Enable CSS Agent"));
//        enableCSS.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Enable the CSS agent, which usually triggers a series of CSS.styleSheetAdded
//                // events. We can only enable the CSS agent if the DOM agent is enabled first, so we
//                // need to chain the two commands.
//                executeDevToolsMethod("DOM.enable")
//                        .thenCompose(unused -> executeDevToolsMethod("CSS.enable"));
//            }
//        });
//        testMenu.add(devToolsProtocolMenu);
//
//        JMenuItem testURLRequest = new JMenuItem("URL Request");
//        testURLRequest.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                UrlRequestDialog dlg = new UrlRequestDialog(owner_, "URL Request Test");
//                dlg.setVisible(true);
//            }
//        });
//        testMenu.add(testURLRequest);
//
//        JMenuItem reparent = new JMenuItem("Reparent");
//        reparent.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final BrowserFrame newFrame = new BrowserFrame("New Window");
//                newFrame.setLayout(new BorderLayout());
//                final JButton reparentButton = new JButton("Reparent <");
//                reparentButton.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (reparentPending_) return;
//                        reparentPending_ = true;
//
//                        if (reparentButton.getText().equals("Reparent <")) {
//                            owner_.removeBrowser(new Runnable() {
//                                public void run() {
//                                    newFrame.add(browser_.getUIComponent(), BorderLayout.CENTER);
//                                    newFrame.setBrowser(browser_);
//                                    reparentButton.setText("Reparent >");
//                                    reparentPending_ = false;
//                                }
//                            });
//                        } else {
//                            newFrame.removeBrowser(new Runnable() {
//                                public void run() {
//                                    JRootPane rootPane = (JRootPane) owner_.getComponent(0);
//                                    Container container = rootPane.getContentPane();
//                                    JPanel panel = (JPanel) container.getComponent(0);
//                                    panel.add(browser_.getUIComponent());
//                                    owner_.setBrowser(browser_);
//                                    owner_.revalidate();
//                                    reparentButton.setText("Reparent <");
//                                    reparentPending_ = false;
//                                }
//                            });
//                        }
//                    }
//                });
//                newFrame.add(reparentButton, BorderLayout.NORTH);
//                newFrame.setSize(400, 400);
//                newFrame.setVisible(true);
//            }
//        });
//        testMenu.add(reparent);
//
//        JMenuItem newwindow = new JMenuItem("New window");
//        newwindow.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final Catfish frame = new Catfish(OS.isLinux(), false, false, 0, null);
//                frame.setSize(800, 600);
//                frame.setVisible(true);
//            }
//        });
//        testMenu.add(newwindow);
//
//
//
//        JMenuItem screenshotSync = new JMenuItem("Screenshot (on AWT thread, native res)");
//        screenshotSync.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                long start = System.nanoTime();
//                CompletableFuture<BufferedImage> shot = browser.createScreenshot(true);
//                System.out.println("Took screenshot from the AWT event thread in "
//                        + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " msecs");
//                try {
//                    displayScreenshot(shot.get());
//                } catch (InterruptedException | ExecutionException exc) {
//                    // cannot happen, future is already resolved in this case
//                }
//            }
//        });
//        screenshotSync.setEnabled(owner.isOsrEnabled());
//        testMenu.add(screenshotSync);
//
//        JMenuItem screenshotSyncScaled = new JMenuItem("Screenshot (on AWT thread, scaled)");
//        screenshotSyncScaled.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                long start = System.nanoTime();
//                CompletableFuture<BufferedImage> shot = browser.createScreenshot(false);
//                System.out.println("Took screenshot from the AWT event thread in "
//                        + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " msecs");
//                try {
//                    displayScreenshot(shot.get());
//                } catch (InterruptedException | ExecutionException exc) {
//                    // cannot happen, future is already resolved in this case
//                }
//            }
//        });
//        screenshotSyncScaled.setEnabled(owner.isOsrEnabled());
//        testMenu.add(screenshotSyncScaled);
//
//        JMenuItem screenshotAsync = new JMenuItem("Screenshot (from other thread, scaled)");
//        screenshotAsync.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                long start = System.nanoTime();
//                CompletableFuture<BufferedImage> shot = browser.createScreenshot(false);
//                shot.thenAccept((image) -> {
//                    System.out.println("Took screenshot asynchronously in "
//                            + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " msecs");
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            displayScreenshot(image);
//                        }
//                    });
//                });
//            }
//        });
//        screenshotAsync.setEnabled(owner.isOsrEnabled());
//        testMenu.add(screenshotAsync);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(owner_,
                        "<html>" +
                                "<h1>Catfish Browser Project</h1>" +
                                "<br>" +
                                "<h2>Version 0.3</h2>" +
                                "<br>Created by LynxJr" +
                                "<br>CAT 2025" +
                                "<br>Runs on Chromium (CEF)</html>",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(aboutItem);

        JMenuItem licenceItem = new JMenuItem("Licence");
        licenceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String licenseText =
                        "The Catfish Browser Project is a browser based of Chromium.\n" +
                                "Copyright (C) 2025  LynxJr\n\n" +
                                "This program is free software: you can redistribute it and/or modify\n" +
                                "it under the terms of the GNU General Public License as published by\n" +
                                "the Free Software Foundation, either version 3 of the License, or\n" +
                                "(at your option) any later version.\n\n" +
                                "This program is distributed in the hope that it will be useful,\n" +
                                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                                "GNU General Public License for more details.\n\n" +
                                "You should have received a copy of the GNU General Public License\n" +
                                "along with this program.  If not, see <https://www.gnu.org/licenses/>.\n\n" +
                                "This program comes with ABSOLUTELY NO WARRANTY.\n" +
                                "This is free software, and you are welcome to redistribute it\n" +
                                "under certain conditions.";

                JTextArea textArea = new JTextArea(licenseText);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                textArea.setCaretPosition(0); // scroll to top by default

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 300));

                JOptionPane.showMessageDialog(owner_,
                        scrollPane,
                        "License",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(licenceItem);

        add(fileMenu);
        add(bookmarkMenu_);
        add(testMenu);
        add(helpMenu);
    }

    public void addBookmark(String name, String URL) {
        if (bookmarkMenu_ == null) return;

        // Test if the bookmark already exists. If yes, update URL
        Component[] entries = bookmarkMenu_.getMenuComponents();
        for (Component itemEntry : entries) {
            if (!(itemEntry instanceof JMenuItem)) continue;

            JMenuItem item = (JMenuItem) itemEntry;
            if (item.getText().equals(name)) {
                item.setActionCommand(URL);
                return;
            }
        }

        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(URL);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.loadURL(e.getActionCommand());
            }
        });
        bookmarkMenu_.add(menuItem);
        validate();
    }

    private void displayScreenshot(BufferedImage aScreenshot) {
        JFrame frame = new JFrame("Screenshot");
        ImageIcon image = new ImageIcon();
        image.setImage(aScreenshot);
        frame.setLayout(new FlowLayout());
        JLabel label = new JLabel(image);
        label.setPreferredSize(new Dimension(aScreenshot.getWidth(), aScreenshot.getHeight()));
        frame.add(label);
        frame.setVisible(true);
        frame.pack();
    }

    private CompletableFuture<String> executeDevToolsMethod(String methodName) {
        return executeDevToolsMethod(methodName, null);
    }

    private CompletableFuture<String> executeDevToolsMethod(
            String methodName, String paramsAsJson) {
        if (devToolsClient_ == null) {
            devToolsClient_ = browser_.getDevToolsClient();
            devToolsClient_.addEventListener(
                    (method, json) -> System.out.println("CDP event " + method + ": " + json));
        }

        return devToolsClient_.executeDevToolsMethod(methodName, paramsAsJson)
                .handle((error, json) -> {
                    System.out.println(
                            "CDP result of " + methodName + ": " + (error != null ? error : json));
                    return null;
                });
    }



    public void addBookmarkSeparator() {
        bookmarkMenu_.addSeparator();
    }
}
