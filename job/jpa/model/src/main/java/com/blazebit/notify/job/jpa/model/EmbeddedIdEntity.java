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

package com.blazebit.notify.job.jpa.model;

import javax.persistence.Transient;
import java.io.Serializable;

public abstract class EmbeddedIdEntity<I extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;

	private I id;

	public EmbeddedIdEntity() {
	}

	public EmbeddedIdEntity(I id) {
		this.id = id;
	}

	public I id() {
		return id;
	}

	public void setId(I id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getNoProxyClass(getClass()) != getNoProxyClass(obj.getClass()))
			return false;
		EmbeddedIdEntity<?> other = (EmbeddedIdEntity<?>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Transient
	public Class<?> getEntityClass() {
		return getNoProxyClass(getClass());
	}

	private static Class<?> getNoProxyClass(Class<?> clazz) {
		while (clazz.getName().contains("javassist")) {
			clazz = clazz.getSuperclass();
		}
		
		return clazz;
	}
}
