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
package com.sonicle.webtop.core.servlet;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.PushEndpoint;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.bol.js.JsWTSPrivate;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.servlet.BeforeStart;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.util.LoggerUtils;
import freemarker.template.Template;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Start extends AbstractServlet {
	public static final String URL = "start"; // This must reflect web.xml!
	private static final Logger logger = WT.getLogger(Start.class);
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoggerUtils.setContextDC(RunContext.getRunProfileId());
		WebTopApp wta = getWebTopApp(request);
		SettingsManager setm = wta.getSettingsManager();
		WebTopSession wts = SessionContext.getCurrent(true);
		
		try {
			logger.trace("Servlet: start [{}]", ServletHelper.getSessionID(request));
			
			wts.initPrivateEnvironment(request);
			Locale locale = wts.getLocale();
			Map vars = new HashMap();
			
			String userTitle = null;
			if (wts.getProfileId() != null) {
				UserProfile.Data ud = WT.getUserData(wts.getProfileId());
				if (ud != null) {
					userTitle = ud.getDisplayName();
				}
			}
			
			// Page + loader variables
			AbstractServlet.fillPageVars(vars, locale, userTitle, null);
			vars.put("loadingMessage", wta.lookupResource(locale, "tpl.start.loading"));
			
			// Startup variables
			JsWTSPrivate jswts = new JsWTSPrivate();
			jswts.sessionId = wts.getId();
			jswts.securityToken = wts.getCSRFToken();
			jswts.contextPath = ServletHelper.getBaseUrl(request);
			jswts.pushUrl = PathUtils.concatPaths(wts.getClientUrl(), PushEndpoint.URL);
			wts.fillStartup(jswts);
			vars.put("WTS", LangUtils.unescapeUnicodeBackslashes(jswts.toJson()));
			
			ServletUtils.setHtmlContentType(response);
			ServletUtils.setCacheControlPrivateNoCache(response);
			
			// Load and build template
			Template tpl = WT.loadTemplate(CoreManifest.ID, "tpl/page/private.html");
			tpl.process(vars, response.getWriter());
			
		} catch(Exception ex) {
			logger.error("Error", ex);
		}
	}
	
	private static class MaintenanceException extends Exception {}
}
