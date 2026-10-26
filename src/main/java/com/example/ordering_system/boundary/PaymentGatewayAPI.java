package com.example.ordering_system.boundary;

public interface PaymentGatewayAPI {
    String mintaTransaksiPembayaran(double total, String tokenKartu);
}
