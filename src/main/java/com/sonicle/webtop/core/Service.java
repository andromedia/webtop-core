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

import com.sonicle.commons.MailUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.CoreEnvironment;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.bol.ActivityGrid;
import com.sonicle.webtop.core.bol.CausalGrid;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsActivity;
import com.sonicle.webtop.core.bol.js.JsCausal;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsFeedback;
import com.sonicle.webtop.core.bol.js.JsGridSync;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.js.JsRole;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsWhatsnewTab;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.bol.model.InternetRecipient;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TimeZone;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	private static final Logger logger = WT.getLogger(Service.class);
	private CoreManager core;
	private CoreServiceSettings ss;
	private CoreUserSettings us;
	
	private WebTopApp getApp() {
		return ((CoreEnvironment)getEnv()).getApp();
	}
	
	@Override
	public void initialize() throws Exception {
		UserProfile profile = getEnv().getProfile();
		core = new CoreManager(getApp());
		ss = new CoreServiceSettings(SERVICE_ID, profile.getDomainId());
		us = new CoreUserSettings(profile.getId());
	}

	@Override
	public void cleanup() throws Exception {
		
	}

	@Override
	public ClientOptions returnClientOptions() {
		UserProfile profile = getEnv().getProfile();
		ClientOptions co = new ClientOptions();
		
		co.put("wtUpiProviderWritable", core.isUserInfoProviderWritable());
		co.put("wtWhatsnewEnabled", ss.getWhatsnewEnabled());
		co.put("wtOtpEnabled", ss.getOTPEnabled());
		
		co.put("profileId", profile.getStringId());
		co.put("domainId", profile.getDomainId());
		co.put("userId", profile.getUserId());
		
		co.put("theme", us.getTheme());
		co.put("layout", us.getLayout());
		co.put("laf", us.getLookAndFeel());
		co.put("desktopNotification", us.getDesktopNotification());
		
		co.put("language", profile.getLanguageTag());
		co.put("timezone", profile.getTimeZone().getID());
		co.put("startDay", us.getStartDay());
		co.put("shortDateFormat", us.getShortDateFormat());
		co.put("longDateFormat", us.getLongDateFormat());
		co.put("shortTimeFormat", us.getShortTimeFormat());
		co.put("longTimeFormat", us.getLongTimeFormat());
		
		return co;
	}
	
	
	
	public void processLookupLanguages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		LinkedHashMap<String, JsSimple> items = new LinkedHashMap<>();
		Locale locale = ((CoreEnvironment)getEnv()).getSession().getLocale();
		Locale loc = null;
		String lang = null;
		
		try {
			for(AppLocale apploc : WT.getInstalledLocales()) {
				loc = apploc.getLocale();
				lang = loc.getLanguage();
				if(!items.containsKey(lang)) {
					//items.put(lang, new JsSimple(lang, loc.getDisplayLanguage(locale)));
					items.put(lang, new JsSimple(apploc.getId(), apploc.getLocale().getDisplayName(locale)));
				}
			}
			new JsonResult("languages", items.values(), items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupLanguages", ex);
			new JsonResult(false, "Unable to lookup languages").printTo(out);
		}
	}
	
	public void processLookupTimezones(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {	
			String normId = null;
			int off;
			for(TimeZone tz : WT.getTimezones()) {
				normId = StringUtils.replace(tz.getID(), "_", " ");
				off = tz.getRawOffset()/3600000;
				items.add(new JsSimple(tz.getID(), MessageFormat.format("{0} (GMT{1}{2})", normId, (off<0) ? "-" : "+", Math.abs(off))));
			}
			new JsonResult("timezones", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupTimezones", ex);
			new JsonResult(false, "Unable to lookup timezones").printTo(out);
		}
	}
	
	public void processLookupThemes(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = null;
		
		try {
			items = core.listThemes();
			new JsonResult("themes", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupThemes", ex);
			new JsonResult(false, "Unable to lookup themes").printTo(out);
		}
	}
	
	public void processLookupLayouts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = null;
		
		try {
			items = core.listLayouts();
			new JsonResult("layouts", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupLayouts", ex);
			new JsonResult(false, "Unable to lookup layouts").printTo(out);
		}
	}
	
	public void processLookupLAFs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = null;
		
		try {
			items = core.listLAFs();
			new JsonResult("lafs", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupLAFs", ex);
			new JsonResult(false, "Unable to lookup look&feels").printTo(out);
		}
	}
	
	public void processLookupTextEncodings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			SortedMap<String, Charset> charsets = Charset.availableCharsets();
			for(Charset charset : charsets.values()) {
				if(!charset.canEncode()) continue;
				items.add(new JsSimple(charset.name(), charset.displayName(Locale.ENGLISH)));
			}
			new JsonResult("encodings", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupTextEncodings", ex);
			new JsonResult(false, "Unable to lookup available text encodings").printTo(out);
		}
	}
	
	public void processLookupDomains(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			/*
			if(getRunContext().isSysAdmin()) {
				// WebTopAdmin can access to all domains
				if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
				List<ODomain> domains = core.listDomains(true);
				for(ODomain domain : domains) {
					items.add(new JsSimple(domain.getDomainId(), JsSimple.description(domain.getDescription(), domain.getDomainId())));
				}
			} else {
				// Domain users can only access to their domain
				ODomain domain = core.getDomain(up.getDomainId());
				items.add(new JsSimple(domain.getDomainId(), JsSimple.description(domain.getDescription(), domain.getDomainId())));
			}
			*/
			
			if(wildcard && RunContext.isSysAdmin()) {
				items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			}
			List<ODomain> domains = core.listDomains(true);
			for(ODomain domain : domains) {
				items.add(new JsSimple(domain.getDomainId(), JsSimple.description(domain.getDescription(), domain.getDomainId())));
			}
			new JsonResult("domains", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupDomains", ex);
			new JsonResult(false, "Unable to lookup domains").printTo(out);
		}
	}
	
	public void processLookupDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsRole> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean users = ServletUtils.getBooleanParameter(request, "users", true);
			boolean groups = ServletUtils.getBooleanParameter(request, "groups", true);
			String domainId = ServletUtils.getStringParameter(request, "domainId", null);
			
			if(!RunContext.isSysAdmin()) {
				domainId = up.getDomainId();
			}
			
			if(wildcard) items.add(JsRole.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			List<Role> roles = core.listRoles(domainId, users, groups);
			for(Role role : roles) {
				items.add(new JsRole(role));
			}
			
			new JsonResult("roles", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupDomainRoles", ex);
			new JsonResult(false, "Unable to lookup roles").printTo(out);
		}
	}
	
	public void processLookupDomainUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			String domainId = ServletUtils.getStringParameter(request, "domainId", null);
			
			List<OUser> users = null;
			if(RunContext.isSysAdmin()) {
				if(!StringUtils.isEmpty(domainId)) {
					users = core.listUsers(domainId, true);
				} else {
					users = core.listUsers(true);
				}
			} else {
				// Domain users can only see users belonging to their own domain
				users = core.listUsers(up.getDomainId(), true);
			}
			
			if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for(OUser user : users) {
				items.add(new JsSimple(user.getUserId(), JsSimple.description(user.getDisplayName(), user.getUserId())));
			}
			
			new JsonResult("users", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupUsers", ex);
			new JsonResult(false, "Unable to lookup users").printTo(out);
		}
	}
	
	public void processLookupActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsActivity> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			UserProfile.Id pid = new UserProfile.Id(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<OActivity> activities = core.listLiveActivities(pid);
			for(OActivity activity : activities) {
				items.add(new JsActivity(activity));
			}
			
			new JsonResult("activities", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupActivities", ex);
			new JsonResult(false, "Unable to lookup activities").printTo(out);
		}
	}
	
	public void processManageActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<ActivityGrid> items = core.listLiveActivities(queryDomains());
					new JsonResult("activities", items, items.size()).printTo(out);
				} else {
					OActivity item = core.getActivity(id);
					new JsonResult(item).printTo(out);
				}
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.addActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.updateActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.deleteActivity(pl.data.getActivityId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageActivities", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
	
	public void processLookupCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCausal> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			String customerId = ServletUtils.getStringParameter(request, "customerId", null);
			UserProfile.Id pid = new UserProfile.Id(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<OCausal> causals = core.listLiveCausals( pid, customerId);
			for(OCausal causal : causals) {
				items.add(new JsCausal(causal));
			}
			new JsonResult("causals", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupCausals", ex);
			new JsonResult(false, "Unable to lookup causals").printTo(out);
		}
	}
	
	public void processManageCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<CausalGrid> items =  core.listLiveCausals(queryDomains());
					new JsonResult("causals", items, items.size()).printTo(out);
				} else {
					OCausal item = core.getCausal(id);
					new JsonResult(item).printTo(out);
				}
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				core.addCausal( pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				core.updateCausal(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				core.deleteCausal(pl.data.getCausalId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageCausals", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCustomers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", "");
			
			List<OCustomer> customers = core.listCustomersByLike("%" + query + "%");
			for(OCustomer customer : customers) {
				items.add(new JsSimple(customer.getCustomerId(), customer.getDescription()));
			}
			new JsonResult("customers", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCustomers", ex);
			new JsonResult(false, "Error in LookupCustomers").printTo(out);
		}
	}
	
	public void processLookupStatisticCustomers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		List<JsSimple> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			String parentCustomerId = ServletUtils.getStringParameter(request, "parentCustomerId", null);
			String query = ServletUtils.getStringParameter(request, "query", "");
			UserProfile.Id pid = new UserProfile.Id(profileId);
			CustomerDAO cdao = CustomerDAO.getInstance();
			con = WT.getCoreConnection();
			
			//TODO: spostare recupero nel manager
			List<OCustomer> customers = cdao.viewByParentDomainLike(con, parentCustomerId, pid.getDomainId(), "%" + query + "%");
			ArrayList<String> parts = null;
			String address = null, description;
			for(OCustomer customer : customers) {
				parts = new ArrayList<>();
				if(!StringUtils.isEmpty(customer.getAddress())) {
					parts.add(customer.getAddress());
				}
				if(!StringUtils.isEmpty(customer.getCity())) {
					parts.add(customer.getCity());
				}
				if(!StringUtils.isEmpty(customer.getCountry())) {
					parts.add(customer.getCountry());
				}
				address = StringUtils.join(parts, ", ");
				if(StringUtils.isEmpty(address)) {
					description = customer.getDescription();
				} else {
					description = MessageFormat.format("{0} ({1})", customer.getDescription(), address);
				}
				items.add(new JsSimple(customer.getCustomerId(), description));
			}
			
			new JsonResult("customers", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupStatisticCustomers", ex);
			new JsonResult(false, "Error in LookupStatisticCustomers").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	
	
	
	
	/*
	public void processGetOptionsUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		UserProfile up = getEnv().getProfile();
		
		try {
			ArrayList<JsSimple> data = new ArrayList<>();
			if(up.isWebTopAdmin()) {
				con = WT.getCoreConnection();
				UserDAO udao = UserDAO.getInstance();
				List<OUser> users = udao.selectAll(con);
				String id = null, descr = null;
				for(OUser user : users) {
					id = DomainAccount.buildName(user.getDomainId(), user.getUserId());
					descr = MessageFormat.format("{0} ({1})", user.getDisplayName(), id);
					data.add(new JsSimple(id, descr));
				}
				
			} else {
				//TODO: maybe define a permission to other users to control others options
				data.add(new JsSimple(up.getStringId(), up.getDisplayName()));
			}
			new JsonResult("users", data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetOptionsUsers", ex);
			new JsonResult(false, "Unable to get users").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	public void processGetOptionsServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<UserOptionsServiceData> data = null;
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			UserProfile.Id targetPid = new UserProfile.Id(id);
			if(RunContext.getProfileId().equals(targetPid)) {
				data = core.listUserOptionServices();
			} else {
				CoreManager xcore = WT.getCoreManager(targetPid);
				data = xcore.listUserOptionServices();
			}
			
			/*
			//TODO: aggiornare l'implementazione
			data.add(new UserOptionsServiceData("com.sonicle.webtop.core", "wt", "WebTop Services", "Sonicle.webtop.core.view.CoreOptions"));
			if(!UserProfile.isWebTopAdmin(id)) data.add(new UserOptionsServiceData("com.sonicle.webtop.calendar", "wtcal", "Calendario", "Sonicle.webtop.calendar.CalendarOptions"));
			if(!UserProfile.isWebTopAdmin(id)) data.add(new UserOptionsServiceData("com.sonicle.webtop.mail", "wtmail", "Posta Elettronica", "Sonicle.webtop.mail.MailOptions"));
			*/
			new JsonResult(data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GetOptionsServices", ex);
			new JsonResult(false, "Error in GetOptionsServices").printTo(out);
		}
	}
	
	public void processLookupSessionServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		Locale locale = wts.getLocale();
		
		ArrayList<JsSimple> items = new ArrayList<>();
		List<String> ids = wts.getServices();
		for(String id : ids) {
			items.add(new JsSimple(id, WT.lookupResource(id, locale, BaseService.RESOURCE_SERVICE_NAME)));
		}
		new JsonResult("services", items).printTo(out);
	}
	
	public void processFeedback(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			Payload<MapItem, JsFeedback> pl = ServletUtils.getPayload(request, JsFeedback.class);
			
			logger.debug("message: {}", pl.data.message);
			Thread.sleep(4000);
			new JsonResult().printTo(out);
			//new JsonResult(false, "Erroreeeeeeeeeeeeeeeeeeeeeee").printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error executing action Feedback", ex);
			new JsonResult(false, "Unable to send feedback report.").printTo(out);
		}
	}
	
	public void processGetWhatsnewTabs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		ArrayList<JsWhatsnewTab> tabs = null;
		JsWhatsnewTab tab = null;
		String html = null;
		UserProfile profile = getEnv().getProfile();
		
		try {
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			tabs = new ArrayList<>();
			List<String> ids = wts.getServices();
			for(String id : ids) {
				if(full || wts.needWhatsnew(id, profile)) {
					html = wts.getWhatsnewHtml(id, profile, full);
					if(!StringUtils.isEmpty(html)) {
						tab = new JsWhatsnewTab(id);
						tab.title = WT.lookupResource(id, profile.getLocale(), CoreLocaleKey.SERVICE_NAME);
						tabs.add(tab);
					}
				}
			}
			new JsonResult(tabs).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GetWhatsnewTabs", ex);
			new JsonResult(false, "Error in GetWhatsnewTabs").printTo(out);
		}
	}
	
	public void processGetWhatsnewHTML(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			String html = wts.getWhatsnewHtml(id, getEnv().getProfile(), full);
			out.println(html);
			
		} catch (Exception ex) {
			logger.error("Error in GetWhatsnewHTML", ex);
		}
	}
	
	public void processTurnOffWhatsnew(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		
		try {
			UserProfile profile = getEnv().getProfile();
			List<String> ids = wts.getServices();
			for(String id : ids) {
				wts.resetWhatsnew(id, profile);
			}
			
		} catch (Exception ex) {
			logger.error("Error in TurnOffWhatsnew", ex);
		} finally {
			new JsonResult().printTo(out);
		}
	}
	
	public void processSnoozeReminder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			Integer snooze = ServletUtils.getIntParameter(request, "snooze", 5);
			PayloadAsList<JsReminderInApp.List> pl = ServletUtils.getPayloadAsList(request, JsReminderInApp.List.class);
			
			DateTime remindOn = DateTimeUtils.now(false).plusMinutes(snooze);
			for(JsReminderInApp js : pl.data) {
				core.snoozeReminder(JsReminderInApp.createReminderInApp(getEnv().getProfileId(), js), remindOn);
			}
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in SnoozeReminder", ex);
			new JsonResult(false, "Error in SnoozeReminder").printTo(out);
		}
	}
	
	public void processManageOTP(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		UserProfile.Id pid = getEnv().getProfile().getId();
		CoreManager corem = null;
		
		try {
			String operation = ServletUtils.getStringParameter(request, "operation", true);
			if(operation.equals("configure") || operation.equals("activate") || operation.equals("deactivate")) {
				// These work only on a target user!
				String profileId = ServletUtils.getStringParameter(request, "profileId", true);
				
				UserProfile.Id targetPid = new UserProfile.Id(profileId);
				corem = (targetPid.equals(core.getTargetProfileId())) ? core : WT.getCoreManager(targetPid);
				
				if(operation.equals("configure")) {
					String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
					if(deliveryMode.equals(CoreUserSettings.OTP_DELIVERY_EMAIL)) {
						String address = ServletUtils.getStringParameter(request, "address", true);
						InternetAddress ia = MailUtils.buildInternetAddress(address, null);
						if(!MailUtils.isAddressValid(ia)) throw new WTException("Indirizzo non valido"); //TODO: messaggio in lingua
						
						OTPManager.EmailConfig config = corem.otpConfigureUsingEmail(address);
						logger.debug("{}", config.otp.getVerificationCode());
						wts.setProperty("OTP_SETUP", config);

					} else if(deliveryMode.equals(CoreUserSettings.OTP_DELIVERY_GOOGLEAUTH)) {
						OTPManager.GoogleAuthConfig config = corem.otpConfigureUsingGoogleAuth(200);
						wts.setProperty("OTP_SETUP", config);
					}
					new JsonResult(true).printTo(out);
					
				} else if(operation.equals("activate")) {
					int code = ServletUtils.getIntParameter(request, "code", true);

					OTPManager.Config config = (OTPManager.Config)wts.getProperty("OTP_SETUP");
					boolean enabled = corem.otpActivate(config, code);
					if(!enabled) throw new WTException("Codice non valido"); //TODO: messaggio in lingua
					wts.clearProperty("OTP_SETUP");
					
					new JsonResult().printTo(out);

				} else if(operation.equals("deactivate")) {
					corem.otpDeactivate();
					new JsonResult().printTo(out);

				}
			} else if(operation.equals("untrustthis")) {
				// This works only on current session user!
				OTPManager otpm = core.getOTPManager();
				TrustedDeviceCookie tdc = otpm.readTrustedDeviceCookie(pid, request);
				if(tdc != null) {
					otpm.removeTrustedDevice(pid, tdc.deviceId);
					otpm.clearTrustedDeviceCookie(pid, response);
				}
				new JsonResult().printTo(out);
				
			} else if(operation.equals("untrustothers")) {
				// This works only on current session user!
				OTPManager otpm = core.getOTPManager();
				TrustedDeviceCookie thistdc = otpm.readTrustedDeviceCookie(pid, request);
				List<JsTrustedDevice> tds = otpm.listTrustedDevices(pid);
				for(JsTrustedDevice td: tds) {
					if((thistdc != null) && (td.deviceId.equals(thistdc.deviceId))) continue;
					otpm.removeTrustedDevice(pid, td.deviceId);
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageOTP", ex);
			new JsonResult(false, "Error in ManageOTP").printTo(out);
		}
	}
	
	public void processGetOTPGoogleAuthQRCode(HttpServletRequest request, HttpServletResponse response) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		
		try {
			OTPManager.GoogleAuthConfig config = (OTPManager.GoogleAuthConfig)wts.getProperty("OTP_SETUP");
			ServletUtils.writeContent(response, config.qrcode, config.qrcode.length, "image/png");
			
		} catch (Exception ex) {
			logger.error("Error in GetOTPGoogleAuthQRCode", ex);
		}
	}
	
	public void processManageSyncDevices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				UserProfile.Data ud = core.getUserData(RunContext.getProfileId());
				DateTimeFormatter fmt = JsGridSync.createFormatter(ud.getTimeZone());
				List<SyncDevice> devices = core.listZPushDevices();
				ArrayList<JsGridSync> items = new ArrayList<>();
				for(SyncDevice device : devices) {
					items.add(new JsGridSync(device.device, device.user, device.lastSync, fmt));
				}
				new JsonResult(items).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				//PayloadAsList<JsGridSyncList> pl = ServletUtils.getPayloadAsList(request, JsGridSyncList.class);
				Payload<MapItem, JsGridSync> pl = ServletUtils.getPayload(request, JsGridSync.class);
				CompositeId cid = new CompositeId(pl.data.id);
				
				core.deleteZPushDevice(cid.getToken(0), cid.getToken(1));
				new JsonResult().printTo(out);
				
			} else if(crud.equals("info")) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				CompositeId cid = new CompositeId(id);
				
				String info = core.getZPushDetailedInfo(cid.getToken(0), cid.getToken(1), "</br>");
				new JsonResult(info).printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageSyncDevices", ex);
			new JsonResult(false, "Error in ManageSyncDevices").printTo(out);
		}
	}
	
	
	public void processLookupInternetRecipients(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<InternetRecipient> items = null;
		
		try {
			items = core.listProfileInternetRecipients("", 100);
			new JsonResult("recipients", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error in LookupInternetRecipients", ex);
			new JsonResult(false, "Error in LookupInternetRecipients").printTo(out);
		}
	}
	
	
	
	private List<String> queryDomains() {
		List<String> domains = new ArrayList<>();
		if(RunContext.isWebTopAdmin()) domains.add("*");
		domains.add(RunContext.getProfileId().getDomainId());
		return domains;
	}
	
	
	
	
	
	public void processServerEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<ServiceMessage> messages = new ArrayList();
		
		try {
			messages = ((CoreEnvironment)getEnv()).getSession().getEnqueuedMessages();
			
		} catch (Exception ex) {
			logger.error("Error executing action ServerEvents", ex);
		} finally {
			new JsonResult(JsonResult.gson.toJson(messages)).printTo(out);
		}
	}
}
