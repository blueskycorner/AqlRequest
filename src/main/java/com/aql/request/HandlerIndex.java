package com.aql.request;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

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
import io.searchbox.core.Index;
import io.searchbox.indices.mapping.PutMapping;

public class HandlerIndex implements RequestHandler<DynamodbEvent, String> {

	private static final Logger LOG = Logger.getLogger(HandlerIndex.class);
	private static final String INDEX = "aql_requests";
	private static final String TYPE = "request";
    @Override
	public String handleRequest(DynamodbEvent input, Context context) 
	{
		LOG.info("received: " + input);
		String ESEndpoint = "http://" + System.getenv("ELASTICSEARCH_ENDPOINT") + ":80";
		LOG.info("elasticsearchEndpoint: " + ESEndpoint);
		
        try
        {
        	JestClientFactory factory = new JestClientFactory();
        	 factory.setHttpClientConfig(new HttpClientConfig
        	                        .Builder(ESEndpoint)
        	                        .multiThreaded(true)
        				//Per default this implementation will create no more than 2 concurrent connections per given route
        				.defaultMaxTotalConnectionPerRoute(2)
        				// and no more 20 connections in total
        				.maxTotalConnection(20)
        	                        .build());
        	 JestClient client = factory.getObject();
        	 
//        	 PutMapping putMapping = new PutMapping.Builder(
//        		        INDEX,
//        		        TYPE,
//        		        "{ \"" + TYPE + "\" : { \"properties\" : { \"timestamp\" : {\"type\" : \"date\"}, "
//        		        										+ "\"email\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"app_lib_version\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"core_lib_version\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"code_letter\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"batch_size\" : {\"type\" : \"long\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"procedure\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"type\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"aql\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"},"
//        		        										+ "\"ip\" : {\"type\" : \"ip\","
//        		        										+ "\"standard\" : {\"type\" : \"string\",\"index\": \"not_analyzed\"} } } }"
//        		).build();
//        		client.execute(putMapping);
        		
//        	 ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        	for (DynamodbStreamRecord record : input.getRecords()) {

                if (record != null) 
                {
                	StreamRecord sr = record.getDynamodb();
                	Map<String, AttributeValue> map = sr.getNewImage();
                	LOG.info("record : " + map);
                	// instance a json mapper
                	

                	// generate json
//                	AqlRequest r = new AqlRequest();
//                	r.setApp_lib_version(map.get("app_lib_version").getS());
//                	r.setCore_lib_version(map.get("core_lib_version").getS());
//                	r.setEmail(map.get("email").getS());
//                	r.setIp(map.get("ip").getS());
//                	r.setTimestamp(map.get("timestamp").getN());
//                	byte[] json = mapper.writeValueAsBytes(yourbeaninstance);
                	
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
                 	client.execute(index);
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
