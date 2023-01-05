package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.util.context.Context;

public class E04_ThreadHopFixSubscriber {

	public static void main(String[] args) {
		Flux.just(1)
		    .transformDeferredContextual((f, ctx) ->
				    f.delayElements(Duration.ofSeconds(1))
				     .doOnNext(i -> printContext(ctx.get("CORRELATION_ID"))))
		    .collectList()
			.contextWrite(Context.of("CORRELATION_ID", UUID.randomUUID().toString()))
		    .block();
	}

	// Note we needed to add the argument to ensure decoupling from Context.
	// TODO: make it a helper class
	static void printContext(String userId) {
		System.out.println("User: " + userId);
	}
}
