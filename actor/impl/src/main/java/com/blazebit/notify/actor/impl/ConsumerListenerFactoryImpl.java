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

package com.blazebit.notify.actor.impl;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.actor.ActorManager;
import com.blazebit.notify.actor.ConsumingActor;
import com.blazebit.notify.actor.spi.ActorManagerFactory;
import com.blazebit.notify.actor.spi.ConsumerListener;
import com.blazebit.notify.actor.spi.ConsumerListenerFactory;

@ServiceProvider(ConsumerListenerFactory.class)
public class ConsumerListenerFactoryImpl implements ConsumerListenerFactory {

    @Override
    public <T> ConsumerListener<T> createConsumerListener(ActorContext context, ConsumingActor<T> consumingActor) {
        return new ConsumerListenerImpl<>(context, consumingActor);
    }

}
