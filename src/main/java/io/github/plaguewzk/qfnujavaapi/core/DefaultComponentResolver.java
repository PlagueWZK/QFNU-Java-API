package io.github.plaguewzk.qfnujavaapi.core;

import java.util.Objects;

public final class DefaultComponentResolver implements ComponentResolver {
    private final QFNUContext context;
    private ParserFactory parserFactory;
    private ServiceFactory serviceFactory;

    public DefaultComponentResolver(QFNUContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public void bind(ParserFactory parserFactory, ServiceFactory serviceFactory) {
        this.parserFactory = Objects.requireNonNull(parserFactory, "parserFactory");
        this.serviceFactory = Objects.requireNonNull(serviceFactory, "serviceFactory");
    }

    @Override
    public QFNUContext context() {
        return context;
    }

    @Override
    public QFNUExecutor executor() {
        return context.executor();
    }

    @Override
    public <T> T service(Class<T> serviceType) {
        ensureBound();
        return serviceFactory.getService(serviceType);
    }

    @Override
    public <T> T parser(Class<T> parserType) {
        ensureBound();
        return parserFactory.getParser(parserType);
    }

    private void ensureBound() {
        if (parserFactory == null || serviceFactory == null) {
            throw new IllegalStateException("组件解析器尚未完成绑定");
        }
    }
}
