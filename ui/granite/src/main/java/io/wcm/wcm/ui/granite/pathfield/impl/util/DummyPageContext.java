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

import java.io.IOException;
import java.util.Enumeration;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

/**
 * Workaround because {@link com.adobe.granite.ui.components.ExpressionHelper} supports
 * no direct pass-in of SlingHttpServletRequest in AEM 6.2 API (this was fixed in AEM 6.3 API).
 */
@SuppressWarnings("deprecation")
public class DummyPageContext extends PageContext {

  private final SlingHttpServletRequest slingRequest;
  private final SlingHttpServletResponse slingResponse;
  private final JspWriter jspWriter;

  /**
   * @param slingRequest Request
   */
  public DummyPageContext(SlingHttpServletRequest slingRequest, SlingHttpServletResponse slingResponse) {
    this.slingRequest = slingRequest;
    this.slingResponse = slingResponse;
    this.jspWriter = new DummyJspWriter();
  }

  @Override
  public ServletRequest getRequest() {
    return slingRequest;
  }

  @Override
  public ServletResponse getResponse() {
    return slingResponse;
  }

  @Override
  public JspWriter getOut() {
    return jspWriter;
  }


  // --- unsupported methods ---

  @Override
  public void initialize(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL, boolean needsSession, int bufferSize,
      boolean autoFlush) throws IOException, IllegalStateException, IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void release() {
    throw new UnsupportedOperationException();
  }

  @Override
  public HttpSession getSession() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getPage() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Exception getException() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletConfig getServletConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void forward(String relativeUrlPath) throws ServletException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void include(String relativeUrlPath) throws ServletException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void handlePageException(Exception e) throws ServletException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void handlePageException(Throwable t) throws ServletException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(String name, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(String name, Object value, int scope) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAttribute(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAttribute(String name, int scope) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object findAttribute(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAttribute(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAttribute(String name, int scope) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getAttributesScope(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enumeration<String> getAttributeNamesInScope(int scope) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExpressionEvaluator getExpressionEvaluator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public VariableResolver getVariableResolver() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ELContext getELContext() {
    throw new UnsupportedOperationException();
  }

}
