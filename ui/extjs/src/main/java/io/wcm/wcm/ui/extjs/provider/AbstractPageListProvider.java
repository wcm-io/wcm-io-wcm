/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.wcm.ui.extjs.provider;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.wcm.ui.extjs.provider.impl.util.PageIterator;

/**
 * Exports the list of child pages of the addressed resource in JSON format to the response.
 * This can be used by the <code>io.wcm.wcm.ui.form.Selection</code> widget.
 * Abstract implementation, some methods can be overwritten by sublcasses.
 */
@ConsumerType
@SuppressWarnings("deprecation")
public abstract class AbstractPageListProvider extends AbstractPageProvider {
  private static final long serialVersionUID = 1L;

  @Override
  protected JSONArray getJsonContent(Resource rootResource, PageFilter pageFilter) throws JSONException {
    JSONArray pagelist = new JSONArray();

    Iterator<Page> pages = new PageIterator(rootResource.listChildren(), pageFilter);

    while (pages.hasNext()) {
      Page page = pages.next();

      JSONObject childItem = new JSONObject();
      childItem.put("value", page.getPath());
      childItem.put("text", page.getTitle());
      pagelist.put(childItem);
    }

    return pagelist;
  }

}
