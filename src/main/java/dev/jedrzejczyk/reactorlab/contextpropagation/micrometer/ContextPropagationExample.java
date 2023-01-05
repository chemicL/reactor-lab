package dev.jedrzejczyk.reactorlab.contextpropagation.micrometer;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ContextPropagationExample {

	private static ThreadLocal<String> sub = ThreadLocal.withInitial(() -> "");

	public static void main(String[] args) {
		ContextRegistry.getInstance().registerThreadLocalAccessor("sub", sub);

		sub.set("sub1");
		String value = Mono.just("hello")
		                   .publishOn(Schedulers.boundedElastic())
		                   .<String>handle((s, sink) -> {
							   ContextSnapshot snapshot =
									   ContextSnapshot.captureAll(sink.contextView());
							   try (ContextSnapshot.Scope __ =
									        snapshot.setThreadLocals()) {
								   String su = sub.get();
								   sink.next(su + ": " + s);
							   }
		                   })
//		                   .contextWrite(Context.of("sub", "sub1"))
		                   .contextCapture()
		                   .block();

		System.out.println(value);
	}
}
