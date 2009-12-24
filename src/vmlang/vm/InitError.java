package vmlang.vm;

public class InitError extends VMError {
	
	public String message;
	
	public InitError(String msg) {
		message = msg;
	}
	
}