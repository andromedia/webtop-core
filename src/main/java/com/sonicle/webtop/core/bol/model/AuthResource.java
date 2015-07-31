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
package com.sonicle.webtop.core.bol.model;

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public class AuthResource {
	public static final String ACTION_READ = "READ";
	public static final String ACTION_WRITE = "WRITE";
	public static final String ACTION_EDIT = "EDIT";
	public static final String ACTION_DELETE = "DELETE";
	
	private final String name;
	private final LinkedHashSet<String> actions = new LinkedHashSet<>();
	
	public AuthResource(String name) {
		this.name = name.toUpperCase();
	}
	
	public AuthResource(String name, String[] actions) {
		this(name);
		for(String action : actions) {
			if(!StringUtils.isEmpty(action)) {
				this.actions.add(action.trim());
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public String[] getActions() {
		return actions.toArray(new String[actions.size()]);
	}
	
	public static String namespacedName(String serviceId, String resourceName) {
		return MessageFormat.format("{0}.{1}", serviceId, resourceName);
	}
	
	public static String permissionString(String resource, String action, String instance) {
		return MessageFormat.format("{0}:{1}:{2}", resource, StringUtils.defaultString(action, "*"), StringUtils.defaultString(instance, "*"));
	}
	
	/*
	public static String ns(String namespace, String name) {
		return MessageFormat.format("{0}.{1}", namespace, name);
	}
	*/
}
