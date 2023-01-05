package dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal;

import java.util.concurrent.Callable;

import reactor.util.context.Context;

public class ContextCallable<V> implements Callable<V> {

	private final Context     ctx;
	private final Callable<V> wrapped;

	public ContextCallable(Context ctx, Callable<V> wrapped) {
		this.ctx = ctx;
		this.wrapped = wrapped;
	}

	@Override
	public V call() throws Exception {
		Context old = ThreadLocalContext.ctx.get();
		ThreadLocalContext.ctx.set(ctx);
		V result = wrapped.call();
		ThreadLocalContext.ctx.set(old);
		return result;
	}
}
