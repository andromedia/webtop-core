/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.column.Icon', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.soiconcolumn',
	
	tdCls: 'so-'+'grid-cell-iconcolumn',
	innerCls: 'so-'+'grid-cell-inner-iconcolumn',
	
	/**
	 * @cfg {String} iconClsField
	 * The fieldName for getting the CSS class to apply to the icon image.
	 * To determine the class dynamically, configure the column with a `getIconCls` function.
	 */
	iconClsField: null,
	
	/**
	 * @cfg {Function} getIconCls
	 * A function which returns the CSS class to apply to the icon image.
	 */
	getIconCls: null,
	
	/**
	 * @cfg {String} tipField
	 * The fieldName for getting the tooltip to apply to the icon image.
	 * To determine the class dynamically, configure the column with a `getTip` function.
	 */
	tipField: null,
	
	/**
	 * @cfg {Function} getTip
	 * A function which returns the tooltip to apply to the icon image.
	 */
	getTip: null,
	
	/**
	 * @cfg {Number} iconSize
	 * The icon size in px.
	 */
	iconSize: 16,
	
	constructor: function() {
		this.scope = this;
		this.callParent(arguments);
	},
	
	defaultRenderer: function(value, cellValues) {
		var me = this,
				cssPrefix = 'so-',
				cls = cssPrefix + 'grid-iconcolumn',
				size = me.iconSize,
				rec = cellValues ? cellValues.record : null,
				ico = me.evalValue(me.getIconCls, me.iconClsField, value, rec),
				ttip = me.evalValue(me.getTip, me.tipField, value, rec, null);
		
		if(ico) cls += ' ' + ico;
		return '<div class="'+cls+'" style="width:'+size+'px;height:'+size+'px"'+(ttip ? ' data-qtip="'+ttip+'"' : '')+' />';
	},
	
	evalValue: function(getFn, field, value, rec, fallback) {
		if(Ext.isFunction(getFn)) {
			return getFn.apply(this, [value, rec]);
		} else if(rec && !Ext.isEmpty(field)) {
			return rec.get(field);
		} else {
			return (fallback === undefined) ? value : fallback;
		}
	}
});
