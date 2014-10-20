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


### Override markup of the paragraph system

The markup provided by the paragraph system without any override is very simple:

```html
<div data-sly-use.parsys="io.wcm.wcm.parsys.controller.Parsys"
    data-sly-list.item="${parsys.items}" data-sly-unwrap>

  <div style="clear:both" data-sly-test="${item.newArea}"></div>

  <div class="${item.cssClassName}"
      data-sly-resource="${item.resourcePath @ resourceType=item.resourceType}"></div>

</div>
```

This generated the wrapping markup of the paragraph systems for both contained components an the new area. Before the new area an additional DIV with `clear:both` is inserted to avoid floating issues with the new area. To overwrite this markup you can create a subcomponent and define you own markup.

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
