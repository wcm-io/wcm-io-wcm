<%--
  #%L
  wcm.io
  %%
  Copyright (C) 2019 wcm.io
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
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ExpressionHelper"%>
<%@page import="org.apache.jackrabbit.util.Text"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.caconfig.resource.ConfigurationResourceResolver"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="com.google.common.collect.ImmutableMap"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%

Config cfg = cmp.getConfig();
ExpressionHelper ex = cmp.getExpressionHelper();

String rootPath = ex.getString(cfg.get("rootPath", "/"));
String filter = cfg.get("filter", "hierarchyNotFile");
boolean multiple = cfg.get("multiple", false);
String selectionCount = multiple ? "multiple" : "single";

// build path to picker and suggestion src based on overlayed pathfield content from wcm.io
String defaultPickerSrc = "/mnt/overlay/wcm-io/wcm/ui/granite/content/form/pathfield/picker.html"
    + "?_charset_=utf-8&path={value}&root=" + Text.escape(rootPath) + "&filter=" + Text.escape(filter) + "&selectionCount=" + Text.escape(selectionCount);
String pickerSrc = ex.getString(cfg.get("pickerSrc", defaultPickerSrc));

String defaultSuggestionSrc = "/mnt/overlay/wcm-io/wcm/ui/granite/content/form/pathfield/suggestion{.offset,limit}.html"
    + "?_charset_=utf-8&root=" + Text.escape(rootPath) + "&filter=" + Text.escape(filter) + "{&query}";
String suggestionSrc = ex.getString(cfg.get("suggestionSrc", defaultSuggestionSrc));

ValueMap overwriteProperties = new ValueMapDecorator(ImmutableMap.<String,Object>of(
    "pickerSrc", pickerSrc,
    "suggestionSrc", suggestionSrc));

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, overwriteProperties);

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType(GraniteUi.getExistingResourceType(resourceResolver,
    "granite/ui/components/coral/foundation/form/pathfield",
    "granite/ui/components/foundation/form/pathbrowser"));
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>
