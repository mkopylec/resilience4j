/*
 * Copyright 2019 Robert Winkler
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
package io.github.resilience4j.circuitbreaker.operator;

import io.github.resilience4j.AbstractSingleObserver;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.StopWatch;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;

class SingleCircuitBreaker<T> extends Single<T> {

    private final CircuitBreaker circuitBreaker;
    private final Single<T> upstream;

    SingleCircuitBreaker(Single<T> upstream, CircuitBreaker circuitBreaker) {
        this.upstream = upstream;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> downstream) {
        if(circuitBreaker.tryAcquirePermission()){
            upstream.subscribe(new CircuitBreakerSingleObserver(downstream));
        }else{
            downstream.onSubscribe(EmptyDisposable.INSTANCE);
            downstream.onError(new CallNotPermittedException(circuitBreaker));
        }
    }

    class CircuitBreakerSingleObserver extends AbstractSingleObserver<T> {

        private final StopWatch stopWatch;

        CircuitBreakerSingleObserver(SingleObserver<? super T> downstreamObserver) {
            super(downstreamObserver);
            this.stopWatch = StopWatch.start();
        }

        @Override
        protected void hookOnError(Throwable e) {
            circuitBreaker.onError(stopWatch.stop().toNanos(), e);
        }

        @Override
        protected void hookOnSuccess(T value) {
            long durationInNanos = stopWatch.stop().toNanos();
            if (!circuitBreaker.onResult(durationInNanos, value)) {
                circuitBreaker.onSuccess(durationInNanos);
            }
        }

        @Override
        protected void hookOnCancel() {
            circuitBreaker.releasePermission();
        }
    }
}
