/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.wcm.commons.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.apache.commons.lang3.CharEncoding;

import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Servlet which renders a Sitemap. The implementing Servlet only has to provide a filter method which checks, if certain pages should be included or excluded
 */
public abstract class AbstractSitemapServlet extends SlingSafeMethodsServlet {

  /**
   * Selector
   */
  public static final String SELECTOR = "sitemap";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSitemapServlet.class);

  // ISO 8601 (W3C) date format for serializing the modification date
  private final DateFormat mISO8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private static final Namespace NAMESPACE = Namespace.getNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");

  private UrlHandler urlHandler;

  /**
   * Check if the given page should be included into the sitemap
   * @param page
   * @return the strategy to include the page
   */
  protected abstract SITEMAP_INCLUSION_STRATEGY includes(Page page);

  /**
   * Check for the Root page of the current site (using {@link SiteRoot})
   * @param request
   * @return the root page for the sitemap or current page if none is found
   */
  protected Page getRootPage(SlingHttpServletRequest request) {
    SiteRoot siteRoot = request.adaptTo(SiteRoot.class);
    if (siteRoot != null) {
      return siteRoot.getRootPage();
    }
    PageManager pm = request.getResourceResolver().adaptTo(PageManager.class);
    if (pm != null) {
      return pm.getContainingPage(request.getResource());
    }
    return null;
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

    Page root = getRootPage(request);
    if (root == null) {
      LOG.warn("Could not find rootpage, sending 500");
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    urlHandler = request.adaptTo(UrlHandler.class);
    Document doc = new Document();
    Element rootElement = new Element("urlset", NAMESPACE);
    doc.addContent(rootElement);
    // add childpages
    addPageRecursively(root, rootElement);

    // Output
    response.setContentType(ContentType.XML);
    response.setCharacterEncoding(CharEncoding.UTF_8);

    XMLOutputter xmlout = new XMLOutputter();
    xmlout.setFormat(Format.getCompactFormat().setOmitDeclaration(false));
    xmlout.output(doc, response.getWriter());
  }

  /**
   * @param page
   * @param rootElement
   */
  private void addPageRecursively(Page page, Element rootElement) {
    switch (includes(page)) {
      case EXCLUDE_RECURSIVE:
        // page should not be included, return
        return;
      case EXCLUDE:
        // page should not be included but possible child pages
        break;
      case INCLUDE:
      default:
        // include the actual page
        addPage(page, rootElement);
        break;
    }
    page.listChildren().forEachRemaining(childPage -> addPageRecursively(childPage, rootElement));
  }

  private void addPage(Page page, Element rootElement) {
    Element urlElement = new Element("url", NAMESPACE);
    rootElement.addContent(urlElement);
    Element locElement = new Element("loc", NAMESPACE).setText(externalizePageLocation(page));
    urlElement.addContent(locElement);
    String lastMod = getLastMod(page);
    if (StringUtils.isNotBlank(lastMod)) {
      Element lastModElement = new Element("lastmod", NAMESPACE).setText(lastMod);
      urlElement.addContent(lastModElement);
    }
  }

  private String externalizePageLocation(Page page) {
    if (urlHandler != null) {
      return urlHandler.get(page).extension(FileExtension.HTML).buildExternalLinkUrl();
    }
    // fallback to page path with .html extension
    return page.getPath() + ".html";
  }

  private String getLastMod(Page page) {
    Calendar modificationDate = page.getLastModified();
    if (modificationDate != null) {
      return mISO8601DateFormat.format(modificationDate.getTime());
    }
    else {
      return StringUtils.EMPTY;
    }
  }

  /**
   * Strategies to include pages into the sitemap
   */
  public enum SITEMAP_INCLUSION_STRATEGY {
    /**
     * include the page
     */
    INCLUDE,
    /**
     * exclude the page but check again for it's children
     */
    EXCLUDE,
    /**
     * exclude the page and all of it's children
     */
    EXCLUDE_RECURSIVE;
  }
}
