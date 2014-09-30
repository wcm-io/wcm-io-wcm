/**
 * Authoring GUI listener methods (for Touch UI)
 */
(function ($, window, undefined) {

  window.wcmio = window.wcmio || {};
  window.wcmio.wcm = window.wcmio.wcm || {};
  window.wcmio.wcm.parsys = window.wcmio.wcm.parsys || {};
  
  /**
   * Handles update component list event, which is issued whenever a new bar/area is rendered.
   * Fetches the allowed components via AJAX call based on allowed components in page component definition.
   */
  window.wcmio.wcm.parsys.updateComponentListHandler = function(cell, allowed, componentList) {
    // get destination path from current component path
    if (this.path && allowed) {

      // split page path from local path
      var startIndex = this.path.indexOf("/jcr:content");
      if (startIndex===-1) {
        startIndex = this.path.length;
      }
      var pagePath = this.path.substring(0, startIndex);
      var localPath = this.path.substring(startIndex + 1);

      // get allowed components based on relative path from backend
      $.ajax({
        url: pagePath + ".wcmio-parsys-components.json?path=" + encodeURI(localPath),
        success: function(result) {
          if (result) {
            // remove all elements
            allowed.splice(0, allowed.length);
            // add elements from new array
            for (var i=0; i<result.length; i++) {
              var componentPath = result[i];
              // unlike CQ5, CQ6 touch edit mode seems to expect an absolute component path
              if (componentPath.indexOf("/apps/")!=0) {
                componentPath = "/apps/" + componentPath;
              }
              allowed.push(componentPath);
            }
          }
        },
        async: false
      });
    }
  };
  
}(jQuery, this));
