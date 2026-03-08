package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceFactoryTest {

    @Test
    void shouldCacheServiceInstances() {
        ServiceFactory serviceFactory = new ServiceFactory(newContext());

        DummyService first = serviceFactory.getService(DummyService.class);
        DummyService second = serviceFactory.getService(DummyService.class);

        assertSame(first, second);
    }

    @Test
    void shouldThrowWhenServiceConstructorMissing() {
        ServiceFactory serviceFactory = new ServiceFactory(newContext());

        assertThrows(ServiceCreationException.class, () -> serviceFactory.getService(InvalidService.class));
    }

    private static QFNUContext newContext() {
        return new QFNUContext(new QFNUExecutor(new OkHttpClient()), "student", "password", null);
    }

    public static final class DummyService {
        public DummyService(QFNUContext context) {
        }
    }

    public static final class InvalidService {
    }
}
