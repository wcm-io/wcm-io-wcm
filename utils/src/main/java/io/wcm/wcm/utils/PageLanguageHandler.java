package io.wcm.wcm.utils;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.contenttype.FileExtension;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Model that allows access to alternative languages of a page
 */
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class })
public class PageLanguageHandler {

  @AemObject
  private PageManager pageManager;

  /**
   * @return map of all alternative language versions of the page (including itself)
   */
  public Map<Locale, String> getAlternativeLanguageUrls(Page page) {
    Map<Locale, String> ret = new HashMap<>();
    SiteRoot siteRoot = page.adaptTo(SiteRoot.class);
    if (siteRoot == null)
      return ret;

    Page siteRootPage = siteRoot.getRootPage();

    String relativePath = StringUtils.substringAfter(page.getPath(), siteRootPage.getPath());

    // assuming ../country/language structure
    Page countryRoot = siteRootPage.getParent(2);

    if(countryRoot == null)
      return ret;

    for (Iterator<Page> countryRootPages = countryRoot.listChildren(); countryRootPages.hasNext(); ) {
      Page countryRootPage = countryRootPages.next();
      for (Iterator<Page> languageRootPages = countryRootPage.listChildren(); languageRootPages.hasNext(); ) {
        Page languageRoot = languageRootPages.next();

        Page alternativeVersion = pageManager.getPage(languageRoot.getPath() + relativePath);
        if (alternativeVersion != null) {
          UrlHandler urlHandler = alternativeVersion.adaptTo(UrlHandler.class);
          if (urlHandler != null) {
            String pageUrl = urlHandler.get(alternativeVersion).extension(FileExtension.HTML).buildExternalLinkUrl();
            if (StringUtils.isNotBlank(pageUrl))
              ret.put(alternativeVersion.getLanguage(), pageUrl);
          }
        }
      }
    }
    return ret;
  }
}
