package dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal;

import java.util.concurrent.Callable;

import reactor.util.context.Context;

public class ContextCallable<V> implements Callable<V> {

	private final Context     ctx;
	private final Callable<V> wrapped;

	public ContextCallable(Callable<V> wrapped) {
		this.ctx = ThreadLocalContext.ctx.get();
		this.wrapped = wrapped;
	}

	@Override
	public V call() throws Exception {
		Context old = ThreadLocalContext.ctx.get();
		ThreadLocalContext.ctx.set(ctx);
		try {
			return wrapped.call();
		} finally {
			ThreadLocalContext.ctx.set(old);
		}
	}
}
