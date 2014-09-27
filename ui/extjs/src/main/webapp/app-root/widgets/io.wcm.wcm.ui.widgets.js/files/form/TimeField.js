/**
 * Form field for time value.
 */
io.wcm.wcm.ui.form.TimeField = CQ.Ext.extend(CQ.Ext.form.TimeField, {

  /**
   * @cfg {String} hiddenFormat Format of datetime used to store value in hidden field and submitted to server
   * (defaults to 'Y-m-d\\TH:i:sP' that is ISO8601 format)
   */
  hiddenFormat: 'Y-m-d\\TH:i:s.000P',

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};
    var defaults = {
      "format": "H:i"
    };
    CQ.Util.applyDefaults(config, defaults);

    io.wcm.wcm.ui.form.TimeField.superclass.constructor.call(this, config);
  },

  /**
   * @private
   * Renders additional hidden field which contains formatted date/time value that can be posted to sling
   */
  onRender:function(ct, position) {
    // don't run more than once
    if (this.isRendered) {
      return;
    }

    // render underlying hidden field
    io.wcm.wcm.ui.form.TimeField.superclass.onRender.call(this, ct, position);

    // add hidden type hint
    if (!this.hiddenValueElement) {
      this.hiddenValueElement = new CQ.Ext.form.Hidden({
        name: this.name,
        renderTo: ct
      });
    }

    // prevent primary field from being submitted
    this.el.dom.removeAttribute("name");

    // add hidden type hint
    // add additional hidden form field for JCR attribute type information
    if (!this.typeHintElement) {
      this.typeHintElement = new CQ.Ext.form.Hidden({
        name: this.name + "@TypeHint",
        value: "Time",
        renderTo: ct
      });
    }

    // we're rendered flag
    this.isRendered = true;

    // update hidden field
    this.updateHidden();
  },

  /**
   * @param {Mixed} val Value to set
   * Sets the value of this field
   */
  setValue: function(val) {
    if (!val) {
      io.wcm.wcm.ui.form.TimeField.superclass.setValue.call(this, null);
      this.updateHidden();
      return;
    }

    if ('number' === typeof val) {
      val = new Date(val);
    }

    if ('string' === typeof val) {
      if (val === "now") {
        val = new Date();
      } else {
        var v = Date.parse(val);
        if (!v) {
          v = Date.parseDate(val, this.format);
        }
        if (!v) {
          v = Date.parseDate(val, this.hiddenFormat);
        }
        if (v) {
          val = new Date(v);
        }
      }
    }
    val = val ? val : new Date();
    // don't use "val instanceof Date" as that doesn't work between iframes
    if (typeof val.setDate === "function" && typeof val.setTime === "function") {
      io.wcm.wcm.ui.form.TimeField.superclass.setValue.call(this, val);
    }

    this.updateHidden();
  },

  /**
   * @private Handles blur event
   */
  onBlur: function(ct) {
    this.updateHidden();
  },

  /**
   * @private Updates the underlying hidden field value
   */
  updateHidden: function() {
    if (this.isRendered) {
      var t = this.getValue();
      if (t && !this.isDate(t)) {
        if (t === "now") {
          t = new Date();
        } else {
          t = Date.parseDate(t, this.format);
        }
      }
      this.dateValue = t;
      this.hiddenValueElement.el.dom.value = (this.dateValue && (typeof this.dateValue.format === "function")) ?
          this.dateValue.format(this.hiddenFormat) : '';
    }
  },

  /**
   * private
   * Checks if the object is a date by not using instanceof
   */
  isDate: function(obj) {
      return (typeof(obj)!=='undefined') && ((typeof obj.setDate === "function") && (typeof obj.setTime === "function") &&
          (typeof obj.getFirstDateOfMonth === "function") && (typeof obj.getFirstDayOfMonth === "function"));
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.timefield", io.wcm.wcm.ui.form.TimeField);
