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
