package io.wcm.wcm.utils.sitemap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import com.day.cq.commons.date.DateUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;

import io.wcm.handler.url.UrlHandler;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.util.Template;
import io.wcm.wcm.utils.PageLanguageHandler;

/**
 * Configurable Servlet that provides a Sitemap XML at _jcr_content.sitemap.xml
 */
@Component(service = Servlet.class, property = {
    "sling.servlet.selectors=sitemap",
    "sling.servlet.extensions=xml",
    "sling.servlet.methods=GET"
}, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SiteMapServlet.Config.class, factory = true)
public class SiteMapServlet extends SlingSafeMethodsServlet {

  private static final Namespace NAMESPACE = Namespace.getNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");
  private static final Namespace NAMESPACE_XHTML = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");

  private SiteMapServlet.Config config;

  private List<Pattern> excludedPath;

  @Activate
  void activate(SiteMapServlet.Config config) {
    this.config = config;

    this.excludedPath = Arrays.stream(config.excludedPaths()).map(Pattern::compile).collect(Collectors.toList());
  }

  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/xml");
    response.setCharacterEncoding("utf-8");

    Document xmlDocument = new Document();
    Element rootElement = new Element("urlset", NAMESPACE);
    rootElement.addNamespaceDeclaration(NAMESPACE_XHTML);
    xmlDocument.setRootElement(rootElement);

    writeSiteMap(request, rootElement);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    OutputStream out = response.getOutputStream();
    outputter.output(xmlDocument, out);
  }

  private void writeSiteMap(SlingHttpServletRequest request, Element rootElement) {
    ResourceResolver resourceResolver = request.getResourceResolver();
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    Page page = pageManager.getContainingPage(request.getResource());
    PageLanguageHandler pageLanguageHandler = request.adaptTo(PageLanguageHandler.class);
    UrlHandler urlHandler = request.adaptTo(UrlHandler.class);

    writePageEntry(urlHandler, rootElement, pageLanguageHandler, page);
  }

  private void writePageEntry(UrlHandler urlHandler, Element rootElement, PageLanguageHandler pageLanguageHandler, Page page) {
    if (included(page)) {
      Element urlElement = createUrlElement(urlHandler, page);
      rootElement.addContent(urlElement);

      pageLanguageHandler.getAlternativeLanguageUrls(page).forEach((key, value) -> urlElement.addContent(createLinkEntry(value, key)));

    }
    Iterator<Page> pageIterator = page.listChildren(new PageFilter(false, true));
    while (pageIterator.hasNext()) {
      Page child = pageIterator.next();
      writePageEntry(urlHandler, rootElement, pageLanguageHandler, child);
    }
  }

  private Element createUrlElement(UrlHandler urlHandler, Page page) {
    String url = urlHandler.get(page).extension(FileExtension.HTML).buildExternalLinkUrl();

    Element urlElement = new Element("url", NAMESPACE);
    Element locElement = new Element("loc", NAMESPACE).setText(url);
    urlElement.addContent(locElement);

    Float priority = page.getProperties().get(config.propertyPriority(), Float.class);
    if (priority != null) {
      Element priorityElement = new Element("priority", NAMESPACE).setText(priority.toString());
      urlElement.addContent(priorityElement);
    }
    if (page.getLastModified() != null) {
      Element lastModElement = new Element("lastmod", NAMESPACE).setText(DateUtil.getISO8601DateNoTime(page.getLastModified()));
      urlElement.addContent(lastModElement);
    }
    return urlElement;
  }

  private Element createLinkEntry(String link, Locale locale) {
    Element linkElement = new Element("link", NAMESPACE_XHTML);
    linkElement.setAttribute("rel", "alternate");
    linkElement.setAttribute("hreflang", locale.toLanguageTag());
    linkElement.setAttribute("href", link);
    return linkElement;
  }

  private boolean included(Page page) {
    return (!page.getProperties().get(config.propertyExclude(), false)
        && Arrays.stream(config.excludedTemplates()).noneMatch(it -> Template.is(page, it))
        && excludedPath.stream().noneMatch(it -> it.matcher(page.getPath()).matches()));
  }

  /**
   * Configuration definition
   */
  @ObjectClassDefinition(name = "wcm.io - Sitemap",
      description = "Servlet for Sitemap generation")
  public @interface Config {

    @AttributeDefinition(name = "Sling Resource Type", description = "Sling Resource Type for the Home Page.")
    String[] sling_servlet_resourceTypes() default {};

    @AttributeDefinition(name = "Excluded paths", description = "Excluded Path Regex")
    String[] excludedPaths() default {};

    @AttributeDefinition(name = "Excluded templates", description = "Excluded templates")
    String[] excludedTemplates() default {};

    @AttributeDefinition(name = "Priority property", description = "Property")
    String propertyPriority() default "siteMapPriority";

    @AttributeDefinition(name = "Exclude property", description = "Page Property indicating that the page should be excluded from sitemap")
    String propertyExclude() default "siteMapExcluded";

  }
}
