
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.inject.util.Modules;
import com.guicesample.Bar;
import com.guicesample.Context;
import com.guicesample.Daemon;
import com.guicesample.OtherBar;
import com.guicesample.service.Foo;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import com.guicesample.service.OtherFooService;

public class TestInjection {
  @Test
  void testRequestInjection() {
    final Injector injector = Guice.createInjector(Modules.override(new Daemon.TestModule()).with(new Daemon.TestOverrideModule()), new ServletModule());

    final Context context = new Context();
    context.init("value");

    final Map<Key<?>, Object> seedMap = new HashMap<>();
    final Binding<Context> binding = injector.getBinding(Context.class);
    seedMap.put(binding.getKey(), context);

    final RequestScoper scope = ServletScopes.scopeRequest(seedMap);
    try (final RequestScoper.CloseableScope ignored = scope.open()) {
      final Foo instance = injector.getInstance(Foo.class);
      final Foo instance2 = injector.getInstance(Foo.class);

      // Foo's provider is request scoped so we should get the same instance and context
      assertEquals(instance, instance2);
      assertEquals(context, instance.getContext());
      assertEquals(OtherFooService.class, instance.getClass());

      final Bar bar = injector.getInstance(Bar.class);
      assertEquals(context, bar.getContext());
      assertEquals(OtherBar.class, bar.getClass());
    }
  }
}
