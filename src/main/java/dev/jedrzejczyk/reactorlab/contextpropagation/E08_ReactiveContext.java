package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class E08_ReactiveContext {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		long correlationId = correlationId();
		log("Assembling the chain", correlationId);
		Mono.just("test-product")
		    .delayElement(Duration.ofMillis(1))
		    .flatMap(product ->
				    Flux.concat(
							addProduct(product),
							notifyShop(product))
				        .then())
			.contextWrite(Context.of("CORRELATION_ID", correlationId))
		    .block();
	}

	static long correlationId() {
		return Math.abs(ThreadLocalRandom.current().nextLong());
	}

	static Mono<Void> addProduct(String productName) {
		return Mono.deferContextual(ctx -> {
			log("Adding product: " + productName, ctx.get("CORRELATION_ID"));
			return Mono.empty();
		});
	}

	static Mono<Boolean> notifyShop(String productName) {
		return Mono.deferContextual(ctx -> {
			log("Notifying shop about: " + productName, ctx.get("CORRELATION_ID"));
			return Mono.just(true);
		});
	}

	static void log(String message, long correlationId) {
		String threadName = Thread.currentThread().getName();
		String threadNameTail = threadName.substring(Math.max(0, threadName.length() - 10));
		System.out.printf("[%10s][%20s] %s%n",
				threadNameTail, correlationId, message);
	}
}
