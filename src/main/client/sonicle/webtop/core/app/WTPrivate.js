/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.app.WTPrivate', {
	override: 'Sonicle.webtop.core.app.WT',
	
	palette: [
		'AC725E','D06B64','F83A22','FA573C','FF7537','FFAD46','FAD165','FBE983',
		'4986E7','9FC6E7','9FE1E7','92E1C0','42D692','16A765','7BD148','B3DC6C',
		'9A9CFF','B99AFF','A47AE2','CD74E6','F691B2','CCA6AC','CABDBF','C2C2C2',
		'FFFFFF'
	],
	
	getColorPalette: function() {
		return this.palette;
	},
	
	logout: function() {
		window.location = 'logout';
	},
	
	/**
	 * Checks against a resource if specified action is allowed.
	 * @param {String} [id] The service ID.
	 * @param {String} resource The resource name.
	 * @param {String} action The action name.
	 * @return {Boolean} 'True' if action is allowed, 'False' otherwise.
	 */
	isPermitted: function(id, resource, action) {
		if(arguments.length === 2) {
			action = resource;
			resource = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.isPermitted(resource, action);
	},
	
	/**
	 * Creates a displayable view.
	 * @param {String} sid The service ID.
	 * @param {String} name The class name or alias.
	 * @param {Object} opts
	 * @param {Object} opts.viewCfg
	 * @param {Object} opts.containerCfg
	 * @returns {Ext.window.Window} The container.
	 */
	createView: function(sid, name, opts) {
		opts = opts || {};
		var svc = this.getApp().getService(sid);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+sid+']');
		return this.getApp().viewport.getController().createView(svc, name, opts);
	},
	
	/**
	 * Returns the ID of currently active (displayed) service.
	 * @returns {String}
	 */
	getActiveService: function() {
		return this.getApp().viewport.getController().active;
	},
	
	/**
	 * Returns the layout in use.
	 * Value is taken from core variable 'layout'.
	 * @returns {String} The layout value.
	 */
	getLayout: function() {
		return WT.getVar('layout');
	},
	
	/**
	 * Returns the startDay in use (0=Sunday, 1=Monday).
	 * Value is taken from core variable 'startDay'.
	 * @returns {Integer} The startDay value.
	 */
	getStartDay: function() {
		return WT.getVar('startDay');
	},
	
	/**
	 * Returns the timezone in use.
	 * Value is taken from core variable 'timezone'.
	 * @returns {String} The timezone ID.
	 */
	getTimezone: function() {
		return WT.getVar('timezone');
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a short date. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'shortDateFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortDateFmt: function() {
		var fmt = WT.getVar('shortDateFormat');
		return (Ext.isEmpty(fmt)) ? 'd/m/Y' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a long date. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'longDateFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongDateFmt: function() {
		var fmt = WT.getVar('longDateFormat');
		return (Ext.isEmpty(fmt)) ? 'd/m/Y' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a short time. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'shortTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortTimeFmt: function() {
		//g:i A', e.g., '3:15 PM'. For 24-hour time format try 'H:i'
		var fmt = WT.getVar('shortTimeFormat');
		return (Ext.isEmpty(fmt)) ? 'H:i' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a long time. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'longTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongTimeFmt: function() {
		var fmt = WT.getVar('longTimeFormat');
		return (Ext.isEmpty(fmt)) ? 'H:i:s' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date+time format representing a short date+time.
	 * @returns {String} ExtJs format string.
	 */
	getShortDateTimeFmt: function() {
		return WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(); 
	},
	
	/**
	 * Returns if 24h time is in use.
	 * Value is taken from core variable 'use24HourTime'.
	 * @returns {Boolean}
	 */
	getUse24HourTime: function() {
		return WT.getVar('use24HourTime');
	},
	
	print: function(html) {
		Sonicle.PrintMgr.print(html);
	},
	
	optionsProxy: function(svc) {
		return WTF.apiProxy(svc, 'UserOptions', 'data', {
			extraParams: {
				options: true
			}
		});
	},
	
	componentLoader: function(svc, act, opts) {
		if(!opts) opts = {};
		return {
			url: WTF.requestBaseUrl(),
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			contentType: 'html',
			loadMask: true
		};
	}

});
