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

package com.blazebit.notify.server.config;

import com.blazebit.notify.expression.ExpressionServiceFactory;
import com.blazebit.notify.notification.NotificationJobContext;
import com.blazebit.notify.notification.channel.smtp.SmtpChannel;
import com.blazebit.notify.notification.channel.smtp.SmtpNotificationMessageResolver;
import com.blazebit.notify.server.notification.*;
import com.blazebit.notify.template.api.TemplateContext;
import com.blazebit.persistence.CriteriaBuilderFactory;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.concurrent.ScheduledExecutorService;

@ApplicationScoped
public class JobManagerProducer {

    @Inject
    EntityManager entityManager;
    @Inject
    CriteriaBuilderFactory criteriaBuilderFactory;
    @Inject
    ExpressionServiceFactory expressionServiceFactory;

    @Resource
    ManagedScheduledExecutorService scheduledExecutorService;

    @Produces
    @ApplicationScoped
    public NotificationJobContext createNotificationJobContext() {
        return NotificationJobContext.Builder.create()
                .withService(EntityManager.class, entityManager)
                .withService(ExpressionServiceFactory.class, expressionServiceFactory)
                .withService(CriteriaBuilderFactory.class, criteriaBuilderFactory)
                .withService(ScheduledExecutorService.class, scheduledExecutorService)
                .withJobProcessorFactory(new NotificationJobProcessorFactoryImpl())
                .withJobInstanceProcessorFactory(new NotificationJobInstanceProcessorFactoryImpl())
                .withNotificationProcessorFactory(new NotificationProcessorFactoryImpl())
                .withChannelResolver(new ChannelResolverImpl())
                .withRecipientResolver(new NotificationRecipientResolverImpl())
                .withProperty(SmtpChannel.SMTP_HOST_PROPERTY, "192.168.99.100")
                .withProperty(SmtpChannel.SMTP_PORT_PROPERTY, 25)
//                .withProperty(SmtpChannel.SMTP_USER_PROPERTY, "test")
//                .withProperty(SmtpChannel.SMTP_PASSWORD_PROPERTY, "test")
                .withProperty(SmtpNotificationMessageResolver.SMTP_TEMPLATE_CONTEXT_PROPERTY, TemplateContext.Builder.create().createContext())
                .withProperty(SmtpNotificationMessageResolver.SMTP_TEMPLATE_PROCESSOR_FACTORY_PROPERTY, "freemarker")
                .withProperty(SmtpNotificationMessageResolver.SMTP_MESSAGE_FROM_PROPERTY, "christian@blazebit.com")
                .withProperty(SmtpNotificationMessageResolver.SMTP_MESSAGE_FROM_NAME_PROPERTY, "Christian Beikov")
//                .withProperty(SmtpNotificationMessageResolver.SMTP_MESSAGE_REPLY_TO_PROPERTY, "christian@blazebit.com")
//                .withProperty(SmtpNotificationMessageResolver.SMTP_MESSAGE_REPLY_TO_NAME_PROPERTY, "Christian Beikov")
                .withProperty(SmtpNotificationMessageResolver.SMTP_MESSAGE_SUBJECT_PROPERTY, "subject.ftl")
                .withProperty(SmtpNotificationMessageResolver.SMTP_MESSAGE_TEXT_PROPERTY, "text.ftl")
                .createContext();
    }

    public void closeNotificationJobContext(@Disposes NotificationJobContext notificationJobContext) {
        notificationJobContext.stop();
    }
}
