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

package com.blazebit.notify.domain.impl.spi;

import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.impl.boot.model.DomainBuilderImpl;
import com.blazebit.notify.domain.spi.DomainBuilderProvider;
import com.blazebit.notify.domain.spi.DomainContributor;

import java.util.ServiceLoader;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DomainBuilderProviderImpl implements DomainBuilderProvider {

    @Override
    public DomainBuilder createEmptyBuilder() {
        return new DomainBuilderImpl();
    }

    @Override
    public DomainBuilder createDefaultBuilder() {
        DomainBuilderImpl domainBuilder = new DomainBuilderImpl();
        for (DomainContributor domainContributor : ServiceLoader.load(DomainContributor.class)) {
            domainContributor.contribute(domainBuilder);
        }
        return domainBuilder;
    }
}