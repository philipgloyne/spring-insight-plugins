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

import java.util.ArrayList;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.time.TimeRange;

public abstract class AbstractRabbitMQResourceAnalyzer implements EndPointAnalyzer, ExternalResourceAnalyzer {
	static MD5NameGenerator nameGenerator = new MD5NameGenerator();

	static final String RABBIT = "RabbitMQ";

	public RabbitPluginOperationType operationType;

	AbstractRabbitMQResourceAnalyzer(RabbitPluginOperationType operationType) {
		super();
		this.operationType = operationType;
	}

	protected abstract String getExchange(Operation op);

	protected abstract String getRoutingKey(Operation op);

	public EndPointAnalysis locateEndPoint(Trace trace) {
		EndPointAnalysis analysis = null;
		Frame frame = trace.getFirstFrameOfType(operationType.getOperationType());

		if (frame != null) {
			Operation op = frame.getOperation();
			if (op != null) {
				String label = buildLabel(op);
				String endPointLabel = RABBIT + "-" + label;

				String example = getExample(label);
				EndPointName endPointName = getName(label);

				TimeRange responseTime = frame.getRange();
				return new EndPointAnalysis(responseTime, endPointName, endPointLabel, example, 0);
			}
		}

		return analysis;
	}

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		List<Frame> queueFrames = trace.getLastFramesOfType(operationType.getOperationType());

		List<ExternalResourceDescriptor> queueDescriptors = new ArrayList<ExternalResourceDescriptor>();
		String vendor = RABBIT;
		String type = ExternalResourceType.QUEUE.name();

		for (Frame queueFrame : queueFrames) {
			Operation op = queueFrame.getOperation();

			String label = buildLabel(op);
			String host = op.get("host", String.class);            
			Integer portProperty = op.get("port", Integer.class);
			int port = portProperty == null ? -1 : portProperty;

			String hashString = nameGenerator.getName(label + host + port);

			ExternalResourceDescriptor descriptor = new ExternalResourceDescriptor(queueFrame, RABBIT + ":" + hashString, RABBIT + "-" + label, type, vendor, host, port);
			queueDescriptors.add(descriptor);            
		}

		return queueDescriptors;
	}

	private EndPointName getName(String label) {
		return EndPointName.valueOf(label);
	}

	private String getExample(String label) {
		return operationType.getEndPointPrefix()+label;
	}

	private String buildLabel(Operation op) {
		StringBuilder sb = new StringBuilder();

		String routingKey = getRoutingKey(op);
		String exchange = getExchange(op);      

		boolean hasExchange = !isTrimEmpty(exchange);		

		if (hasExchange) {
			sb.append("Exchange#").append(exchange);
		} 

		if (!isTrimEmpty(routingKey)) {				
			if (hasExchange) {
				sb.append(" ");
			}
			sb.append("RoutingKey#").append(routingKey);
		}

		return sb.toString();
	}
	
	private static boolean isTrimEmpty(String str){
    	return (str == null) || (str.trim().length() == 0);
    }

}