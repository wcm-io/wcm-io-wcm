package io.wcm.wcm.utils.sitemap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

import java.util.HashMap;
import java.util.Map;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.utils.testcontext.AppAemContext;

@ExtendWith(AemContextExtension.class)
class SiteMapServletTest {

  private static final Map<String, String> NAMESPACE = new HashMap<>();

  static {
    NAMESPACE.put("ns", "http://www.sitemaps.org/schemas/sitemap/0.9");
    NAMESPACE.put("xhtml", "http://www.w3.org/1999/xhtml");
  }

  private final AemContext context = AppAemContext.newAemContext();

  private MockSlingHttpServletRequest request;

  private SiteMapServlet underTest = new SiteMapServlet();

  @BeforeEach
  public void setup() {
    context.load().json("/sitemap/sitemapservlet-sample.json", "/content");

    request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext()) {

      @Override
      public String getResponseContentType() {
        return "text/xml";
      }
    };

  }

  @Test
  public void defaultSetup() throws Exception {
    Map<String, Object> parameters = new HashMap<>();

    context.registerInjectActivateService(underTest, parameters);

    request.setResource(context.resourceResolver().getResource("/content/foo/en/en"));
    underTest.doGet(request, context.response());
    String output = context.response().getOutputAsString();

    assertThat(output, hasXPath("count(//ns:loc)", equalTo("3")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[1]/text()", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:lastmod)[1]/text()", equalTo("2021-01-01")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[2]/text()", equalTo("/content/foo/en/en/events.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[3]/text()", equalTo("/content/foo/en/en/about.html")).withNamespaceContext(NAMESPACE));
  }


  @Test
  public void exclude_property() throws Exception {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("propertyExclude", "customExcludeProperty");
    context.registerInjectActivateService(underTest, parameters);

    request.setResource(context.resourceResolver().getResource("/content/foo/en/en"));
    underTest.doGet(request, context.response());

    String output = context.response().getOutputAsString();

    assertThat(output, hasXPath("count(//ns:loc)", equalTo("2")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[1]/text()", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[2]/text()", equalTo("/content/foo/en/en/about.html")).withNamespaceContext(NAMESPACE));
  }

  @Test
  public void priority_property() throws Exception {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("propertyPriority", "customPriorityProperty");
    context.registerInjectActivateService(underTest, parameters);

    request.setResource(context.resourceResolver().getResource("/content/foo/en/en"));
    underTest.doGet(request, context.response());

    String output = context.response().getOutputAsString();


    assertThat(output, hasXPath("count(//ns:loc)", equalTo("3")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[1]/text()", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:priority)[1]/text()", equalTo("0.75")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[2]/text()", equalTo("/content/foo/en/en/events.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:priority)[2]/text()", equalTo("1.0")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[3]/text()", equalTo("/content/foo/en/en/about.html")).withNamespaceContext(NAMESPACE));
  }

  @Test
  public void exclude_path() throws Exception {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("excludedPaths", "/content/foo/en/en/.*");
    context.registerInjectActivateService(underTest, parameters);

    request.setResource(context.resourceResolver().getResource("/content/foo/en/en"));
    underTest.doGet(request, context.response());

    String output = context.response().getOutputAsString();

    assertThat(output, hasXPath("count(//ns:loc)", equalTo("1")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[1]/text()", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));
  }

  @Test
  public void exclude_template() throws Exception {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("excludedTemplates", "/foo/template2");
    context.registerInjectActivateService(underTest, parameters);

    request.setResource(context.resourceResolver().getResource("/content/foo/en/en"));
    underTest.doGet(request, context.response());

    String output = context.response().getOutputAsString();

    assertThat(output, hasXPath("count(//ns:loc)", equalTo("2")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[1]/text()", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[2]/text()", equalTo("/content/foo/en/en/about.html")).withNamespaceContext(NAMESPACE));
  }

  @Test
  public void alternativeLanguages() throws Exception {

    Map<String, Object> parameters = new HashMap<>();
    context.registerInjectActivateService(underTest, parameters);

    request.setResource(context.resourceResolver().getResource("/content/foo/en/en"));
    underTest.doGet(request, context.response());

    String output = context.response().getOutputAsString();

    assertThat(output, hasXPath("count(//ns:loc)", equalTo("3")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//ns:loc)[1]/text()", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));

    assertThat(output, hasXPath("(//xhtml:link)[1]/@href", equalTo("/content/foo/en/de.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[1]/@hreflang", equalTo("de")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[2]/@href", equalTo("/content/foo/en/en.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[2]/@hreflang", equalTo("en")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[3]/@href", equalTo("/content/foo/en/fr.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[3]/@hreflang", equalTo("fr-FR")).withNamespaceContext(NAMESPACE));

    assertThat(output, hasXPath("(//ns:loc)[2]/text()", equalTo("/content/foo/en/en/events.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[4]/@href", equalTo("/content/foo/en/de/events.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[4]/@hreflang", equalTo("de")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[5]/@href", equalTo("/content/foo/en/en/events.html")).withNamespaceContext(NAMESPACE));
    assertThat(output, hasXPath("(//xhtml:link)[5]/@hreflang", equalTo("en")).withNamespaceContext(NAMESPACE));

    assertThat(output, hasXPath("(//ns:loc)[3]/text()", equalTo("/content/foo/en/en/about.html")).withNamespaceContext(NAMESPACE));
  }
}