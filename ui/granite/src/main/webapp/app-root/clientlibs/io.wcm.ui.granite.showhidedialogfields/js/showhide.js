/**
 * Extension to the standard dropdown/select and checkbox components. It enables hidding/unhidding of other components 
 * based on the selection made in the dropdown/select or the checkbox state.
 *
 * Usage:
 * - Add the class wcmio-dialog-showhide to the dropdown/select or checkbox element
 * - Add the data attribute wcmio-dialog-showhide-target to the element.
 *   The value should be a selector, usually a specific class name (the target class),
 *   to find all possible target elements that can be shown/hidden.
 * - Add the target class to each target component that can be shown/hidden
 * - Add the class hidden to each target component to make them initially hidden
 * - Add the data attribute showhidetargetvalue to each target component, the value should equal the value of the select
 *   option that will unhide this element. In case of a checkbox use "true" or "false" for checkbox state.
 *
 * To ensure the show/hide features is applied only to a certain group of elements in the edit dialog,
 * when it cannot be ensured that the CSS class is unique across the whole dialog (e.g. in multi fields):
 * - Add the data attribute wcmio-dialog-showhide-parent to the dropdown/select element, value should be
 *   a selector that identifies a common parent element. Only dialog fields that are children of that element 
 *   (e.g. a container) will be processed.
 *
 * This only supports Coral UI 3.
 */
(function(document, $) {
  "use strict";

  // when dialog gets injected
  $(document).on("foundation-contentloaded", function(e) {
    // if there is already an inital value make sure the according target element becomes visible
    showHideHandler($(".wcmio-dialog-showhide", e.target));
  });

  function showHideHandler(el) {
    el.each(function(i, element) {
      if ($(element).is("coral-select") || $(element).is("coral-checkbox")) {
        Coral.commons.ready(element, function(component) {
          showHide(component, element);
          component.on("change", function() {
            showHide(component, element);
          });
        });
      }
    });
  }

  function showHide(component, element) {
    // get the selector to find the target elements.
    var target = $(element).data("wcmioDialogShowhideTarget");
    if (!target) {
      return;
    }

    // optional: get the selector to find the comment parent element
    var parentSelector = $(element).data("wcmioDialogShowhideParent");

    // check if all elements in the dialog, or only those that whare the same parent should be processed
    var $target;
    var $parent = [];
    if (parentSelector) {
      $parent = $(element).parents(parentSelector);
    }
    if ($parent.length > 0) {
      $target = $(target, $parent);
    }
    else {
      $target = $(target);
    }

    var value;
    if ($(element).is("coral-checkbox") && typeof component.checked !== "undefined") {
      value = component.checked ? "true" : "false";
    }
    else if (typeof component.value !== "undefined") {
      value = component.value;
    }
    else if (typeof component.getValue === "function") {
      value = component.getValue();
    }

    $target.each(function(index, element) {
      // make sure all unselected target elements are hidden.
      // unhide the target element that contains the selected value as data-showhidetargetvalue attribute
      var show = element && element.dataset.showhidetargetvalue === value;
      setVisibilityAndHandleFieldValidation($(element), show);
    });
  }

  /**
   * Shows or hides an element based on parameter "show" and toggles validations if needed. If element
   * is being shown, all VISIBLE fields inside it whose validation is false would be changed to set the validation
   * to true. If element is being hidden, all fields inside it whose validation is true would be changed to
   * set validation to false.
   *
   * @param {jQuery} $element Element to show or hide.
   * @param {Boolean} show <code>true</code> to show the element.
   */
   function setVisibilityAndHandleFieldValidation($element, show) {

     // if target element is part of a field wrapper, target the wrapper instead
     var $fieldWrapperParent = $element.parent(".coral-Form-fieldwrapper");
     if ($fieldWrapperParent.length > 0) {
       $element = $fieldWrapperParent;
     }

     if (show) {
       $element.removeClass("hide");
       $element.find("input[aria-required=false], coral-multifield[aria-required=false], foundation-autocomplete[aria-required=false]")
           .filter(":not(.hide>input)")
           .filter(":not(input.hide)")
           .filter(":not(foundation-autocomplete[aria-required=false] input)")
           .filter(":not(.hide>coral-multifield)")
           .filter(":not(input.coral-multifield)")
           .each(function(index, field) {
             toggleValidation($(field));
           });
     }
     else {
       $element.addClass("hide");
       $element.find("input[aria-required=true], coral-multifield[aria-required=true], foundation-autocomplete[required]")
           .filter(":not(foundation-autocomplete[required] input)")
           .each(function(index, field) {
             toggleValidation($(field));
           });
     }
   }

  /**
   * If the form element is not shown we have to disable the required validation for that field.
   *
   * @param {jQuery} $field To disable / enable required validation.
   */
  function toggleValidation($field) {
    var propRequired = $field.prop("required");
    var ariaRequired = $field.attr("aria-required");
    var isRequired = (ariaRequired === "true");

    if ($field.is("foundation-autocomplete") && propRequired !== "undefined") {
      if (propRequired === true) {
        $field[0].required = false;
        $field.attr("aria-required", false);
      }
      else if (propRequired === false) {
        $field[0].required = true;
        $field.removeAttr("aria-required");
      }
    }
    else if (typeof ariaRequired !== "undefined") {
      $field.attr("aria-required", String(!isRequired));
    }

    var api = $field.adaptTo("foundation-validation");
    if (api) {
      if (isRequired) {
        api.checkValidity();
      }
      api.updateUI();
    }
  }

})(document, Granite.$);
