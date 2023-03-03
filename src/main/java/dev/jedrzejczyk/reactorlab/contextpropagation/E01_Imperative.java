package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.util.concurrent.ThreadLocalRandom;

public class E01_Imperative {

	private static final ThreadLocal<Long> CORRELATION_ID = new ThreadLocal<>();

	public static void main(String[] args) {

		initRequest();

		addProduct("test-product");
		notifyShop("test-product");
	}

	static void initRequest() {
		CORRELATION_ID.set(correlationId());
	}

	private static long correlationId() {
		return Math.abs(ThreadLocalRandom.current().nextLong());
	}

	static void addProduct(String productName) {
		log("Adding product: " + productName);
	}

	static void notifyShop(String productName) {
		log("Notifying shop about: " + productName);
	}

	static void log(String message) {
		String threadName = Thread.currentThread().getName();
		String threadNameTail = threadName.substring(Math.max(0, threadName.length() - 10));
		System.out.printf("[%10s][%20s] %s%n",
				threadNameTail, CORRELATION_ID.get(), message);
	}

}
