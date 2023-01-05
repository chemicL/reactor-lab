package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.util.UUID;

public class E01_ThreadLocalExample {

	static final ThreadLocal<String> correlationId = ThreadLocal.withInitial(() -> "");

	public static void main(String[] args) {
		correlationId.set(UUID.randomUUID().toString());
		printContext();
	}

	static void printContext() {
		System.out.println("CorrelationID: " + correlationId.get());
	}
}
