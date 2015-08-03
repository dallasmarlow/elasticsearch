package org.apache.mesos.elasticsearch.scheduler;

import org.apache.mesos.Protos;
import org.apache.mesos.elasticsearch.scheduler.state.SerializableState;
import org.apache.mesos.elasticsearch.scheduler.state.State;
import org.apache.mesos.state.Variable;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Tests State class.
 */
public class StateTest {
    private State state;

    @Before
    public void before() throws ExecutionException, InterruptedException {
        state = new State(new TestStateImpl());
    }

    @Test
    public void testSanityCheck() {
        assertNotNull("State should not be null.", state);
    }

    @Test
    public void testInitialGetFrameworkID() throws NotSerializableException {
        assertTrue("FrameworkID should be empty if not first time.", state.getFrameworkID().getValue().isEmpty());
    }

    @Test
    public void testThatStoreFrameworkIDStores() throws NotSerializableException {
        Protos.FrameworkID frameworkID = Protos.FrameworkID.newBuilder().setValue("TEST_ID").build();
        state.setFrameworkId(frameworkID);
        assertNotNull("FramekworkID should not be null once set.", state.getFrameworkID());
        assertEquals("FramekworkID should be equal to the one set.", state.getFrameworkID().getValue(), frameworkID.getValue());
    }

    private static class TestVariable extends Variable {
        private byte[] myByte = new byte[0];

        @Override
        public byte[] value() {
            return myByte;
        }

        @Override
        public Variable mutate(byte[] value) {
            myByte = value;
            return this;
        }
    }

    @Test
    public void testMkDirJustSlashShouldNotCrash() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        state.mkdir("/");
    }

    @Test(expected = Exception.class)
    public void testMkDirTrailingSlash() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        state.mkdir("/mesos/");
    }

    @Test
    public void testMkDirOk() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        state.mkdir("/mesos");
    }

    /**
     * Dummy storage class to replace zookeeper.
     */
    @SuppressWarnings("unchecked")
    public class TestStateImpl implements SerializableState {
        Map<String, Object> map = new HashMap<>();
        @Override
        public <T> T get(String key) throws NotSerializableException {
            return (T) map.getOrDefault(key, null);
        }

        @Override
        public <T> void set(String key, T object) throws NotSerializableException {
            if (key.endsWith("/") && !key.equals("/")) {
                throw new NotSerializableException("Trailing slashes are not allowed");
            }
            map.put(key, object);
        }
    }
}