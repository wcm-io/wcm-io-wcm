/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
            var componentPath = result[i];
            // Classic UI expected component path without leading /apps/ or /libs/
            if (componentPath.indexOf("/apps/")==0 || componentPath.indexOf("/libs/")==0) {
              componentPath = componentPath.substring(6);
            }
            allowed.push(componentPath);
          }
        }
      }
    }

  };

}();
