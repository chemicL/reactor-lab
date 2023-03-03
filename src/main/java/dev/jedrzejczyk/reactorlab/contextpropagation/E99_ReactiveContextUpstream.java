package dev.jedrzejczyk.reactorlab.contextpropagation;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class E99_ReactiveContextUpstream {

	private static final ThreadLocal<Long> CORRELATION_ID = new ThreadLocal<>();

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		Schedulers.onScheduleHook("context.propagation", WrappedRunnable::new);

		Hooks.onEachOperator(Operators.lift((scannable, subscriber) -> new CorrelatingSubscriber<>(subscriber)));
//		Hooks.onLastOperator(Operators.lift((scannable, subscriber) -> new CorrelatingSubscriber<>(subscriber)));

//		ContextRegistry.getInstance().registerThreadLocalAccessor(
//				"CORRELATION_ID", CORRELATION_ID::get, CORRELATION_ID::set, CORRELATION_ID::remove);
//
//		Hooks.enableAutomaticContextPropagation();

		initRequest();
		addProduct("test-product")
				.doOnSuccess(v -> log("Added."))
				.doOnRequest(r -> log("Requested."))
//				.contextCapture()
				.doOnSubscribe(s -> new Thread(() -> s.request(1)).start())
				.block();

		initRequest();
		notifyShop("test-product")
				.doOnRequest(r -> log("Requested."))
				.doOnSuccess(v -> log("Notified."))
//				.contextCapture()
				.doOnSubscribe(s -> new Thread(() -> s.request(1)).start())
				.block();
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
				.doOnSuccess(v -> log("Done!"))
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
			return makeRequest(productName)
					.then()
					.publishOn(Schedulers.single());
		});
	}

	static Mono<Boolean> notifyShop(String productName) {
		return Mono.defer(() -> {
			log("Notifying shop about: " + productName);
			return makeRequest(productName)
					.flux()
					.flatMap(v -> Flux.just(v).hide())
					.single();
		});
	}

	static Mono<Boolean> makeRequest(String productName) {
		return Mono.fromFuture(CompletableFuture.supplyAsync(() -> true,
				CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)));
//				.contextWrite(Function.identity());
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

	static class CorrelatingSubscriber<T> implements CoreSubscriber<T> {
		final CoreSubscriber<T> delegate;
		Long correlationId;

		public CorrelatingSubscriber(CoreSubscriber<T> delegate) {
//			System.out.println("Creating correlating subscriber");
			this.delegate = delegate;
		}

		@Override
		public void onSubscribe(Subscription s) {
			delegate.onSubscribe(s);
			this.correlationId = CORRELATION_ID.get();
		}

		@Override
		public void onNext(T t) {
			CORRELATION_ID.set(this.correlationId);
			delegate.onNext(t);
		}

		@Override
		public void onError(Throwable t) {
			CORRELATION_ID.set(this.correlationId);
			delegate.onError(t);
		}

		@Override
		public void onComplete() {
			CORRELATION_ID.set(this.correlationId);
			delegate.onComplete();
		}
	}
}
