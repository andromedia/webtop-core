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
package com.sonicle.webtop.core.sdk;

import com.sonicle.webtop.core.RunContext;
import com.sonicle.webtop.core.WT;
import java.util.Locale;
import net.sf.qualitycheck.Check;

/**
 *
 * @author malbinola
 */
public abstract class BaseServiceManager {
	private final String serviceId;
	private final RunContext context;
	private final UserProfile.Id targetProfile;
	
	public BaseServiceManager(String serviceId, RunContext context) {
		this(serviceId, context, null);
	}
	
	public BaseServiceManager(String serviceId, RunContext context, UserProfile.Id targetProfileId) {
		this.serviceId = Check.notNull(serviceId);
		this.context = Check.notNull(context);
		this.targetProfile = targetProfileId;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public RunContext getRunContext() {
		return context;
	}
	
	public UserProfile.Id getRunProfileId() {
		return context.getProfileId();
	}
	
	public UserProfile.Id getTargetProfileId() {
		return (targetProfile != null) ? targetProfile : getRunProfileId();
	}
	
	public ServiceManifest getManifest() {
		return WT.getManifest(getServiceId());
	}
	
	/**
	 * Returns the localized string associated to the key.
	 * @param locale The requested locale.
	 * @param key The resource key.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(Locale locale, String key) {
		return WT.lookupResource(getServiceId(), locale, key);
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param locale The requested locale.
	 * @param key The resource key.
	 * @param escapeHtml True to apply HTML escaping.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(Locale locale, String key, boolean escapeHtml) {
		return WT.lookupResource(getServiceId(), locale, key, escapeHtml);
	}
}
