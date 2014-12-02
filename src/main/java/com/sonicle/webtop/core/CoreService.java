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
package com.sonicle.webtop.core;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsFeedback;
import com.sonicle.webtop.core.bol.js.JsOptionsService;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsWhatsnewTab;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.CoreLocaleKey;
import com.sonicle.webtop.core.sdk.SuperEnvironment;
import com.sonicle.webtop.core.sdk.JsOptions;
import com.sonicle.webtop.core.sdk.Service;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreService extends Service {
	
	public static final Logger logger = Service.getLogger(CoreService.class);

	@Override
	public void initialize() {
		
	}

	@Override
	public void cleanup() {
		
	}
	
	public void processGetLocales(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		Locale locale = env.getSession().getLocale();
		
		try {
			//TODO: handle locales dinamically
			ArrayList<JsSimple> data = new ArrayList<>();
			data.add(new JsSimple("it_IT", env.lookupResource(CoreManifest.ID, locale, MessageFormat.format(CoreLocaleKey.LOCALE_X, "it_IT"))));
			data.add(new JsSimple("en_EN", env.lookupResource(CoreManifest.ID, locale, MessageFormat.format(CoreLocaleKey.LOCALE_X, "en_EN"))));
			new JsonResult("locales", data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetLocales", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processGetThemes(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			//TODO: handle themes dinamically
			ArrayList<JsSimple> data = new ArrayList<>();
			data.add(new JsSimple("aria", "Aria"));
			data.add(new JsSimple("classic", "Classic"));
			data.add(new JsSimple("crisp", "Crisp"));
			data.add(new JsSimple("crisp-touch", "Crisp Touch"));
			data.add(new JsSimple("gray", "Gray"));
			data.add(new JsSimple("neptune", "Neptune"));
			data.add(new JsSimple("neptune-touch", "Neptune Touch"));
			new JsonResult("themes", data).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action GetThemes", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processGetLooksAndFeels(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			//TODO: handle lafs dinamically
			ArrayList<JsSimple> lafs = new ArrayList<>();
			lafs.add(new JsSimple("default", "Default"));
			new JsonResult("lafs", lafs).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action GetLooksAndFeels", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processGetOptionsUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		Connection con = null;
		
		try {
			ArrayList<JsSimple> data = new ArrayList<>();
			UserProfile up = env.getProfile();
			if(up.isSystemAdmin()) {
				con = env.getCoreConnection();
				UserDAO udao = UserDAO.getInstance();
				List<OUser> users = udao.selectAll(con);
				String id = null, descr = null;
				for(OUser user : users) {
					id = Principal.buildName(user.getDomainId(), user.getUserId());
					descr = MessageFormat.format("{0} ({1})", user.getDisplayName(), id);
					data.add(new JsSimple(id, descr));
				}
				
			} else {
				//TODO: maybe define a permission to other users to control others options
				data.add(new JsSimple(up.getId(), up.getDisplayName()));
			}
			new JsonResult("users", data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetOptionsUsers", ex);
			new JsonResult(false).printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processGetOptionsServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			
			ArrayList<JsOptionsService> data = new ArrayList<>();
			data.add(new JsOptionsService("com.sonicle.webtop.core", "wt", "WebTop Services", "Sonicle.webtop.core.view.CoreOptions"));
			if(!UserProfile.isSystemAdmin(id)) data.add(new JsOptionsService("com.sonicle.webtop.calendar", "wtcal", "Calendario", "Sonicle.webtop.calendar.CalendarOptions"));
			if(!UserProfile.isSystemAdmin(id)) data.add(new JsOptionsService("com.sonicle.webtop.mail", "wtmail", "Posta Elettronica", "Sonicle.webtop.mail.MailOptions"));
			new JsonResult(data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetOptionsServices", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processGetUserServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		Locale locale = env.getSession().getLocale();
		
		ArrayList<JsSimple> items = new ArrayList<>();
		List<String> ids = env.getSession().getServices();
		for(String id : ids) {
			items.add(new JsSimple(id, env.lookupResource(id, locale, Service.RESOURCE_SERVICE_NAME)));
		}
		new JsonResult("services", items).printTo(out);
	}
	
	public void processFeedback(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			JsFeedback js = ServletUtils.getPayload(request, JsFeedback.class);
			logger.debug("message: {}", js.message);
			Thread.sleep(4000);
			new JsonResult().printTo(out);
			//new JsonResult(false, "Erroreeeeeeeeeeeeeeeeeeeeeee").printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error executing action Feedback", ex);
			new JsonResult(false, "Unable to send feedback report.").printTo(out);
		}
	}
	
	public void processGetWhatsnewTabs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsWhatsnewTab> tabs = null;
		JsWhatsnewTab tab = null;
		String html = null;
		SuperEnvironment env = getSuperEnv();
		UserProfile profile = env.getProfile();
		
		try {
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			tabs = new ArrayList<>();
			List<String> ids = env.getSession().getServices();
			for(String id : ids) {
				if(full || env.getManager().needWhatsnew(id, profile)) {
					html = env.getManager().getWhatsnewHtml(id, env.getProfile(), full);
					if(!StringUtils.isEmpty(html)) {
						tab = new JsWhatsnewTab(id);
						tab.title = env.lookupResource(id, profile.getLocale(), CoreLocaleKey.SERVICE_NAME);
						tabs.add(tab);
					}
				}
			}
			new JsonResult(tabs).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetWhatsnewTabs", ex);
			new JsonResult(false, "Unable to get What's New info.").printTo(out);
		}
	}
	
	public void processGetWhatsnewHTML(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			String html = env.getManager().getWhatsnewHtml(id, env.getProfile(), full);
			out.println(html);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetWhatsnewHTML", ex);
		}
	}
	
	public void processTurnOffWhatsnew(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		
		try {
			UserProfile profile = env.getProfile();
			List<String> ids = env.getSession().getServices();
			for(String id : ids) {
				env.getManager().resetWhatsnew(id, profile);
			}
			
		} catch (Exception ex) {
			logger.error("Error executing action TurnOffWhatsnew", ex);
		} finally {
			new JsonResult().printTo(out);
		}
	}
	
	public void processSetToolComponentWidth(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String serviceId = ServletUtils.getStringParameter(request, "serviceId", true);
			Integer width = ServletUtils.getIntParameter(request, "width", true);
			
			UserProfile profile = getSuperEnv().getProfile();
			CoreUserSettings cus = new CoreUserSettings(profile.getDomainId(), profile.getUserId(), serviceId);
			cus.setViewportToolWidth(width);
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action SetTheme", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processManageTFA(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		TFAManager tfam = env.getTFAManager();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals("generate")) {
				String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
				if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_EMAIL)) {
					String email = ServletUtils.getStringParameter(request, "emailAddress", true);
					tfam.initTFAUsingEmail(env.getSession(), email);
					
				} else if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_GOOGLEAUTH)) {
					tfam.initTFAUsingGoogleAuth(env.getSession());
				}
				new JsonResult(true).printTo(out);
				
			} else if(crud.equals("activate")) {
				String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
				Integer code = ServletUtils.getIntParameter(request, "code", true);
				boolean enabled = tfam.activateTFA(env.getSession(), deliveryMode, code);
				new JsonResult(enabled).printTo(out);
				
			} else if(crud.equals("untrustthis")) {
				TrustedDeviceCookie tdc = tfam.readTrustedDeviceCookie(env.getProfile(), request);
				if(tdc == null) throw new Exception("This device is already untrusted");
				tfam.removeTrustedDevice(env.getProfile(), tdc.deviceId);
				tfam.clearTrustedDeviceCookie(env.getProfile(), response);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("untrustothers")) {
				TrustedDeviceCookie thistdc = tfam.readTrustedDeviceCookie(env.getProfile(), request);
				ArrayList<JsTrustedDevice> tds = tfam.getTrustedDevices(env.getProfile());
				for(JsTrustedDevice td: tds) {
					if((thistdc != null) && (td.deviceId.equals(thistdc.deviceId))) continue;
					tfam.removeTrustedDevice(env.getProfile(), td.deviceId);
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error executing action ManageTFA", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processServerEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SuperEnvironment env = getSuperEnv();
		List<ServiceMessage> messages = new ArrayList();
		
		try {
			messages = env.getSession().pollMessageQueue();
			
		} catch (Exception ex) {
			logger.error("Error executing action ServerEvents", ex);
		} finally {
			String raw = JsonResult.gson.toJson(messages);
			new JsonResult(raw).printTo(out);
		}
		
		/*
		WebSocketMessage wsm=getFullEnv().getSession().pollMessageQueue();
		if (wsm!=null) {
			JsonResult jsr=new JsonResult(wsm);
			jsr.printTo(out);
		} else {
			new JsonResult().printTo(out);
		}
		*/
	}
	
}
