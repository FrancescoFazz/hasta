package com.hasta.backend.auction.controller;

import com.hasta.backend.auction.model.Auction;
import com.hasta.backend.auction.model.CreateAuctionRequest;
import com.hasta.backend.auction.model.PlaceBidRequest;
import com.hasta.backend.auction.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService){
        this.auctionService = auctionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Auction createAuction(@RequestBody @Valid CreateAuctionRequest request){
        return auctionService.createAuction(request);
    }

    @GetMapping
    public List<Auction> getAllAuctions() {
        return auctionService.findAll();
    }

    @GetMapping("/{id}")
    public Auction getAuction(@PathVariable Long id) {
        return auctionService.findById(id);
    }

    @PutMapping("/{id}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeAuction(@PathVariable Long id) {
        auctionService.closeAuction(id);
    }

    @PostMapping("/{id}/bids")
    public void placeBid(
            @PathVariable Long id,
            @RequestBody @Valid PlaceBidRequest request) {
        auctionService.placeBid(id, request.getUserId(), request.getAmount());
    }
}