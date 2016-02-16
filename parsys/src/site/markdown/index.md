## About WCM Parsys

AEM paragraph system based on path configuration in page components.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.parsys/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.parsys)


### Documentation

* [Usage][usage]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The wcm.io can be used in AEM in the same way the AEM-builtin paragraph system is used, and is supported by the Touch UI and Classic UI edit modes in the same fashion.

Differences to the AEM-builtin paragraph system:

* It does not use the design mode to configure the allowed components. Instead the metadata that described which components are allowed in which template at which position is stored as nodes in the page components and delivered together with the application.
* Allowed components can be defined based on path patterns and parent component relations.
* Additional rules for allowing or denying components can be provided via OSGi factory configurations.
* Via properties it is possible to change the decoration markup and CSS classes of the paragraph and paragraph items.
* This parsys does not support column controls or iparsys inheritance, it is only a simple paragraph system which allows full control about the markup generated for the child resources and the new area.
* Written in Sightly.


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
