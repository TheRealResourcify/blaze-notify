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
package com.blazebit.notify.predicate.model;

import java.util.Objects;

public class EnumValue {
    private final String enumName;
    private final String enumKey;

    public EnumValue(String enumName, String enumKey) {
        this.enumName = enumName;
        this.enumKey = enumKey;
    }

    public String getEnumName() {
        return enumName;
    }

    public String getEnumKey() {
        return enumKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumValue enumValue = (EnumValue) o;
        return Objects.equals(enumName, enumValue.enumName) &&
                Objects.equals(enumKey, enumValue.enumKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumName, enumKey);
    }
}
