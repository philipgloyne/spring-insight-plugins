/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.rabbitmqClient;

import com.springsource.insight.intercept.operation.Operation;

public class RabbitMQPublishResourceAnalyzer extends AbstractRabbitMQResourceAnalyzer {
    
	public RabbitMQPublishResourceAnalyzer() {
		super(RabbitPluginOperationType.PUBLISH);
	}

	@Override
    protected String getRoutingKey(Operation op) {
        return op.get("routingKey", String.class);
    }
    
    @Override
    protected String getExchange(Operation op) {
        return op.get("exchange", String.class);
    }
}
