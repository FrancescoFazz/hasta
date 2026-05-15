package com.hasta.backend.auction;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService){
        this.auctionService = auctionService;
    }

    @PostMapping
    public ResponseEntity<Auction> createAuction(@RequestBody @Valid CreateAuctionRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.addAuction(request));
    }

    @GetMapping
    public ResponseEntity<List<Auction>> findAll(){
        return ResponseEntity.ok(auctionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Auction>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.findById(id));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Void> closeAuction(
            @PathVariable Long id,
            @RequestParam Long winnerId,
            @RequestParam BigDecimal finalPrice){
        auctionService.closeAuction(id, winnerId, finalPrice);
        return ResponseEntity.noContent().build();
    }

}
