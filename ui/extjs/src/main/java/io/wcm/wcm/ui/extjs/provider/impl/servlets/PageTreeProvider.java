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
package io.wcm.wcm.ui.extjs.provider.impl.servlets;

import javax.servlet.Servlet;

import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import com.day.cq.commons.predicate.PredicateProvider;

import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.ui.extjs.provider.AbstractPageTreeProvider;

/**
 * Exports the resource tree at the addressed resource in JSON format to the response.
 * This can be used by the <code>io.wcm.wcm.ui.form.BrowseField</code> widget.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + FileExtension.JSON,
    "sling.servlet.selectors=" + PageTreeProvider.SELECTOR,
    "sling.servlet.resourceTypes=sling/servlet/default",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public final class PageTreeProvider extends AbstractPageTreeProvider {
  private static final long serialVersionUID = 1L;

  static final String SELECTOR = "wcm-io-wcm-ui-extjs-pagetree";

  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  private PredicateProvider predicateProvider;

  @Override
  protected PredicateProvider getPredicateProvider() {
    return predicateProvider;
  }

}
