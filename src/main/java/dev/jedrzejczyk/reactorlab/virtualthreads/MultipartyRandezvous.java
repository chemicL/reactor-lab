package dev.jedrzejczyk.reactorlab.virtualthreads;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipartyRandezvous {

	public static void main(String[] args) {
		int participants = Runtime.getRuntime().availableProcessors() + 1;

		ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();
		ThreadFactory platformThreadFactory = Thread.ofPlatform().factory();

		// Picking virtual makes it never finish.
//		ThreadFactory usedFactory = platformThreadFactory;
		ThreadFactory usedFactory = virtualThreadFactory;

		CountDownLatch latch = new CountDownLatch(participants);
		AtomicInteger missing = new AtomicInteger(participants);

		try (ExecutorService executorService =
		        Executors.newThreadPerTaskExecutor(usedFactory)) {

			for (int i = 0; i < participants; ++i) {
				executorService.submit(new Participant(missing, latch));
			}

			latch.await();
		} catch (InterruptedException e) {
			System.out.println("Interrupted!");
		}
	}

	static class Participant implements Runnable {

		private final AtomicInteger count;
		private final CountDownLatch latch;

		public Participant(AtomicInteger count, CountDownLatch latch) {
			this.count = count;
			this.latch = latch;
		}

		@Override
		public void run() {
			if (count.decrementAndGet() != 0) {
				while (count.get() != 0) {
					// Uncommenting makes VirtualThread variant finish successfully.
//					Thread.yield();
				}
			}

			System.out.println("We all met!");
			latch.countDown();
		}
	}
}
