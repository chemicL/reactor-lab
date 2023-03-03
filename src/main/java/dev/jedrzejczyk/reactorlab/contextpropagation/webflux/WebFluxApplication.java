package dev.jedrzejczyk.reactorlab.contextpropagation.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class WebFluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebFluxApplication.class, args);
		Hooks.enableAutomaticContextPropagation();
	}

}

@RestController
class WebFluxController {

	Logger log = LoggerFactory.getLogger(WebFluxController.class);

	@GetMapping("/")
	public Mono<String> hello() {
		log.info("Inside hello");
		return Mono.just("Hello")
	           .doOnNext(s -> log.info("Request received."));
//				.<String>handle((s, sink) -> {
//					log.info("Request handled.");
//					sink.next(s);
//				});
	}

	@GetMapping("/ping")
	public String ping() {
		log.info("Got ping");
		return "pong";
	}
}
