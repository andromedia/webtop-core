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
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.util.LoggerUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class PrivateRequest extends BaseRequest {
	public static final String URL = "/service-request"; // Shiro.ini must reflect this URI!
	public static final String URL_LEGACY = "/ServiceRequest";
	private static final Logger logger = WT.getLogger(PrivateRequest.class);
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoggerUtils.setContextDC(RunContext.getRunProfileId());
		WebTopApp wta = getWebTopApp(request);
		WebTopSession wts = SessionContext.getCurrent(true);
		
		try {
			String service = ServletUtils.getStringParameter(request, "service", true);
			String action = ServletUtils.getStringParameter(request, "action", true);
			Boolean nowriter = ServletUtils.getBooleanParameter(request, "nowriter", false);
			String optionsProfile = ServletUtils.getStringParameter(request, "optionsProfile", false);
			
			if (logger.isTraceEnabled()) {
				logger.trace("processRequest [{}, {}, {}, {}, {}, {}, {}]", request.getMethod(), wts.getId(), wts.getProfileId(), service, action, nowriter, optionsProfile);
			}
			
			if (StringUtils.isBlank(optionsProfile)) {
				// Retrieves instantiated service
				BaseService instance = wts.getPrivateServiceById(service);
				
				// Gets method and invokes it...
				MethodInfo meinfo = getMethod(instance.getClass(), service, action, nowriter);
				invokeMethod(instance, meinfo, service, request, response);
				
			} else {
				ServiceManager svcm = wta.getServiceManager();
				
				// Retrieves instantiated userOptions service (session context away)
				UserProfileId pid = new UserProfileId(optionsProfile);
				BaseUserOptionsService instance = svcm.instantiateUserOptionsService(wts.getUserProfile(), wts.getId(), service, pid);
				
				// Gets method and invokes it...
				MethodInfo meinfo = getMethod(instance.getClass(), service, action, nowriter);
				invokeMethod(instance, meinfo, service, request, response);
			}
			
		} catch(Throwable t) {
			logger.warn("Error in serviceRequest servlet", t);
			throw new ServletException(LangUtils.getDeepestCause(t));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
}
