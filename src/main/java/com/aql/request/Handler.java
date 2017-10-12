package com.aql.request;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aql.request.AqlRequest;
import com.aql.request.RequestContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.regions.Regions;
import java.util.regex.*;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);
        private static final ObjectMapper objectMapper = new ObjectMapper();
	private DynamoDB dynamoDb;
    	private String DYNAMODB_TABLE_NAME = "aqlRequest";
	private com.amazonaws.regions.Regions REGION = Regions.EU_WEST_1;

	private void Record(AqlRequest r, String ip, long date)
	{
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(com.amazonaws.regions.Region.getRegion(REGION));
        this.dynamoDb = new DynamoDB(client);

	}

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);
        AqlRequest r = null;
	String ip = null;
        try
        {
            r = objectMapper.readValue(input.get("body").toString(), AqlRequest.class);
	
	    String sRequestContext = input.get("requestContext").toString();
	    Pattern p = Pattern.compile("sourceIp=(.*), acc");//. represents single character  
	    Matcher m = p.matcher(sRequestContext);
	
	    boolean b = m.find();
	    if (b)
	    {
		ip = m.group(1);
	    }
	    LOG.info("SourceIp : " + ip);
	    long date = System.currentTimeMillis();

	    Record(r,ip,date);
       }
        catch (java.io.IOException e) 
       {
            e.printStackTrace();
       }
		Response responseBody = new Response("OK");
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & Serverless");
		headers.put("Content-Type", "application/json");
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(headers)
				.build();
	}
}
