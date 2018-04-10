<%--
  #%L
  wcm.io
  %%
  Copyright (C) 2014 - 2015 wcm.io
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
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
