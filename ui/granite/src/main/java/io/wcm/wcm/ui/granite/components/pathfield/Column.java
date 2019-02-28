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
package io.wcm.wcm.ui.granite.components.pathfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents a column in the column view.
 */
@ProviderType
public final class Column {

  private String columnId;
  private boolean hasMore;
  private boolean lazy;
  private String activeId;
  private boolean metaElement;
  private final List<ColumnItem> items = new ArrayList<>();

  Column() {
    // non-public constructor
  }

  public String getColumnId() {
    return this.columnId;
  }

  Column columnId(String value) {
    this.columnId = value;
    return this;
  }

  public boolean isHasMore() {
    return this.hasMore;
  }

  Column hasMore(boolean value) {
    this.hasMore = value;
    return this;
  }

  public boolean isLazy() {
    return this.lazy;
  }

  Column lazy(boolean value) {
    this.lazy = value;
    return this;
  }

  public String getActiveId() {
    return this.activeId;
  }

  Column activeId(String value) {
    this.activeId = value;
    return this;
  }

  public boolean isMetaElement() {
    return this.metaElement;
  }

  Column metaElement(boolean value) {
    this.metaElement = value;
    return this;
  }

  void addItem(ColumnItem item) {
    this.items.add(item);
  }

  public List<ColumnItem> getItems() {
    return Collections.unmodifiableList(this.items);
  }

}