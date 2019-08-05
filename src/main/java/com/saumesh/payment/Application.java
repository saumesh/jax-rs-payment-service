package com.saumesh.payment;

import com.saumesh.payment.config.AppBinder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.jersey.servlet.ServletContainer;

public class Application extends ResourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static final int DEFAULT_PORT = 8080;

    private Server jettyServer;

    public Application() {
        packages("com.saumesh.payment");
        register(JacksonFeature.class);
        register(new AppBinder());
    }

    public static void main(String[] args) {
        Application app = new Application();

        int port;
        try {
            port = Integer.getInteger("server.port");
        } catch (Exception excp) {
            logger.info("Could not read/parse 'server.port' value. Switching to default port {} ", DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        try {
            app.startJettyServer(port);
        } catch (Exception excp) {
            logger.error("Error occurred while starting Jetty", excp);
            System.exit(1);
        }

        app.initialize();
    }

    private void startJettyServer(int port) throws Exception {
        jettyServer = new Server(port);

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        jettyServer.setHandler(servletContextHandler);

        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", "com.saumesh.payment");
        servletHolder.setInitParameter("javax.ws.rs.Application", "com.saumesh.payment.Application");

        jettyServer.start();
        jettyServer.join();
    }

    protected void initialize() {
        //Initialize
    }

    private void shutdown() {
        if(jettyServer != null) {
            try {
                jettyServer.destroy();
            } catch (Exception excp) {
                logger.error("Error in shutting down Jetty", excp);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.shutdown();
        super.finalize();
    }
}
