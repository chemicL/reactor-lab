package dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal;

import reactor.util.context.Context;

public class ContextRunnable implements Runnable {

	private final Context  ctx;
	private final Runnable wrapped;

	public ContextRunnable(Runnable wrapped) {
		this.ctx = ThreadLocalContext.ctx.get();
		this.wrapped = wrapped;
	}

	@Override
	public void run() {
		Context old = ThreadLocalContext.ctx.get();
		ThreadLocalContext.ctx.set(this.ctx);
		try {
			wrapped.run();
		} finally {
			ThreadLocalContext.ctx.set(old);
		}
	}
}
