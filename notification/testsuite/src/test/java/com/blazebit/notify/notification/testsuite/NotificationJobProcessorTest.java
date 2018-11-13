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
package com.blazebit.notify.notification.testsuite;

import com.blazebit.notify.notification.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Queue;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NotificationJobProcessorTest<N extends Notification<T>, T extends NotificationMessage> extends AbstractConfigurationTest<N, T> {

    public NotificationJobProcessorTest(Channel<N, T> channel, NotificationJobScheduler jobScheduler, T defaultMessage, Queue<NotificationMessage> sink) {
        super(channel, jobScheduler, defaultMessage, sink);
    }

    @Parameterized.Parameters
    public static Object[][] createCombinations() {
        return createCombinations(new NotificationJobProcessor() {
            @Override
            public Notification process(NotificationJob notificationJob, NotificationJobContext context) {
                notificationJob.getChannel().sendNotification(null, new SimpleNotificationMessage());
                notificationJob.getChannel().sendNotification(null, new SimpleNotificationMessage());
                return null;
            }
        });
    }

    @Test
    public void simpleTest() {
        jobScheduler.add(new SimpleNotificationJob((Channel<SimpleNotification<SimpleNotificationMessage>, SimpleNotificationMessage>) channel, new SimpleSchedule(), new SimpleSchedule()));
        jobScheduler.stop();
        assertEquals(2, sink.size());
    }

}