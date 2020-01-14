## WCM Commons System Configuration

### Service user configuration

The Component Property Resolver 

The URL handler requires a service user mapping for detecting accessing component properties on publish instances.

Create a service user mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

```
user.mapping=["io.wcm.wcm.commons:component-properties\=sling-scripting"]
```

The service user `sling-scripting` that comes with AEM can be used for this.

This configuration is required **on both author and publish instances**.