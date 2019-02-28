/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.wcm.ui.granite.pathfield.impl.util;

import java.util.Iterator;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

/**
 * Wraps a resource and filters all children by given predicate.
 */
public class PredicatedResourceWrapper extends ResourceWrapper {

  private final Predicate predicate;

  /**
   * @param resource Resource
   * @param predicate Predicate
   */
  public PredicatedResourceWrapper(Resource resource, Predicate predicate) {
    super(resource);
    this.predicate = predicate;
  }

  @Override
  public Resource getChild(String relPath) {
    Resource child = super.getChild(relPath);
    if (child == null || !predicate.evaluate(child)) {
      return null;
    }
    return new PredicatedResourceWrapper(child, predicate);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterator<Resource> listChildren() {
    return new TransformIterator(new FilterIterator(super.listChildren(), predicate), new Transformer() {
      @Override
      public Resource transform(Object o) {
        return new PredicatedResourceWrapper((Resource)o, predicate);
      }
    });
  }

  @Override
  public boolean hasChildren() {
    if (!super.hasChildren()) {
      return false;
    }
    return listChildren().hasNext();
  }

}
