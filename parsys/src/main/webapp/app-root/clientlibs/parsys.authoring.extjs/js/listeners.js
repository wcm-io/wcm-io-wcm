/**
 * Authoring GUI listener methods (for Classic UI)
 */
wcmio.wcm.parsys = function() {

  return {

    /**
     * Handles update component list event, which is issued whenever a new bar/area is rendered.
     * Fetches the result components via AJAX call based on result components in template definition.
     */
    updateComponentListHandler : function(cell, allowed, componentList) {

      // get destination path from current component path
      if (this.path && componentList && componentList.path && allowed) {

        // get local path inside page
        var startIndex = componentList.path.indexOf("/jcr:content");
        if (startIndex===-1) {
          startIndex = componentList.path.length;
        }
        var localPath = this.path.substring(startIndex + 1);

        CQ.Log.debug("wcmio.wcm.parsys.updateComponentListHandler: get components for local path " + localPath + " " +
            "(this.path=" + this.path + ", " + "componentList.path=" + componentList.path + ")");

        // get result components based on relative path from backend
        var url = CQ.Util.externalize(componentList.path + ".wcmio-parsys-components" +
            CQ.HTTP.EXTENSION_JSON + "?" + CQ.Ext.urlEncode({"path":localPath}));
        url = CQ.HTTP.noCaching(url);
        var result = CQ.Util.eval(CQ.HTTP.get(url));
        if (result) {
          // remove all elements
          allowed.splice(0, allowed.length);
          // add elements from new array
          for (var i=0; i<result.length; i++) {
            allowed.push(result[i]);
          }
        }
      }
    }

  };

}();
