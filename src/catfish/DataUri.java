/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish;

/**
 *
 * @author lynxjr
 */
public class DataUri {
    public static String create(String mimeType, String contents) {
        return "data:" + mimeType + ";base64,"
                + java.util.Base64.getEncoder().encodeToString(contents.getBytes());
    }
}
