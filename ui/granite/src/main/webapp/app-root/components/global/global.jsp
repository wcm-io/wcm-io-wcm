<%@page import="com.adobe.granite.xss.XSSAPI"%><%
%><%@page import="com.day.cq.i18n.I18n"%><%
%><%@page import="com.adobe.granite.ui.components.ComponentHelper"%><%
%><%@page session="false" pageEncoding="UTF-8" contentType="text/html" %><%
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %><%
%><%@taglib prefix="ui" uri="http://www.adobe.com/taglibs/granite/ui/1.0" %><%
%><sling:defineObjects /><%

final ComponentHelper cmp = new ComponentHelper(pageContext);
final I18n i18n = cmp.getI18n();
final XSSAPI xssAPI = cmp.getXss();

%><%!
String outVar(XSSAPI xssAPI, I18n i18n, String text) {
  return xssAPI.encodeForHTML(i18n.getVar(text));
}

String outAttrVar(XSSAPI xssAPI, I18n i18n, String text) {
  return xssAPI.encodeForHTMLAttr(i18n.getVar(text));
}

%>