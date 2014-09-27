/**
 * The MultiField is an editable list of form fields for editing multi-value properties.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Allow drag and drop into subfields</li>
 * </ul>
 */
io.wcm.wcm.ui.form.MultiField = CQ.Ext.extend(CQ.form.MultiField, {

  getDropTargets : function() {
    var subDropTargets = [];
    try {
      // Build a list of drop targets from all sub fields
      this.items.each(function(item/* , index, length */) {
        if (item instanceof CQ.form.MultiField.Item) {
          subDropTargets = subDropTargets.concat(item.field.getDropTargets());
        }
      }, this);
    }
    catch (e) {
    }
    return subDropTargets;
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.multifield", io.wcm.wcm.ui.form.MultiField);
