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
package com.sonicle.webtop.core.admin;

import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.CorePrivateEnvironment;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.ORunnableUpgradeStatement;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUpgradeStatement;
import com.sonicle.webtop.core.bol.js.JsDomain;
import com.sonicle.webtop.core.bol.js.JsGridDomainRole;
import com.sonicle.webtop.core.bol.js.JsGridDomainUser;
import com.sonicle.webtop.core.bol.js.JsGridUpgradeRow;
import com.sonicle.webtop.core.bol.js.JsRole;
import com.sonicle.webtop.core.bol.js.JsRoleLkp;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsUser;
import com.sonicle.webtop.core.bol.model.DirectoryUser;
import com.sonicle.webtop.core.bol.model.DomainEntity;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.versioning.IgnoreErrorsAnnotationLine;
import com.sonicle.webtop.core.versioning.RequireAdminAnnotationLine;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	private static final Logger logger = WT.getLogger(Service.class);
	private CoreManager core;
	private CoreAdminManager coreadm;
	
	@Override
	public void initialize() throws Exception {
		core = WT.getCoreManager();
		coreadm = (CoreAdminManager)WT.getServiceManager(SERVICE_ID);
	}

	@Override
	public void cleanup() throws Exception {
		core = null;
	}
	
	private WebTopApp getWta() {
		return ((CorePrivateEnvironment)getEnv()).getApp();
	}
	
	private ExtTreeNode createTreeNode(String id, String type, String text, boolean leaf, String iconClass) {
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", type);
		node.setIconClass(iconClass);
		return node;
	}
	
	public void processLookupDomainGroups(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean uid = ServletUtils.getBooleanParameter(request, "uid", false);
			
			if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for(OGroup group : coreadm.listGroups(domainId)) {
				items.add(new JsSimple(uid ? group.getUserUid() : group.getGroupId(), group.getDisplayName()));
			}
			
			new JsonResult("groups", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDomainGroups", ex);
			new JsonResult(false, "Unable to lookup groups").printTo(out);
		}
	}
	
	public void processLookupDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsRoleLkp> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			for(Role role : coreadm.listRoles(domainId)) {
				items.add(new JsRoleLkp(role, RoleWithSource.SOURCE_ROLE));
			}
			
			new JsonResult("roles", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDomainRoles", ex);
			new JsonResult(false, "Unable to lookup roles").printTo(out);
		}
	}
	
	
	
	
	
	
	
	
	
	
	private static final String NTYPE_SETTINGS = "settings";
	private static final String NTYPE_DOMAINS = "domains";
	private static final String NTYPE_DOMAIN = "domain";
	private static final String NTYPE_GROUPS = "groups";
	private static final String NTYPE_USERS = "users";
	private static final String NTYPE_ROLES = "roles";
	private static final String NTYPE_DBUPGRADER = "dbupgrader";
	
	private ExtTreeNode createDomainNode(String parentId, ODomain domain) {
		CompositeId cid = new CompositeId(parentId, domain.getDomainId());
		ExtTreeNode node = new ExtTreeNode(cid.toString(), domain.getDescription(), false);
		node.setIconClass(domain.getEnabled() ? "wta-icon-domain-xs" : "wta-icon-domain-disabled-xs");
		node.put("_type", NTYPE_DOMAIN);
		node.put("_domainId", domain.getDomainId());
		//node.put("_internetDomain", domain.getInternetName());
		//node.put("_dirCapPasswordWrite", dirCapPasswordWrite);
		//node.put("_dirCapUsersWrite", dirCapUsersWrite);
		return node;
	}
	
	private ExtTreeNode createDomainChildNode(String parentId, String text, String iconClass, String type, String domainId, boolean passwordPolicy, boolean authCapPasswordWrite, boolean authCapUsersWrite) {
		CompositeId cid = new CompositeId(parentId, type);
		ExtTreeNode node = new ExtTreeNode(cid.toString(), text, true);
		node.setIconClass(iconClass);
		node.put("_type", type);
		node.put("_domainId", domainId);
		node.put("_passwordPolicy", passwordPolicy);
		node.put("_authCapPasswordWrite", authCapPasswordWrite);
		node.put("_authCapUsersWrite", authCapUsersWrite);
		return node;
	}
	
	public void processManageAdminTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Locale locale = getEnv().getWebTopSession().getLocale();
		//DirectoryManager dirMgr = DirectoryManager.getManager();
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String nodeId = ServletUtils.getStringParameter(request, "node", true);
				
				if(nodeId.equals("root")) { // Admin roots...
					children.add(createTreeNode(NTYPE_SETTINGS, NTYPE_SETTINGS, lookupResource(CoreAdminLocale.TREE_ADMIN_SETTINGS), true, "wta-icon-settings-xs"));
					children.add(createTreeNode(NTYPE_DOMAINS, NTYPE_DOMAINS, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAINS), false, "wta-icon-domains-xs"));
					children.add(createTreeNode(NTYPE_DBUPGRADER, NTYPE_DBUPGRADER, lookupResource(CoreAdminLocale.TREE_ADMIN_DBUPGRADER), true, "wta-icon-dbUpgrader-xs"));
				} else {
					CompositeId cid = new CompositeId(3).parse(nodeId, true);
					if(cid.getToken(0).equals("domains")) {
						if(cid.hasToken(1)) {
							String domainId = cid.getToken(1);
							ODomain domain = core.getDomain(domainId);
							AbstractDirectory dir = core.getAuthDirectory(domain);
							boolean passwordPolicy = domain.getAuthPasswordPolicy();
							boolean dirCapPasswordWrite = dir.hasCapability(DirectoryCapability.PASSWORD_WRITE);
							boolean dirCapUsersWrite = dir.hasCapability(DirectoryCapability.USERS_WRITE);
							
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_SETTINGS), "wta-icon-settings-xs", NTYPE_SETTINGS, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_GROUPS), "wta-icon-groups-xs", NTYPE_GROUPS, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_USERS), "wta-icon-users-xs", NTYPE_USERS, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_ROLES), "wta-icon-roles-xs", NTYPE_ROLES, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
						
						} else { // Available webtop domains
							for(ODomain domain : core.listDomains(false)) {
								children.add(createDomainNode(nodeId, domain));
							}
						}
					}
				}
				new JsonResult("children", children).printTo(out);
			}
		} catch(Exception ex) {
			logger.error("Error in ManageStoresTree", ex);
		}
	}
	
	public void processManageSystemSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<SystemSetting> items = coreadm.listSystemSettings(false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				if(!coreadm.updateSystemSetting(setting.serviceId, setting.key, setting.value)) {
					throw new WTException("Cannot insert setting [{0}, {1}]", setting.serviceId, setting.key);
				}
				
				OSettingDb info = coreadm.getSettingInfo(setting.serviceId, setting.key);
				if(info != null) {
					setting = new SystemSetting(setting.serviceId, setting.key, setting.value, info.getType(), info.getHelp());
				} else {
					setting = new SystemSetting(setting.serviceId, setting.key, setting.value, null, null);
				}
				new JsonResult(setting).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.updateSystemSetting(sid, setting.key, setting.value)) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if(!StringUtils.equals(key, setting.key)) {
					coreadm.deleteSystemSetting(sid, key);
				}
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSettings", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomains(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				DomainEntity domain = coreadm.getDomain(id);
				new JsonResult(new JsDomain(domain)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsDomain> pl = ServletUtils.getPayload(request, JsDomain.class);
				AbstractDirectory dir = DirectoryManager.getManager().getDirectory(pl.data.authScheme);
				coreadm.addDomain(JsDomain.buildDomainEntity(pl.data, dir));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsDomain> pl = ServletUtils.getPayload(request, JsDomain.class);
				AbstractDirectory dir = DirectoryManager.getManager().getDirectory(pl.data.authScheme);
				coreadm.updateDomain(JsDomain.buildDomainEntity(pl.data, dir));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				coreadm.deleteDomain(domainId);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("init")) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				coreadm.initDomainWithDefaults(domainId);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomains", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<DomainSetting> items = coreadm.listDomainSettings(domainId, false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				if(!coreadm.updateSystemSetting(setting.serviceId, setting.key, setting.value)) {
					throw new WTException("Cannot insert setting [{0}, {1}]", setting.serviceId, setting.key);
				}
				setting = new DomainSetting(setting.domainId, setting.serviceId, setting.key, setting.value, null, null);
				new JsonResult(setting).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.updateSystemSetting(sid, setting.key, setting.value)) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if(!StringUtils.equals(key, setting.key)) {
					coreadm.deleteSystemSetting(sid, key);
				}
					
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSettings", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				
				List<JsGridDomainUser> items = new ArrayList<>();
				for(DirectoryUser dirUser : coreadm.listDirectoryUsers(domainId)) {
					items.add(new JsGridDomainUser(dirUser));
				}
				new JsonResult("users", items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				boolean deep = ServletUtils.getBooleanParameter(request, "deep", false);
				ServletUtils.StringArray profileIds = ServletUtils.getObjectParameter(request, "profileIds", ServletUtils.StringArray.class, true);
				
				UserProfile.Id pid = new UserProfile.Id(profileIds.get(0));
				coreadm.deleteUser(deep, pid);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals("enable") || crud.equals("disable")) {
				ServletUtils.StringArray profileIds = ServletUtils.getObjectParameter(request, "profileIds", ServletUtils.StringArray.class, true);
				
				UserProfile.Id pid = new UserProfile.Id(profileIds.get(0));
				coreadm.updateUser(pid, crud.equals("enable"));
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomainUsers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				
				UserProfile.Id pid = new UserProfile.Id(id);
				UserEntity user = coreadm.getUser(pid);
				new JsonResult(new JsUser(user)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsUser> pl = ServletUtils.getPayload(request, JsUser.class);
				coreadm.addUser(JsUser.buildUserEntity(pl.data), pl.data.password.toCharArray());
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsUser> pl = ServletUtils.getPayload(request, JsUser.class);
				coreadm.updateUser(JsUser.buildUserEntity(pl.data));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageUsers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processChangeUserPassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			char[] newPassword = ServletUtils.getStringParameter(request, "newPassword", true).toCharArray();
			
			UserProfile.Id pid = new UserProfile.Id(profileId);
			coreadm.updateUserPassword(pid, newPassword);
			
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in ChangeUserPassword", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				
				List<JsGridDomainRole> items = new ArrayList<>();
				for(Role role : coreadm.listRoles(domainId)) {
					items.add(new JsGridDomainRole(role));
				}
				new JsonResult("roles", items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<JsGridDomainRole.List> pl = ServletUtils.getPayloadAsList(request, JsGridDomainRole.List.class);
				JsGridDomainRole role = pl.data.get(0);
				
				coreadm.deleteRole(role.roleUid);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomainRoles", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				RoleEntity role = coreadm.getRole(id);
				new JsonResult(new JsRole(role)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsRole> pl = ServletUtils.getPayload(request, JsRole.class);
				coreadm.addRole(JsRole.buildRoleEntity(pl.data));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsRole> pl = ServletUtils.getPayload(request, JsRole.class);
				coreadm.updateRole(JsRole.buildRoleEntity(pl.data));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageRoles", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	
	
	
	public void processManageDbUpgrades(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			synchronized (lock1) {
				String crud = ServletUtils.getStringParameter(request, "crud", true);
				DbUpgraderEnvironment upEnv = getDbUpgraderEnvironment();
				if (crud.equals(Crud.READ)) {
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					for(ORunnableUpgradeStatement stmt : upEnv.runnableStmts) {
						items.add(new JsGridUpgradeRow(stmt));
					}
					Integer nextStmtId = upEnv.getStatementId(false);
					
					new JsonResult("stmts", items, items.size())
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId)
							.setSelected(nextStmtId)
							.printTo(out);
					
				} else if (crud.equals("play1")) {
					String stmtBody = ServletUtils.getStringParameter(request, "stmtBody", true);
					Integer stmtId = ServletUtils.getIntParameter(request, "stmtId", null);
					
					// Determine which index to use... that belonging to the passed
					// id or the current (in sequence) one
					int index = upEnv.currentIndex;
					if (stmtId != null) {
						int indexOfId = upEnv.getIndexById(stmtId);
						if(index == -1) throw new WTException("Index of statement ID not found [{0}]", stmtId);
						index = indexOfId;
					}
					
					ORunnableUpgradeStatement stmt = upEnv.runnableStmts.get(index);
					stmt.setStatementBody(stmtBody);
					coreadm.executeUpgradeStatement(stmt, false);
					upEnv.updateExecuted(index, stmt);
					
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					items.add(new JsGridUpgradeRow(stmt));
					Integer nextStmtId = upEnv.getStatementId(true);
					Integer selectedId = (stmtId != null) ? stmtId : nextStmtId;
					
					new JsonResult(items, items.size())
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId)
							.setSelected(selectedId)
							.printTo(out);
					
				} else if (crud.equals("play")) {
					String stmtBody = ServletUtils.getStringParameter(request, "stmtBody", true);
					ArrayList<ORunnableUpgradeStatement> executed = new ArrayList<>();
					
					while (upEnv.currentIndex != Integer.MAX_VALUE) {
						final ORunnableUpgradeStatement item = upEnv.runnableStmts.get(upEnv.currentIndex);
						if (executed.isEmpty()) { // Use passed statement body only for the first statement
							item.setStatementBody(stmtBody);
						}
						if (!executed.isEmpty() && item.getRequireAdmin()) break;
						
						boolean ret = coreadm.executeUpgradeStatement(item, item.getIgnoreErrors());
						upEnv.updateExecuted(upEnv.currentIndex, item);
						
						executed.add(item);
						if(!ret) break; // Exits, admin must review execution...
						upEnv.currentIndex = upEnv.nextIndex();
					}
					
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					for(ORunnableUpgradeStatement stmt : executed) {
						items.add(new JsGridUpgradeRow(stmt));
					}
					Integer nextStmtId = upEnv.getStatementId(true);
					
					new JsonResult(items, items.size())
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId)
							.setSelected(nextStmtId)
							.printTo(out);
					
				} else if (crud.equals("skip")) {
					Integer stmtId = ServletUtils.getIntParameter(request, "stmtId", null);
					
					// Determine which index to use... that belonging to the passed
					// id or the current (in sequence) one
					int index = upEnv.currentIndex;
					if (stmtId != null) {
						int indexOfId = upEnv.getIndexById(stmtId);
						if(index == -1) throw new WTException("Index of statement ID not found [{0}]", stmtId);
						index = indexOfId;
					}
					
					ORunnableUpgradeStatement stmt = upEnv.runnableStmts.get(index);
					coreadm.skipUpgradeStatement(stmt);
					upEnv.updateExecuted(index, stmt);
					
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					items.add(new JsGridUpgradeRow(stmt));
					Integer nextStmtId = upEnv.getStatementId(true);
					Integer selectedId = (stmtId != null) ? stmtId : nextStmtId;
					
					new JsonResult(items, items.size())
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId)
							.setSelected(selectedId)
							.printTo(out);
				}
			}
		} catch(Exception ex) {
			logger.error("Error in ManageDbUpgrades", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	/*
	public void processManageDbUpgrades(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		OUpgradeStmt item = null;
		ServicesManager svcm = null;
		UpgradeInfo info = null;
		Integer nextId = null, selectedId = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			
			if (crud.equals(Crud.LIST)) {
				nextId = getStatementId(false);
				info = getUpgradeInfo(con);
				new JsonResult(this.upgradeStatements, this.upgradeStatements.size())
					.set("info", info).set("next", nextId).setSelected(nextId)
					.printTo(out);
				
			} else if(crud.equals("exec")) {
				svcm = wta.getServicesManager();
				String statement = ServletUtils.getStringParameter(request, "statement", true);
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				
				// Determine which index to use... that belonging to the passed
				// id or the current (in sequence) one
				int index = this.currentStatementIndex;
				if(id != null) {
					int indexOfId = getIndexById(id);
					if(index == -1) throw new Exception("Merdaaaaaaaaaaa");
					index = indexOfId;
				}
				
				item = this.upgradeStatements.get(index);
				item.statement = statement;
				svcm.executeUpgradeStatement(item, false);
				this.upgradeStatements.set(index, (ORunnableUpgradeStmt)item);
				
				nextId = getStatementId(true);
				selectedId = (id != null) ? id : nextId;
				info = getUpgradeInfo(con);
				new JsonResult(item)
					.set("info", info).set("next", nextId).setSelected(selectedId)
					.printTo(out);
				
			}  else if(crud.equals("play")) {
				svcm = wta.getServicesManager();
				String statement = ServletUtils.getStringParameter(request, "statement", true);
				
				ArrayList<ORunnableUpgradeStmt> executed = new ArrayList<ORunnableUpgradeStmt>();
				boolean ret = false;
				while(this.currentStatementIndex != Integer.MAX_VALUE) {
					item = this.upgradeStatements.get(this.currentStatementIndex);
					if(executed.isEmpty()) item.statement = statement; // Use passed statement only for the first
					if(!executed.isEmpty() && ((ORunnableUpgradeStmt)item).requireAdmin) break;
					
					ret = svcm.executeUpgradeStatement(item, ((ORunnableUpgradeStmt)item).ignoreErrors);
					this.upgradeStatements.set(this.currentStatementIndex, (ORunnableUpgradeStmt)item);
					executed.add((ORunnableUpgradeStmt)item);
					if(!ret) break; // Exits, admin must review execution...
					this.currentStatementIndex = nextUpgradeStatement();
				}
				
				nextId = getStatementId(true);
				info = getUpgradeInfo(con);
				new JsonResult(executed)
					.set("info", info).set("next", nextId).setSelected(nextId)
					.printTo(out);
				
			} else if(crud.equals("skip")) {
				svcm = wta.getServicesManager();
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				
				// Determine which index to use... that belonging to the passed
				// id or the current (in sequence) one
				int index = this.currentStatementIndex;
				if(id != null) {
					int indexOfId = getIndexById(id);
					if(index == -1) throw new Exception("Ooooooops");
					index = indexOfId;
				}
				
				item = this.upgradeStatements.get(index);
				svcm.skipUpgradeStatement(item);
				this.upgradeStatements.set(index, (ORunnableUpgradeStmt)item);
				
				nextId = getStatementId(true);
				selectedId = (id != null) ? id : nextId;
				info = getUpgradeInfo(con);
				new JsonResult(item)
					.set("info", info).set("next", nextId).setSelected(selectedId)
					.printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error managing upgrade statements!", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	private final Object lock1 = new Object();
	private DbUpgraderEnvironment upgradeEnvironment = null;
	
	private DbUpgraderEnvironment getDbUpgraderEnvironment() throws WTException {
		synchronized (lock1) {
			if (this.upgradeEnvironment == null) {
				this.upgradeEnvironment = new DbUpgraderEnvironment(coreadm.listLastUpgradeStatements());
			}
			return this.upgradeEnvironment;
		}	
	}
	
	private static class DbUpgraderEnvironment {
		public int pendingCount = 0;
		public int okCount = 0;
		public int errorCount = 0;
		public int warningCount = 0;
		public int skippedCount = 0;
		public int currentIndex = -1;
		public final ArrayList<ORunnableUpgradeStatement> runnableStmts;
		
		public DbUpgraderEnvironment(List<OUpgradeStatement> stmts) {
			this.pendingCount = 0;
			this.okCount = 0;
			this.errorCount = 0;
			this.warningCount = 0;
			this.skippedCount = 0;
			this.currentIndex = -1;
			this.runnableStmts = new ArrayList<>();
			
			boolean ignoreErrors = false, requireAdmin = false;
			String sqlComments = "", annComments = "";
			for (OUpgradeStatement stmt : stmts) {
				if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_ANNOTATION)) {
					if (IgnoreErrorsAnnotationLine.matches(stmt.getStatementBody())) {
						ignoreErrors = true;
						annComments += stmt.getStatementBody() + " ";
						
					} else if (RequireAdminAnnotationLine.matches(stmt.getStatementBody())) {
						requireAdmin = true;
						annComments += stmt.getStatementBody() + " ";
					}
					
				} else if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_COMMENT)) {
					sqlComments = stmt.getStatementBody();
					
				} else if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_SQL)) {
					String comments = org.apache.commons.lang3.StringUtils.trim(annComments);
					if (!org.apache.commons.lang3.StringUtils.isEmpty(sqlComments)) {
						if (!org.apache.commons.lang3.StringUtils.isEmpty(comments)) comments += "\n";
						comments += sqlComments;
					}
					runnableStmts.add(new ORunnableUpgradeStatement(stmt, requireAdmin, ignoreErrors, org.apache.commons.lang3.StringUtils.trim(comments)));
					requireAdmin = ignoreErrors = false;
					sqlComments = annComments = "";
					
					if (stmt.getRunStatus() == null) {
						pendingCount++;
					} else if (stmt.getRunStatus().equals(ORunnableUpgradeStatement.RUN_STATUS_OK)) {
						okCount++;
					} else if (stmt.getRunStatus().equals(ORunnableUpgradeStatement.RUN_STATUS_ERROR)) {
						errorCount++;
					} else if (stmt.getRunStatus().equals(ORunnableUpgradeStatement.RUN_STATUS_WARNING)) {
						warningCount++;
					} else if (stmt.getRunStatus().equals(ORunnableUpgradeStatement.RUN_STATUS_SKIPPED)) {
						skippedCount++;
					}
				}
			}
			this.currentIndex = nextIndex();
		}
		
		public final Integer nextIndex() {
			int index = Integer.MAX_VALUE;
			if(this.currentIndex != Integer.MAX_VALUE) {
				int start = (this.currentIndex < 0) ? 0 : this.currentIndex;
				ListIterator<ORunnableUpgradeStatement> liter = this.runnableStmts.listIterator(start);
				while (liter.hasNext()) {
					final int ni = liter.nextIndex();
					final ORunnableUpgradeStatement item = liter.next();
					if(org.apache.commons.lang3.StringUtils.isEmpty(item.getRunStatus()) || item.getRunStatus().equals(ORunnableUpgradeStatement.RUN_STATUS_ERROR)) {
						index = ni;
						break;
					}
				}
			}
			return index;
		}
		
		public final Integer getStatementId(boolean next) {
			if (next) this.currentIndex = nextIndex();
			if ((this.currentIndex == -1) || (this.currentIndex == Integer.MAX_VALUE)) return null;
			return this.runnableStmts.get(this.currentIndex).getUpgradeStatementId();
		}

		public final int getIndexById(Integer id) {
			if (id != null) {
				for (int i=0; i<this.runnableStmts.size(); i++) {
					if (this.runnableStmts.get(i).getUpgradeStatementId().equals(id)) return i;
				}
			}
			return -1;
		}
		
		public void updateExecuted(int index, ORunnableUpgradeStatement stmt) {
			runnableStmts.set(index, stmt);
			
			//TODO: aggiornare conteggi
		}
	}
}
