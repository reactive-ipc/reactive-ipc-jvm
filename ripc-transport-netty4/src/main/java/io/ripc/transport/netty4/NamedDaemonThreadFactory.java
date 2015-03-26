package io.ripc.transport.netty4;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class NamedDaemonThreadFactory implements ThreadFactory {

	private final AtomicLong counter = new AtomicLong(1);
	private final String prefix;

	public NamedDaemonThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable r) {
		String name = prefix + "-" + counter.getAndIncrement();
		Thread t = new Thread(r, name);
		t.setDaemon(true);
		return t;
	}
	
}
