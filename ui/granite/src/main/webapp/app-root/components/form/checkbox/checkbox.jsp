<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ComponentHelper.Options"%>
<%@page import="com.adobe.granite.ui.components.Tag"%>
<%@include file="../../global/global.jsp" %><%

Tag tag = cmp.consumeTag();

cmp.include(resource, "/libs/granite/ui/components/foundation/form/checkbox", new Options().tag(tag));

Config cfg = cmp.getConfig();
String name = cfg.get("name", String.class);

%>
<input type="hidden" name="<%=name%>@Delete" value="true" />
<input type="hidden" name="<%=name%>@TypeHint" value="Boolean" />
