package com.example.springbootcamelkafka.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "transaction_type")
    @Column(nullable = false)
    private String transaction_type;

    @JsonFormat(pattern = "amount")
    @Column(nullable = false)
    private Integer amount;
}
