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

import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.notification.*;
import com.blazebit.notify.template.api.TemplateContext;
import com.blazebit.notify.template.api.TemplateProcessor;
import com.blazebit.notify.template.api.TemplateProcessorFactory;

import java.util.*;
import java.util.function.Function;

public class SmtpNotificationMessageResolver implements NotificationMessageResolver<SmtpNotificationMessage> {

    public static final String SMTP_MESSAGE_FROM_PROPERTY = "channel.smtp.message.from";
    public static final String SMTP_MESSAGE_FROM_NAME_PROPERTY = "channel.smtp.message.from_name";
    public static final String SMTP_MESSAGE_REPLY_TO_PROPERTY = "channel.smtp.message.reply_to";
    public static final String SMTP_MESSAGE_REPLY_TO_NAME_PROPERTY = "channel.smtp.message_reply_to_name";
    public static final String SMTP_MESSAGE_ENVELOP_FROM_PROPERTY = "channel.smtp.message.envelop_from";
    public static final String SMTP_MESSAGE_RESOURCE_BUNDLE_PROPERTY = "channel.smtp.message.resource_bundle";

    public static final String SMTP_TEMPLATE_CONTEXT_PROPERTY = "channel.smtp.message.template_context";
    public static final String SMTP_TEMPLATE_PROCESSOR_FACTORY_PROPERTY = "channel.smtp.message.template_processor_factory";

    public static final String SMTP_MESSAGE_SUBJECT_PROPERTY = "channel.smtp.message.subject";
    public static final String SMTP_MESSAGE_TEXT_PROPERTY = "channel.smtp.message.text";
    public static final String SMTP_MESSAGE_HTML_PROPERTY = "channel.smtp.message.html";
    public static final String SMTP_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY = "channel.smtp.message.attachement_processors";

    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String envelopeFrom;
    private final Function<Locale, ResourceBundle> resourceBundleAccessor;
    private final TemplateProcessor<String> subjectTemplateProcessor;
    private final TemplateProcessor<String> textBodyTemplateProcessor;
    private final TemplateProcessor<String> htmlBodyTemplateProcessor;
    private final Collection<TemplateProcessor<Attachment>> attachmentProcessors;

    public SmtpNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
        this.from = configurationSource.getPropertyOrFail(SMTP_MESSAGE_FROM_PROPERTY, String.class, Function.identity());
        this.fromDisplayName = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_FROM_NAME_PROPERTY, String.class, Function.identity(), o -> null);
        this.replyTo = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_REPLY_TO_PROPERTY, String.class, Function.identity(), o -> null);
        this.replyToDisplayName = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_REPLY_TO_NAME_PROPERTY, String.class, Function.identity(), o -> null);
        this.envelopeFrom = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_ENVELOP_FROM_PROPERTY, String.class, Function.identity(), o -> null);
        this.resourceBundleAccessor = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_RESOURCE_BUNDLE_PROPERTY, Function.class, s -> resourceBundleByName(s), o -> null);
        TemplateContext templateContext = configurationSource.getPropertyOrDefault(SMTP_TEMPLATE_CONTEXT_PROPERTY, TemplateContext.class, null, o -> jobContext.getService(TemplateContext.class));
        TemplateProcessorFactory templateProcessorFactory = configurationSource.getPropertyOrDefault(SMTP_TEMPLATE_PROCESSOR_FACTORY_PROPERTY, TemplateProcessorFactory.class, s -> {
            if (templateContext == null) {
                throw new NotificationException("No template context given!");
            }
            return templateContext.getTemplateProcessorFactory(s, String.class);
        }, o -> null);
        Function<String, TemplateProcessor> templateProcessorFunction = s -> templateProcessorByName(templateContext, templateProcessorFactory, configurationSource, s);
        this.subjectTemplateProcessor = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_SUBJECT_PROPERTY, TemplateProcessor.class, templateProcessorFunction, o -> null);
        this.textBodyTemplateProcessor = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_TEXT_PROPERTY, TemplateProcessor.class, templateProcessorFunction, o -> null);
        this.htmlBodyTemplateProcessor = configurationSource.getPropertyOrDefault(SMTP_MESSAGE_HTML_PROPERTY, TemplateProcessor.class, templateProcessorFunction, o -> null);
        Object o = configurationSource.getProperty(SMTP_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY);
        List<TemplateProcessor<Attachment>> attachmentProcessors = Collections.emptyList();
        if (o instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) o;
            attachmentProcessors = new ArrayList<>(collection.size());
            for (Object element : collection) {
                if (element instanceof TemplateProcessor<?>) {
                    attachmentProcessors.add((TemplateProcessor<Attachment>) element);
                } else if (element instanceof String) {
                    attachmentProcessors.add(templateProcessorFunction.apply((String) element));
                } else {
                    throw new NotificationException("Invalid attachment processor given via property '" + SMTP_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY + "': " + element);
                }
            }
        } else if (o != null) {
            throw new NotificationException("Invalid attachment processors given via property '" + SMTP_MESSAGE_ATTACHMENT_PROCESSORS_PROPERTY + "': " + o);
        }
        this.attachmentProcessors = attachmentProcessors;
    }

    public SmtpNotificationMessageResolver(String from, String fromDisplayName, String replyTo, String replyToDisplayName, String envelopeFrom, String resourceBundleName,
                                           TemplateProcessor<String> subjectTemplateProcessor, TemplateProcessor<String> textBodyTemplateProcessor,
                                           TemplateProcessor<String> htmlBodyTemplateProcessor, Collection<TemplateProcessor<Attachment>> attachmentProcessors) {
        this.from = from;
        this.fromDisplayName = fromDisplayName;
        this.replyTo = replyTo;
        this.replyToDisplayName = replyToDisplayName;
        this.envelopeFrom = envelopeFrom;
        this.resourceBundleAccessor = resourceBundleByName(resourceBundleName);
        this.subjectTemplateProcessor = subjectTemplateProcessor;
        this.textBodyTemplateProcessor = textBodyTemplateProcessor;
        this.htmlBodyTemplateProcessor = htmlBodyTemplateProcessor;
        this.attachmentProcessors = attachmentProcessors == null ? Collections.emptyList() : attachmentProcessors;
    }

    private static Function<Locale, ResourceBundle> resourceBundleByName(String name) {
        return locale -> ResourceBundle.getBundle(name, locale);
    }

    private static TemplateProcessor<String> templateProcessorByName(TemplateContext templateContext, TemplateProcessorFactory<String> templateProcessorFactory, ConfigurationSource configurationSource, String string) {
        if (templateContext == null) {
            throw new NotificationException("No template context given!");
        }
        if (templateProcessorFactory == null) {
            throw new NotificationException("No template processor factory given!");
        }
        return templateProcessorFactory.createTemplateProcessor(templateContext, key -> {
            if ("template".equals(key)) {
                return string;
            } else {
                return configurationSource.getProperty(key);
            }
        });
    }

    @Override
    public SmtpNotificationMessage resolveNotificationMessage(Notification<?> notification) {
        Map<String, Object> model = new HashMap<>(notification.getParameters());
        NotificationRecipient<?> notificationRecipient = notification.getRecipient();
        Locale locale = notificationRecipient.getLocale();
        if (resourceBundleAccessor != null) {
            ResourceBundle resourceBundle = resourceBundleAccessor.apply(locale);
            model.put("resourceBundle", resourceBundle);
        }
        model.put("locale", locale);
        model.put("recipient", notificationRecipient);
        model = Collections.unmodifiableMap(model);

        EmailSubject subject = subjectTemplateProcessor == null ? null : new EmailSubject(subjectTemplateProcessor.processTemplate(model));
        EmailBody textBody = textBodyTemplateProcessor == null ? null : new EmailBody(textBodyTemplateProcessor.processTemplate(model));
        EmailBody htmlBody = htmlBodyTemplateProcessor == null ? null : new EmailBody(htmlBodyTemplateProcessor.processTemplate(model));
        Collection<Attachment> attachments = new ArrayList<>(attachmentProcessors.size());
        for (TemplateProcessor<Attachment> attachmentTemplateProcessor : attachmentProcessors) {
            attachments.add(attachmentTemplateProcessor.processTemplate(model));
        }
        return new SmtpNotificationMessage(from, fromDisplayName, replyTo, replyToDisplayName, envelopeFrom, subject, textBody, htmlBody, attachments);
    }
}
