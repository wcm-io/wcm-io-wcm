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
package io.wcm.wcm.ui.granite.pathfield.impl.predicate;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.Predicate;
import org.apache.sling.api.resource.Resource;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Resources with primary type nt:hierarchyNode that are not nt:file.
 */
public class HierarchyNotFilePredicate implements Predicate {

  /**
   * Filter name
   */
  public static final String FILTER = "hierarchyNotFile";

  @Override
  public boolean evaluate(Object object) {
    Node node = ((Resource)object).adaptTo(Node.class);
    if (node == null) {
      return false;
    }
    try {
      return node.isNodeType(JcrConstants.NT_HIERARCHYNODE) && !node.isNodeType(JcrConstants.NT_FILE);
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

}
