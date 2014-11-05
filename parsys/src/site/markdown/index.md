## About WCM Parsys

AEM paragraph system based on path configuration in page components.

### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.wcm.parsys</artifactId>
  <version>0.5.0</version>
</dependency>
```

### Documentation

* [Usage][usage]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The wcm.io can be used in AEM in the same way the AEM-builtin paragraph system is used, and is supported by the Touch UI and Classic UI edit modes in the same fashion.

Differences to the AEM-builtin paragraph system:

* It does not use the design mode to configure the allowed components. Instead the metadata that described which components are allowed in which template at which position is stored as nodes in the page components and delivered together with the application.
* Allowed components can be defined based on path patterns and parent component relations.
* Additional rules for allowing or deniying components can be provided via OSGi factory configurations.
* This parsys does not support column controls or iparsys inheritance, but is only a simple paragraph system which allows full control about the markup generated for the child resources and the new area.
* Written in Sightly.


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
