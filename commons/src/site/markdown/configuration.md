## WCM Commons System Configuration

### Service user configuration

WCM Commons requires a service user mapping for accessing component properties in the `/apps` folder.

Create a principal-based service user mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

```
user.mapping=["io.wcm.wcm.commons:component-properties\=[sling-scripting]"]
```

The principal `sling-scripting` that comes with AEM can be used for this.

This configuration is required **on both author and publish instances**.
