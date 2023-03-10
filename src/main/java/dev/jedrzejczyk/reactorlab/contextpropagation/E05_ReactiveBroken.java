package dev.jedrzejczyk.reactorlab.contextpropagation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class E05_ReactiveBroken {

	private static final ThreadLocal<Long> CORRELATION_ID = new ThreadLocal<>();

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		handleRequest().block();
	}

	static Mono<Void> handleRequest() {
		initRequest();
		log("Assembling the chain");

		return Mono.just("test-product")
				.delayElement(Duration.ofMillis(1))
				.flatMap(product ->
						Flux.concat(
										addProduct(product),
										notifyShop(product))
								.then());
	}

	static void initRequest() {
		CORRELATION_ID.set(correlationId());
	}

	private static long correlationId() {
		return Math.abs(ThreadLocalRandom.current().nextLong());
	}

	static Mono<Void> addProduct(String productName) {
		log("Adding product: " + productName);
		return Mono.empty();
	}

	static Mono<Boolean> notifyShop(String productName) {
		log("Notifying shop about: " + productName);
		return Mono.just(true);
	}

	static void log(String message) {
		String threadName = Thread.currentThread().getName();
		String threadNameTail = threadName.substring(Math.max(0, threadName.length() - 10));
		System.out.printf("[%10s][%20s] %s%n",
				threadNameTail, CORRELATION_ID.get(), message);
	}
}
