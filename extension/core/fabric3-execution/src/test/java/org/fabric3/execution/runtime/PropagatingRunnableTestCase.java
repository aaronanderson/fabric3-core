package org.fabric3.execution.runtime;

import junit.framework.TestCase;

import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 *
 */
public class PropagatingRunnableTestCase extends TestCase {

    public void testPropagation() throws Exception {
        MockRunnable delegate = new MockRunnable();
        PropagatingRunnable runnable = new PropagatingRunnable(delegate, null, null, null);
        runnable.run();
    }

    private class MockRunnable implements Runnable {

        public void run() {
            assertNotNull(WorkContextCache.getThreadWorkContext());
        }
    }
}
