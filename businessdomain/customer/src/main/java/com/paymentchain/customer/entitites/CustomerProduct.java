package com.paymentchain.customer.entitites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class CustomerProduct {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private long productId;
    @Transient
    private String productName;
    @JsonIgnore//necesario para que no haya recursividad infinita
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Customer.class)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
