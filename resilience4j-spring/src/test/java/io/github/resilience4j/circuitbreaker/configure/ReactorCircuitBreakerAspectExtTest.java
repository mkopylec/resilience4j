/*
 * Copyright 2019 Mahmoud Romeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.circuitbreaker.configure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * aspect unit test
 */
@RunWith(MockitoJUnitRunner.class)
public class ReactorCircuitBreakerAspectExtTest {

	@Mock
	ProceedingJoinPoint proceedingJoinPoint;

	@InjectMocks
	ReactorCircuitBreakerAspectExt reactorCircuitBreakerAspectExt;


	@Test
	public void testCheckTypes() {
		assertThat(reactorCircuitBreakerAspectExt.canHandleReturnType(Mono.class)).isTrue();
		assertThat(reactorCircuitBreakerAspectExt.canHandleReturnType(Flux.class)).isTrue();
	}

	@Test
	public void testReactorTypes() throws Throwable {
		CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("test");

		when(proceedingJoinPoint.proceed()).thenReturn(Mono.just("Test"));
		assertThat(reactorCircuitBreakerAspectExt.handle(proceedingJoinPoint, circuitBreaker, "testMethod")).isNotNull();

		when(proceedingJoinPoint.proceed()).thenReturn(Flux.just("Test"));
		assertThat(reactorCircuitBreakerAspectExt.handle(proceedingJoinPoint, circuitBreaker, "testMethod")).isNotNull();
	}


}