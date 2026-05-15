package com.hasta.backend.auction;

import com.hasta.backend.product.Product;
import com.hasta.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantitySold;

    @Column(nullable = false)
    private BigDecimal startingPrice;

    private BigDecimal finalPrice;

    @CreationTimestamp
    private Instant startTime;
    private Instant  endTime;

    private boolean sold;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
