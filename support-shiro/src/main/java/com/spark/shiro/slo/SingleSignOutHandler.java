package com.spark.shiro.slo;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SingleSignOutHandler {
    private static final int DECOMPRESSION_FACTOR = 10;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private SessionMappingStorage sessionMappingStorage = new HashMapBackedSessionMappingStorage();
    private String artifactParameterName;
    private String logoutParameterName;
    private String frontLogoutParameterName;
    private String relayStateParameterName;
    private String casServerUrlPrefix;
    private boolean artifactParameterOverPost;
    private boolean eagerlyCreateSessions;
    private List<String> safeParameters;
    private LogoutStrategy logoutStrategy;

    public SingleSignOutHandler() {
        this.artifactParameterName = Protocol.CAS2.getArtifactParameterName();
        this.logoutParameterName = (String)ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue();
        this.frontLogoutParameterName = (String)ConfigurationKeys.FRONT_LOGOUT_PARAMETER_NAME.getDefaultValue();
        this.relayStateParameterName = (String)ConfigurationKeys.RELAY_STATE_PARAMETER_NAME.getDefaultValue();
        this.casServerUrlPrefix = "";
        this.artifactParameterOverPost = false;
        this.eagerlyCreateSessions = true;
        this.logoutStrategy = (LogoutStrategy)(isServlet30() ? new Servlet30LogoutStrategy() : new Servlet25LogoutStrategy());
    }

    public void setSessionMappingStorage(SessionMappingStorage storage) {
        this.sessionMappingStorage = storage;
    }

    public void setArtifactParameterOverPost(boolean artifactParameterOverPost) {
        this.artifactParameterOverPost = artifactParameterOverPost;
    }

    public SessionMappingStorage getSessionMappingStorage() {
        return this.sessionMappingStorage;
    }

    public void setArtifactParameterName(String name) {
        this.artifactParameterName = name;
    }

    public void setLogoutParameterName(String name) {
        this.logoutParameterName = name;
    }

    public void setCasServerUrlPrefix(String casServerUrlPrefix) {
        this.casServerUrlPrefix = casServerUrlPrefix;
    }

    public void setFrontLogoutParameterName(String name) {
        this.frontLogoutParameterName = name;
    }

    public void setRelayStateParameterName(String name) {
        this.relayStateParameterName = name;
    }

    public void setEagerlyCreateSessions(boolean eagerlyCreateSessions) {
        this.eagerlyCreateSessions = eagerlyCreateSessions;
    }

    public synchronized void init() {
        if (this.safeParameters == null) {
            CommonUtils.assertNotNull(this.artifactParameterName, "artifactParameterName cannot be null.");
            CommonUtils.assertNotNull(this.logoutParameterName, "logoutParameterName cannot be null.");
            CommonUtils.assertNotNull(this.frontLogoutParameterName, "frontLogoutParameterName cannot be null.");
            CommonUtils.assertNotNull(this.sessionMappingStorage, "sessionMappingStorage cannot be null.");
            CommonUtils.assertNotNull(this.relayStateParameterName, "relayStateParameterName cannot be null.");
            CommonUtils.assertNotNull(this.casServerUrlPrefix, "casServerUrlPrefix cannot be null.");
            if (CommonUtils.isBlank(this.casServerUrlPrefix)) {
                this.logger.warn("Front Channel single sign out redirects are disabled when the 'casServerUrlPrefix' value is not set.");
            }

            if (this.artifactParameterOverPost) {
                this.safeParameters = Arrays.asList(this.logoutParameterName, this.artifactParameterName);
            } else {
                this.safeParameters = Arrays.asList(this.logoutParameterName);
            }
        }

    }

    private boolean isTokenRequest(HttpServletRequest request) {
        return CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.artifactParameterName, this.safeParameters));
    }

    private boolean isBackChannelLogoutRequest(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) && !this.isMultipartRequest(request) && CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.logoutParameterName, this.safeParameters));
    }

    private boolean isFrontChannelLogoutRequest(HttpServletRequest request) {
        return "GET".equals(request.getMethod()) && CommonUtils.isNotBlank(this.casServerUrlPrefix) && CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.frontLogoutParameterName));
    }

    public boolean process(HttpServletRequest request, HttpServletResponse response) {
        if (this.isTokenRequest(request)) {
            this.logger.trace("Received a token request");
            this.recordSession(request);
            return true;
        } else if (this.isBackChannelLogoutRequest(request)) {
            this.logger.trace("Received a back channel logout request");
            this.destroySession(request);
            return false;
        } else if (this.isFrontChannelLogoutRequest(request)) {
            this.logger.trace("Received a front channel logout request");
            this.destroySession(request);
            String redirectionUrl = this.computeRedirectionToServer(request);
            if (redirectionUrl != null) {
                CommonUtils.sendRedirect(response, redirectionUrl);
            }

            return false;
        } else {
            this.logger.trace("Ignoring URI for logout: {}", request.getRequestURI());
            return true;
        }
    }

    private void recordSession(HttpServletRequest request) {
        //HttpSession session = request.getSession(this.eagerlyCreateSessions);
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser == null) {
            this.logger.debug("No session currently exists (and none created).  Cannot record session information for single sign out.");
        } else {
            String token = CommonUtils.safeGetParameter(request, this.artifactParameterName, this.safeParameters);
            this.logger.debug("Recording session for token {}", token);

            try {
                this.sessionMappingStorage.removeSessionByMappingId(token);
            } catch (Exception var5) {
                ;
            }

            this.sessionMappingStorage.addSessionById(token, currentUser);
        }
    }

    private String uncompressLogoutMessage(String originalMessage) {
        byte[] binaryMessage = Base64.decodeBase64(originalMessage);
        Inflater decompresser = null;

        String var6;
        try {
            decompresser = new Inflater();
            decompresser.setInput(binaryMessage);
            byte[] result = new byte[binaryMessage.length * 10];
            int resultLength = decompresser.inflate(result);
            var6 = new String(result, 0, resultLength, "UTF-8");
        } catch (Exception var10) {
            this.logger.error("Unable to decompress logout message", var10);
            throw new RuntimeException(var10);
        } finally {
            if (decompresser != null) {
                decompresser.end();
            }

        }

        return var6;
    }

    private void destroySession(HttpServletRequest request) {
        String logoutMessage;
        if (this.isFrontChannelLogoutRequest(request)) {
            logoutMessage = this.uncompressLogoutMessage(CommonUtils.safeGetParameter(request, this.frontLogoutParameterName));
        } else {
            logoutMessage = CommonUtils.safeGetParameter(request, this.logoutParameterName, this.safeParameters);
        }

        this.logger.trace("Logout request:\n{}", logoutMessage);
        String token = XmlUtils.getTextForElement(logoutMessage, "SessionIndex");
        if (CommonUtils.isNotBlank(token)) {
            Subject subject = this.sessionMappingStorage.removeSessionByMappingId(token);
            if (subject != null) {
                Serializable sessionID = subject.getSession().getId();
                this.logger.debug("Invalidating session [{}] for token [{}]", sessionID, token);
                try {
                    subject.logout();
                    request.getSession().invalidate();
                } catch (IllegalStateException var7) {
                    this.logger.debug("Error invalidating session.", var7);
                }
            }
        }

    }

    private String computeRedirectionToServer(HttpServletRequest request) {
        String relayStateValue = CommonUtils.safeGetParameter(request, this.relayStateParameterName);
        if (CommonUtils.isNotBlank(relayStateValue)) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.casServerUrlPrefix);
            if (!this.casServerUrlPrefix.endsWith("/")) {
                buffer.append("/");
            }

            buffer.append("logout?_eventId=next&");
            buffer.append(this.relayStateParameterName);
            buffer.append("=");
            buffer.append(CommonUtils.urlEncode(relayStateValue));
            String redirectUrl = buffer.toString();
            this.logger.debug("Redirection url to the CAS server: {}", redirectUrl);
            return redirectUrl;
        } else {
            return null;
        }
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart");
    }

    private static boolean isServlet30() {
        try {
            return HttpServletRequest.class.getMethod("logout") != null;
        } catch (NoSuchMethodException var1) {
            return false;
        }
    }

    private class Servlet30LogoutStrategy implements LogoutStrategy {
        private Servlet30LogoutStrategy() {
        }

        public void logout(HttpServletRequest request) {
            try {
                request.logout();
            } catch (ServletException var3) {
                logger.debug("Error performing request.logout.");
            }

        }
    }

    private class Servlet25LogoutStrategy implements LogoutStrategy {
        private Servlet25LogoutStrategy() {
        }

        public void logout(HttpServletRequest request) {
        }
    }

    private interface LogoutStrategy {
        void logout(HttpServletRequest var1);
    }
}