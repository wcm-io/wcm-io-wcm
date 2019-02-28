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
<%@page import="com.adobe.granite.license.ProductInfoProvider"%>
<%@page import="org.apache.jackrabbit.util.Text"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%

String resourceType = GraniteUi.getExistingResourceType(resourceResolver,
    "granite/ui/components/coral/foundation/form/pathfield",
    "granite/ui/components/foundation/form/pathbrowser");
Map<String,Object> props = new HashMap<>();

ProductInfoProvider productInfoProvider = sling.getService(ProductInfoProvider.class);
Version productVersion = productInfoProvider.getProductInfo().getVersion();

// only compatbile with path field from AEM 6.3 SP3 or higher AEM versions
boolean isPathField = StringUtils.equals(resourceType, "granite/ui/components/coral/foundation/form/pathfield")
    && productVersion.getMajor() >= 6
    && (productVersion.getMinor() > 3 || (productVersion.getMinor() == 3 && productVersion.getMicro() >= 3));

if (isPathField) {
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
    
  props.put("pickerSrc", pickerSrc);
  props.put("suggestionSrc", suggestionSrc);  
}
else {
  resourceType = "granite/ui/components/foundation/form/pathbrowser";
}

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(props));

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType(resourceType);
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>