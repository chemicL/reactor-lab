package dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal;

import reactor.util.context.Context;

public class ContextRunnable implements Runnable {

	private final Context  ctx;
	private final Runnable wrapped;

	public ContextRunnable(Context ctx, Runnable wrapped) {
		this.ctx = ctx;
		this.wrapped = wrapped;
	}

	@Override
	public void run() {
		Context old = ThreadLocalContext.ctx.get();
		ThreadLocalContext.ctx.set(this.ctx);
		wrapped.run();
		ThreadLocalContext.ctx.set(old);
	}
}
