package com.example.ordering_system.service.impl;

import com.example.ordering_system.boundary.PaymentGatewayAPI;
import com.example.ordering_system.dto.request.CheckoutItemRequest;
import com.example.ordering_system.dto.request.CheckoutRequest;
import com.example.ordering_system.dto.response.CheckoutResponse;
import com.example.ordering_system.entity.ItemPesanan;
import com.example.ordering_system.entity.Menu;
import com.example.ordering_system.entity.Pelanggan;
import com.example.ordering_system.entity.Pembayaran;
import com.example.ordering_system.entity.Pesanan;
import com.example.ordering_system.entity.StatusPesanan;
import com.example.ordering_system.exception.BusinessValidationException;
import com.example.ordering_system.exception.ResourceNotFoundException;
import com.example.ordering_system.repository.MenuRepository;
import com.example.ordering_system.repository.PelangganRepository;
import com.example.ordering_system.repository.PesananRepository;
import com.example.ordering_system.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final PelangganRepository pelangganRepository;
    private final MenuRepository menuRepository;
    private final PesananRepository pesananRepository;
    private final PaymentGatewayAPI paymentGatewayAPI; // Boundary Layer

    @Override
    @Transactional
    public CheckoutResponse prosesCheckout(CheckoutRequest request) {
        // 1. Verifikasi Pelanggan (Entity query)
        Pelanggan pelanggan = pelangganRepository.findById(request.getIdPelanggan())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pelanggan dengan ID " + request.getIdPelanggan() + " tidak ditemukan"));

        // 2. Inisialisasi Pesanan Baru
        Pesanan pesanan = Pesanan.builder()
                .idPesanan("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .tanggal(LocalDateTime.now())
                .status(StatusPesanan.MENUNGGU_BAYAR)
                .pelanggan(pelanggan)
                .build();

        // 3. Iterasi Data Keranjang: Validasi Stok Menu & Rakit ItemPesanan (Komposisi & Agregasi)
        for (CheckoutItemRequest itemReq : request.getKeranjangData()) {
            Menu menu = menuRepository.findById(itemReq.getIdMenu())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Menu dengan ID " + itemReq.getIdMenu() + " tidak ditemukan"));

            // Validasi dan Kurangi Stok melalui Domain Method Menu
            if (!menu.kurangStok(itemReq.getJumlah())) {
                throw new BusinessValidationException(
                        "Stok tidak mencukupi untuk menu: " + menu.getNamaMenu() + " (Sisa: " + menu.getStok() + ")");
            }
            menuRepository.save(menu); // Update stok

            // Buat ItemPesanan
            ItemPesanan item = ItemPesanan.builder()
                    .idItem("ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .jumlah(itemReq.getJumlah())
                    .menu(menu)
                    .build();

            // Tambahkan ke Pesanan (Menghitung subtotal & total biaya secara terpadu)
            pesanan.tambahItem(item);
        }

        // 4. Interaksi ke Boundary Eksternal: Payment Gateway
        String refCode;
        try {
            refCode = paymentGatewayAPI.mintaTransaksiPembayaran(pesanan.getTotalBiaya(), request.getTokenKartu());
        } catch (Exception ex) {
            throw new BusinessValidationException("Pembayaran gagal diproses: " + ex.getMessage());
        }

        // 5. Pembentukan Entitas Pembayaran
        Pembayaran pembayaran = Pembayaran.builder()
                .idPembayaran("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .jumlahBayar(pesanan.getTotalBiaya())
                .metode(request.getMetodePembayaran())
                .statusBayar("SUCCESS")
                .kodeReferensi(refCode)
                .pesanan(pesanan)
                .build();

        // 6. Verifikasi Pembayaran via Domain Method
        if (pembayaran.prosesVerifikasi()) {
            pesanan.updateStatus(StatusPesanan.DIPROSES);
        } else {
            pesanan.updateStatus(StatusPesanan.BATAL);
        }
        pesanan.setPembayaran(pembayaran);

        // 7. Simpan Pesanan (Cascade Type ALL akan menyimpan daftar ItemPesanan & Pembayaran secara otomatis)
        Pesanan savedPesanan = pesananRepository.save(pesanan);

        return CheckoutResponse.builder()
                .idPesanan(savedPesanan.getIdPesanan())
                .idPembayaran(pembayaran.getIdPembayaran())
                .totalBiaya(savedPesanan.getTotalBiaya())
                .statusPesanan(savedPesanan.getStatus())
                .statusPembayaran(pembayaran.getStatusBayar())
                .kodeReferensi(pembayaran.getKodeReferensi())
                .tanggal(savedPesanan.getTanggal())
                .build();
    }

    @Override
    @Transactional
    public void batalkanTransaksi(String idPesanan, String alasan) {
        Pesanan pesanan = pesananRepository.findById(idPesanan)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pesanan dengan ID " + idPesanan + " tidak ditemukan"));

        if (pesanan.getStatus() == StatusPesanan.SELESAI || pesanan.getStatus() == StatusPesanan.DIKIRIM) {
            throw new BusinessValidationException("Pesanan sudah dikirim/selesai dan tidak dapat dibatalkan.");
        }

        // Kembalikan stok menu yang dipesan
        for (ItemPesanan item : pesanan.getDaftarItem()) {
            Menu menu = item.getMenu();
            menu.setStok(menu.getStok() + item.getJumlah());
            menuRepository.save(menu);
        }

        pesanan.updateStatus(StatusPesanan.BATAL);
        pesananRepository.save(pesanan);
        System.out.println("== [LOG PEMBATALAN] Pesanan " + idPesanan + " dibatalkan. Alasan: " + alasan + " ==");
    }
}
