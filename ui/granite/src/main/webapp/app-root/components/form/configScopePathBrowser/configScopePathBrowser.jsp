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
<%@page import="com.adobe.granite.ui.components.Config"%>
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

String rootPath = null;
Resource contentResource = GraniteUi.getContentResourceOrParent(request);
if (contentResource != null) {
  // detect root path of current site via Sling Context-Aware Configuration API
  ConfigurationResourceResolver configResolver = sling.getService(ConfigurationResourceResolver.class);
  if (configResolver != null) {
    // get inner-most context path
    rootPath = configResolver.getContextPath(contentResource);
  }
}

ValueMap overwriteProperties;
if (rootPath != null) {
  // check for optional appendix
  Config cfg = cmp.getConfig();
  String appendPath = cfg.get("appendPath", String.class);
  if (appendPath != null) {
    rootPath += appendPath;
  }
  overwriteProperties = new ValueMapDecorator(ImmutableMap.<String,Object>of("rootPath", rootPath));
}
else {
  overwriteProperties = ValueMapDecorator.EMPTY;
}

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, overwriteProperties);

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType(GraniteUi.getExistingResourceType(resourceResolver,
    "/libs/granite/ui/components/coral/foundation/form/pathfield",
    "/libs/granite/ui/components/foundation/form/pathbrowser"));
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>