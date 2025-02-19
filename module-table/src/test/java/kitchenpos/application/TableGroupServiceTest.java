package kitchenpos.application;

import static kitchenpos.common.exception.ErrorCode.EXISTS_NOT_COMPLETION_STATUS;
import static kitchenpos.common.exception.ErrorCode.NOT_SAME_BETWEEN_ORDER_TABLES_COUNT_AND_SAVED_ORDER_TABLES;
import static kitchenpos.common.exception.ErrorCode.ORDER_TABLES_MUST_BE_AT_LEAST_TWO;
import static kitchenpos.common.exception.ErrorCode.TABLE_IS_NOT_EMPTY_OR_ALREADY_REGISTER_TABLE_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import kitchenpos.common.exception.KitchenposException;
import kitchenpos.table.application.TableGroupService;
import kitchenpos.table.application.validator.TableGroupValidator;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTables;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.table.dto.request.TableGroupRequest;
import kitchenpos.table.dto.response.TableGroupResponse;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.domain.TableGroupRepository;
import kitchenpos.table.event.ValidateUnGroupEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableGroupServiceTest {
    @Mock
    private TableGroupValidator tableGroupValidator;
    @Mock
    private OrderTables orderTables;
    @Mock
    private OrderTableRepository orderTableRepository;
    @Mock
    private TableGroupRepository tableGroupRepository;
    @InjectMocks
    private TableGroupService tableGroupService;
    private OrderTable 주문_좌석_1;
    private OrderTable 주문_좌석_2;
    private TableGroupRequest 좌석_그룹_요청;
    private TableGroupRequest 좌석_그룹_요청_2;
    private TableGroup 생성된_좌석_그룹;
    private OrderTable 주문_좌석_3;
    private OrderTable 주문_좌석_4;
    private TableGroup 생성된_좌석_그룹_2;

    @BeforeEach
    void setUp() {
        주문_좌석_1 = new OrderTable(1L, null, 1, true);
        주문_좌석_2 = new OrderTable(2L, null, 2, true);
        좌석_그룹_요청 = new TableGroupRequest(Arrays.asList(주문_좌석_1.getId(), 주문_좌석_2.getId()));
        생성된_좌석_그룹 = new TableGroup(1L, Arrays.asList(주문_좌석_1, 주문_좌석_2));

        주문_좌석_3 = new OrderTable(3L, null, 1, true);
        주문_좌석_4 = new OrderTable(4L, null, 1, false);
        좌석_그룹_요청_2 = new TableGroupRequest(Arrays.asList(주문_좌석_3.getId(), 주문_좌석_4.getId()));
        생성된_좌석_그룹_2 = new TableGroup(2L, Arrays.asList(주문_좌석_3, 주문_좌석_4));
    }

    @Test
    void 생성() {
        given(orderTableRepository.findAllByIdIn(anyList())).willReturn(Arrays.asList(주문_좌석_1, 주문_좌석_2));
        doNothing().when(tableGroupValidator).validateCreate(anyList(), anyList());
        given(tableGroupRepository.save(any())).willReturn(생성된_좌석_그룹);

        TableGroupResponse response = tableGroupService.create(좌석_그룹_요청);

        assertAll(
                () -> assertThat(response.getOrderTables().size()).isEqualTo(2)
        );
    }

    @Test
    void 좌석_그룹으로_지정하려고_하는_좌석_개수가_1개인_경우() {
        좌석_그룹_요청 = new TableGroupRequest(Arrays.asList(주문_좌석_1.getId()));
        doThrow(new KitchenposException(ORDER_TABLES_MUST_BE_AT_LEAST_TWO))
                .when(tableGroupValidator).validateCreate(anyList(), anyList());

        assertThatThrownBy(
                () -> tableGroupService.create(좌석_그룹_요청)
        )
                .isInstanceOf(KitchenposException.class)
                .hasMessageContaining(ORDER_TABLES_MUST_BE_AT_LEAST_TWO.getDetail());
    }

    @Test
    void 좌석_그룹_지정을_요청한_좌석_개수와_실제_등록된_좌석_개수가_다른_경우() {
        given(orderTableRepository.findAllByIdIn(anyList()))
                .willReturn(Arrays.asList(주문_좌석_1, 주문_좌석_2, 주문_좌석_3));
        doThrow(new KitchenposException(NOT_SAME_BETWEEN_ORDER_TABLES_COUNT_AND_SAVED_ORDER_TABLES))
                .when(tableGroupValidator).validateCreate(anyList(), anyList());

        assertThatThrownBy(
                () -> tableGroupService.create(좌석_그룹_요청_2)
        )
                .isInstanceOf(KitchenposException.class)
                .hasMessageContaining(NOT_SAME_BETWEEN_ORDER_TABLES_COUNT_AND_SAVED_ORDER_TABLES.getDetail());
    }

    @Test
    void 사용중인_좌석을_그룹으로_지정하려_하는_경우() {
        주문_좌석_1 = new OrderTable(1L, null, 1, false);

        given(orderTableRepository.findAllByIdIn(anyList())).willReturn(Arrays.asList(주문_좌석_1, 주문_좌석_2));
        doThrow(new KitchenposException(TABLE_IS_NOT_EMPTY_OR_ALREADY_REGISTER_TABLE_GROUP))
                .when(tableGroupValidator).validateCreate(anyList(), anyList());

        assertThatThrownBy(
                () -> tableGroupService.create(좌석_그룹_요청)
        )
                .isInstanceOf(KitchenposException.class)
                .hasMessageContaining(TABLE_IS_NOT_EMPTY_OR_ALREADY_REGISTER_TABLE_GROUP.getDetail());
    }

    @Test
    void 좌석_그룹_해제() {
        given(orderTableRepository.findAllByTableGroupId(anyLong())).willReturn(Arrays.asList(주문_좌석_3, 주문_좌석_4));

        tableGroupService.ungroup(생성된_좌석_그룹_2.getId());

        assertAll(
                () -> assertThat(주문_좌석_3.getTableGroup()).isNull(),
                () -> assertThat(주문_좌석_4.getTableGroup()).isNull()
        );
    }
}
