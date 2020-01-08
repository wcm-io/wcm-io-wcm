## Granite UI validators

Collection of CoralUI 3 validators.


### E-Mail validator

Validates a string against a predefined E-Mail pattern.

```json
"email": {
  "sling:resourceType": "granite/ui/components/coral/foundation/form/textfield",
  "name": "./email",
  "fieldLabel": "E-Mail",
  "validation": ["wcmio.email"]
}
```
Examples for matching values:

* `firstname.lastname@mycompany.com`


### URL validator

Validates a string against a predefined URL pattern.

```json
"url": {
  "sling:resourceType": "granite/ui/components/coral/foundation/form/textfield",
  "name": "./url",
  "fieldLabel": "URL",
  "validation": ["wcmio.url"]
}
```

Examples for matching values:

* `http://www.mydomain.com`
* `https://www.mydomain.com/path1/page1.html`
* `//www.mydomain.com/path2`
* `mailto:firstname.lastname@mycompany.com`
* `tel:+123 456 789`


### Path validator

Validates a string against a predefined content path pattern.

```json
"url": {
  "sling:resourceType": "granite/ui/components/coral/foundation/form/textfield",
  "name": "./path",
  "fieldLabel": "Path",
  "validation": ["wcmio.path"]
}
```

Examples for matching values:

* `/content/site1/page1`
* `/content/dam/sample.jpg`


### Pattern validator

Validates a string against a custom regex pattern. A custom validation message can be provided as well.

```json
"hex": {
  "sling:resourceType": "granite/ui/components/coral/foundation/form/textfield",
  "name": "./hex",
  "fieldLabel": "Hex Number",
  "validation": ["wcmio.pattern"],
  "granite:data": {
    "wcmio-pattern": "^[0-9a-fA-F]+$",
    "wcmio-patternmessage": "Must be a valid hexadecimal number."
  }
}
```
