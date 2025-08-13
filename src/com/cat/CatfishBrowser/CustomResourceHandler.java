/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cat.CatfishBrowser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

/**
 *
 * @author LynxJr
 */
public class CustomResourceHandler implements CefResourceHandler {
    private byte[] data;
    private int offset = 0;
    private String mimeType = "text/html";

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        String url = request.getURL();
        
        try {
            if (url.equals("catfish://newtab")) {
                data = readResourceFile("/com/cat/CatfishBrowser/assets/newtab.html");
                mimeType = "text/html";
                callback.Continue();
                return true;
            }
            else if (url.equals("catfish://cats")) {
                String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Cats!</title>\n" +
                    "    <style>\n" +
                    "        html, body {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            height: 100%;\n" +
                    "            width: 100%;\n" +
                    "            background-color: #000;\n" +
                    "            overflow: hidden;\n" +
                    "        }\n" +
                    "        #content-wrapper {\n" +
                    "            position: absolute;\n" +
                    "            top: 0;\n" +
                    "            left: 0;\n" +
                    "            right: 0;\n" +
                    "            bottom: 0;\n" +
                    "            display: flex;\n" +
                    "            justify-content: center;\n" +
                    "            align-items: center;\n" +
                    "            background-color: #000;\n" +
                    "        }\n" +
                    "        #content-iframe {\n" +
                    "            border: none;\n" +
                    "            background-color: #000;\n" +
                    "            min-width: 100%;\n" +
                    "            min-height: 100%;\n" +
                    "            transform-origin: center center;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "    <script>\n" +
                    "        function resizeIframe() {\n" +
                    "            const wrapper = document.getElementById('content-wrapper');\n" +
                    "            const iframe = document.getElementById('content-iframe');\n" +
                    "            const containerRatio = wrapper.clientWidth / wrapper.clientHeight;\n" +
                    "            \n" +
                    "            // Set iframe dimensions based on content aspect ratio\n" +
                    "            iframe.style.width = '100%';\n" +
                    "            iframe.style.height = '100%';\n" +
                    "            \n" +
                    "            // Center the iframe with black bars if needed\n" +
                    "            iframe.style.objectFit = 'contain';\n" +
                    "            \n" +
                    "            // Handle zoom changes\n" +
                    "            window.addEventListener('resize', function() {\n" +
                    "                iframe.style.transform = 'scale(' + (window.innerWidth / iframe.scrollWidth) + ')';\n" +
                    "            });\n" +
                    "        }\n" +
                    "        \n" +
                    "        window.onload = function() {\n" +
                    "            resizeIframe();\n" +
                    "            history.replaceState({}, '', 'catfish://cats');\n" +
                    "            window.addEventListener('beforeunload', function() {\n" +
                    "                history.replaceState({}, '', 'catfish://cats');\n" +
                    "            });\n" +
                    "        };\n" +
                    "    </script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div id=\"content-wrapper\">\n" +
                    "        <iframe id=\"content-iframe\" src=\"https://cataas.com/cat\"></iframe>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

                data = html.getBytes(StandardCharsets.UTF_8);
                mimeType = "text/html";
                callback.Continue();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
        response.setMimeType(mimeType);
        response.setStatus(200);
        responseLength.set(data != null ? data.length : 0);
    }

    @Override
    public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) {
        if (data == null || offset >= data.length) {
            bytesRead.set(0);
            return false;
        }
        
        int transfer = Math.min(bytesToRead, data.length - offset);
        System.arraycopy(data, offset, dataOut, 0, transfer);
        offset += transfer;
        bytesRead.set(transfer);
        return true;
    }

    @Override
    public void cancel() {
        offset = 0;
        data = null;
    }
    
    private byte[] readResourceFile(String path) throws IOException {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) throw new FileNotFoundException(path);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(temp)) != -1) {
            buffer.write(temp, 0, bytesRead);
        }
        return buffer.toByteArray();
    }
}
