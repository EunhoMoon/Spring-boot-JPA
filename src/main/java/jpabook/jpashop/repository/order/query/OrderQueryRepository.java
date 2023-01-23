package jpabook.jpashop.repository.order.query
        ;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtoList() {

        findOrders().forEach(o -> {
            o.setOrderItems(findOrderItems(o.getOrderId()));
        });
        return findOrders();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                                "FROM OrderItem oi " +
                                "JOIN oi.item i " +
                                "WHERE oi.order.id = : orderId", OrderItemQueryDto.class
                ).setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "SELECT " +
                                "new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, o.member.name, o.orderDate, o.status, o.delivery.address) " +
                                "FROM Order o " +
                                "JOIN o.member m " +
                                "JOIN o.delivery d", OrderQueryDto.class
                )
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();

        List<Long> orderIds = toOrderIds(result);

        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                                "FROM OrderItem oi " +
                                "JOIN oi.item i " +
                                "WHERE oi.order.id IN :orderIds", OrderItemQueryDto.class
                ).setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                        "SELECT new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count) " +
                                "FROM Order o " +
                                "JOIN o.member m " +
                                "JOIN o.delivery d " +
                                "JOIN o.orderItems oi " +
                                "JOIN oi.item i"
                        , OrderFlatDto.class
                )
                .getResultList();
    }
}
