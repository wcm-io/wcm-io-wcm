## Parsys usage

### Paragraph system in sightly template

To use the paragraph system in your sightly template:

```html
<div data-sly-resource="${'./content'
    @ resourceType='/apps/wcm-io/wcm/parsys/components/parsys'}"
    data-sly-unwrap></div>
```


### Configure allowed components

To define which components are allowed in your paragraph system you create a node `wcmio:parsysConfig` in your page component (similar to nodes like `cq:dialog` and `cq:editConfig`). Example:

```json
"wcmio:parsysConfig": {
  "jcr:primaryType": "nt:unstructured",
  "paths": {

    /* Allowed components in jcr:content/content node */
    "content": {
      "allowedChildren": [
        "/apps/myapp/components/component1"
      ]
    },

    /* Allowed components in jcr:content/leftcol/teasers node */
    "leftcol_teasers": {
      "path": "jcr:content/lefcol/teasers",
      "allowedChildren": [
        "/apps/myapp/components/component1",
        "/apps/myapp/components/component2"
      ]
    },

    /* Allowed components in a nested parsys that is contained in component1 */
    "nested_parsys": {
      "pattern": "^jcr:content/.*$",
      "parentAncestorLevel": 2,
      "allowedParents": [
        "/apps/myapp/components/component1"
      ],
      "allowedChildren": [
        "/apps/myapp/components/subcomponent1",
        "/apps/myapp/components/subcomponent2"
      ]
    }

  }
}
```

A `wcmio:parsysConfig` contains always a `paths` node defining the allowance definition. Each definition has node name, and a set of properties defining the allowance. Supported properties:

* **allowedChildren**: Array with resource types of the components allowed for this path definition.

* **path**: Optional. Defines the exact relative of the paragraph system inside the page relative to page root node (without leading slash but including `jcr:content`). If this property is not set it is derived from the node name of the path definition. In this case the node name is prefixed with `jcr:content/`. If you want to define a deeper path you have to specify the `path` property.

* **pattern**: Optional. Instead of the `path` property you can define a regular expression for matching the relative path (again, without leading slash, but including `jcr:content`). The component is allowed for all paragraph systems for which the path pattern matches.

* **allowedParents**: Optional. Defines an additional restriction to the path or path pattern to allow components only below certain parent components. The parent components are references by their resource types.

* **parentAncestorLevel**: Optional. Only supported together with `allowedParents`. Two possible values are allowed:
    * **1**: `allowedParents` restriction is applied to the paragraph system component itself (the direct parent of the component)
    * **2**: `allowedParents` restriction is applied to the component that contains the paragraph system (the grand-parent of the component)

If you have an inheritance hierarchy of page components using `sling:resourceSuperType` you can define a `wcmio:parsysConfig` on each level of this hierarchy. The definitions are merged together according to the inheritance hierarchy. Thus it is possible to centrally define global allowed components rules for all page components in your application.

Via the OSGi factory configuration _wcm.io Paragraph System Configuration Extension_ it is possible to define additional allowed components definitions via the Felix Console. It supports the same properties as listed above, and additionally:

* **pageComponentPath**: Resource type of the page component this configuration should apply to

* **deniedChildren**: List of components not allowed at the defined position. This allows neglecting allowances defined in the page component itself.


### Customize the paragraph system markup

You can set the following property in the component node of the paragraph system to customize the markup:

* **wcmio:parsysGenerateDefaultCss**: Generates CSS classes by default: "section" on each paragraph/new area and "clear:both" on a new area. This is a boolean property and defaults to true if not set.

* **wcmio:parsysParagraphCss**: Defines additional custom CSS classes to be set on each paragraph.

* **wcmio:parsysNewAreaCss**: Defines additional custom CSS classes to be set on the new area.

* **wcmio:parsysParagraphElement**: Sets the element name to be used for the element wrapping each paragraph component. Defaults to "div" if not set.

* **wcmio:parsysWrapperElement**: Sets the element name to be used for the element wrapping the whole paragraph system. If not set no wrapping element is generated for the paragraph system.

* **wcmio:parsysWrapperCss**: Defines custom CSS classes to be set on the wrapper element.


### Override markup of the paragraph system

When customizing the markup (element names and CSS classes) of the paragraph system is not enough you can override the markup of the paragraph system.

In this example the paragraph systems uses an UL with nested LIs for each component with some extra CSS classes:

```html
<ul class="link-list" data-sly-use.parsys="io.wcm.wcm.parsys.controller.Parsys"
    data-sly-list.item="${parsys.items}">

  <li class="link-list-item ${item.cssClassName}“
      data-sly-resource="${item.resourcePath @ resourceType=item.resourceType}"></li>

</ul>
```


### Paragraph system with editbar (Classic UI)

If you want to use editbars instead of rollover edit mode for the paragraph system you can use:

```html
<div data-sly-resource="${'./content'
    @ resourceType='/apps/wcm-io/wcm/parsys/components/parsysEditbar'}"
    data-sly-unwrap></div>
```

This is only supported in Classic UI.
