## Granite UI validators

Collection of CoralUI 3 validators that can be used in AEM 6.2 and up.


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
