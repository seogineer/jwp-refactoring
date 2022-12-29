package kitchenpos.table.event;

import kitchenpos.table.domain.OrderTable;

public class ValidateUnGroupEventPublisher {
    private final OrderTable orderTable;

    public ValidateUnGroupEventPublisher(OrderTable orderTable) {
        this.orderTable = orderTable;
    }

    public OrderTable getOrderTable() {
        return orderTable;
    }
}
