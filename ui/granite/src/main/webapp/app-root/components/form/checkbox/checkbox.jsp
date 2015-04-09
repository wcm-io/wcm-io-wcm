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
