/* 
 * Copyright (C) 2018 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.field.Search', {
	alternateClassName: 'WTA.ux.field.Search',
	extend: 'Ext.form.field.Text',
	alias: ['widget.wtsearchfield'],
	requires: [
		'Sonicle.form.trigger.Clear',
		'Sonicle.plugin.FieldTooltip'
	],
	
	plugins: ['sofieldtooltip'],
	selectOnFocus: true,
	width: 250,
	
	/**
     * @event query
	 * Fires when the user presses the ENTER key or clicks on the search icon.
	 * @param {Ext.form.field.Text} this
	 * @param {String} value
     */
	
	constructor: function(cfg) {
		var me = this;
		
		if (Ext.isEmpty(cfg.emptyText)) {
			cfg.emptyText = WT.res('textfield.search.emp');
		}
		cfg.triggers = Ext.apply(cfg.triggers || {}, {
			clear: {
				type: 'soclear',
				weight: -1,
				hideWhenEmpty: true,
				hideWhenMouseOut: true
			},
			search: {
				cls: Ext.baseCSSPrefix + 'form-search-trigger',
				handler: function(s) {
					me.fireQuery(s.getValue());
				}
			}
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.on('clear', me.onClear, me);
		me.on('specialkey', me.onSpecialKey, me);
	},
	
	destroy: function() {
		var me = this;
		me.un('clear', me.onClear);
		me.un('specialkey', me.onSpecialKey);
		me.callParent();
	},
	
	privates: {
		onClear: function(s) {
			this.fireQuery(null);
		},
		
		onSpecialKey: function(s, e) {
			if (e.getKey() === e.ENTER) this.fireQuery(s.getValue());
		},

		fireQuery: function(value) {
			this.fireEvent('query', this, value);
		}
	}
});