package com.aql.request;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.indices.mapping.PutMapping;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

public class HandlerIndex implements RequestHandler<DynamodbEvent, String> {

	private static final Logger LOG = Logger.getLogger(HandlerIndex.class);
	private static final String INDEX = "aql_requests";
	private static final String TYPE = "request";
	private static final String ES_SERVICE = "es";
    @Override
	public String handleRequest(DynamodbEvent input, Context context) 
	{
		LOG.info("received: " + input);
		String ESEndpoint = "http://" + System.getenv("ELASTICSEARCH_ENDPOINT") + ":80";
		LOG.info("elasticsearchEndpoint: " + ESEndpoint);
		String region = System.getenv("REGION");
		LOG.info("region: " + region);
		
        try
        {
        	DefaultAWSCredentialsProviderChain awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
        	final com.google.common.base.Supplier<LocalDateTime> clock = () -> LocalDateTime.now(ZoneOffset.UTC);
        	AWSSigner awsSigner = new AWSSigner(awsCredentialsProvider, region, ES_SERVICE, clock);

        	JestClientFactory factory = new JestClientFactory() {
        	    @Override
        	    protected HttpClientBuilder configureHttpClient(HttpClientBuilder builder) {
        	        builder.addInterceptorLast(new AWSSigningRequestInterceptor(awsSigner));
        	        return builder;
        	    }
        	};
        	 factory.setHttpClientConfig(new HttpClientConfig
        	                        .Builder(ESEndpoint)
        	                        .multiThreaded(true)
        				//Per default this implementation will create no more than 2 concurrent connections per given route
        				.defaultMaxTotalConnectionPerRoute(2)
        				// and no more 20 connections in total
        				.maxTotalConnection(20)
        	                        .build());
        	 JestClient client = factory.getObject();
        	 
        	for (DynamodbStreamRecord record : input.getRecords()) {

                if (record != null) 
                {
                	StreamRecord sr = record.getDynamodb();
                	Map<String, AttributeValue> map = sr.getNewImage();
                	LOG.info("record : " + map);
                	// instance a json mapper
                	                	
                	Map<String, Object> json = new HashMap<String, Object>();
                	json.put("timestamp",Long.parseLong(map.get("timestamp").getN()));
                	json.put("email",map.get("email").getS());
                	json.put("app_lib_version",map.get("app_lib_version").getS());
                	json.put("core_lib_version",map.get("core_lib_version").getS());
                	json.put("standard",map.get("standard").getS());
                	json.put("code_letter",map.get("code_letter").getS());
                	json.put("batch_size",Integer.parseInt(map.get("batch_size").getN()));
                	json.put("procedure",map.get("procedure").getS());
                	json.put("type",map.get("type").getS());
                	json.put("aql",map.get("aql").getS());
                	json.put("ip",map.get("ip").getS());
                	json.put("country",map.get("country").getS());
                	json.put("location",map.get("location").getS());
                	
                    LOG.info("item : " + json);
                    
                    Index index = new Index.Builder(json).index(INDEX).type(TYPE).build();
                 	DocumentResult doc = client.execute(index);
                 	LOG.info("getErrorMessage : " + doc.getErrorMessage());
                 	LOG.info("doc : " + doc.toString());
                }

                // Your code here
            }
       }
        catch (Exception e) 
       {
            e.printStackTrace();
       }
        
        return null;
	}
}
