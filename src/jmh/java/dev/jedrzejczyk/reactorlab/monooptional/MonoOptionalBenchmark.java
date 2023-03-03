package dev.jedrzejczyk.reactorlab.monooptional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Mono;

@BenchmarkMode({Mode.AverageTime})
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class MonoOptionalBenchmark {

	private static final Throwable EXCEPTION = new RuntimeException() {
		@Override
		public synchronized Throwable fillInStackTrace() {
			return this;
		}
	};

	@Benchmark
	public void singleOptionalBaseline(Blackhole blackhole) {
		blackhole.consume(
				Mono.just(1)
				    .map(Optional::ofNullable)
				    .defaultIfEmpty(Optional.empty())
				    .block()
		);
	}

	@Benchmark
	public void singleOptionalEmptyBaseline(Blackhole blackhole) {
		blackhole.consume(
				Mono.empty()
				    .map(Optional::ofNullable)
				    .defaultIfEmpty(Optional.empty())
				    .block()
		);
	}

	@Benchmark
	public void singleOptionalErrorBaseline(Blackhole blackhole) {
		blackhole.consume(
				Mono.error(EXCEPTION)
				    .map(Optional::ofNullable)
				    .defaultIfEmpty(Optional.empty())
				    .onErrorComplete()
				    .block()
		);
	}

	@Benchmark
	public void singleOptional(Blackhole blackhole) {
		blackhole.consume(
				Mono.just(1)
				    .singleOptional()
				    .block()
		);
	}

	@Benchmark
	public void singleOptionalEmpty(Blackhole blackhole) {
		blackhole.consume(
				Mono.empty()
				    .singleOptional()
				    .block()
		);
	}

	@Benchmark
	public void singleOptionalError(Blackhole blackhole) {
		blackhole.consume(
				Mono.error(EXCEPTION)
				    .singleOptional()
				    .onErrorComplete()
				    .block()
		);
	}
}