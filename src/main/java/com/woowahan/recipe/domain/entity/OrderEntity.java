package com.woowahan.recipe.domain.entity;

import com.woowahan.recipe.domain.OrderStatus;
import com.woowahan.recipe.exception.AppException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.woowahan.recipe.exception.ErrorCode.NOT_ENOUGH_STOCK;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private DeliveryEntity delivery;

    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Builder
    public OrderEntity(Long id, UserEntity user, DeliveryEntity delivery, OrderStatus orderStatus) {
        this.id = id;
        this.orderNumber = createOrderNumber();
        this.user = user;
        this.delivery = delivery;
        this.orderStatus = orderStatus;
    }

    /* 연관관계 메서드 */
    public void addUser(UserEntity user) {
        this.user = user;
//        user.getOrders().add(this);
    }

    public void addOrderItem(OrderItemEntity orderItem) {
        orderItems.add(orderItem);
        orderItem.addOrder(this);
    }

    public void addDelivery(DeliveryEntity delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /* 생성 메서드 */
    // 개별 주문
    public static OrderEntity createOrder(UserEntity user, DeliveryEntity delivery, OrderItemEntity orderItem) {
        OrderEntity order = new OrderEntity();
        order.addUser(user);
        order.addDelivery(delivery);
        order.addOrderItem(orderItem);
        order.orderNumber = order.createOrderNumber();
        order.orderStatus = OrderStatus.ORDER;
        return order;
    }

    public String createOrderNumber() {
        String date = String.valueOf(LocalDate.now(ZoneId.of("Asia/Seoul")));
        String now = date.replace("-", "");
        String substring = UUID.randomUUID().toString().substring(0, 8);
        return now + substring;
    }

    // 장바구니 주문
    public static OrderEntity createOrder(UserEntity user, DeliveryEntity delivery, OrderItemEntity... orderItems) {
        OrderEntity order = new OrderEntity();
        order.addUser(user);
        order.addDelivery(delivery);

        for (OrderItemEntity orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.orderStatus = OrderStatus.ORDER;
        return order;
    }

    /* 비지니스 로직 */
    public void cancel() {
        if (delivery.getDeliveryStatus().equals(DeliveryStatus.COMP)) {
            throw new AppException(NOT_ENOUGH_STOCK, NOT_ENOUGH_STOCK.getMessage());
        }

        this.orderStatus = OrderStatus.CANCEL;
        for (OrderItemEntity orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItemEntity orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public int getTotalCounts() {
        int totalCount = 0;
        for (OrderItemEntity orderItem : orderItems) {
            totalCount += orderItem.getCount();
        }
        return totalCount;
    }

}
