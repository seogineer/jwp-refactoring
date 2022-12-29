package kitchenpos.order.event;

import static kitchenpos.common.exception.ErrorCode.EXISTS_NOT_COMPLETION_STATUS;

import java.util.Arrays;
import kitchenpos.common.exception.KitchenposException;
import kitchenpos.order.domain.OrderRepository;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.event.ValidateUnGroupEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ValidateUnGroupEventHandler {
    private final OrderRepository orderRepository;

    public ValidateUnGroupEventHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventListener
    @Transactional
    public void handle(ValidateUnGroupEventPublisher eventPublisher) {
        existsByOrderTableIdAndOrderStatusIn(eventPublisher.getOrderTable());
    }

    public void existsByOrderTableIdAndOrderStatusIn(OrderTable orderTable) {
        if (orderRepository.existsByOrderTableIdAndOrderStatusIn(
                orderTable.getId(),
                Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL))
        ) {
            throw new KitchenposException(EXISTS_NOT_COMPLETION_STATUS);
        }
    }
}
