// simple mocking to capture registered validators
var validators = {};
window.Granite = {
  "$": function(arg) {
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
        return arg;
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
require('../../../main/webapp/clientlibs-root/io.wcm.ui.granite.validation/js/validation.js');

// helper methos for assertion
var assert = require('assert');
var assertValid = function(validate, value) {
  it('valid: ' + value, function() {
    assert.equal(validate(value), null);
  });
}
var assertInvalid = function(validate, value) {
  it('invalid: ' + value, function() {
    assert.notEqual(validate(value), null);
  });
}

// assert validator implementation
describe('wcmio.email', function() {
  var validate = validators['[data-validation="wcmio.email"]'];
  assertValid(validate, "firstname.lastname@mycompany.com");
  assertInvalid(validate, "simplestring");
});

describe('wcmio.url', function() {
  var validate = validators['[data-validation="wcmio.url"]'];
  assertValid(validate, "http://myhost");
  assertValid(validate, "http://www.domain.com/path1");
  assertValid(validate, "https://myhost/path1/path2");
  assertValid(validate, "ftp://myhost");
  // TODO: does not work yet
  //assertValid(validate, "//myhost");
  //assertValid(validate, "mailto:firstname.lastname@mycompany.com");
  //assertValid(validate, "tel:+49 123 456789");
  assertInvalid(validate, "simplestring");
  assertInvalid(validate, "www.domain.com");
  assertInvalid(validate, "/only/a/path");
});
