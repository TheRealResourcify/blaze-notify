/*
 * Copyright 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.notify.notification.channel.smtp;

import com.blazebit.notify.notification.Channel;
import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.security.HostnameVerificationPolicy;
import com.blazebit.notify.notification.security.JSSETruststoreConfigurator;
import com.blazebit.notify.notification.security.TruststoreProvider;
import com.blazebit.notify.notification.security.TruststoreProviderFactory;
import com.sun.mail.smtp.SMTPMessage;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLSocketFactory;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmtpChannel<N extends Notification<SmtpNotificationReceiver, N, SmtpMessage>> implements Channel<SmtpNotificationReceiver, N, SmtpMessage> {

    private static final Logger LOG = Logger.getLogger(SmtpChannel.class.getName());

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String HTML_MIME_TYPE = "text/html; charset=" + CHARSET_UTF8;

    private final Config config;
    private Session session;
    private Transport transport;

    public SmtpChannel(Config config) {
        this.config = config;
    }

    @Override
    public void init() {
        Properties props = new Properties();
        if (config.host != null) {
            props.setProperty("mail.smtp.host", config.host);
        }

        if (config.port != null) {
            props.setProperty("mail.smtp.port", config.port.toString());
        }

        if (config.auth) {
            props.setProperty("mail.smtp.auth", "true");
        }

        if (config.enableSsl) {
            props.setProperty("mail.smtp.ssl.enable", "true");
        }

        if (config.enableStartTls) {
            props.setProperty("mail.smtp.starttls.enable", "true");
        }

        if (config.enableSsl || config.enableStartTls) {
            setupTruststore(props);
        }

        props.setProperty("mail.smtp.timeout", Long.toString(config.timeout));
        props.setProperty("mail.smtp.connectiontimeout", Long.toString(config.connectionTimeout));

        session = Session.getInstance(props);
        try {
            transport = session.getTransport("smtp");
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (transport != null) {
            try {
                transport.close();
            } catch (MessagingException e) {
                LOG.log(Level.WARNING, "Failed to close transport", e);
            }
        }
    }

    @Override
    public void sendNotificationMessage(SmtpNotificationReceiver receiver, SmtpMessage message) {
        try {
            if (!transport.isConnected()) {
                if (config.auth) {
                    transport.connect(config.user, config.password);
                } else {
                    transport.connect();
                }
            }


            SMTPMessage msg = new SMTPMessage(session);

            EmailBody textBody = message.getTextBody();
            EmailBody htmlBody = message.getHtmlBody();
            if (textBody != null && htmlBody != null) {
                Multipart multipart = new MimeMultipart("alternative");
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(textBody.getBody(), CHARSET_UTF8);
                multipart.addBodyPart(textPart);

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody.getBody(), HTML_MIME_TYPE);
                multipart.addBodyPart(htmlPart);

                msg.setContent(multipart);
            } else if (textBody != null) {
                msg.setText(textBody.getBody(), CHARSET_UTF8);
            } else if (htmlBody != null) {
                msg.setContent(htmlBody.getBody(), HTML_MIME_TYPE);
            }

            String from = message.getFrom();
            String fromDisplayName = message.getFromDisplayName();
            msg.setFrom(toInternetAddress(from, fromDisplayName));

            msg.setReplyTo(new Address[]{toInternetAddress(from, fromDisplayName)});
            String replyTo = message.getReplyTo();
            if (replyTo != null && !replyTo.isEmpty()) {
                msg.setReplyTo(new Address[]{toInternetAddress(replyTo, message.getReplyToDisplayName())});
            }
            String envelopeFrom = message.getEnvelopeFrom();
            if (envelopeFrom != null && !envelopeFrom.isEmpty()) {
                msg.setEnvelopeFrom(envelopeFrom);
            }

            msg.setHeader("To", receiver.getEmail());
            msg.setSubject(message.getSubject().getSubject(), "utf-8");
            msg.saveChanges();
            msg.setSentDate(new Date());

            transport.sendMessage(msg, new InternetAddress[]{new InternetAddress(receiver.getEmail())});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send email", e);
            throw new RuntimeException(e);
        }
    }

    protected InternetAddress toInternetAddress(String email, String displayName) throws UnsupportedEncodingException, AddressException {
        if (email == null || "".equals(email.trim())) {
            throw new IllegalArgumentException("Please provide a valid address", null);
        }
        if (displayName == null || "".equals(displayName.trim())) {
            return new InternetAddress(email);
        }
        return new InternetAddress(email, displayName, "utf-8");
    }

    private void setupTruststore(Properties props) {
        TruststoreProvider truststoreProvider = loadTruststoreProvider();
        JSSETruststoreConfigurator configurator = new JSSETruststoreConfigurator(truststoreProvider);

        SSLSocketFactory factory = configurator.getSSLSocketFactory();
        if (factory != null) {
            props.put("mail.smtp.ssl.socketFactory", factory);
            if (configurator.getProvider().getPolicy() == HostnameVerificationPolicy.ANY) {
                props.setProperty("mail.smtp.ssl.trust", "*");
            }
        }
    }

    private TruststoreProvider loadTruststoreProvider() {
        Iterator<TruststoreProviderFactory> iter = ServiceLoader.load(TruststoreProviderFactory.class).iterator();
        return iter.hasNext() ? iter.next().create() : null;
    }

    public static class Config {
        private final String host;
        private final Integer port;
        private final boolean auth;
        private final String user;
        private final String password;
        private final boolean enableSsl;
        private final boolean enableStartTls;
        private final long timeout;
        private final long connectionTimeout;

        Config(String host, Integer port, boolean auth, String user, String password, boolean enableSsl, boolean enableStartTls, long timeout, long connectionTimeout) {
            this.host = host;
            this.port = port;
            this.auth = auth;
            this.user = user;
            this.password = password;
            this.enableSsl = enableSsl;
            this.enableStartTls = enableStartTls;
            this.timeout = timeout;
            this.connectionTimeout = connectionTimeout;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public boolean isAuth() {
            return auth;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public boolean isEnableSsl() {
            return enableSsl;
        }

        public boolean isEnableStartTls() {
            return enableStartTls;
        }

        public long getTimeout() {
            return timeout;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String host;
            private Integer port;
            private boolean auth;
            private String user;
            private String password;
            private boolean enableSsl;
            private boolean enableStartTls;
            private long timeout = 10000;
            private long connectionTimeout = 10000;

            public Config build() {
                return new Config(host, port, auth, user, password, enableSsl, enableStartTls, timeout, connectionTimeout);
            }

            public Builder withHost(String host) {
                this.host = host;
                return this;
            }

            public Builder withPort(Integer port) {
                this.port = port;
                return this;
            }

            public Builder enableAuth(String user, String password) {
                this.auth = true;
                this.user = user;
                this.password = password;
                return this;
            }

            public Builder withEnableSsl(boolean enableSsl) {
                this.enableSsl = enableSsl;
                return this;
            }

            public Builder withEnableStartTls(boolean enableStartTls) {
                this.enableStartTls = enableStartTls;
                return this;
            }

            public Builder withTimeout(long timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder withConnectionTimeout(long connectionTimeout) {
                this.connectionTimeout = connectionTimeout;
                return this;
            }
        }
    }
}