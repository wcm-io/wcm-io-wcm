/**
 * Hiden form field.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Fix problem with default value in CQ54 </li>
 * </ul>
 */
io.wcm.wcm.ui.form.Hidden = CQ.Ext.extend(CQ.Ext.form.Hidden, {

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};

    // set value to defaultValue to fix problem in CQ54 with applying default values
    if (config.value===undefined && config.defaultValue!==undefined) {
      config.value = config.defaultValue;
    }

    io.wcm.wcm.ui.form.Hidden.superclass.constructor.call(this, config);
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.hidden", io.wcm.wcm.ui.form.Hidden);
