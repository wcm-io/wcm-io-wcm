<%@ page import="com.adobe.granite.ui.components.Config" %>
<%@page import="com.adobe.granite.ui.components.ComponentHelper.Options"%>
<%@ page import="com.adobe.granite.ui.components.Tag" %>
<%@include file="../../global/global.jsp" %>
<%
    final Tag tag = cmp.consumeTag();

    cmp.include(resource, "/libs/granite/ui/components/foundation/form/select", new Options().tag(tag));

    final Config cfg = cmp.getConfig();
    final String name = cfg.get("name", String.class);
    final boolean multifield = cfg.get("multiple", false);

    final String typeHintValue;
    if (multifield) {
        typeHintValue = "String[]";
    } else {
        typeHintValue = "String";
    }

%>
<input type="hidden" name="<%=name%>@TypeHint" value="<%=typeHintValue%>" />