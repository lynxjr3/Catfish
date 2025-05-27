/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish.handler;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.network.CefRequest;

/**
 *
 * @author lynxjr
 */
public class SearchSchemeHandler extends CefResourceHandlerAdapter{
    public static final String scheme = "search";
    public static final String domain = "";

    private final CefBrowser browser_;

    public SearchSchemeHandler(CefBrowser browser) {
        browser_ = browser;
    }

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        // cut away "scheme://"
        String requestUrl = request.getURL();
        String newUrl = requestUrl.substring(scheme.length() + 3);
        // cut away a trailing "/" if any
        if (newUrl.indexOf('/') == newUrl.length() - 1) {
            newUrl = newUrl.substring(0, newUrl.length() - 1);
        }
        newUrl = "http://www.google.com/#q=" + newUrl;

        CefRequest newRequest = CefRequest.create();
        if (newRequest != null) {
            newRequest.setMethod("GET");
            newRequest.setURL(newUrl);
            newRequest.setFirstPartyForCookies(newUrl);
            browser_.loadRequest(newRequest);
        }
        return false;
    }
}
