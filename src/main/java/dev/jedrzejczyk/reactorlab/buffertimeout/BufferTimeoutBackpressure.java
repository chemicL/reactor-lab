package dev.jedrzejczyk.reactorlab.buffertimeout;

import java.time.Duration;

import reactor.core.publisher.Flux;

public class BufferTimeoutBackpressure {

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
//            .bufferTimeout(2, Duration.ofMillis(10), true)
            .bufferTimeout(2, Duration.ofMillis(10))
            .doOnRequest(r -> System.out.println("BUFFER Requested " + r))
            .doOnNext(l -> System.out.println("Got buffer: " + l))
            .flatMapSequential(l -> Flux.fromIterable(l).delayElements(Duration.ofSeconds(1)), 1, 1)
            .doOnEach(s -> {
	            if (s.isOnNext()) {
		            System.out.println("Got " + s.get());
	            }
            })
            .subscribe(i -> {}, System.err::println, () -> System.out.println("Done"));

		Thread.sleep(100_000);
	}
}
