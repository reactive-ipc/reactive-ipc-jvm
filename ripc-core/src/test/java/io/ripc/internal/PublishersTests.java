package io.ripc.internal;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;

/**
 * @author Stephane Maldini
 */
public class PublishersTests extends PublisherVerification<Integer> {
	public PublishersTests() {
		super(new TestEnvironment(2000, true), 3500);
	}

	@Override
	public Publisher<Integer> createPublisher(long elements) {
		if(elements > 1000) throw new UnsupportedOperationException("Unsupported Publisher");
		int size = (int)elements;
		Integer[] data = new Integer[size];
		return Publishers.just(data);
	}

	@Override
	public void required_spec317_mustSupportAPendingElementCountUpToLongMaxValue() throws Throwable {
		//IGNORE
	}

	@Override
	public Publisher<Integer> createFailedPublisher() {
		return Publishers.error(new Exception());
	}
}
