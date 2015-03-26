package io.ripc.core;

/**
 * Created by jbrisbin on 3/26/15.
 */
public interface DemandCalculator {

	long calculateDemand(long pending);

}
