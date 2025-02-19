package kitchenpos.order.dto.request;

import kitchenpos.order.domain.OrderStatus;

public class OrderStatusRequest {
    private OrderStatus orderStatus;

    public OrderStatusRequest() {
    }

    public OrderStatusRequest(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
}
