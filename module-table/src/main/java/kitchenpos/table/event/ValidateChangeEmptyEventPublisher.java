package kitchenpos.table.event;

import kitchenpos.table.domain.OrderTable;

public class ValidateChangeEmptyEventPublisher {
    private final OrderTable orderTable;

    public ValidateChangeEmptyEventPublisher(OrderTable orderTable) {
        this.orderTable = orderTable;
    }

    public OrderTable getOrderTable() {
        return orderTable;
    }
}
