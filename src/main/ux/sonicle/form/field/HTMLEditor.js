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

Ext.define('Sonicle.form.field.HTMLEditor', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Ext.ux.form.TinyMCETextArea'
	],
	alias: ['widget.sohtmleditor'],
	
	layout: 'border',
    border: false,
	
	tmce: null,
	toolbar: null,
	
	initComponent: function() {
		var me=this;
		
		me.callParent(arguments);
		
		me.toolbar=Ext.create('Ext.toolbar.Toolbar',{
			region: 'north',
			items: [
				{
					xtype: 'combo', 
					width: 150,
					store: Ext.create('Ext.data.Store', {
						fields: ['fn'],
						data : [
							{ fn: "Arial" },
							{ fn: "Comic Sans MS"},
							{ fn: "Courier New"},
							{ fn: "Helvetica"},
							{ fn: "Tahoma"},
							{ fn: "Times New Roman"},
							{ fn: "Verdana"}
						]
					}),
					forceSelection: true,
					autoSelect: true,
					displayField: 'fn',
					valueField: 'fn',
					queryMode: 'local',
					listeners: {
						'select': function(c,r,o) {
							me.execCommand('fontname',false,r.get('fn'));
						}
					}
				},
				{
					xtype: 'combo', 
					width: 70,
					store: Ext.create('Ext.data.Store', {
						fields: ['fs'],
						data : [
							{ fs: "6" },
							{ fs: "8"},
							{ fs: "10"},
							{ fs: "12"},
							{ fs: "14"},
							{ fs: "16"},
							{ fs: "18"},
							{ fs: "20"},
							{ fs: "22"},
							{ fs: "24"}
						]
					}),
					forceSelection: true,
					autoSelect: true,
					displayField: 'fs',
					valueField: 'fs',
					queryMode: 'local',
					listeners: {
						'select': function(c,r,o) {
							me.execCommand('fontsize',false,r.get('fs'));
						}
					}
				},
				'-',
				{
					xtype: 'button', iconCls: 'wtmail-icon-bold-xs' , text: 'B',
					handler: function() {
						me.execCommand('bold');
					}
				},
				{
					xtype: 'button', iconCls: 'wtmail-icon-italic-xs' , text: 'I',
					handler: function() {
						me.execCommand('italic');
					}
				},
				{
					xtype: 'button', iconCls: 'wtmail-icon-underline-xs' , text: 'U',
					handler: function() {
						me.execCommand('underline');
					}
				}
			]
		});

		me.tmce=Ext.create({
			xtype: 'tinymce_textarea',
			region: 'center',
			fieldStyle: 'font-family: Courier New; font-size: 12px;',
			style: { border: '0' },
			tinyMCEConfig: {
				plugins: [
				"advlist autolink lists link image charmap print preview hr anchor pagebreak",
				"searchreplace wordcount visualblocks visualchars code fullscreen",
				"insertdatetime media nonbreaking save table contextmenu directionality",
				"emoticons template paste textcolor"
				],

				toolbar: false,
				//toolbar1: "newdocument fullpage | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify | styleselect formatselect fontselect fontsizeselect",
				//toolbar2: "cut copy paste | searchreplace | bullist numlist | outdent indent blockquote | undo redo | link unlink anchor image media code | inserttime preview | forecolor backcolor",
				//toolbar3: "table | hr removeformat | subscript superscript | charmap emoticons | print fullscreen | ltr rtl | spellchecker | visualchars visualblocks nonbreaking template pagebreak restoredraft",
				menubar: false,
				toolbar_items_size: 'small'
			},
			value: 'This is the WebTop-TinyMCE HTML Editor'

		});
		
		me.add(me.toolbar);
		me.add(me.tmce);
		
	},
	
	execCommand: function(cmd, ui, value, obj) {
		var ed = tinymce.get(this.tmce.getInputId());
		ed.execCommand(cmd,ui,value,obj);
	}
	
	
});