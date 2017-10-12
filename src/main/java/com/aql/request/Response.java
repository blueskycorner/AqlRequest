package com.aql.request;

public class Response {

	private final String message;

	public Response(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}
