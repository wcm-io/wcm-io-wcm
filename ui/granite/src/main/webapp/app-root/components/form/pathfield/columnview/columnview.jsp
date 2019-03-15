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
<%@include file="/libs/granite/ui/global.jsp"%><%

%><%@page session="false"
          import="org.apache.sling.commons.json.io.JSONStringer,
                  com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.ui.components.Config,
                  com.adobe.granite.ui.components.ExpressionHelper,
                  com.adobe.granite.ui.components.Tag,
                  com.adobe.granite.ui.components.ComponentHelper.Options,
                  io.wcm.wcm.ui.granite.components.pathfield.ColumnView,
                  io.wcm.wcm.ui.granite.components.pathfield.Column,
                  io.wcm.wcm.ui.granite.components.pathfield.ColumnItem"%><%--

This is an enhanced version of /libs/granite/ui/components/coral/foundation/columnview
It supports basically the same parameters.

--%><%
if (!cmp.getRenderCondition(resource, false).check()) {
  return;
}

Config cfg = cmp.getConfig();
ExpressionHelper ex = cmp.getExpressionHelper();
ColumnView model = slingRequest.adaptTo(ColumnView.class);

String src = ex.getString(cfg.get("src", String.class));
if (src != null && src.startsWith("/")) {
  src = request.getContextPath() + src;
}

String previewSrc = ex.getString(cfg.get("previewSrc", String.class));
if (previewSrc != null && previewSrc.startsWith("/")) {
  previewSrc = request.getContextPath() + previewSrc;
}

String layoutName = "foundation-layout-columnview";
boolean isSelectionMode = ex.getBoolean(cfg.get("selectionMode", "true"));
String selectionCount = ex.getString(cfg.get("selectionCount", "multiple"));

Tag tag = cmp.consumeTag();
AttrBuilder attrs = tag.getAttrs();
cmp.populateCommonAttrs(attrs);

attrs.addClass("foundation-collection");
attrs.add("data-foundation-collection-id", model.getCurrentResource().getPath());
attrs.add("data-foundation-collection-src", src);
attrs.add("data-foundation-selections-mode", selectionCount);
attrs.add("data-foundation-mode-group", cfg.get("modeGroup", String.class));

String layoutJson = new JSONStringer()
    .object()
    .key("name").value(layoutName)
    .key("limit").value(cfg.get("limit", 40))
    .key("previewSrc").value(previewSrc)
    // This is used as an id to identify the layout when there are multiple layouts to represent the same collection.
    .key("layoutId").value(resource.getName())
    .endObject()
    .toString();

attrs.addClass(layoutName);
attrs.add("data-foundation-layout", layoutJson);

attrs.add("selectionmode", isSelectionMode ? selectionCount : "none");
%><coral-columnview <%= attrs %>><%

for (Column column : model.getColumns()) {

  AttrBuilder columnAttrs = new AttrBuilder(request, xssAPI);
  columnAttrs.add("data-foundation-layout-columnview-columnid", column.getColumnId());
  if (column.isHasMore()) {
    columnAttrs.add("data-foundation-layout-columnview-hasmore", column.isHasMore());
  }
  if (column.isLazy()) {
    columnAttrs.add("data-foundation-layout-columnview-lazy", column.isLazy());
    columnAttrs.add("data-foundation-layout-columnview-activeitem", column.getActiveId());
  }
  %><coral-columnview-column <%= columnAttrs %>><coral-columnview-column-content><%

  if (column.isLazy()) {
    %><coral-wait size="L" centered></coral-wait><%
  }
  else {
    for (ColumnItem columnItem : column.getItems()) {
      AttrBuilder itemAttrs = new AttrBuilder(request, xssAPI);
      itemAttrs.addClass("foundation-collection-item");
      itemAttrs.add("data-foundation-collection-item-id", columnItem.getItemId());

      cmp.include(columnItem.getResource(), columnItem.getResourceType(), new Options().tag(new Tag(itemAttrs)));
    }
  }

  if (column.isMetaElement()) {
    // Put meta element here instead of under <coral-columnview-column>,
    // as somehow Coral is moving all the elements under <coral-columnview-column> to be under <coral-columnview-column-content>
    // even though <coral-columnview-column-content> is already given.
    String metaResourceType = cfg.get("metaResourceType", String.class);
    if (metaResourceType != null) {
        %><sling:include resource="<%=model.getCurrentResource()%>" resourceType="<%=metaResourceType%>" /><%
    }
  }

  %></coral-columnview-column-content></coral-columnview-column><%
}

%></coral-columnview>
