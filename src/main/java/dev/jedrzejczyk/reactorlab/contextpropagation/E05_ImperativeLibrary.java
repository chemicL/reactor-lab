package dev.jedrzejczyk.reactorlab.contextpropagation;

import java.time.Duration;
import java.util.UUID;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import reactor.core.publisher.Mono;

public class E05_ImperativeLibrary {

	public static void main(String[] args) {
		ContextRegistry.getInstance().registerThreadLocalAccessor(
				"CORRELATION_ID", LoggingUtil.correlationId
		);

		LoggingUtil.correlationId.set(UUID.randomUUID().toString());

		Mono.just("process")
		    .delayElement(Duration.ofSeconds(1))
		    .<String>handle((s, sink) -> {
			    ContextSnapshot snapshot = ContextSnapshot.captureAll(sink.contextView());
			    try (ContextSnapshot.Scope __ = snapshot.setThreadLocals()) {
				    LoggingUtil.log();
				    sink.next(s);
			    }
		    })
//		    .contextWrite(Context.of("CORRELATION_ID", UUID.randomUUID().toString()))
            .contextCapture()
            .block();
	}
}

class LoggingUtil {
	static ThreadLocal<String> correlationId = ThreadLocal.withInitial(() -> "");

	public static void log() {
		System.out.println("CorrelationID: " + correlationId.get());
	}
}
