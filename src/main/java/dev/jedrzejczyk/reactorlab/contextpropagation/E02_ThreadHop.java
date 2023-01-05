package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;

import reactor.core.publisher.Flux;

public class E02_ThreadHop {

	static final ThreadLocal<String> correlationId = ThreadLocal.withInitial(() -> "");

	public static void main(String[] args) {
		Flux.just(1)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> printContext())
				.collectList()
				.block();
	}

	static void printContext() {
		System.out.println("CorrelationID: " + correlationId.get());
	}
}
