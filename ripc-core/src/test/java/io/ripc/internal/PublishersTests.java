package io.ripc.internal;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.SkipException;

/**
 * @author Stephane Maldini
 */
public class PublishersTests extends PublisherVerification<Integer> {
	public PublishersTests() {
		super(new TestEnvironment(2000, true), 3500);
	}

	@Override
	public Publisher<Integer> createPublisher(long elements) {

		int size = (int)elements;
		Integer[] data = new Integer[size];
		for(int i = 0; i < data.length; i++){
			data[i] = i;
		}
		return Publishers.just(data);
	}

	@Override
	public void required_spec317_mustSupportAPendingElementCountUpToLongMaxValue() throws Throwable {
		throw new SkipException("Not Yet Implemented");
	}

	@Override
	public Publisher<Integer> createFailedPublisher() {
		return Publishers.error(new Exception());
	}
}
