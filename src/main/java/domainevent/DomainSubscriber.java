package domainevent;

import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;

import java.lang.reflect.Method;

@Log
@EqualsAndHashCode
class DomainSubscriber implements Comparable<DomainSubscriber> {
    private final Object object;
    private final Method method;
    private final DomainEvent domainEvent;
    private final ExpressionEval expressionEval;

    public DomainSubscriber(Object object, Method method, DomainEvent domainEvent, ExpressionEval expressionEval) {
        this.object = object;
        this.method = method;
        this.domainEvent = domainEvent;
        this.expressionEval = expressionEval;
    }

    private void call(Object event) {
        if (whenConditionNotMatch(event)) {
            return;
        }
        try {
            method.invoke(object, event);
        } catch (Exception e) {
            onCallException(e);
        }
    }

    private void onCallException(Exception e) {
        if (e.getCause() instanceof RuntimeException) {
            throw (RuntimeException) e.getCause();
        }
        throw new InvocationException(e);
    }

    private boolean whenConditionNotMatch(Object event) {
        return hasWhenCondition() && !expressionEval.when(domainEvent.when(), event);
    }

    private boolean hasWhenCondition() {
        return !"".equals(domainEvent.when());
    }

    public void onMessageObject(Object event) {
        call(event);
    }

    @Override
    public int compareTo(DomainSubscriber o) {
        return domainEvent.priority() - o.domainEvent.priority();
    }

}
