package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class E07_ReactiveCleanup {

	private static final ThreadLocal<Long> CORRELATION_ID = new ThreadLocal<>();

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		Schedulers.onScheduleHook("context.propagation", WrappedRunnable::new);

		log("Got first request, calling handler");
		handleRequest().block();

		log("Got second request, calling handler");
		log("There should be no correlationId on this line!");

		handleRequest().block();
	}

	static Mono<Void> handleRequest() {
		return Mono.fromSupplier(() -> {
					initRequest();
					return "test-product";
				}).flatMap(product ->
						Flux.concat(
										addProduct(product),
										notifyShop(product))
								.then())
				.doOnSuccess(v -> log("Done."))
				.doFinally(signalType -> CORRELATION_ID.remove());
	}

	static void initRequest() {
		CORRELATION_ID.set(correlationId());
	}

	private static long correlationId() {
		return Math.abs(ThreadLocalRandom.current().nextLong());
	}

	static Mono<Void> addProduct(String productName) {
		return Mono.defer(() -> {
			log("Adding product: " + productName);
			return Mono.<Void>empty()
			           .delaySubscription(Duration.ofMillis(10), Schedulers.single());
		});
	}

	static Mono<Boolean> notifyShop(String productName) {
		return Mono.defer(() -> {
			log("Notifying shop about: " + productName);
			return Mono.just(true);
		});
	}

	static void log(String message) {
		String threadName = Thread.currentThread().getName();
		String threadNameTail = threadName.substring(Math.max(0, threadName.length() - 10));
		System.out.printf("[%10s][%20s] %s%n",
				threadNameTail, CORRELATION_ID.get(), message);
	}
	static class WrappedRunnable implements Runnable {
		private final Long correlationId;
		private final Runnable wrapped;

		public WrappedRunnable(Runnable wrapped) {
			this.correlationId = CORRELATION_ID.get();
			this.wrapped = wrapped;
		}

		@Override
		public void run() {
			Long old = CORRELATION_ID.get();
			CORRELATION_ID.set(this.correlationId);
			try {
				wrapped.run();
			} finally {
				CORRELATION_ID.set(old);
			}
		}
	}
}
