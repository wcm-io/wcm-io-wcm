package io.wcm.wcm.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.Map;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.url.SiteConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.utils.testcontext.AppAemContext;


@ExtendWith(AemContextExtension.class)
class PageLanguageHandlerTest {

  private final AemContext context = AppAemContext.newAemContext();

  private PageLanguageHandler underTest;

  @BeforeEach
  public void setup() {
    context.load().json("/sitemap/sitemapservlet-sample.json", "/content");
    underTest = context.request().adaptTo(PageLanguageHandler.class);
  }

  @Test
  void getAlternativeLanguages_rootPage() {
    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.pageManager().getPage("/content/foo/en/en"));

    assertEquals(2, alternativeLanguageUrls.size());

    assertEquals("/content/foo/en/en.html", alternativeLanguageUrls.get(Locale.ENGLISH));
    assertEquals("/content/foo/en/de.html", alternativeLanguageUrls.get(Locale.GERMAN));
  }


  @Test
  void getAlternativeLanguages_nestedPage() {
    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.pageManager().getPage("/content/foo/en/en/events"));

    assertEquals(2, alternativeLanguageUrls.size());

    assertEquals("/content/foo/en/en/events.html", alternativeLanguageUrls.get(Locale.ENGLISH));
    assertEquals("/content/foo/en/de/events.html", alternativeLanguageUrls.get(Locale.GERMAN));
  }

  @Test
  void getAlternativeLanguages_noOtherPages() {
    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.pageManager().getPage("/content/foo/en/en/about"));

    assertEquals(1, alternativeLanguageUrls.size());

    assertEquals("/content/foo/en/en/about.html", alternativeLanguageUrls.get(Locale.ENGLISH));
  }

  @Test
  void ensureCorrectUrlHandlerUsed() {
    MockContextAwareConfig.writeConfiguration(context, "/content/foo/en/de", SiteConfig.class.getName(),
        "siteUrl", "http://de.dummysite.org",
        "siteUrlSecure", "https://de.dummysite.org",
        "siteUrlAuthor", "https://author.dummysite.org");
    MockContextAwareConfig.writeConfiguration(context, "/content/foo/en/en", SiteConfig.class.getName(),
        "siteUrl", "http://en.dummysite.org",
        "siteUrlSecure", "https://en.dummysite.org",
        "siteUrlAuthor", "https://author.dummysite.org");

    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.pageManager().getPage("/content/foo/en/en/events"));

    assertEquals(2, alternativeLanguageUrls.size());

    assertEquals("http://en.dummysite.org/content/foo/en/en/events.html", alternativeLanguageUrls.get(Locale.ENGLISH));
    assertEquals("http://de.dummysite.org/content/foo/en/de/events.html", alternativeLanguageUrls.get(Locale.GERMAN));
  }

}