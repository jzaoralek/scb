package com.jzaoralek.scb.dataservice.exception;

public class ApiExceptionJson {

	public enum ApiExceptionJsonType {
		RUNTIME,
		VALIDATION;
	}

	private String code;
	private String name;
	private String description;
	private ApiExceptionJsonType type;

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ApiExceptionJsonType getType() {
		return type;
	}
	public void setType(ApiExceptionJsonType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ApiExceptionJson [code=" + code + ", name=" + name + ", description=" + description + ", type=" + type
				+ "]";
	}
}