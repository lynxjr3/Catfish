/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish.dialog;

import org.cef.browser.CefBrowser;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;

/**
 *
 * @author lynxjr
 */
@SuppressWarnings("serial")
public class DevToolsDialog extends JDialog{
    private final CefBrowser browser_;
    public DevToolsDialog(Frame owner, String title, CefBrowser browser) {
        this(owner, title, browser, null);
    }

    public DevToolsDialog(Frame owner, String title, CefBrowser browser, Point inspectAt) {
        super(owner, title, false);
        browser_ = browser;

        setLayout(new BorderLayout());
        setSize(50, 50);
        setLocation(owner.getLocation().x + 20, owner.getLocation().y + 20);

        browser.openDevTools(inspectAt);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void dispose() {
        browser_.closeDevTools();
        super.dispose();
    }
}
