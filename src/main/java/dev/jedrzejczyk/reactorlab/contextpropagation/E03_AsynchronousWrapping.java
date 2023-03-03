package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class E03_AsynchronousWrapping {

	private static final ThreadLocal<Long> CORRELATION_ID = new ThreadLocal<>();

	static Executor executor = new WrappedExecutor(ForkJoinPool.commonPool());

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		initRequest();

		handleRequest().get();
	}

	static CompletableFuture<Void> handleRequest() {
		return CompletableFuture
				.runAsync(() -> addProduct("test-product"), executor)
				.thenRunAsync(() -> notifyShop("test-product"), executor);
	}
	static void initRequest() {
		CORRELATION_ID.set(correlationId());
	}

	private static long correlationId() {
		return Math.abs(ThreadLocalRandom.current().nextLong());
	}

	static void addProduct(String productName) {
		log("Adding product: " + productName);
	}

	static void notifyShop(String productName) {
		log("Notifying shop about: " + productName);
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

	static class WrappedCallable<V> implements Callable<V> {

		private final Long correlationId;
		private final Callable<V> wrapped;

		public WrappedCallable(Callable<V> wrapped) {
			this.correlationId = CORRELATION_ID.get();
			this.wrapped = wrapped;
		}

		@Override
		public V call() throws Exception {
			Long old = CORRELATION_ID.get();
			CORRELATION_ID.set(this.correlationId);
			try {
				return wrapped.call();
			} finally {
				CORRELATION_ID.set(old);
			}
		}
	}

	static class WrappedExecutor implements Executor {

		private final Executor actual;

		WrappedExecutor(Executor actual) {
			this.actual = actual;
		}

		@Override
		public void execute(Runnable command) {
			actual.execute(new WrappedRunnable(command));
		}
	}

	static class WrappingExecutorService implements ScheduledExecutorService {

		private static ScheduledExecutorService service;

		public WrappingExecutorService(int parallelism) {
			service = Executors.newScheduledThreadPool(parallelism);
		}

		@Override
		public void shutdown() {
			service.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			return service.shutdownNow();
		}

		@Override
		public boolean isShutdown() {
			return service.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			return service.isTerminated();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
			return service.awaitTermination(timeout, unit);
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return service.submit(new WrappedCallable<T>(task));
		}

		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return service.submit(new WrappedRunnable(task),
					result);
		}

		@Override
		public Future<?> submit(Runnable task) {
			return service.submit(new WrappedRunnable(task));
		}

		@Override
		public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
			return service.schedule(new WrappedRunnable(command), delay, unit);
		}

		@Override
		public <V> ScheduledFuture<V> schedule(Callable<V> callable,
				long delay,
				TimeUnit unit) {
			return service.schedule(new WrappedCallable<>(callable), delay, unit);
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
				long initialDelay,
				long period,
				TimeUnit unit) {
			return service.scheduleAtFixedRate(new WrappedRunnable(command), initialDelay, period, unit);
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
				long initialDelay,
				long delay,
				TimeUnit unit) {
			return service.scheduleWithFixedDelay(new WrappedRunnable(command), initialDelay, delay, unit);
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
				throws InterruptedException {
			return service.invokeAll(tasks);
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
				long timeout,
				TimeUnit unit) throws InterruptedException {
			return service.invokeAll(tasks, timeout, unit);
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return service.invokeAny(tasks);
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
				long timeout,
				TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return service.invokeAny(tasks, timeout, unit);
		}

		@Override
		public void execute(Runnable command) {
			service.execute(new WrappedRunnable(command));
		}

		@Override
		public String toString() {
			return service.toString();
		}
	}
}
