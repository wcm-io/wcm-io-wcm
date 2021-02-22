package io.wcm.wcm.commons.sitemap;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";

    private static final Logger log = LoggerFactory.getLogger(SiteMapServlet.class);

    private SiteMapServlet.Config config;

    @Activate
    void activate(SiteMapServlet.Config config) {
        this.config = config;
    }

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/xml");
        response.setCharacterEncoding("utf-8");

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();

        XMLStreamWriter stream = null;
        try {
            stream = outputFactory.createXMLStreamWriter(response.getWriter());
            stream.writeStartDocument("1.0");
            stream.writeStartElement("", "urlset", NAMESPACE);
            stream.writeNamespace("", NAMESPACE);

            writeSiteMap(request, stream);

            stream.writeEndElement();
            stream.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (XMLStreamException e) {
                    log.warn("Can not close xml stream writer", e);
                }
            }
        }
    }

    private void writeSiteMap(SlingHttpServletRequest request, XMLStreamWriter stream) throws XMLStreamException {
        SiteRoot siteRoot = request.adaptTo(SiteR)
    }

    /**
     * Configuration definition
     */
    @ObjectClassDefinition(name = "wcm.io - Sitemap",
            description = "Servlet for Sitemap generation")
    public @interface Config {

        @AttributeDefinition(name = "Sling Resource Type", description = "Sling Resource Type for the Home Page.")
        String[] sling_servlet_resourceTypes() default {};

        @AttributeDefinition(name = "Excluded paths", description = "Excluded Path Regex (relative to root page)")
        String[] excludedPaths() default {};

        @AttributeDefinition(name = "Excluded templates", description = "Excluded templates")
        String[] excludedTemplates() default {};

        @AttributeDefinition(name = "Priority property", description = "Property")
        String propertyPriority() default "siteMapPriority";

        @AttributeDefinition(name = "Exclude property", description = "Page Property indicating that the page should be excluded from sitemap")
        String propertyExclude() default "siteMapExcluded";

    }
}
