## Sightly components


### wcmInit

On the author instance, this component initalizes the edit mode and loads all required client libs. Supports both Touch UI and Classic UI.

Usage in your page component Sightly template:

```html
<head>
  <meta data-sly-resource="${'.' @ resourceType='/apps/adaptto/components/global/wcmInit' }"
      data-sly-unwrap></meta>
</head>
```

#### ClassicUI custom widget clientlibs

If you want have specifying a set of custom client library categories (only useful for Classic UI) you can create a subcomponent
and specifiy additonal property.

Component definition for subcomponent:

```json
{
  "jcr:primaryType": "cq:Component",
  "sling:resourceSuperType": "/apps/wcm-io/wcm/commons/components/global/wcmInit"
}

```

Markup for subcomponent:

```html
<head
    data-sly-use.render="render.html"
    data-sly-call="${render.head
        @ clientLibCategoriesClassicUI='cq.wcm.edit,myapp.widgets1,myapp.widgets2'}"
    data-sly-unwrap>  
</head>
```


### page

Base page component which defines a simplified page properties dialog with only basic features.

You can inherit from this in you page components:

```json
{
  "jcr:primaryType": "cq:Component",
  "sling:resourceSuperType": "/apps/wcm-io/wcm/commons/components/global/page"
}

```
