package com.amazonaws.xray.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A plugin, for use with the {@code AWSXRayRecorderBuilder} class, which will add Elastic Beanstalk environment information to segments generated by the built {@code AWSXRayRecorder} instance.
 * 
 * @see com.amazonaws.xray.AWSXRayRecorderBuilder#withPlugin(Plugin)
 *
 */
public class ElasticBeanstalkPlugin implements Plugin {
    private static final Log logger =
        LogFactory.getLog(ElasticBeanstalkPlugin.class);

    private ObjectMapper objectMapper;
    private Map<String, Object> runtimeContext;

    private static final String CONF_PATH = "/var/elasticbeanstalk/xray/environment.conf";
    private static final String SERVICE_NAME = "elastic_beanstalk";

    public ElasticBeanstalkPlugin() {
        objectMapper = new ObjectMapper();
        runtimeContext = new HashMap<>();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    public void populateRuntimeContext() {
        byte[] manifestBytes = new byte[0];
        try {
            manifestBytes = Files.readAllBytes(Paths.get(CONF_PATH));
        } catch (IOException | OutOfMemoryError | SecurityException e) {
            logger.warn("Unable to read Beanstalk configuration at path " + CONF_PATH + " : " + e.getMessage());
            return;
        }
        try {
            TypeReference<HashMap<String,Object>> typeReference = new TypeReference<HashMap<String,Object>>() {};
            runtimeContext = objectMapper.readValue(manifestBytes, typeReference);
        } catch (IOException e) {
            logger.warn("Unable to read Beanstalk configuration at path " + CONF_PATH + " : " + e.getMessage());
            return;
        }
    }

    @Override
    public Map<String, Object> getRuntimeContext() {
        populateRuntimeContext();
        return runtimeContext;
    }

    private static final String ORIGIN = "AWS::ElasticBeanstalk::Environment";
    @Override
    public String getOrigin() {
        return ORIGIN;
    }
}