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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public enum ArithmeticOperatorType {
	
	PLUS("+"),
	MINUS("-"),
	MULTIPLY("*"),
	DIVIDE("/");
	
	private static Map<String, ArithmeticOperatorType> OPERATOR_MAP;
	
	static {
		Map<String, ArithmeticOperatorType> operator_map = new HashMap<>();
		for(ArithmeticOperatorType operatorType : EnumSet.allOf(ArithmeticOperatorType.class)) {
			operator_map.put(operatorType.getOperator(), operatorType);
		}
		OPERATOR_MAP = Collections.unmodifiableMap(operator_map);
	}
	
	private final String operator;

	private ArithmeticOperatorType(String operator) {
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}
	
	public static ArithmeticOperatorType valueOfOperator(String operator) {
		ArithmeticOperatorType operatorType = OPERATOR_MAP.get(operator);
		if (operatorType == null) {
			throw new IllegalArgumentException("Invalid operator: " + operator);
		} else {
			return operatorType;
		}
	}
}
