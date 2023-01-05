package dev.jedrzejczyk.reactorlab.windowtimeout;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WindowTimeoutBackpressure {

	public static void main(String[] args) throws InterruptedException {
		Flux.<Integer>create(s -> {
			    for (int i = 0; i < 10; i++) {
				    try {
					    Thread.sleep(50);
					    s.next(i);
				    } catch (InterruptedException e) {
					    s.error(e);
					    return;
				    }
			    }
			    s.complete();
		    })
		    .doOnRequest(r -> System.out.println("SINK Requested " + r))
//		    .windowTimeout(2, Duration.ofMillis(10), true)
		    .windowTimeout(2, Duration.ofMillis(10))
            .doOnRequest(r -> System.out.println("BUFFER Requested " + r))
            .doOnNext(l -> System.out.println("Got buffer: " + l))
            .flatMapSequential(l -> l.flatMap(i -> Mono.just(i).doOnSubscribe(s -> System.out.println("WINDOW")).delayElement(Duration.ofSeconds(1))), 1, 1)
            .doOnEach(s -> {
	            if (s.isOnNext()) {
		            System.out.println("Got " + s.get());
	            }
            })
            .subscribe(i -> {}, System.err::println, () -> System.out.println("Done"));

		Thread.sleep(100_000);
	}
}
