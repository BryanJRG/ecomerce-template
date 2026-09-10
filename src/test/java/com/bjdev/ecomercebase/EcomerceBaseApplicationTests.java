package com.bjdev.ecomercebase;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Docker Compose support is skipped during tests by default (spring.docker.compose.skip.in-tests
 * defaults to true) so a plain `mvn test` never surprises anyone by starting containers. This
 * context-load test IS meant to exercise the real compose.yaml services, so it opts back in here
 * — via @TestPropertySource, not a shadowing src/test/resources/application.yml (a same-named
 * resource on the test classpath fully replaces the main one instead of merging with it, which
 * would silently drop spring.profiles.active: dev and everything under application-dev.yml).
 */
@SpringBootTest
@TestPropertySource(properties = "spring.docker.compose.skip.in-tests=false")
class EcomerceBaseApplicationTests {

	@Test
	void contextLoads() {
	}

}
