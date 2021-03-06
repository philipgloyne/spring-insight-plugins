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
package com.springsource.insight.plugin.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

/**
 */
public class JaxrsOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private final RestServiceInstance   _testService;
    private final JaxrsExternalResourceAnalyzer analyzer = new JaxrsExternalResourceAnalyzer();
    
    public JaxrsOperationCollectionAspectTest() {
        _testService = new RestServiceInstance();
    }

    @Test
    public void testRootPath () {
        runTestRestService(_testService, RestServiceDefinitions.ROOT_PATH);
    }

    @Test
    public void testYesterdayPath () {
        runTestRestService(_testService, RestServiceDefinitions.YESTERDAY_PATH);
    }

    @Test
    public void testTomorrowPath () {
        runTestRestService(_testService, RestServiceDefinitions.TOMORROW_PATH);
    }

    private Operation runTestRestService (final RestServiceInstance testService, final String pathDefinition) {
        testService.initialize();

        try {
            final Date  expDate, actDate;
            final long  now=System.currentTimeMillis();
            if (RestServiceDefinitions.ROOT_PATH.equals(pathDefinition)) {
                expDate = testService.getCurrentDate();
                actDate = new Date(now);
            } else if (RestServiceDefinitions.YESTERDAY_PATH.equals(pathDefinition)) {
                expDate = testService.getYesterdayDate(now, true);
                actDate = new Date(now - 86400000L);
            } else if (RestServiceDefinitions.TOMORROW_PATH.equals(pathDefinition)) {
                expDate = testService.getTomorrowDate(now, false);
                actDate = new Date(now + 86400000L);
            } else {
                Assert.fail("Unknown path definition: " + pathDefinition);
                return null;
            }

            final Operation op = getLastEntered();
            Assert.assertNotNull("No operation extracted", op);
            Assert.assertEquals("Mismatched operation type(s)", JaxrsDefinitions.TYPE, op.getType());
            Assert.assertEquals("Mismatched retrieval method", GET.class.getSimpleName(), op.get("method", String.class));

            if (op.isFinalizable()) {
                op.finalizeConstruction();
            }

            final long  valsDiff=Math.abs(expDate.getTime() - actDate.getTime());
            Assert.assertTrue("Mismatched call return values", valsDiff < 5000L);

            final String    expTemplate=RestServiceDefinitions.ROOT_PATH.equals(pathDefinition)
                                ? RestServiceDefinitions.ROOT_PATH
                                : RestServiceDefinitions.ROOT_PATH + "/" + pathDefinition
                                ;
            final String    actTemplate=op.get("requestTemplate", String.class);
            Assert.assertEquals("Mismatched request template", expTemplate, actTemplate);
            
            if (!RestServiceDefinitions.ROOT_PATH.equals(pathDefinition)) {
                final OperationList opList=op.get("pathParams", OperationList.class);
                Assert.assertNotNull("Missing path parameters list", opList);
                Assert.assertEquals("Unexpected number of path parameters", 1, opList.size());
                
                final OperationMap  opMap=opList.get(0, OperationMap.class);
                Assert.assertNotNull("Missing path parameters map", opMap);
                Assert.assertEquals("Unexpected number of mapped path parameters", 3, opMap.size());

                final String    paramName=opMap.get("name", String.class);
                Assert.assertEquals("Mismatched path param name", RestServiceDefinitions.NOW_PARAM_NAME, paramName);

                final Long  paramValue=opMap.get("value", Long.class);
                Assert.assertNotNull("Missing path parameter value", paramValue);
                Assert.assertEquals("Mismatched path parameter value", now, paramValue.longValue());

                final String    paramType=opMap.get("type", String.class);
                Assert.assertEquals("Mismatched path param type", PathParam.class.getSimpleName(), paramType);

                final JaxrsParamType    enumType=JaxrsParamType.fromTypeName(paramType);
                Assert.assertEquals("Mismatched path param enum", JaxrsParamType.PATH, enumType);
            }
            
            //test external resources
            SimpleFrameBuilder builder = new SimpleFrameBuilder();
            builder.enter(op);           
            Frame frame = builder.exit();
            Trace trace = Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
            
            List<ExternalResourceDescriptor> externalResourceDescriptors = (List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);
            assertNotNull("No descriptors extracted", externalResourceDescriptors);
            ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);        
    		assertSame("Mismatched operation instance", op, descriptor.getFrame().getOperation());
    		assertDescriptorContents("testExactlyTwoDifferentExternalResourceNames", expTemplate, descriptor);
            
            return op;
        } finally {
            testService.destroy();
        }
    }
    
    private static ExternalResourceDescriptor assertDescriptorContents (String testName, String path, ExternalResourceDescriptor descriptor) {

        assertEquals(testName + ": Mismatched label", path, descriptor.getLabel());
        assertEquals(testName + ": Mismatched type", ExternalResourceType.WEB_SERVICE.name(), descriptor.getType());

        String expectedHash = MD5NameGenerator.getName(path);
        assertEquals(testName + ": Mismatched name", JaxrsDefinitions.TYPE.getName() + ":" + expectedHash, descriptor.getName());
        return descriptor;
    }
    
    /*
     * @see com.springsource.insight.collection.OperationCollectionAspectTestSupport#getAspect()
     */
    @Override   // co-variant return
    public JaxrsOperationCollectionAspect getAspect() {
        return JaxrsOperationCollectionAspect.aspectOf();
    }
}
