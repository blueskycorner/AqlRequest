package com.aql.request;

public class AqlRequest {

	private String email;
	private String app_lib_version;
	private String core_lib_version;
	private RequestData requestData;

	public void setEmail(String pi_email){email=pi_email;}
	public String getEmail(){return email;}
	
	public void setApp_lib_version(String pi_app_lib_version){app_lib_version=pi_app_lib_version;}
	public String getApp_lib_version(){return app_lib_version;}
	
	public void setCore_lib_version(String pi_core_lib_version){core_lib_version=pi_core_lib_version;}
        public String getCore_lib_version(){return core_lib_version;}

	public void setRequestData(RequestData pi_requestData){requestData=pi_requestData;}
        public RequestData getRequestData(){return requestData;}
}
