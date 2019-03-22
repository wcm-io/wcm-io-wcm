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
package io.wcm.wcm.commons.util;

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.wcm.api.NameConstants.NT_TEMPLATE;
import static io.wcm.wcm.commons.util.EditableTemplate.NN_EDITABLE_TEMPLATE_INITIAL;
import static io.wcm.wcm.commons.util.EditableTemplate.NN_EDITABLE_TEMPLATE_POLICIES;
import static io.wcm.wcm.commons.util.EditableTemplate.NN_EDITABLE_TEMPLATE_STRUCTURE;
import static io.wcm.wcm.commons.util.EditableTemplate.PN_EDITABLE;
import static io.wcm.wcm.commons.util.EditableTemplate.isEditRestricted;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.controller.EditableTemplateSupport;
import io.wcm.wcm.commons.testcontext.AppAemContext;

public class EditableTemplateTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private Resource editableTemplate;
  private Resource classicTemplate;

  private Resource editableComponentInitial;
  private Resource editableComponentStructure;
  private Resource lockedComponentStructure;

  @Before
  public void setUp() {
    // prepare editable template
    editableTemplate = context.create().resource("/conf/app1/settings/wcm/templates/template1",
        JCR_PRIMARYTYPE, NT_TEMPLATE);
    context.create().page(editableTemplate.getPath() + "/" + NN_EDITABLE_TEMPLATE_POLICIES);
    Page initial = context.create().page(editableTemplate.getPath() + "/" + NN_EDITABLE_TEMPLATE_INITIAL);
    Page structure = context.create().page(editableTemplate.getPath() + "/" + NN_EDITABLE_TEMPLATE_STRUCTURE);

    editableComponentInitial = context.create().resource(initial, "editableComponent");
    editableComponentStructure = context.create().resource(structure, "editableComponent",
        PN_EDITABLE, true);
    lockedComponentStructure = context.create().resource(structure, "lockedComponent");

    // prepare classic template
    classicTemplate = context.create().resource("/apps/app1/templates/template2",
        JCR_PRIMARYTYPE, NT_TEMPLATE);
  }

  @Test
  @SuppressWarnings("null")
  public void testNoPage() {
    ComponentContext componentContext = mock(ComponentContext.class);
    assertFalse(isEditRestricted(componentContext));

    Resource resource = context.create().resource("/content/myresource");
    context.currentResource(resource);
    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWithoutTemplate() {
    Page page = context.create().page("/content/mypage");
    Resource resource = context.create().resource(page, "resource1");
    context.currentResource(resource);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWitInvalidTemplate() {
    Page page = context.create().page("/content/mypage", "/apps/app1/templates/invalidTemplate");
    Resource resource = context.create().resource(page, "resource1");
    context.currentResource(resource);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWithClassicTemplate() {
    Page page = context.create().page("/content/mypage", classicTemplate.getPath());
    Resource resource = context.create().resource(page, "resource1");
    context.currentResource(resource);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testResourceInEditableTemplate_EditableComponentInitial() {
    context.currentResource(editableComponentInitial);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testResourceInEditableTemplate_EditableComponentStructure() {
    context.currentResource(editableComponentStructure);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testResourceInEditableTemplate_LockedComponentStructure() {
    context.currentResource(lockedComponentStructure);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWithEditableTemplate_EditableComponent() {
    Page page = context.create().page("/content/mypage", editableTemplate.getPath());
    Resource resource = context.create().resource(page, editableComponentStructure.getName());
    context.currentResource(resource);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWithEditableTemplate_LockedComponent() {
    Page page = context.create().page("/content/mypage", editableTemplate.getPath());
    Resource resource = context.create().resource(page, lockedComponentStructure.getName());
    context.currentResource(resource);
    assertTrue(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertTrue(model.isEditRestricted());
  }

  @Test
  public void testPageWithEditableTemplate_OtherComponent() {
    Page page = context.create().page("/content/mypage", editableTemplate.getPath());
    Resource resource = context.create().resource(page, "resource1");
    context.currentResource(resource);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWithEditableTemplate_EditableComponent_ResourceInStructure() {
    Page page = context.create().page("/content/mypage", editableTemplate.getPath());
    context.currentPage(page);
    context.currentResource(editableComponentStructure);
    assertFalse(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertFalse(model.isEditRestricted());
  }

  @Test
  public void testPageWithEditableTemplate_LockedComponent_ResourceInStructure() {
    Page page = context.create().page("/content/mypage", editableTemplate.getPath());
    context.currentPage(page);
    context.currentResource(lockedComponentStructure);
    assertTrue(isEditRestricted(WCMUtils.getComponentContext(context.request())));

    EditableTemplateSupport model = AdaptTo.notNull(context.request(), EditableTemplateSupport.class);
    assertTrue(model.isEditRestricted());
  }

}
