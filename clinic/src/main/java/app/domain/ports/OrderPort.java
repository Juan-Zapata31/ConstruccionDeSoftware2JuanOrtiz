package app.domain.ports;

import app.domain.models.Order;

public interface OrderPort {

    public boolean existsByNumber(String number);

    public void save(Order order);

}
