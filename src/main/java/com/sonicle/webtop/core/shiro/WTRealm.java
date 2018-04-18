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
package com.sonicle.webtop.core.shiro;

import com.sonicle.security.Principal;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory.AuthUser;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WTRealm extends AuthorizingRealm {
	private final static Logger logger = (Logger)LoggerFactory.getLogger(WTRealm.class);
	private final Object lock1 = new Object();
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken upt = (UsernamePasswordToken)token;
			
			String domainId = null;
			if (token instanceof UsernamePasswordDomainToken) {
				domainId = ((UsernamePasswordDomainToken)token).getDomain();
			}
			//logger.debug("isRememberMe={}",upt.isRememberMe());
			
			String sprincipal = (String)upt.getPrincipal();
			String internetDomain = StringUtils.lowerCase(StringUtils.substringAfterLast(sprincipal, "@"));
			String username = StringUtils.substringBeforeLast(sprincipal, "@");
			logger.trace("doGetAuthenticationInfo [{}, {}, {}]", domainId, internetDomain, username);
			
			Principal principal = authenticateUser(domainId, internetDomain, username, upt.getPassword());
			
			// Update token with new values resulting from authentication
			if (token instanceof UsernamePasswordDomainToken) {
				((UsernamePasswordDomainToken)token).setDomain(principal.getDomainId());
			}
			upt.setUsername(principal.getUserId());
			
			return new WTAuthenticationInfo(principal, upt.getPassword(), this.getName());
			
		} else {
			return null;
		}
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if (principals == null) throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		
		try {
			Principal principal = (Principal)principals.getPrimaryPrincipal();
			logger.trace("doGetAuthorizationInfo [{}]", principal);
			return loadAuthorizationInfo(principal);
			
		} catch(Exception ex) {
			throw new AuthorizationException(ex);
		}
	}
	
	@Override
	/*
	 * Protect caching of impersonated authorization from original user authorization cache
	 */
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
		if (principals.getPrimaryPrincipal() instanceof com.sonicle.security.Principal) {
			com.sonicle.security.Principal sprincipal=(com.sonicle.security.Principal)principals.getPrimaryPrincipal();
			if (sprincipal.isImpersonated()) {
				return "admin!"+sprincipal.getUserId()+"@"+sprincipal.getDomainId();
			}
		}
        return principals;
    }

	private Principal authenticateUser(String domainId, String internetDomain, String username, char[] password) throws AuthenticationException {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		AuthenticationDomain authAd = null, priAd = null;
		boolean autoCreate = false, impersonate = false;
		
		try {
			DirectoryManager dirManager = DirectoryManager.getManager();
			
			// Defines authentication domains for the auth phase and for 
			// building the right principal
			logger.debug("Building the authentication domain");
			if (isSysAdmin(internetDomain, username)) {
				impersonate = false;
				authAd = priAd = wtMgr.createSysAdminAuthenticationDomain();
				
			} else {
				if (wta.getServiceManager().isInMaintenance(CoreManifest.ID)) throw new WTException("Maintenance is active. Only sys-admin can login.");
				ODomain domain = null;
				if (!StringUtils.isBlank(internetDomain)) {
					List<ODomain> domains = wtMgr.listByInternetDomain(internetDomain);
					if (domains.isEmpty()) throw new WTException("No enabled domains match specified internet domain [{0}]", internetDomain);
					if (domains.size() != 1) throw new WTException("Multiple domains match specified internet domain [{0}]", internetDomain);
					domain = domains.get(0);
				} else {
					domain = wtMgr.getDomain(domainId);
					if ((domain == null) || !domain.getEnabled()) throw new WTException("Domain not found [{0}]", domainId);
				}
				
				if (isSysAdminImpersonate(username)) {
					impersonate = true;
					authAd = wtMgr.createSysAdminAuthenticationDomain();
					priAd = wtMgr.createAuthenticationDomain(domain);
				} else if (isDomainAdminImpersonate(username)) {
					impersonate = true;
					authAd = priAd = wtMgr.createAuthenticationDomain(domain);
				} else {
					impersonate = false;
					authAd = priAd = wtMgr.createAuthenticationDomain(domain);
				}
				autoCreate = domain.getUserAutoCreation();
			}
			
			DirectoryOptions opts = wta.createDirectoryOptions(authAd);
			AbstractDirectory directory = dirManager.getDirectory(authAd.getDirUri().getScheme());
			if (directory == null) throw new WTException("Directory not supported [{0}]", authAd.getDirUri().getScheme());
			
			// Prepare principal for authentication
			String authUsername = impersonate ? "admin" : directory.sanitizeUsername(opts, username);
			Principal authPrincipal = new Principal(authAd, impersonate, authAd.getDomainId(), authUsername, password);
			logger.debug("Authenticating principal [{}, {}]", authPrincipal.getDomainId(), authPrincipal.getUserId());
			AuthUser userEntry = directory.authenticate(opts, authPrincipal);
			
			// Authentication phase passed succesfully, now build the right principal!
			Principal principal = null;
			if (impersonate) {
				String impUsername = sanitizeImpersonateUsername(username);
				principal = new Principal(priAd, impersonate, priAd.getDomainId(), impUsername, password);
				
				UserProfileId pid = new UserProfileId(principal.getDomainId(), principal.getUserId());
				OUser ouser = wta.getWebTopManager().getUser(pid);
				// We cannot continue if the user is not present, impersonation needs it!
				if (ouser == null) throw new WTException("User not found [{0}]", pid.toString());
				principal.setDisplayName(ouser.getDisplayName());
				
			} else {
				// Authentication result points to the right userId...
				principal = new Principal(priAd, impersonate, priAd.getDomainId(), userEntry.userId, password);
				principal.setDisplayName(StringUtils.defaultIfBlank(userEntry.displayName, userEntry.userId));
			}

			synchronized (lock1) {
				WebTopManager.CheckUserResult chk = wtMgr.checkUser(principal.getDomainId(), principal.getUserId());
				if (autoCreate && !chk.exist) {
					logger.debug("Creating user [{}]", principal.getSubjectId());
					wtMgr.addUser(false, createUserEntity(principal.getDomainId(), userEntry), false, null);
				} else if (!chk.exist) {
					throw new WTException("User does not exist [{0}]", principal.getSubjectId());
				} else if (chk.exist && !chk.enabled) {
					throw new WTException("User is disabled [{0}]", principal.getSubjectId());
				}
			}
			
			return principal;
			
		} catch(URISyntaxException | WTException | DirectoryException ex) {
			logger.error("Authentication error", ex);
			throw new AuthenticationException(ex);
		}	
	}
	
	private WTAuthorizationInfo loadAuthorizationInfo(Principal principal) throws Exception {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId pid = new UserProfileId(principal.getDomainId(), principal.getUserId());
		
		HashSet<String> roles = new HashSet<>();
		HashSet<String> perms = new HashSet<>();
		
		if (Principal.xisAdmin(pid.toString())) {
			roles.add("SYSADMIN");
			roles.add("WTADMIN");
			perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "SYSADMIN"), ServicePermission.ACTION_ACCESS, "*"));
			perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), ServicePermission.ACTION_ACCESS, "*"));
			
		} else if (principal.isImpersonated()) {
			roles.add("WTADMIN");
			perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), ServicePermission.ACTION_ACCESS, "*"));
		}
		
		// Force core private service permission for any principal
		String authRes = ServicePermission.namespacedName(CoreManifest.ID, "SERVICE");
		perms.add(ServicePermission.permissionString(authRes, ServicePermission.ACTION_ACCESS, CoreManifest.ID));
		
		Set<RoleWithSource> userRoles = wtMgr.getComputedRolesByUser(pid, true, true);
		for(RoleWithSource role : userRoles) {
			roles.add(role.getRoleUid());

			List<ORolePermission> rolePerms = wtMgr.listRolePermissions(role.getRoleUid());
			for(ORolePermission perm : rolePerms) {
				// Generate resource namespaced name:
				// resource "TEST" for service "com.sonicle.webtop.core" 
				// will become "com.sonicle.webtop.core.TEST"
				authRes = ServicePermission.namespacedName(perm.getServiceId(), perm.getKey());
				// Generate permission string that shiro can understand 
				// under the form: {resource}:{action}:{instance}
				perms.add(ServicePermission.permissionString(authRes, perm.getAction(), perm.getInstance()));
			}
		}
		return new WTAuthorizationInfo(roles, perms);
	}
	
	private boolean isSysAdmin(String internetDomain, String username) {
		return StringUtils.equals(StringUtils.lowerCase(username), "admin") && StringUtils.isBlank(internetDomain);
	}
	
	private String sanitizeImpersonateUsername(String username) {
		String s = StringUtils.removeStart(username, "admin!");
		return StringUtils.removeStart(s, "admin$");
	}
	
	private boolean isSysAdminImpersonate(String username) {
		return StringUtils.startsWith(StringUtils.lowerCase(username), "admin!");
	}
	
	private boolean isDomainAdminImpersonate(String username) {
		return StringUtils.startsWith(StringUtils.lowerCase(username), "admin$");
	}
	
	private UserEntity createUserEntity(String domainId, AuthUser userEntry) {
		UserEntity ue = new UserEntity();
		ue.setDomainId(domainId);
		ue.setUserId(userEntry.userId);
		ue.setEnabled(true);
		ue.setFirstName(userEntry.firstName);
		ue.setLastName(userEntry.lastName);
		ue.setDisplayName(userEntry.displayName);
		return ue;
	}
}
