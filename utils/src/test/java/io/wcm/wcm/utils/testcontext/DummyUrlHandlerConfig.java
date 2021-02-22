package io.wcm.wcm.utils.testcontext;

import java.util.ArrayList;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.api.Page;

import io.wcm.handler.url.integrator.IntegratorMode;
import io.wcm.handler.url.spi.UrlHandlerConfig;

/**
 * Dummy link configuration
 */
@SuppressWarnings("null")
public class DummyUrlHandlerConfig extends UrlHandlerConfig {

  public static final int SITE_ROOT_LEVEL = 3;

  @Override
  public List<IntegratorMode> getIntegratorModes() {
    return new ArrayList<>();
  }

  @Override
  public boolean isSecure(Page page) {
    return false;
  }

  @Override
  public boolean isIntegrator(Page page) {
    return false;
  }

  @Override
  public int getSiteRootLevel(Resource contextResource) {
    return SITE_ROOT_LEVEL;
  }

}
