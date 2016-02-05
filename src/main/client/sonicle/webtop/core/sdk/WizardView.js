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
Ext.define('Sonicle.webtop.core.view.WizardView', {
	alternateClassName: 'WT.sdk.WizardView',
	extend: 'WT.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.sdk.WizardPage',
		'Sonicle.form.Spacer'
	],
	
	layout: 'card',
	bodyPadding: 5,
	confirm: 'yn',
	
	config: {
		//TODO: completare descrizioni config
		
		/**
		 * @cfg {Boolean} useTrail
		 * 'true' to enable display of step trails, 'false' otherwise. Default is true
		 */
		useTrail: true,
		infiniteTrail: false,
		disableNavAtEnd: true,
		applyButton: true,
		doButtonText: WT.res('wizard.btn-do.lbl'),
		doAction: false,
		lockDoAction: true,
		endHeaderText: WT.res('wizard.end.hd')
	},
	
	pages: null,
	
	/**
	 * @private
	 */
	isMultiPath: false,
	
	initComponent: function() {
		var me = this,
				vm = me.getVM();
		
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, {
			pathgroup: WTF.radioGroupBind(null, 'path', me.getId()+'-pathgroup')
		}));
		
		Ext.apply(me, {
			tbar: [
				'->',
				{
					xtype: 'tbtext',
					reference: 'trail'
				}
			],
			bbar: [
				'->',
				{
					reference: 'btnback',
					xtype: 'button',
					text: WT.res('wizard.btn-back.lbl'),
					handler: function() {
						me.navigate(-1);
					},
					disabled: true
				}, {
					reference: 'btnforw',
					xtype: 'button',
					text: WT.res('wizard.btn-forw.lbl'),
					handler: function() {
						me.navigate(1);
					},
					disabled: true
				}, {
					reference: 'btndo',
					xtype: 'button',
					text: me.getDoButtonText(),
					handler: function(s) {
						if(me.getLockDoAction()) s.setDisabled(true);
						me.onDoClick();
					},
					hidden: !me.hasDoAction(),
					disabled: true
				}, {
					reference: 'btncancel',
					xtype: 'button',
					text: WT.res('wizard.btn-cancel.lbl'),
					handler: function() {
						if(!me.isPathSelection() && !me.hasNext(1)) me.fireEvent('wizardcompleted', me);
						me.closeView();
					}
				}
			]
		});
		me.callParent(arguments);
		
		me.isMultiPath = false;
		if(!Ext.isArray(me.pages)) {
			Ext.iterate(this.pages, function(k,v) {
				if(!Ext.isArray(v)) Ext.Error.raise('Invalid pages definition object');
			});
			me.isMultiPath = true;
		}
		
		if(me.isMultiPath) {
			me.initPathPage();
		} else {
			me.initPages();
		}
	},
	
	initChooserPage: function() {
		var me = this;
		me.add(me.createPathPage('', {}));
		me.onNavigate('path');
	},
	
	initPages: function() {
		var me = this,
				curpath = me.getVM().get('path');
		me.add(me.createPages(curpath));
		me.onNavigate(me.getPages()[0]);
	},
	
	getPages: function() {
		var me = this,
				curpath = me.getVM().get('path');
		if(me.isMultiPath) {
			return me.pages[curpath];
		} else {
			return me.pages;
		}
	},
	
	createPages: function(path) {
		return [];
	},
	
	createPathPage: function(title, fieldLabel, fieldItems) {
		var me = this,
				items = [];
		
		Ext.iterate(fieldItems, function(obj,i) {
			items.push({
				name: me.getId()+'-pathgroup',
				inputValue: obj.value,
				boxLabel: obj.label
			});
		});
		
		return {
			itemId: 'path',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: title
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				items: [{
					xtype: 'fieldset',
					title: fieldLabel,
					items: [{
						xtype: 'radiogroup',
						bind: {
							value: '{pathgroup}'
						},
						columns: 1,
						items: items
					}]
				}]
			}]
		};
	},
	
	createEndPage: function() {
		var me = this;
		return {
			itemId: 'end',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: me.getEndHeaderText()
			}, {
				xtype: 'sospacer'
			}, {
				reference: 'log',
				xtype: 'textarea',
				hidden: !me.getApplyButton(),
				readOnly: true,
				anchor: '100% -40'
			}]
		};
	},
	
	isPathSelection: function(page) {
		page = page || this.getActivePage();
		return page === 'path';
	},
	
	getPagesCount: function() {
		var pages = this.getPages();
		return pages ? pages.length : 0;
	},
	
	getPageIndex: function(page) {
		var pages = this.getPages();
		return pages ? pages.indexOf(page) : -1;
	},
	
	getActivePage: function() {
		var page = this.getLayout().getActiveItem();
		return page.getItemId();
	},
	
	getPageCmp: function(itemId) {
		return this.getComponent(itemId);
	},
	
	hasDoAction: function() {
		return Ext.isString(this.getDoAction());
	},
	
	hasNext: function(dir, page) {
		var me = this,
				next = (arguments.length === 1) ? me.computeNext(dir) : me.computeNext(dir, page);
		return !Ext.isEmpty(next);
	},
	
	/**
	 * @private
	 * @param {type} dir Navigation direction: 1 -> forward, -1 -> backward.
	 * @param {type} [page] 
	 * @return {String} The next page or null if there are no more pages.
	 */
	computeNext: function(dir, page) {
		var me = this, index;
		if(arguments.length === 1) {
			page = me.getActivePage();
		}
		index = me.getPageIndex(page) + dir;
		return ((index >= 0) && (index < me.getPagesCount())) ? me.getPages()[index] : null;
	},
	
	/**
	 * @private
	 */
	navigate: function(dir) {
		var me = this,
				prev = me.getActivePage(),
				next = me.computeNext(dir);
		
		if(me.isPathSelection(prev)) {
			me.initPages();
		} else {
			if(me.fireEvent('beforenavigate', me, dir, next, prev) !== false) {
				me.onNavigate(next);
				me.fireEvent('navigate', me, dir, next, prev);
			}
		}
	},
	
	onNavigate: function(page) {
		var me = this,
				btnCancel = me.lookupReference('btncancel'),
				btnBack = me.lookupReference('btnback'),
				btnForw = me.lookupReference('btnforw'),
				btnDo = me.lookupReference('btndo'),
				hasPrev = me.hasNext(-1, page),
				hasNext = me.hasNext(1, page);
		
		me.activatePage(page);
		if(me.isPathSelection()) {
			btnBack.setDisabled(true);
			btnForw.setDisabled(false);
		} else if(!hasNext && me.getDisableNavAtEnd()) {
			btnBack.setDisabled(true);
			btnForw.setDisabled(true);
		} else {
			btnBack.setDisabled(!hasPrev);
			btnForw.setDisabled(!hasNext);
		}
		if(me.hasDoAction()) btnDo.setDisabled(hasNext);
		if(!hasNext) btnCancel.setText(WT.res('wizard.btn-close.lbl'));
		if(!me.isPathSelection() && me.getUseTrail()) me.updateTrail();
	},
	
	updateTrail: function() {
		var me = this,
				trail = me.lookupReference('trail'),
				curr = me.getPageIndex(me.getActivePage()) +1,
				count = (me.getInfiniteTrail()) ? '?' : me.getPagesCount();
		trail.update(Ext.String.format(WT.res('wizard.trail.lbl'), curr, count));
	},
	
	activatePage: function(page) {
		var me = this,
				lay = me.getLayout();
		lay.setActiveItem(page);
		lay.getActiveItem().doLayout();
	},
	
	onDoClick: function() {
		var me = this,
				page = me.getPageCmp('end');
		
		if(!me.hasDoAction()) return;
		me.wait();
		WT.ajaxReq(me.mys.ID, me.getDoAction(), {
			params: {
				step: 'end'
			},
			callback: function(success, json) {
				me.unwait();
				page.lookupReference('log').setValue(json.data);
				if(success) {
					me.fireEvent('dosuccess', me);
				} else {
					me.fireEvent('dofailure', me, json.data);
				}
			}
		});
	},
	
	canCloseView: function() {
		var me = this;
		// Returns false to stop view closing and to display a confirm message.
		if(me.isPathSelection()) return true;
		if(me.hasNext(1)) return false;
		return true;
	}
});
