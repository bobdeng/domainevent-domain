package domainevent;

import lombok.extern.java.Log;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Log
public class DomainEventService {
    private final EventPublisher bobEventPublisher;
    private final ExpressionEval expressionEval;
    private final Map<String, DomainSubscribers> subscribersMap = new ConcurrentHashMap<>();
    private final Map<Class, DomainSubscribers> subscribersMapOfClass = new ConcurrentHashMap<>();

    public DomainEventService(EventPublisher bobEventPublisher, ExpressionEval expressionEval) {

        this.bobEventPublisher = bobEventPublisher;
        this.expressionEval = expressionEval;
    }

    public void publish(Object object) {
        bobEventPublisher.publish(object);
    }

    public void registerSubscriber(Object domainEventSubscriber, Class clz) {
        Stream.of(clz.getMethods())
                .filter(method -> method.getDeclaredAnnotation(DomainEvent.class) != null)
                .forEach(method -> registerSubscriber(domainEventSubscriber, method));
    }

    private void registerSubscriber(Object domainEventSubscriber, Method method) {
        DomainEvent domainEvent = method.getDeclaredAnnotation(DomainEvent.class);
        Class argument = method.getParameterTypes()[0];
        DomainSubscriber subscriber = new DomainSubscriber(domainEventSubscriber, method, domainEvent, expressionEval);
        if ("".equals(domainEvent.value())) {
            registerByEventClass(argument, subscriber);
            return;
        }
        registerByEventNames(subscriber, method, domainEvent);
    }

    private void registerByEventClass(Class argument, DomainSubscriber subscriber) {
        DomainSubscribers domainSubscribers = subscribersMapOfClass.getOrDefault(argument, new DomainSubscribers());
        domainSubscribers.add(subscriber);
        subscribersMapOfClass.put(argument, domainSubscribers);
    }

    private void registerByEventNames(DomainSubscriber subscriber, Method method, DomainEvent domainEvent) {
        DomainSubscribers domainSubscribers = subscribersMap.getOrDefault(domainEvent.value(), new DomainSubscribers());
        domainSubscribers.add(subscriber);
        subscribersMap.put(domainEvent.value(), domainSubscribers);
    }

    public void onMessageObject(Object event) {
        Optional.ofNullable(subscribersMapOfClass.get(event.getClass()))
                .ifPresent(domainSubscribers -> domainSubscribers.onMessageObject(event));
    }
}
