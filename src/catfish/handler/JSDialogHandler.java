/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish.handler;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandlerAdapter;
import org.cef.misc.BoolRef;

/**
 *
 * @author lynxjr
 */
public class JSDialogHandler extends CefJSDialogHandlerAdapter{
    @Override
    public boolean onJSDialog(CefBrowser browser, String origin_url, JSDialogType dialog_type,
            String message_text, String default_prompt_text, CefJSDialogCallback callback,
            BoolRef suppress_message) {
        if (message_text.equalsIgnoreCase("Never displayed")) {
            suppress_message.set(true);
            System.out.println(
                    "The " + dialog_type + " from origin \"" + origin_url + "\" was suppressed.");
            System.out.println(
                    "   The content of the suppressed dialog was: \"" + message_text + "\"");
        }
        return false;
    }
}
