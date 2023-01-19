package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired EntityManager em;
    @Autowired OrderRepository orderRepository;

    @Test
    @DisplayName("상품 주문")
    public void orderTest() {
        // given
        Member member = createMember();
        Item item = createBook("테스트", 10000, 10);

        int orderCount = 1;

        // when
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(getOrder.getOrderItems().size(), 1, "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(10000 * orderCount, getOrder.getTotalPrice(), "주문 가격은 가격 * 수량이다.");
        assertEquals(9, item.getStockQuantity(), "주문 수량 만큼 재고가 줄어야 한다.");
    }

    @Test
    @DisplayName("상품 주문시 재고수량 초과")
    public void orderExceptionTest() {
        // given
        Member member = createMember();
        Item item = createBook("테스트", 10000, 1);

        int orderCount = 2;

        // expect
        assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), item.getId(), orderCount);
        });
    }

    @Test
    @DisplayName("주문 취소")
    public void orderCancel() {
        // given
        Member member = createMember();
        Item item = createBook("테스트", 10000, 2);
        int orderCount = 1;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL");
        assertEquals(item.getStockQuantity(), 2, "주문 취소 상품은 재고 복구");
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강남구", "1234"));
        em.persist(member);
        return member;
    }

    private Item createBook(String name, int price, int quantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

}