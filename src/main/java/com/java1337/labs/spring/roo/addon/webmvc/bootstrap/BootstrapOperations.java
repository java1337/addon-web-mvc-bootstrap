package com.java1337.labs.spring.roo.addon.webmvc.bootstrap;


/**
 * Interface of operations this add-on offers. Typically used by a command type or an external add-on.
 *
 * @since 1.1
 */
public interface BootstrapOperations {


    public static final String MODULE_MVC_BOOTSTRAP = "addon-web-mvc-bootstrap";

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isInstallBootstrapAvailable();

    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();
}