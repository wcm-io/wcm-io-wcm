## WCM Commons System Configuration

### Service user configuration

WCM Commons requires a service user mapping for accessing component properties in the `/apps` folder.

Create a principal-based service user mapping with an entry like this:

```
  org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-myapp-wcmio-wcm-commons-cmp
    user.mapping=["io.wcm.wcm.commons:component-properties\=[sling-scripting]"]
```

The built-in principal `sling-scripting` has read access to `/apps` and `/libs`.

This configuration is required **on both author and publish instances**.


### AEM Instance Type configuration

To detect whether code is currently running on an Author or Publish instance (without relying on the deprecated `SlingSettingsService`), it is required to provide an OSGi configuration "wcm.io Commons AEM Instance Type" for author and publish instances:

```
[configurations runModes=author]
  io.wcm.wcm.commons.instancetype.impl.InstanceTypeServiceImpl
    instance.type="author"

[configurations runModes=publish]
  io.wcm.wcm.commons.instancetype.impl.InstanceTypeServiceImpl
    instance.type="publish"
```

If this configuration is not present, the [InstanceTypeService][InstanceTypeService] implementation tries to guess the instance type from other OSGi configurations, but this is only a fallback.


[InstanceTypeService]: apidocs/io/wcm/wcm/commons/instancetype/InstanceTypeService.html
