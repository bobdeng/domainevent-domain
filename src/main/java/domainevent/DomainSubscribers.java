package domainevent;

import java.util.ArrayList;
import java.util.List;

public class DomainSubscribers {
    private final List<DomainSubscriber> subscribers = new ArrayList<>();

    public void add(DomainSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void onMessageObject(Object event) {
        subscribers
                .stream()
                .sorted()
                .forEach(domainSubscriber -> domainSubscriber.onMessageObject(event));
    }
}
