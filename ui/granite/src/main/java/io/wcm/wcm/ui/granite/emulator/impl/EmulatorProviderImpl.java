/* Copyright (c) pro!vision GmbH. All rights reserved. */
package io.wcm.wcm.ui.granite.emulator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.emulator.Emulator;
import com.day.cq.wcm.emulator.EmulatorGroup;
import com.day.cq.wcm.emulator.EmulatorProvider;
import com.day.cq.wcm.mobile.api.device.DeviceGroup;
import com.day.cq.wcm.mobile.api.device.DeviceGroupList;
import com.google.common.collect.ImmutableList;

/**
 * Simple implementation of {@link EmulatorProvider} to activate AEM 6.1 responsive mode for this application.
 * To work it needs a property "cq:deviceGroups[]" set to "/etc/mobile/groups/responsive" in the site root page.
 */
@Component(immediate = true, metatype = true,
label = "wcm.io Emulator Provider",
description = "Provides emulators based on device groups in the pages with a configurable set of templates.")
@Service(EmulatorProvider.class)
public class EmulatorProviderImpl implements EmulatorProvider {

  @Property(cardinality = Integer.MAX_VALUE,
      label = "Template Patterns",
      description = "List of regular expressions to match template paths this emulator provider should apply to.")
  static final String PROPERYT_TEMPLATE_PATH_PATTERNS = "templatePathPatterns";

  private List<Pattern> templatePathPatterns;

  private static final Logger log = LoggerFactory.getLogger(EmulatorProviderImpl.class);

  @Activate
  void activate(Map<String,Object> config) {
    templatePathPatterns = new ArrayList<>();
    String[] patterns = PropertiesUtil.toStringArray(config.get(PROPERYT_TEMPLATE_PATH_PATTERNS), new String[0]);
    for (String pattern : patterns) {
      try {
        templatePathPatterns.add(Pattern.compile(pattern));
      }
      catch (PatternSyntaxException ex) {
        log.warn("Ignoring invalid template path pattern: " + pattern, ex);
      }
    }
  }

  @Override
  public boolean handles(Resource resource) {
    // check if resource is a page, and if the page uses a configured template
    Page page = resource.adaptTo(Page.class);
    if (page != null) {
      String templatePath = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
      if (StringUtils.isNotEmpty(templatePath)) {
        for (Pattern pattern : templatePathPatterns) {
          Matcher matcher = pattern.matcher(templatePath);
          if (matcher.matches()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public List<Emulator> getEmulators(Resource resource) {
    // return all emulators from all groups
    List<Emulator> emulators = new ArrayList<>();
    for (EmulatorGroup group : getEmulatorGroups(resource)) {
      emulators.addAll(group.getEmulators());
    }
    return emulators;
  }

  @Override
  public List<EmulatorGroup> getEmulatorGroups(Resource resource) {
    // convert device groups defined in page props to emulator groups
    Page page = resource.adaptTo(Page.class);
    if (page != null) {
      DeviceGroupList deviceGroups = page.adaptTo(DeviceGroupList.class);
      if (deviceGroups != null) {
        List<EmulatorGroup> emulatorGroups = new ArrayList<>();
        for (DeviceGroup deviceGroup : deviceGroups) {
          emulatorGroups.add(new EmulatorGroupImpl(deviceGroup));
        }
        return emulatorGroups;
      }
    }
    return ImmutableList.of();
  }


  /**
   * Delegates all calls to the given DeviceGroup.
   */
  private final class EmulatorGroupImpl implements EmulatorGroup {

    private final DeviceGroup deviceGroup;

    EmulatorGroupImpl(DeviceGroup deviceGroup) {
      this.deviceGroup = deviceGroup;
    }

    @Override
    public String getTitle() {
      return deviceGroup.getTitle();
    }

    @Override
    public String getDescription() {
      return deviceGroup.getDescription();
    }

    @Override
    public String getPath() {
      return deviceGroup.getPath();
    }

    @Override
    public String getName() {
      return deviceGroup.getName();
    }

    @Override
    public List<Emulator> getEmulators() {
      return deviceGroup.getEmulators();
    }

  }

}
