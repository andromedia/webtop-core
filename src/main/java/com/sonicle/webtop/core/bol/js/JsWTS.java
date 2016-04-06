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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.web.json.JsonResult;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author malbinola
 */
public class JsWTS {
	public String securityToken;
	public String layoutClassName;
	public HashMap<String, String> appPaths = new HashMap<>();
	public ArrayList<String> appRequires = new ArrayList<>();
	public ArrayList<JsWTS.Service> services = new ArrayList<>();
	public ArrayList<Settings> servicesOptions = new ArrayList<>();
	public ArrayList<Permissions> servicesPerms = new ArrayList<>();
	public String defaultService;
	
	public String toJson() {
		return JsonResult.gson.toJson(this);
	}
	
	public static class ServiceUserOptions {
		public String viewClassName;
		public String modelClassName;
		
		public ServiceUserOptions(String viewClassName, String modelClassName) {
			this.viewClassName = viewClassName;
			this.modelClassName = modelClassName;
		}
	}
	
	public static class Permissions extends HashMap<String, Actions> {
		
	}
	
	public static class Actions extends HashMap<String, Object> {
		
	}
	
	public static class Service {
		public int index;
		public String id;
		public String xid;
		public String ns;
		public String path;
		public String localeClassName;
		public String serviceClassName;
		public String clientOptionsClassName;
		public ServiceUserOptions userOptions;
		public String name;
		public String description;
		public String version;
		public String build;
		public String company;
		public boolean maintenance;
	}
	
	public static class Settings extends HashMap<String, Object> {
		
		public Settings() {
			super();
		}
	}
}
