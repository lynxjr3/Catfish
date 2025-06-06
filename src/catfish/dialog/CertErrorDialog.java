/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish.dialog;
import org.cef.callback.CefCallback;
import org.cef.handler.CefLoadHandler.ErrorCode;

import java.awt.Frame;

import javax.swing.JOptionPane;

/**
 *
 * @author lynxjr
 */
public class CertErrorDialog implements Runnable{
    private final Frame owner_;
    private final ErrorCode cert_error_;
    private final String request_url_;
    private final CefCallback callback_;

    public CertErrorDialog(
            Frame owner, ErrorCode cert_error, String request_url, CefCallback callback) {
        owner_ = owner;
        cert_error_ = cert_error;
        request_url_ = request_url;
        callback_ = callback;
    }

    @Override
    public void run() {
        int dialogResult = JOptionPane.showConfirmDialog(owner_,
                "An certificate error (" + cert_error_ + ") occurreed "
                        + "while requesting\n" + request_url_ + "\nDo you want to proceed anyway?",
                "Certificate error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (dialogResult == JOptionPane.YES_OPTION) {
            callback_.Continue();
        } else {
            callback_.cancel();
        }
    }
}
