package dev.jedrzejczyk.reactorlab.contextpropagation;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import org.junit.jupiter.api.Test;

public class ContextPropagationTest {

	ThreadLocal<String> REF = ThreadLocal.withInitial(() -> "ref_init");

	@Test
	void test() {
		ContextRegistry.getInstance().registerThreadLocalAccessor("KEY", REF);

		REF.set("present");

		ContextSnapshot snapshot = ContextSnapshot.captureAll();

		new Thread(() -> {
			try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {
				System.out.println(REF.get());
			}
		}).start();
	}
}
