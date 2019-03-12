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

import static com.day.cq.commons.jcr.JcrConstants.NT_FOLDER;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.Predicate;
import org.apache.sling.api.resource.Resource;

/**
 * Resources with primary type nt:folder.
 */
public class FolderPredicate implements Predicate {

  /**
   * Filter name
   */
  public static final String FILTER = "folder";

  @Override
  public boolean evaluate(Object object) {
    Node node = ((Resource)object).adaptTo(Node.class);
    if (node == null) {
      return false;
    }
    try {
      return node.isNodeType(NT_FOLDER);
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

}
