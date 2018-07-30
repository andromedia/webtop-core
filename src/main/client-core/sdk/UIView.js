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
Ext.define('Sonicle.webtop.core.sdk.UIView', {
	alternateClassName: 'WTA.sdk.UIView',
	extend: 'WTA.sdk.BaseView',
	
	config: {
		dockableConfig: {
			/**
			 * @cfg {String} iconCls
			 * The icon class to be used to apply as container's iconCls.
			 */
			
			/**
			 * @cfg {String} title
			 * The title text to be used to apply as container's title.
			 * Can be a value or a template string that points to a resource.
			 */
			
			/**
			 * @cfg {Boolean} constrainToService
			 */
			constrainToService: false,
			
			/**
			 * @cfg {Boolean} modal
			 */
			modal: false
			
			/**
			 * @cfg {Object[]/Ext.panel.Tool[]} tools
			 * An array of {@link Ext.panel.Tool} configs/instances to be added to the tool area of the container.
			 */
		},
		
		/**
		 * @cfg {Boolean} promptConfirm
		 * This config controls the display of a confirm message on close.
		 * If True and if {@link #beforeviewclose event} returns that a close 
		 * operation would not be safe, a confirm message will be shown. 
		 */
		promptConfirm: true,
		
		/**
		 * @cfg {'ync'/'yn'} [confirm='ync']
		 * Controls confirm message buttons' appearance.
		 * Two values are allowed:
		 * - 'ync' - Yes+No+Cancel
		 * - 'yn' - Yes+No
		 */
		confirm: 'ync',
		
		/**
		 * @cfg {String} confirmMsg
		 * Custom confirm message to use.
		 */
		confirmMsg: null
	},
	
	/**
	 * @private
	 * @property {Boolean} ctInited
	 */
	ctInited: false,
	
	/**
	 * @event viewshow
	 * Fires after the view is shown.
	 * @param {WTA.sdk.UIView} this
	 */
	
	/**
	 * @event viewdiscard
	 * Fires after the user when prompted, chooses to discard current view.
	 * This event is fired before {@link #viewclose} event.
	 * @param {WTA.sdk.UIView} this
	 */
	
	/**
	 * @event viewclose
	 * Fires after the view is closed.
	 * @param {WTA.sdk.UIView} this
	 */
	
	viewModel: {
		data: {
			_viewTitle: null
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		if (Ext.isObject(cfg.dockableConfig)) {
			Ext.merge(me.config.dockableConfig, cfg.dockableConfig);
			delete cfg.dockableConfig;
		}
		me.callParent([cfg]);
		
		var vm = me.getVM(),
				dcfg = me.getDockableConfig();
		if (!cfg.title) {
			me.setBind({title: '{_viewTitle}'});
			vm.set('_viewTitle', me.buildViewTitle(dcfg.title) || '');
		}
		if (!cfg.iconCls && dcfg.iconCls) {
			me.iconCls = dcfg.iconCls;
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.on('added', function(s, ct) {
			if (!me.ctInited) me.initCt(ct);
		}, me, {single: true});
		me.on('removed', function(s, ct) {
			if (me.ctInited) me.cleanupCt(ct);
		}, me, {single: true});
	},
	
	/**
	 * @private
	 */
	initCt: function(ct) {
		var me = this;
		if (ct.isXType('window')) {
			if (!ct.rendered) {
				ct.title = me.getTitle();
				ct.iconCls = me.getIconCls();
			}
			ct.on('show', me.onCtShow, me);
			ct.on('beforeclose', me.onCtBeforeClose, me);
			ct.on('close', me.onCtClose, me);
			
			// Le toolbar non vengono più applicate al container ma bensì alla vista
			// stessa, ora la view è un panel non più un component
			/*
			if(me.tbar || me.fbar || me.lbar || me.rbar || me.dockedItems || me.buttons) {
				Ext.apply(ct, {
					tbar: me.tbar,
					fbar: me.fbar,
					lbar: me.lbar,
					rbar: me.rbar,
					dockedItems: me.dockedItems,
					buttons: me.buttons,
					buttonAlign: me.buttonAlign,
					minButtonWidth: me.minButtonWidth
				});
				ct.bridgeToolbars(); // Force toolbar initialization on target component
				
				// Cleanup configured props
				var props = ['tbar','fbar','lbar','rbar','dockedItems','buttons'];
				for(var prop in props) delete me[prop];
			}
			*/
			
			// TODO: gestire i window group
			/*
			if(me.useWG) ct.on('hide', me.onCtWndHide, me);
			*/
		}
		me.ctInited = true;
	},
	
	/**
	 * @private
	 */
	cleanupCt: function(ct) {
		var me = this;
		if (ct.isXType('window')) {
			ct.un('show', me.onCtShow, me);
			ct.un('beforeclose', me.onCtBeforeClose, me);
			ct.un('close', me.onCtClose, me);
			ct.un('hide', me.onCtHide, me);
		}
		me.ctInited = false;
	},
	
	/**
	 * @private
	 */
	onCtShow: function() {
		var me = this;
		// TODO: gestire i window group
		/*
		if(me.useWG) {
			me.wg.each(function(wnd) {
				wnd.show();
			}, me);
		}
		*/
		me.fireEvent('viewshow', me);
	},
	
	/**
	 * @private
	 */
	onCtBeforeClose: function() {
		return this.onBeforeClose();
	},
	
	/*
	onCtBeforeClose__: function() {
		var me = this;
		// TODO: gestire i window group
		//if(me.useWG && me.hasWindows()) return false;
		if(me.promptConfirm && me.fireEvent('beforeviewclose', me) === false) {
			me.showConfirm();
			return false;
		}
		return true;
	},
	*/
	
	/**
	 * @private
	 */
	onCtClose: function() {
		var me = this;
		me.fireEvent('viewclose', me);
	},
	
	/**
	 * @private
	 */
	onCtHide: function() {
		// TODO: gestire i window group
		/*
		var me = this;
		if(me.useWG) {
			me.wg.hideAll();
		}
		*/
	},
	
	setTitle: function(title) {
		var me = this,
				ct = me.ownerCt;
		me.callParent(arguments);
		if (me.ctInited) {
			if (ct.isXType('window')) {
				ct.setTitle(title);
			}
		}
	},
	
	setIconCls: function(iconCls) {
		var me = this,
				ct = me.ownerCt;
		me.callParent(arguments);
		if (me.ctInited) {
			if (ct.isXType('window')) {
				ct.setIconCls(iconCls);
			}
		}
	},
	
	getViewTitle: function() {
		return this.getVM().get('_viewTitle');
	},
	
	setViewTitle: function(title) {
		this.getVM().set('_viewTitle', title);
	},
	
	/**
	 * Handler method executed on confirm continue (answer: yes).
	 * Child classes can override this method to implement their own custom logic.
	 */
	onConfirmView: function() {
		this.closeView(false);
	},
	
	/**
	 * Handler method executed on confirm discard (answer: no).
	 * Child classes can override this method to implement their own custom logic.
	 */
	onDiscardView: function() {
		var me = this;
		me.fireEvent('viewdiscard', me);
		me.closeView(false);
	},
	
	/**
	 * Handler method executed on container before close.
	 * Child classes can override this method to implement their own custom logic.
	 */
	onBeforeClose: function() {
		var me = this;
		// TODO: gestire i window group
		/*
		if(me.useWG && me.hasWindows()) return false;
		*/
		if (me.getPromptConfirm() === false) return true;
		if (me.canCloseView() === false) {
			me.showConfirm();
			return false;
		}
		return true;
	},
	
	/**
	 * Method executed during container close. Returning false will prompt 
	 * (if enabled, see {@link #promptConfirm}) a confirm message.
	 * Child classes can override this method to implement their own custom logic.
	 * @return {Boolean}
	 */
	canCloseView: function() {
		return true;
	},
	
	/**
	 * Closes this view. If specified, the 'prompt' parameter overwrites
	 * current {@link #promptConfirm} definition.
	 * @param {Boolean} [prompt] Allow to override prompt confirm behaviour.
	 */
	closeView: function(prompt) {
		var me = this;
		if (prompt !== undefined) me.promptConfirm = prompt;
		if (me.ctInited) me.ownerCt.close();
	},
	
	/**
	 * Hides this view. no 'prompt' will be shown
	 */
	hideView: function() {
		var me = this;
		if (me.ctInited) me.ownerCt.hide();
	},
	
	/**
	 * Shows the confirm message.
	 */
	showConfirm: function() {
		var me = this, msg;
		
		if (me.confirm === 'ync') {
			msg = me.confirmMsg || WT.res('confirm.save');
			WT.confirmYNC(msg, function(bid) {
				if (bid === 'yes') {
					me.onConfirmView();
				} else if(bid === 'no') {
					me.onDiscardView();
				}
			});
		} else {
			msg = me.confirmMsg || WT.res('confirm.areyousure');
			WT.confirm(msg, function(bid) {
				if (bid === 'yes') {
					me.onDiscardView();
				}
			});
		}
	},
	
	/**
	 * Shows this view, rendering it first within the container Component.
	 * @param {Function} [callback] A callback function to call after the view Component is displayed.
	 * @param {Object} [scope] The scope (`this` reference) in which the callback is executed. Defaults to this Component.
	 * @return {Ext.Component} the container Component
	 */
	showView: function(cb, scope) {
		var me = this,
				ct = me.ownerCt;
		if (me.ctInited) {
			if (ct.isXType('window')) {
				return ct.isVisible() ? ct.focus() : ct.show(null, cb, scope || me);
			}
		}
	},
	
	buildViewTitle: function(dockTitle) {
		return this.resTitle(dockTitle);
	},
	
	privates: {
		resTitle: function(key) {
			var me = this,
					sid = Ext.isString(me.mys) ? me.mys : me.mys.ID;
			return key ? WT.resTpl(sid, key) : null;
		}
	}
});
