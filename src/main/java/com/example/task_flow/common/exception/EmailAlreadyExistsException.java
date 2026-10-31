package com.example.task_flow.common.exception;

public class EmailAlreadyExistsException extends RuntimeException{
	public EmailAlreadyExistsException(String message){
		super(message);
	}
}