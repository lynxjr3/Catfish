/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catfish.handler;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefDragData;
import org.cef.handler.CefDragHandler;

/**
 *
 * @author lynxjr
 */
public class DragHandler implements CefDragHandler{
    @Override
    public boolean onDragEnter(CefBrowser browser, CefDragData dragData, int mask) {
        System.out.println("DRAG:");
        System.out.print("  flags:");
        if ((mask & CefDragHandler.DragOperationMask.DRAG_OPERATION_COPY) != 0)
            System.out.print(" COPY");
        if ((mask & CefDragHandler.DragOperationMask.DRAG_OPERATION_LINK) != 0)
            System.out.print(" LINK");
        if ((mask & CefDragHandler.DragOperationMask.DRAG_OPERATION_GENERIC) != 0)
            System.out.print(" GENERIC");
        if ((mask & CefDragHandler.DragOperationMask.DRAG_OPERATION_PRIVATE) != 0)
            System.out.print(" PRIVATE");
        if ((mask & CefDragHandler.DragOperationMask.DRAG_OPERATION_MOVE) != 0)
            System.out.print(" MOVE");
        if ((mask & CefDragHandler.DragOperationMask.DRAG_OPERATION_DELETE) != 0)
            System.out.print(" DELETE");
        System.out.println("\n  " + dragData);
        return false;
    }
}
