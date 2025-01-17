package com.paymentchain.customer.entitites;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Data
@Schema(description = "Customer entity")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Schema(description = "Code of the customer", example = "01", required = true)
    private String code;
    @Schema(description = "Name of the customer", example = "John Doe", required = true)
    private String name;
    @Schema(description = "Phone of the customer", example = "12345311")
    private String phone;
    @Schema(description = "IBAN of the customer", example = "34531235865345")
    private String iban;
    @Schema(description = "Surname of the customer", example = "Perez")
    private String surname;
    @Schema(description = "Address of the customer", example = "Imp. 34")
    private String address;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerProduct> products;
    @Transient// este annotation dice que no vamos a guardar este atributo en la base de datos
    private List<?> transactions = new ArrayList<>();
}
