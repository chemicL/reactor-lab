package dev.jedrzejczyk.reactorlab.contextpropagation.threadlocal;

import reactor.util.context.Context;

public class ThreadLocalContext {

	static final ThreadLocal<Context> ctx = ThreadLocal.withInitial(Context::empty);

	public static void save(String key, Object value) {
		ctx.set(ctx.get().put(key, value));
	}

	public static Object get(String key) {
		return ctx.get().get(key);
	}
}
