package com.saumesh.payment.service.integration.test;

import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;

public abstract class IntegrationTestBase extends JerseyTest {

    @Override
    protected Application configure() {
        return new com.saumesh.payment.Application();
    }
}
