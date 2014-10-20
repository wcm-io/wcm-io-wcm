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
 * Form field for editing styled text information (rich text).
 * <p>Enhancements over AEM version:</p>
 * <ul>
 *   <li>Set link browse protocols to http|https|mailto</li>
 *   <li>Use strong and em instead of b and i for bold/italic</li>
 *   <li>Ensure that all <br> tags are replaced with XHTML-conformant <br/> tags</li>
 * </ul>
 */
io.wcm.wcm.ui.form.RichText = CQ.Ext.extend(CQ.form.RichText, {

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};
    var defaults = {

      "htmlRules": {

        // Ensure valid XHTML markup with <strong> and <em> tags
        "docType": {
          "baseType": "xhtml",
          "version": "1.0",
          "typeConfig": {
            "useSemanticMarkup": true,
            "semanticMarkupMap": {
              "b": "strong",
              "i": "em"
            },
            "isXhtmlStrict": true
          }
        },

        // Default link dialog configuration
        "links": {
          "cssMode": "keep",
          "protocols": [
            "http://",
            "https://",
            "mailto:"
          ],
          "targetConfig": {
            "mode": "blank"
          },
          "ensureInternalLinkExt": false
        }

      },

      "rtePlugins": {

        /*
        // character formatting
        "format": {
          "features": [
            "bold",
            "italic"
          ]
        },

        // paragraph formatting
        "justify": {
          "features": [
            "justifyleft",
            "justifyright",
            "justifycenter"
          ]
        },

        // links
        "links": {
          "features": [
            "modifylink",
            "unlink",
            "anchor"
          ],
          "trimLinkSelection": true
        },

        // lists
        "lists": {
          "features": [
            "ordered",
            "unordered",
            "indent",
            "outdent"
          ]
        },

        // paragraph styles
        "paraformat": {
          "features": [
            "paraformat"
          ],
          "formats": [
            {
              "tag": "p",
              "description": CQ.I18n.getMessage("Paragraph")
            },
            {
              "tag": "h1",
              "description": CQ.I18n.getMessage("Heading 1")
            },
            {
              "tag": "h2",
              "description": CQ.I18n.getMessage("Heading 2")
            },
            {
              "tag": "h3",
              "description": CQ.I18n.getMessage("Heading 3")
            }
          ]
        },

        // character styles
        "styles": {
          "features": [
            "styles"
          ],
          "styles": [
            {
              "cssName": "class1",
              "text": "Class 1"
            },
            {
              "cssName": "class2",
              "text": "Class 2"
            }
          ]
        },

        // edit features
        "edit": {
          "features": [
            "cut",
            "copy",
            "paste-default",
            "paste-plaintext",
            "paste-wordhtml"
          ],
          "defaultPasteMode": "plaintext",
          "stripHtmlTags": true
        },

        // misc. tools
        "misctools": {
          "features": [
            "specialchars",
            "sourceedit"
          ]
        },

        // find & replace
        "findreplace": {
          "features": [
            "find",
            "replace"
          ]
        }
        */

      }

    };
    CQ.Util.applyDefaults(config, defaults);

    // call super constructor
    io.wcm.wcm.ui.form.RichText.superclass.constructor.call(this, config);

  }

});

CQ.Ext.reg("io.wcm.wcm.ui.richtext", io.wcm.wcm.ui.form.RichText);
