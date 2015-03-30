package io.ripc.core;

/**
 * A {@code DemandCalculator} implementation is responsible for calculating what the demand value should be to send to
 * {@link org.reactivestreams.Subscription#request(long)}.
 */
public interface DemandCalculator {

	/**
	 * Calculate demand based on current pending backlog. A value {@code < 1} means "don't make any new requests" since
	 * values less than {@code 1} are illegal according to the Reactive Streams spec. A value of {@code >= 1} means "use
	 * this value as the demand".
	 *
	 * @param pending outstanding backlog of previous demand accumulations
	 * @return &lt 1 to indicate no requests should be performed, &gt= 1 to indicate positive demand
	 */
	long calculateDemand(long pending);

}
