package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

public class ThreadLocalTest {

	static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

	@Test
	void shouldLogUserId() {
		String userId = "SpringUser";
		USER_ID.set(userId);

		CompletableFuture.runAsync(() -> {});

		String s = welcomeMessage();
		System.out.println("Responding with: " + s);
	}

	String welcomeMessage() {
		String message = "Hello!";
		log("Greeting with: " + message);
		return message;
	}

	void log(String message) {
		System.out.printf("[user=%s] %s%n", USER_ID.get(), message);
	}
}
