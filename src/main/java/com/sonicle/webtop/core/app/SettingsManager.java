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
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */

package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.DomainSettingRow;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingReader;
import com.sonicle.webtop.core.bol.ODomainSetting;
import com.sonicle.webtop.core.bol.OSetting;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUserSetting;
import com.sonicle.webtop.core.bol.SettingRow;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.dal.DomainSettingDAO;
import com.sonicle.webtop.core.dal.SettingDAO;
import com.sonicle.webtop.core.dal.SettingDbDAO;
import com.sonicle.webtop.core.dal.UserSettingDAO;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingManager;
import com.sonicle.webtop.core.sdk.interfaces.ISettingManager;
import com.sonicle.webtop.core.sdk.interfaces.IUserSettingManager;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class SettingsManager implements IServiceSettingReader, IServiceSettingManager, IUserSettingManager, ISettingManager {
	private static final Logger logger = WT.getLogger(SettingsManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized SettingsManager initialize(WebTopApp wta) {
		if (initialized) throw new RuntimeException("Initialization already done");
		SettingsManager setm = new SettingsManager(wta);
		initialized = true;
		logger.info("Initialized");
		return setm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private SettingsManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		logger.info("Cleaned up");
	}
	
	/**
	 * Gets the setting (system) value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	private String getSetting(String serviceId, String key) {
		SettingDAO dao = SettingDAO.getInstance();
		Connection con = null;
		OSetting item = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			item = dao.selectByServiceKey(con, serviceId, key);
			return (item != null) ? StringUtils.defaultString(item.getValue()) : null;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read setting [{}, {}]", serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	/**
	 * Gets the setting (domain) value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	private String getSetting(String domainId, String serviceId, String key) {
		DomainSettingDAO dao = DomainSettingDAO.getInstance();
		Connection con = null;
		ODomainSetting item = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			item = dao.selectByDomainServiceKey(con, domainId, serviceId, key);
			return (item != null) ? StringUtils.defaultString(item.getValue()) : null;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read setting (domain) [{}, {}, {}]", domainId, serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Gets the setting (user) value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	private String getSetting(String domainId, String userId, String serviceId, String key) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		OUserSetting item = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			item = dao.selectByDomainUserServiceKey(con, domainId, userId, serviceId, key);
			return (item != null) ? StringUtils.defaultString(item.getValue()) : null;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read user setting [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Deletes the setting (domain) value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	private boolean deleteSetting(String serviceId, String key) {
		SettingDAO dao = SettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			int ret = dao.deleteByServiceKey(con, serviceId, key);
			return (ret > 0);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to delete setting (system) [{}, {}]", serviceId, key, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Deletes the setting (domain) value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	private boolean deleteSetting(String domainId, String serviceId, String key) {
		DomainSettingDAO dao = DomainSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			int ret = dao.deleteByDomainServiceKey(con, domainId, serviceId, key);
			return (ret > 0);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to delete setting (domain) [{}, {}, {}]", domainId, serviceId, key, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Deletes the setting (system) value indicated by the specified key.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteServiceSetting(String serviceId, String key) {
		return deleteSetting(serviceId, key);
	}
	
	/**
	 * Deletes the setting (domain) value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteServiceSetting(String domainId, String serviceId, String key) {
		return deleteSetting(domainId, serviceId, key);
	}
	
	/**
	 * Gets the setting value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getServiceSetting(String serviceId, String key) {
		return getSetting(serviceId, key);
	}
	
	/**
	 * Gets the setting value indicated by the specified key using priority path:
	 *  1 - Setting by domain/service/key
	 *  2 - Setting by service/key
	 * Returns a null value if the key is not found.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getServiceSetting(String domainId, String serviceId, String key) {
		String value = getSetting(domainId, serviceId, key);
		if(value != null) return value;
		return getSetting(serviceId, key);
	}
	
	/**
	 * Sets the setting value indicated by the specified key.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	@Override
	public boolean setServiceSetting(String serviceId, String key, Object value) {
		SettingDAO dao = SettingDAO.getInstance();
		Connection con = null;
		OSetting item = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			item = new OSetting();
			item.setServiceId(serviceId);
			item.setKey(key);
			item.setValue(valueToString(value));
			
			int ret = dao.update(con, item);
			if(ret == 0) ret = dao.insert(con, item);
			return true;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to write setting [{}, {}]", serviceId, key, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Sets the setting value indicated by the specified key. 
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.s
	 */
	@Override
	public boolean setServiceSetting(String domainId, String serviceId, String key, Object value) {
		DomainSettingDAO dao = DomainSettingDAO.getInstance();
		Connection con = null;
		ODomainSetting item = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			item = new ODomainSetting();
			item.setDomainId(domainId);
			item.setServiceId(serviceId);
			item.setKey(key);
			item.setValue(valueToString(value));
			
			int ret = dao.update(con, item);
			if(ret == 0) ret = dao.insert(con, item);
			return true;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to write setting (domain) [{}, {}, {}]", domainId, serviceId, key, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Gets the setting value indicated by the specified key using priority path:
	 *  1 - UserSetting by domain/service/user/key
	 *  2 - ServiceSetting by domain/service/key
	 *  3 - ServiceSetting by service/key
	 * Returns a null value if the key is not found.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getUserSetting(String domainId, String userId, String serviceId, String key) {
		String value = getSetting(domainId, userId, serviceId, key);
		if(value != null) return value;
		return getServiceSetting(domainId, serviceId, key);
	}
	
	/**
	 * Gets the setting value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getUserSetting(UserProfileId profileId, String serviceId, String key) {
		return getUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, key);
	}
	
	/**
	 * Gets the setting values compliant to the specified key.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting. (treated as LIKE query)
	 * @return List of settings.
	 */
	@Override
	public List<OUserSetting> getUserSettings(UserProfileId profileId, String serviceId, String key) {
		return getUserSettings(profileId.getDomainId(), profileId.getUserId(), serviceId, key);
	}
	
	/**
	 * Gets the setting values compliant to the specified key.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting. (treated as LIKE query)
	 * @return List of setting values.
	 */
	@Override
	public List<OUserSetting> getUserSettings(String domainId, String userId, String serviceId, String key) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			return dao.selectByDomainServiceUserKeyLike(con, domainId, userId, serviceId, key);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings (user) [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<UserProfileId> listProfilesWith(String serviceId, String key, Object value) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		ArrayList<UserProfileId> profiles = new ArrayList<>();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			List<OUserSetting> sets = dao.selectByServiceKeyValue(con, serviceId, key, valueToString(value));
			for(OUserSetting set : sets) {
				profiles.add(new UserProfileId(set.getDomainId(), set.getUserId()));
			}
		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings (user) [{}, {}, {}]", serviceId, key, String.valueOf(value), ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return profiles;
	}
	
	public List<OUserSetting> getUserSettings(String serviceId, String key, Object value) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			return dao.selectByServiceKeyValue(con, serviceId, key, valueToString(value));

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings (user) [{}, {}]", serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Sets the setting value indicated by the specified key.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	@Override
	public boolean setUserSetting(UserProfileId profileId, String serviceId, String key, Object value) {
		return setUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, key, value);
	}
	
	/**
	 * Sets the setting value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param userId The user ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	@Override
	public boolean setUserSetting(String domainId, String userId, String serviceId, String key, Object value) {
		if (value!=null) {
			UserSettingDAO dao = UserSettingDAO.getInstance();
			Connection con = null;
			OUserSetting item = null;

			try {
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				item = new OUserSetting();
				item.setDomainId(domainId);
				item.setUserId(userId);
				item.setServiceId(serviceId);
				item.setKey(key);
				item.setValue(valueToString(value));

				int ret = dao.update(con, item);
				if(ret == 0) ret = dao.insert(con, item);
				return true;

			} catch (Exception ex) {
				WebTopApp.logger.error("Unable to set setting (user) [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
				return false;
			} finally {
				DbUtils.closeQuietly(con);
			}
		} else {
			deleteUserSetting(domainId, userId, serviceId, key);
			return true;
		}
	}
	
	/**
	 * Deletes the setting value indicated by the specified key.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	@Override
	public boolean deleteUserSetting(UserProfileId profileId, String serviceId, String key) {
		return deleteUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, key);
	}
	
	/**
	 * Deletes the setting value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	@Override
	public boolean deleteUserSetting(String domainId, String userId, String serviceId, String key) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			int ret = dao.deleteByDomainServiceUserKey(con, domainId, userId, serviceId, key);
			return (ret > 0);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to delete setting (user) [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean clearUserSettings(String domainId, String userId) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			int ret = dao.deleteByDomainUser(con, domainId, userId);
			return (ret > 0);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to clear settings (user) [{}, {}]", domainId, userId, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SystemSetting> listSettings(boolean hidden) {
		ArrayList<SystemSetting> items = new ArrayList<>();
		SettingDAO dao = SettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			for(SettingRow setting : dao.selectAll(con, hidden)) {
				items.add(new SystemSetting(setting));
			}
			return items;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings", ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DomainSetting> listSettings(String domainId, boolean hidden) {
		ArrayList<DomainSetting> items = new ArrayList<>();
		DomainSettingDAO dao = DomainSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			for(DomainSettingRow setting : dao.selectAll(con, hidden)) {
				items.add(new DomainSetting(setting));
			}
			return items;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings", ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OSettingDb getSettingInfo(String serviceId, String key) {
		SettingDbDAO dao = SettingDbDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			return dao.selectByServiceKey(con, serviceId, key);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read setting info", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String valueToString(Object value) {
		return (value == null) ? null : String.valueOf(value);
	}
	
	public static String[] asArray(String value) {
		return StringUtils.split(value, ",");
	}
}
