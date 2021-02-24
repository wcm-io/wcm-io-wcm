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
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="org.osgi.framework.Version"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ExpressionHelper"%>
<%@page import="org.apache.jackrabbit.util.Text"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ResourceResolver"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@page import="io.wcm.wcm.ui.granite.util.RootPathResolver"%>
<%@include file="../../global/global.jsp" %><%--###

wcm.io Granite UI Extensions PathField
======================================

A field that allows the user to enter path.

It extends `granite/ui/components/coral/foundation/form/pathfield` component
(with a fallback to `granite/ui/components/foundation/form/pathbrowser` for older AEM versions).

It supports the same properties as it's super component. The following properties
are overwritten or added.

.. gnd:gnd::

  /**
   * The path of the root of the pathfield.
   */
  - rootPath (StringEL) = {path}

  /**
   * Fallback root path that is used when no root path was configured.
   */
  - fallbackRootPath (StringEL) = '/'

  /**
   * Appendix path added to the root path.
   */
  - appendPath (StringEL) = {path appendix}

###--%><%

String resourceType = GraniteUi.getExistingResourceType(resourceResolver,
    "granite/ui/components/coral/foundation/form/pathfield",
    "granite/ui/components/foundation/form/pathbrowser");
Map<String,Object> props = new HashMap<>();

boolean isPathField = StringUtils.equals(resourceType, "granite/ui/components/coral/foundation/form/pathfield");

// resolver root path
RootPathResolver rootPathResolver = new RootPathResolver(cmp, slingRequest);
String rootPath = rootPathResolver.get();

if (isPathField) {
  Config cfg = cmp.getConfig();
  ExpressionHelper ex = cmp.getExpressionHelper();

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

  props.put("pickerSrc", pickerSrc);
  props.put("suggestionSrc", suggestionSrc);  
}
else {
  resourceType = "granite/ui/components/foundation/form/pathbrowser";
  props.put("rootPath", rootPath);
}

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(props));

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType(resourceType);
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>