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
package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.sdk.ServiceManifest;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class AbstractServlet extends HttpServlet {
	
	protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	protected WebTopApp getWebTopApp(HttpServletRequest request) {
		return WebTopApp.get(request);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	public static void fillSystemVars(Map vars, WebTopApp wta, Locale locale) {
		vars.put("systemInfo", wta.getSystemInfo());
		vars.put("serverInfo", wta.getServerInfo());
		vars.put("jdk", System.getProperty("java.version"));
		vars.put("appName", wta.getWebAppName());
	}
	
	public static void fillPageVars(Map vars, WebTopApp wta, Locale locale, String baseUrl) {
		fillPageVars(vars, wta, locale, null, baseUrl);
	}
	
	public static void fillPageVars(Map vars, WebTopApp wta, Locale locale, String userTitle, String baseUrl) {
		ServiceManifest manifest = wta.getServiceManager().getManifest(CoreManifest.ID);
		String title = wta.getPlatformName() + " " + manifest.getVersion().getMajor();
		if (!StringUtils.isBlank(userTitle)) title += " [" + userTitle + "]";
		vars.put("title", title);
		vars.put("version", manifest.getVersion());
		vars.put("baseUrl", baseUrl);
	}
	
	public static void fillPageVars(Map vars, String title, String baseUrl) {
		vars.put("title", title);
		vars.put("baseUrl", baseUrl);
	}
	
	/*
	public static void fillIncludeVars(Map vars, Locale locale, String theme, String lookAndFeel, boolean rightToLeft, boolean extJsDebug) {
		vars.put("language", locale.getLanguage());
		vars.put("theme", theme);
		vars.put("laf", lookAndFeel);
		vars.put("rtl", String.valueOf(rightToLeft));
		vars.put("debug", String.valueOf(extJsDebug));
	}
	*/
}
