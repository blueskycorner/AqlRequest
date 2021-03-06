package com.aql.request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.aql.request.AqlRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import java.util.regex.*;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
	private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = System.getenv("TABLE_NAME");
    private String REGION = System.getenv("REGION");
	private static final String geoIpEndpoint = "http://freegeoip.net/json/";

	private void Record(AqlRequest r, String ip, long date, String country, String location)
	{
		Region region = RegionUtils.getRegion(REGION);
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(region);
        this.dynamoDb = new DynamoDB(client);
        LOG.info("Connected to dynamo");
        Table t = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
        LOG.info("dynamo table retrieved");
        t.putItem(new PutItemSpec().withItem(new Item()
                        .withLong("timestamp", date)
                        .withString("email", r.getEmail())
                        .withString("ip", ip)
                        .withString("app_lib_version", r.getApp_lib_version())
                        .withString("core_lib_version", r.getCore_lib_version())
                        .withString("standard", r.getRequestData().getStandard())
                        .withLong("batch_size", r.getRequestData().getBatch_size())
                        .withString("code_letter", r.getRequestData().getCode_letter())
                        .withString("type", r.getRequestData().getType())
                        .withString("procedure", r.getRequestData().getProcedure())
                        .withString("aql", r.getRequestData().getAql())
                        .withString("country", country)
                        .withString("location", location)
                        ));
        LOG.info("PutItem OK");
	}

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) 
	{
		LOG.info("received: " + input);
        AqlRequest r = null;
        String ip = null;
        String country = null;
        String location = null;
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
				LOG.info("SourceIp : " + ip);
				
				GeolocationResponse geoLoc = GetIpLocation(ip);
				country = geoLoc.getCountry_name();
				location = geoLoc.getLatitude() + "," + geoLoc.getLongitude();
			}
			
			
			long date = System.currentTimeMillis();
			LOG.info("date : " + date);
			
			Record(r,ip,date, country, location);
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

	private GeolocationResponse GetIpLocation(String ip) 
	{
		GeolocationResponse geoLocationResponse = null;
		
		String geoRequest = geoIpEndpoint + ip;
		LOG.info("geoRequest : " + geoRequest);
		
		try
		{
			HttpURLConnection urlConnection=null;
			URL url = new URL(geoRequest);  
	        urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setDoOutput(true);   
	        urlConnection.setRequestMethod("GET");  
	        urlConnection.setUseCaches(false);  
	        urlConnection.setConnectTimeout(10000);  
	        urlConnection.setReadTimeout(10000);  
	        urlConnection.setRequestProperty("Content-Type","application/json");
	        urlConnection.connect();  
	
	        LOG.info("Response : " + urlConnection.getResponseCode());
	        
	        BufferedReader in = new BufferedReader(
			        new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			LOG.info("Response body: " + response.toString());
	        
	        geoLocationResponse = objectMapper.readValue(response.toString(), GeolocationResponse.class);
	        if (geoLocationResponse != null)
	        {
	        	LOG.info("geoResponse : " + geoLocationResponse);
	        }
		}
		catch (Exception e)
		{
			LOG.error(e.getStackTrace());
		}
    
		return geoLocationResponse;
	}
}
