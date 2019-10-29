package com.guicesample;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Feature;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.util.Modules;
import com.guicesample.service.Foo;
import com.guicesample.service.FooService;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import com.guicesample.service.OtherFooService;

/**
 * Daemon - starts the jetty server and inits Guice.
 */
public class Daemon {
  private static final Logger logger = LoggerFactory.getLogger(Daemon.class);

  private static final Server server = new Server(8080);

  private static Injector injector;

  public static void main(String[] args) throws Exception {
    final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    servletContextHandler.setContextPath("/");
    server.setHandler(servletContextHandler);

    // create a REST API server
    final APIServer apiServer = new APIServer();
    final ServletHolder servletHolder = new ServletHolder(new ServletContainer(apiServer));
    servletHolder.setInitOrder(0);

    // GuiceFilter hooks up Jersey injectables into Guice.
    servletContextHandler.addFilter(GuiceFilter.class, "/*", null);

    // only host servlets on /api/*
    servletContextHandler.addServlet(servletHolder, "/api/*");

    try {
      server.start();
      System.out.println("Server started...");

      runGuiceOnThread();

      server.join();
    } catch (Exception ex) {
      logger.error("Error occurred while starting Jetty", ex);
    } finally {
      server.destroy();
      System.exit(0);
    }
  }

  /**
   * Runs Guice injection in a new, non-jersey thread.  This starts the RequestScope and seeds it with our Context object
   * since we can't depend on HTTP objects being injectible.
   */
  private static void runGuiceOnThread() {
    new Thread(() -> {
      final Context context = new Context();
      context.init("thread");

      final Map<Key<?>, Object> seedMap = new HashMap<>();
      final Binding<Context> binding = injector.getBinding(Context.class);
      seedMap.put(binding.getKey(), context);

      final RequestScoper scope = ServletScopes.scopeRequest(seedMap);
      try (final RequestScoper.CloseableScope ignored = scope.open()) {
        injector.getInstance(Foo.class);
      } catch (Exception e) {
        System.out.println(e);
      }
    }).start();
  }

  /**
   * Config for REST API endpoint
   */
  @ApplicationPath("/")
  public static class APIServer extends ResourceConfig {
    @Inject
    public APIServer() {
      packages("com.guicesample.rest");
      injector = Guice.createInjector(Modules.override(new TestModule()).with(new TestOverrideModule()), new ServletModule());

      // Hooks up The HK2/Guice bridge.  This allows Jersey injection (which uses HK2 by default) to inject Guice bound
      // classes.
      register((Feature) featureContext -> {
        final ServiceLocator locator = InjectionManagerProvider.getInjectionManager(featureContext)
            .getInstance(ServiceLocator.class);

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
        final GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
        return true;
      });
    }
  }

  /**
   * Guice module
   */
  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(Bar.class);
    }

    @Provides
    @RequestScoped
    Foo getFoo(Context context, Bar bar) {
      System.out.println("getFoo context: " + context.getValue());
      System.out.println("Bar context: " + bar.getContext().getValue());
      return new FooService(context);
    }

    /**
     * A provider for Context that works for HTTP initiated request scopes.
     *
     * @param request the http request, made available by the GuiceFilter
     * @return a Context based on the name parameter in the URL.
     */
    @Provides
    @RequestScoped
    Context getContext(HttpServletRequest request) {
      Context context = new Context();
      context.init(request.getParameter("name"));

      return context;
    }
  }

  /**
   * Guice module that overrides TestModule.
   */
  public static class TestOverrideModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(Bar.class).to(OtherBar.class);
    }

    @Provides
    @RequestScoped
    Foo getFoo(Context context, Bar bar) {
      System.out.println("getFoo context: " + context.getValue());
      System.out.println("Bar context: " + bar.getContext().getValue());
      return new OtherFooService(context);
    }
  }
}