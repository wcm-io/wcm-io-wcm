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
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="com.day.cq.wcm.api.Page"%>
<%@page import="io.wcm.config.api.Configuration"%>
<%@page import="io.wcm.sling.commons.resource.ImmutableValueMap"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%

String rootPath = null;
Page contentPage = GraniteUi.getContentPage(request);
if (contentPage != null) {
  // detect root path of current site via Configuration API
  Configuration conf = contentPage.getContentResource().adaptTo(Configuration.class);
  if (conf != null) {
    // configuration id = root path
    rootPath = conf.getConfigurationId();
  }
}

ValueMap overwriteProperties;
if (rootPath != null) {
  overwriteProperties = ImmutableValueMap.of("rootPath", rootPath);
}
else {
  overwriteProperties = ImmutableValueMap.of();
}

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, overwriteProperties);

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("/libs/granite/ui/components/foundation/form/pathbrowser");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>