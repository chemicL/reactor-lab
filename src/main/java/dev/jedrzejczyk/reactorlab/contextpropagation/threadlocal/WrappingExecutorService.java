package dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WrappingExecutorService implements ScheduledExecutorService {

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
		return service.submit(new ContextCallable<T>(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return service.submit(new ContextRunnable(task),
				result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return service.submit(new ContextRunnable(task));
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return service.schedule(new ContextRunnable(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable,
			long delay,
			TimeUnit unit) {
		return service.schedule(new ContextCallable<>(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay,
			long period,
			TimeUnit unit) {
		return service.scheduleAtFixedRate(new ContextRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay,
			long delay,
			TimeUnit unit) {
		return service.scheduleWithFixedDelay(new ContextRunnable(command), initialDelay, delay, unit);
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
		service.execute(new ContextRunnable(command));
	}

	@Override
	public String toString() {
		return service.toString();
	}
}
