package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;

import java.util.concurrent.Callable;

public abstract class AbstractConfiguredRunnable<X> implements Callable<X> {

    protected final Configuration config;

    public AbstractConfiguredRunnable(Configuration config) {
        this.config = config;
    }

    public abstract String getId();
}
