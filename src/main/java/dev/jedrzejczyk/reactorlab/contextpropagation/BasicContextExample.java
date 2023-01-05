package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

public class BasicContextExample {

	public static void main(String[] args) throws Exception {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Scheduler eventLoop = Schedulers.fromExecutorService(executorService);

		CountDownLatch latch = new CountDownLatch(2);

		Flux<String> source =
				Flux.range(0, 100)
				    .delayElements(Duration.ofMillis(10))
				    .map(String::valueOf)
				    .transformDeferredContextual(
							(f, ctx) -> f.map(s -> ctx.get("sub") + ":" + " " + s)
				    );

		source.publishOn(eventLoop)
		      .contextWrite(Context.of("sub", "sub1"))
		      .subscribe(System.out::println, System.err::println, latch::countDown);
		source.publishOn(eventLoop)
		      .contextWrite(Context.of("sub", "sub2"))
		      .subscribe(System.out::println, System.err::println, latch::countDown);

		latch.await(10, TimeUnit.SECONDS);

		eventLoop.disposeGracefully().block();
	}
}

