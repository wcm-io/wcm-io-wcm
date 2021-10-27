/**
 * @jest-environment jsdom
 */
// simple mocking to capture registered validators
var validators = {};
window.Granite = {
  "$": function(obj) {
    return {
      "adaptTo": function(to) {
        if (to == "foundation-registry") {
          return {
            "register": function(name, validator) {
              validators[validator.selector] = validator.validate;
            }
          }
        }
      },
      "val": function() {
        return obj.value;
      },
      "attr": function(param) {
        return obj[param];
      }
    };
  },
  "I18n": {
    "get": function(arg) {
      return arg;
    }
  }
};

// load validation script from clientlib
require('../../../main/webapp/app-root/clientlibs/io.wcm.ui.granite.validation/js/validation.js');

// helper methods for assertion
var assert = require('assert');
var assertValid = function(validate, value) {
  it('valid: ' + value, function() {
    assert.equal(validate({"value":value}), null);
  });
}
var assertInvalid = function(validate, value) {
  it('invalid: ' + value, function() {
    assert.notEqual(validate({"value":value}), null);
  });
}

// assert validator implementation
describe('wcmio.email', function() {
  var validate = validators['[data-foundation-validation="wcmio.email"]'];
  assertValid(validate, "firstname.lastname@mycompany.com");
  assertInvalid(validate, "http://myhost");
  assertInvalid(validate, "http://www.domain.com/path1");
  assertInvalid(validate, "https://myhost/path1/path2");
  assertInvalid(validate, "ftp://myhost");
  assertInvalid(validate, "//myhost");
  assertInvalid(validate, "mailto:firstname.lastname@mycompany.com");
  assertInvalid(validate, "tel:+49 123 456789");
  assertInvalid(validate, "simplestring");
  assertInvalid(validate, "www.domain.com");
  assertInvalid(validate, "/content/site1/page1");
  assertInvalid(validate, "/content/dam/sample.jpg");
  assertInvalid(validate, "/ns1:this/is/ns2:a/path");
});

describe('wcmio.url', function() {
  var validate = validators['[data-foundation-validation="wcmio.url"]'];
  assertInvalid(validate, "firstname.lastname@mycompany.com");
  assertValid(validate, "http://myhost");
  assertValid(validate, "http://www.domain.com/path1");
  assertValid(validate, "https://myhost/path1/path2");
  assertValid(validate, "ftp://myhost");
  assertValid(validate, "//myhost");
  assertValid(validate, "mailto:firstname.lastname@mycompany.com");
  assertValid(validate, "tel:+49 123 456789");
  assertInvalid(validate, "simplestring");
  assertInvalid(validate, "www.domain.com");
  assertInvalid(validate, "/content/site1/page1");
  assertInvalid(validate, "/content/dam/sample.jpg");
  assertInvalid(validate, "/ns1:this/is/ns2:a/path");
});

describe('wcmio.path', function() {
  var validate = validators['[data-foundation-validation="wcmio.path"]'];
  assertInvalid(validate, "firstname.lastname@mycompany.com");
  assertInvalid(validate, "http://myhost");
  assertInvalid(validate, "http://www.domain.com/path1");
  assertInvalid(validate, "https://myhost/path1/path2");
  assertInvalid(validate, "ftp://myhost");
  assertInvalid(validate, "//myhost");
  assertInvalid(validate, "mailto:firstname.lastname@mycompany.com");
  assertInvalid(validate, "tel:+49 123 456789");
  assertInvalid(validate, "simplestring");
  assertInvalid(validate, "www.domain.com");
  assertValid(validate, "/content/site1/page1");
  assertValid(validate, "/content/dam/sample.jpg");
  assertValid(validate, "/ns1:this/is/ns2:a/path");
});

describe('wcmio.pattern', function() {
  var validate = validators['[data-foundation-validation="wcmio.pattern"]'];

  it('matches pattern', function() {
    assert.equal(validate({
      "value": "abc",
      "data-wcmio-pattern": "^ab.*$",
      "data-wcmio-patternmessage": "Invalid."
    }), null);
  });
  
  it('does not match pattern', function() {
    assert.equal(validate({
      "value": "def",
      "data-wcmio-pattern": "^ab.*$",
      "data-wcmio-patternmessage": "Invalid."
    }), "Invalid.");
  });
  
});
