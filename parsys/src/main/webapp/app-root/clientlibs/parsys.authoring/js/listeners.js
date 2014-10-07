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
              allowed.push(result[i]);
            }
          }
        },
        async: false
      });
    }
  };

}(jQuery, this));
