package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.UUID;

import dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal.WrappingExecutorService;
import dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal.ThreadLocalContext;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class E03_ThreadHopFixTL {

	static final Scheduler scheduler =
			Schedulers.fromExecutorService(new WrappingExecutorService(1));

	public static void main(String[] args) {
		ThreadLocalContext.save("CORRELATION_ID", UUID.randomUUID().toString());
		Flux.just(1)
				.delayElements(Duration.ofSeconds(1), scheduler)
				.doOnNext(i -> printContext())
				.collectList()
				.block();
		scheduler.dispose();
	}

	static void printContext() {
		System.out.println("CorrelationID: " + ThreadLocalContext.get("CORRELATION_ID"));
	}
}
