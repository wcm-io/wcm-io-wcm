package io.wcm.wcm.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.utils.testcontext.AppAemContext;


@ExtendWith(AemContextExtension.class)
class PageLanguageHandlerTest {

  private final AemContext context = AppAemContext.newAemContext();


  @Test
  void getAlternativeLanguages_rootPage() {
    context.create().page("/content");
    context.create().page("/content/en");
    context.create().page("/content/en/fr");
    context.currentPage(context.create().page("/content/en/de"));

    PageLanguageHandler underTest = context.request().adaptTo(PageLanguageHandler.class);
    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.currentPage());

    assertEquals(2, alternativeLanguageUrls.size());

    assertEquals("/content/en/fr.html", alternativeLanguageUrls.get(Locale.FRENCH));
    assertEquals("/content/en/de.html", alternativeLanguageUrls.get(Locale.GERMAN));
  }


  @Test
  void getAlternativeLanguages_nestedPage() {
    context.create().page("/content");
    context.create().page("/content/en");
    context.create().page("/content/en/fr");
    context.create().page("/content/en/fr/foo");
    context.create().page("/content/en/fr/foo/bar");

    context.create().page("/content/en/es");
    context.create().page("/content/en/es/foo");

    context.create().page("/content/en/de");
    context.create().page("/content/en/de/foo");
    context.currentPage(context.create().page("/content/en/de/foo/bar"));

    PageLanguageHandler underTest = context.request().adaptTo(PageLanguageHandler.class);
    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.currentPage());

    assertEquals(2, alternativeLanguageUrls.size());

    assertEquals("/content/en/fr/foo/bar.html", alternativeLanguageUrls.get(Locale.FRENCH));
    assertEquals("/content/en/de/foo/bar.html", alternativeLanguageUrls.get(Locale.GERMAN));
  }

  @Test
  void getAlternativeLanguages_noOtherPages() {
    context.create().page("/content");
    context.create().page("/content/en");
    context.currentPage(context.create().page("/content/en/de"));

    PageLanguageHandler underTest = context.request().adaptTo(PageLanguageHandler.class);
    Map<Locale, String> alternativeLanguageUrls = underTest.getAlternativeLanguageUrls(context.currentPage());

    assertEquals(1, alternativeLanguageUrls.size());

    assertEquals("/content/en/de.html", alternativeLanguageUrls.get(Locale.GERMAN));
  }

}