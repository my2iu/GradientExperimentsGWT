package org.programmingbasics.my2iu.gradients.client;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.UIEvent;
import elemental.html.AnchorElement;
import elemental.html.Float32Array;
import elemental.html.ImageElement;
import elemental.util.ArrayOfNumber;

public class Util
{
  public static void removeChildren(Node node)
  {
      Element el = (Element)node;
      el.setInnerHTML("");
  }

  public static void appendAndRewriteChildren(Node destnode, Node srcnode)
  {
      Element el = (Element)srcnode;
      for (int n = 0; n < el.getChildElementCount(); n++)  
      {
          Node child = el.getChildren().item(n);
          destnode.appendChild(rewriteIds(child.cloneNode(true)));
      }
  }

  public static void copyAndRewriteChildren(Node destnode, Node srcnode)
  {
      removeChildren(destnode);
      appendAndRewriteChildren(destnode, srcnode);
  }

  public static Node rewriteIds(Node node)
  {
      if (node.getNodeType() != 1) return node;  // handle elements only
      Element el = (Element)node;
      if (el.hasAttribute("id_"))
          el.setId(el.getAttribute("id_"));
      if (el.getNodeName().equals("IMG") && el.hasAttribute("src_"))
          el.setAttribute("src", el.getAttribute("src_"));
      for (int n = 0; n < el.getChildElementCount(); n++)
          rewriteIds(el.getChildren().item(n));
      return el;
  }
  
  
  public static native ImageElement createImage() /*-{
    // GWT Elemental stuff is still broken and can't create image elements in Firefox
    return new Image();
  }-*/;

//  public static ImageElement createImage()
//  {
//// GWT Elemental stuff is still broken and can't create image elements in Firefox
//    return (ImageElement)Browser.getDocument().createElement("img");
//  }

  public static AnchorElement createAnchor()
  {
    // GWT Elemental stuff is still broken and can't create image elements in Firefox
    AnchorElement a = (AnchorElement)Browser.getDocument().createElement("a");
    a.setHref("#");
    return a;
  }

  public static int getEventXRelativeTo(UIEvent event, Element element)
  {
    int x = getPageXFromEvent(event);
    return pageXRelativeToEl(x, element);
  }

  public static int getEventYRelativeTo(UIEvent event, Element element)
  {
    int y = getPageYFromEvent(event);
    return pageYRelativeToEl(y, element);
  }

  public static int getPageXFromEvent(UIEvent event)
  {
    return event.getPageX();
//    int x = event.clientX;
//    if (!window.pageXOffset && window.pageXOffset != 0)
//    {
//        // This should only happen with IE, and in strict mode, 
//        // IE has a document.documentElement element (doesn't use 
//        // document.body for it)
//        x -= document.documentElement.scrollLeft;
//    }
//    else
//    {
//        x -= window.pageXOffset;
//    }
//    return x;
  }

  public static int getPageYFromEvent(UIEvent event)
  {
    return event.getPageY();
//    // Calculate pageX and pageY from clientX and clientY
//    var y = event.clientY;
//    if (!window.pageXOffset && window.pageXOffset != 0)
//    {
//        // This should only happen with IE, and in strict mode, 
//        // IE has a document.documentElement element (doesn't use 
//        // document.body for it)
//        y -= document.documentElement.scrollTop;
//    }
//    else
//    {
//        y -= window.pageYOffset;
//    }
//    return y;
  }

  public static int pageXRelativeTo(int x, String id)
  {
    return pageXRelativeToEl(x, Browser.getDocument().getElementById(id));
  }

  public static int pageYRelativeTo(int y, String id)
  {
    return pageYRelativeToEl(y, Browser.getDocument().getElementById(id));
  }

  public static int pageXRelativeToEl(int x, Element element)
  {
    // Convert pageX and pageY numbers to be relative to a certain element
    int pageX = 0, pageY = 0;
    while(element.getOffsetParent() != null)
    {
      pageX += element.getOffsetLeft();
      pageY += element.getOffsetTop();
      element = element.getOffsetParent();
    }
    x = x - pageX;
    return x;
  }

  public static int pageYRelativeToEl(int y, Element element)
  {
    // Convert pageX and pageY numbers to be relative to a certain element
    int pageX = 0, pageY = 0;
    while(element.getOffsetParent() != null)
    {
      pageX += element.getOffsetLeft();
      pageY += element.getOffsetTop();
      element = element.getOffsetParent();
    }
    y = y - pageY;
    return y;
  }

}
