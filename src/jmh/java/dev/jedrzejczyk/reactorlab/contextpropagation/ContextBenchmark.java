package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal.WrappingExecutorService;
import dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal.ThreadLocalContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

@State(Scope.Thread)
public class ContextBenchmark {

	ExecutorService executorService = Executors.newFixedThreadPool(1);
	Scheduler eventLoop = Schedulers.fromExecutorService(executorService);
	Scheduler timer = Schedulers.fromExecutorService(Executors.newSingleThreadScheduledExecutor());
	ExecutorService wrappingExecutorService = new WrappingExecutorService(1);
	Scheduler wrappingEventLoop = Schedulers.fromExecutorService(wrappingExecutorService);
	Scheduler wrappingTimer = Schedulers.fromExecutorService(new WrappingExecutorService(1));

	@TearDown()
	public void tearDown() {
		eventLoop.disposeGracefully().block();
		timer.disposeGracefully().block();
		wrappingEventLoop.disposeGracefully().block();
		wrappingTimer.disposeGracefully().block();
	}

	public static void main(String[] args) throws Exception {
		ContextBenchmark benchmark = new ContextBenchmark();
		benchmark.wrapping();
		benchmark.tearDown();
		benchmark = new ContextBenchmark();
		benchmark.noWrapping();
		benchmark.tearDown();
	}

	@Benchmark
	public void noWrapping() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);

		Flux<String> source =
				Flux.range(0, 1000)
				    .delayElements(Duration.ofNanos(1), timer)
				    .map(String::valueOf)
				    .publishOn(eventLoop)
				    .hide().publishOn(eventLoop) // add another hop
				    .transformDeferredContextual(
							(f, ctx) -> f.map(s -> ctx.get("sub") + ":" + " " + s)
				    );

		source
				.contextWrite(Context.of("sub", "sub1"))
				.subscribe(__ -> {}, __ -> {}, latch::countDown);
		source
				.contextWrite(Context.of("sub", "sub2"))
				.subscribe(__ -> {}, __ -> {}, latch::countDown);

		latch.await(10, TimeUnit.SECONDS);
	}

	@Benchmark
	public void wrapping() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);

		Flux<String> source =
				Flux.range(0, 1000)
				    .delayElements(Duration.ofNanos(1), wrappingTimer)
				    .map(String::valueOf)
				    .publishOn(wrappingEventLoop)
				    .hide().publishOn(wrappingEventLoop) // add another hop
				    .map(s -> ThreadLocalContext.get("sub") + ":" + " " + s);

		source
				.doOnSubscribe(__ -> ThreadLocalContext.save("sub", "sub1"))
				.subscribe(__ -> {}, __ -> {}, latch::countDown);
		source
				.doOnSubscribe(__ -> ThreadLocalContext.save("sub", "sub2"))
				.subscribe(__ -> {}, __ -> {}, latch::countDown);

		latch.await(10, TimeUnit.SECONDS);
	}
}
