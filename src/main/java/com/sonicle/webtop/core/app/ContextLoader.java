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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.PropUtils;
import com.sonicle.commons.web.ContextUtils;
import com.sonicle.webtop.core.app.servlet.RestApi;
import com.sonicle.webtop.core.app.shiro.filter.JWTSignatureVerifier;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 * @author malbinola
 */
public class ContextLoader {
	private static final Logger logger = WT.getLogger(ContextLoader.class);
	public static final String WEBAPPNAME_ATTRIBUTE_KEY = "wtwebappname";
	public static final String WEBTOPAPP_ATTRIBUTE_KEY = "wtapp";
	
	public static String getWabappName(ServletContext servletContext) {
		return (String)servletContext.getAttribute(WEBAPPNAME_ATTRIBUTE_KEY);
	}
	
	public void initLogging(ServletContext servletContext) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		String webappFullName = ContextUtils.getWebappFullName(servletContext, false); // Like <context-name>##<context-version>
		Properties systemProps = System.getProperties();
		
		// Locates logback configuration file: try custom (webappConfig) first, then standard ones
		URL fileUrl = findURLOfCustomConfigurationFile(WebTopProps.getWebappsConfigDir(systemProps), webappFullName);
		if (fileUrl == null) {
			ClassLoader classLoader = Loader.getClassLoaderOfObject(this);
			fileUrl = LogbackHelper.findURLOfDefaultConfigurationFile(classLoader);
		}
		
		// Re-inits logger configuration...
		try {
			// https://stackoverflow.com/questions/24235296/how-to-define-logback-variables-properties-before-logback-auto-load-logback-xml
			// Resets current configuration
			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(loggerContext);
			loggerContext.reset();
			
			String logDir = WebTopProps.getLogDir(systemProps);
			logDir = expandLogDirVariables(logDir, webappFullName);
			String logFileBasename = PropUtils.isDefined(systemProps, WebTopProps.LOG_FILE_BASENAME) ? WebTopProps.getLogFileBasename() : null;
			if (StringUtils.isBlank(logFileBasename)) logFileBasename = webappFullName;
			String logFileAppender = WebTopProps.getLogAppender(systemProps);
			
			loggerContext.putProperty("logback.webtop.log.dir", logDir);
			loggerContext.putProperty("logback.webtop.log.file.basename", logFileBasename);
			loggerContext.putProperty("logback.webtop.log.appender", logFileAppender);
			printToSystemOut("[{}] Using {} = {}", webappFullName, "logback.webtop.log.dir", logDir);
			printToSystemOut("[{}] Using {} = {}", webappFullName, "logback.webtop.log.file.basename", logFileBasename);
			printToSystemOut("[{}] Using {} = {}", webappFullName, "logback.webtop.log.appender", logFileAppender);
			
			jc.doConfigure(fileUrl);
			
		} catch(JoranException ex) {
			StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
			throw new WTRuntimeException(ex, "Error configuring loggerContext");
		}
	}
	
	private void printToSystemOut(String message, Object... arguments) {
		System.out.println(MessageFormatter.arrayFormat(message, arguments).getMessage());
	}
	
	private URL findURLOfCustomConfigurationFile(String webappsConfigDir, String webappFullName) {
		File file = null;
		if (!StringUtils.isBlank(webappsConfigDir)) {
			// Try to get file under: "/path/to/webappsConfig/myAppName/logback.xml"
			file = new File(PathUtils.concatPathParts(webappsConfigDir, webappFullName, ContextInitializer.AUTOCONFIG_FILE));
			if (file.exists() && file.isFile()) {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e1) {}
			}
			
			// Try to get file under: "/path/to/webappsConfig/logback.xml"
			file = new File(PathUtils.concatPathParts(webappsConfigDir, ContextInitializer.AUTOCONFIG_FILE));
			if (file.exists() && file.isFile()) {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e1) {}
			}
		}
		return null;
	}
	
	private String expandLogDirVariables(String logDir, String webappFullName) {
		if (logDir == null) return logDir;
		String s = StringUtils.replace(logDir, "${WEBAPP_FULLNAME}", webappFullName);
		s = StringUtils.replace(s, "${WEBAPP_NAME}", ContextUtils.stripWebappVersion(webappFullName));
		return s;
	}
	
	public void initApp(ServletContext servletContext) throws IllegalStateException {
		String webappName = ContextUtils.getWebappFullName(servletContext, false);
		servletContext.setAttribute(WEBAPPNAME_ATTRIBUTE_KEY, webappName);
		if (servletContext.getAttribute(WEBTOPAPP_ATTRIBUTE_KEY) != null) {
			throw new IllegalStateException("There is already a WebTop application associated with the current ServletContext.");
		}
		
		try {
			WebTopApp wta = new WebTopApp(servletContext);
			wta.boot();
			servletContext.setAttribute(WEBTOPAPP_ATTRIBUTE_KEY, wta);
			servletContext.setAttribute(JWTSignatureVerifier.SECRET_CONTEXT_ATTRIBUTE, wta.getDocumentServerSecretIn());
			
			Dynamic atmosphereServlet = servletContext.addServlet("AtmosphereServlet", com.sonicle.webtop.core.app.atmosphere.AtmosphereServlet.class);
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.AtmosphereFramework.analytics", "false");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcasterCacheClass", "com.sonicle.webtop.core.app.atmosphere.UUIDBroadcasterCache");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcasterLifeCyclePolicy", "BroadcasterLifeCyclePolicy.EMPTY");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.JSR356AsyncSupport");
			//atmosphereServlet.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.Tomcat7CometSupport");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.sessionSupport", "true");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.sessionCreate", "false");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcaster.shareableThreadPool", "true");
			atmosphereServlet.setInitParameter("org.atmosphere.cpr.AtmosphereInterceptor", "org.atmosphere.interceptor.ShiroInterceptor");
			atmosphereServlet.setLoadOnStartup(1);
			atmosphereServlet.setAsyncSupported(true);
			atmosphereServlet.addMapping(com.sonicle.webtop.core.app.atmosphere.AtmosphereServlet.URL + "/*");
			
			Dynamic resourcesServlet = servletContext.addServlet("ResourcesServlet", com.sonicle.webtop.core.app.servlet.ResourceRequest.class);
			resourcesServlet.setLoadOnStartup(1);
			resourcesServlet.addMapping(com.sonicle.webtop.core.app.servlet.ResourceRequest.URL + "/*");
			
			Dynamic loginServlet = servletContext.addServlet("LoginServlet", com.sonicle.webtop.core.app.servlet.Login.class);
			loginServlet.setLoadOnStartup(1);
			loginServlet.addMapping(com.sonicle.webtop.core.app.servlet.Login.URL + "/*");
			
			Dynamic logoutServlet = servletContext.addServlet("LogoutServlet", com.sonicle.webtop.core.app.servlet.Logout.class);
			logoutServlet.setLoadOnStartup(1);
			logoutServlet.addMapping(com.sonicle.webtop.core.app.servlet.Logout.URL + "/*");
			
			Dynamic otpServlet = servletContext.addServlet("OtpServlet", com.sonicle.webtop.core.app.servlet.Otp.class);
			otpServlet.setLoadOnStartup(1);
			otpServlet.addMapping(com.sonicle.webtop.core.app.servlet.Otp.URL + "/*");
			
			Dynamic uiPrivateServlet = servletContext.addServlet("UIPrivateServlet", com.sonicle.webtop.core.app.servlet.UIPrivate.class);
			uiPrivateServlet.setLoadOnStartup(1);
			uiPrivateServlet.addMapping(com.sonicle.webtop.core.app.servlet.UIPrivate.URL + "/*");
			
			Dynamic privateRequestServlet = servletContext.addServlet("PrivateRequestServlet", com.sonicle.webtop.core.app.servlet.PrivateRequest.class);
			privateRequestServlet.setLoadOnStartup(1);
			privateRequestServlet.addMapping(com.sonicle.webtop.core.app.servlet.PrivateRequest.URL + "/*");
			privateRequestServlet.addMapping(com.sonicle.webtop.core.app.servlet.PrivateRequest.URL_LEGACY + "/*");
			
			Dynamic publicRequestServlet = servletContext.addServlet("PublicRequestServlet", com.sonicle.webtop.core.app.servlet.PublicRequest.class);
			publicRequestServlet.setLoadOnStartup(1);
			publicRequestServlet.addMapping(com.sonicle.webtop.core.app.servlet.PublicRequest.URL + "/*");
			
			Dynamic docEditorServlet = servletContext.addServlet("DocEditorServlet", com.sonicle.webtop.core.app.servlet.DocEditor.class);
			docEditorServlet.setLoadOnStartup(1);
			docEditorServlet.addMapping(com.sonicle.webtop.core.app.servlet.DocEditor.URL + "/*");
			
			// Adds RestApiServlets dynamically
			ServiceManager svcMgr = wta.getServiceManager();
			for (String serviceId : svcMgr.listRegisteredServices()) {
				ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
				
				if (desc.hasOpenApiDefinitions() || desc.hasRestApiEndpoints()) {
					addRestApiServlet(servletContext, desc);
				}
			}
			
		} catch(Throwable t) {
			servletContext.removeAttribute(WEBTOPAPP_ATTRIBUTE_KEY);
			logger.error("Error initializing WTA [{}]", webappName, t);
		}
	}
	
	private void addRestApiServlet(ServletContext servletContext, ServiceDescriptor desc) {
		String serviceId = desc.getManifest().getId();
		String name = serviceId + "@RestApiServlet";
		String path = com.sonicle.webtop.core.app.servlet.RestApi.URL + "/" + serviceId + "/*";
		
		logger.debug("Adding RestApi servlet [{}] -> [{}]", name, path);
		Dynamic servlet = servletContext.addServlet(name, com.sonicle.webtop.core.app.servlet.RestApi.class);
		servlet.setInitParameter("javax.ws.rs.Application", "com.sonicle.webtop.core.app.JaxRsServiceApplication");
		servlet.setInitParameter("com.sun.jersey.config.feature.DisableWADL", "true");
		servlet.setInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID, serviceId);
		servlet.setLoadOnStartup(2);
		servlet.setAsyncSupported(true);
		servlet.addMapping(path);
	}
	
	protected void destroyApp(ServletContext servletContext) {
		final String appname = getWabappName(servletContext);
		try {
			WebTopApp.get(servletContext).shutdown();
			
		} catch(Throwable t) {
			logger.error("Error destroying WTA [{}]", appname, t);
		} finally {
			servletContext.removeAttribute(WEBTOPAPP_ATTRIBUTE_KEY);
		}
	}
}
