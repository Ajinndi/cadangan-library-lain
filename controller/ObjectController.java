package controller;

import exception.InputTidakValidException;
import exception.MenuTidakDitemukanException;
import exception.StokTidakCukupException;
import model.EnumUkuran;
import model.Kopi;
import model.MenuItem;
import model.Roti;
import model.Teh;
import util.InputUtil;
import view.ObjectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ObjectController {
    private final Scanner sc = new Scanner(System.in);
    private final ObjectView view = new ObjectView();
    
    // Menggunakan Map murni tanpa tanda <> (Raw Type)
    private final Map menuMap = new LinkedHashMap();

    public ObjectController() {
        menuMap.put("M-01", new Kopi("M-01", "Kopi Susu", 18000, 5));
        menuMap.put("M-02", new Teh("M-02", "Teh Tarik", 12000, 10));
        menuMap.put("M-03", new Roti("M-03", "Roti Cokelat", 10000, 8));
    }

    public void jalan() {
        view.tampilkanMenu(new ArrayList(menuMap.values()));
        boolean berjalan = true;
        while (berjalan) {
            view.tampilkanMenuAwal();
            try {
                int pilihan = InputUtil.getInt(sc);
                switch (pilihan) {
                    case 1:
                        view.tampilkanMenu(new ArrayList(menuMap.values()));
                        break;
                    case 2:
                        prosesPesanan();
                        break;
                    case 3:
                        tampilkanMenuUrutHarga(); 
                        break;
                    case 4:
                        hapusMenuHabis(); 
                        break;
                    case 0:
                        berjalan = false;
                        view.tampilkanPesan("Terima kasih sudah berkunjung!");
                        break;
                    default:
                        view.tampilkanPesan("Pilihan menu tidak tersedia.");
                }
            } catch (NumberFormatException e) {
                view.tampilkanPesan("Input menu harus berupa angka.");
            }
        }
    }

    private void prosesPesanan() {
        MenuItem menuTerpilih = pilihMenu();
        if (menuTerpilih == null) return;

        int jumlah = inputJumlahValid(menuTerpilih);
        if (jumlah <= 0) return;

        if (!inputKustomisasiValid(menuTerpilih)) return;

        double total = hitungTotal(menuTerpilih, jumlah);
        if (total < 0) return;

        menuTerpilih.kurangiStok(jumlah);
        view.tampilkanStruk(menuTerpilih, jumlah, total);
    }

    private MenuItem pilihMenu() {
        while (true) {
            String id = view.inputIdMenu(sc);
            try {
                return cariMenu(id);
            } catch (MenuTidakDitemukanException e) {
                view.tampilkanPesan(e.getMessage() + " Silakan masukkan ID lagi.");
            }
        }
    }

    private MenuItem cariMenu(String id) {
        MenuItem item = (MenuItem) menuMap.get(id.toUpperCase());
        if (item == null) {
            throw new MenuTidakDitemukanException("Menu dengan ID " + id + " tidak ada.");
        }
        return item;
    }

    private void tampilkanMenuUrutHarga() {
        List listMenu = new ArrayList(menuMap.values());
        
        // Comparator manual menggunakan Anonymous Class tanpa tanda <>
        Collections.sort(listMenu, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                MenuItem m1 = (MenuItem) o1;
                MenuItem m2 = (MenuItem) o2;
                return Double.compare(m1.getHargaBase(), m2.getHargaBase());
            }
        });

        view.tampilkanPesan("\n--- Menu Berdasarkan Harga Termurah ---");
        view.tampilkanMenu(listMenu);
    }

    // Menggunakan Iterator murni tanpa tanda <> sama sekali (Raw Type + Casting manual)
    private void hapusMenuHabis() {
        Iterator iterator = menuMap.entrySet().iterator();
        int jumlahDihapus = 0;
        
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MenuItem item = (MenuItem) entry.getValue();
            
            if (item.getStok() <= 0) {
                iterator.remove();
                view.tampilkanPesan("Menu " + item.getNama() + " dihapus dari sistem (stok habis).");
                jumlahDihapus++;
            }
        }
        
        if (jumlahDihapus == 0) {
            view.tampilkanPesan("Tidak ada menu dengan stok habis.");
        }
    }

    private int inputJumlahValid(MenuItem item) {
        while (true) {
            try {
                int jumlah = view.inputJumlah(sc);
                item.validasiJumlahPesanan(jumlah);
                return jumlah;
            } catch (NumberFormatException e) {
                view.tampilkanPesan("Jumlah harus berupa angka.");
            } catch (InputTidakValidException | StokTidakCukupException e) {
                view.tampilkanPesan(e.getMessage() + " Silakan masukkan jumlah lagi.");
            }
        }
    }

    private boolean inputKustomisasiValid(MenuItem item) {
        while (true) {
            List customizations = new ArrayList();
            try {
                for (Object obj : item.getCustomizationPrompts()) {
                    String namaKustomisasi = (String) obj;
                    customizations.add(view.inputKustomisasi(sc, namaKustomisasi));
                }
                item.customize(customizations);
                return true;
            } catch (NumberFormatException e) {
                view.tampilkanPesan("Kustomisasi harus berupa angka. Silakan masukkan ulang semua kustomisasi.");
            } catch (InputTidakValidException e) {
                view.tampilkanPesan(e.getMessage() + " Silakan masukkan ulang semua kustomisasi.");
            }
        }
    }

    private double hitungTotal(MenuItem item, int jumlah) {
        while (true) {
            view.tampilkanMetodeHarga(item);
            int pilihan;
            try {
                pilihan = InputUtil.getInt(sc);
            } catch (NumberFormatException e) {
                view.tampilkanPesan("Pilihan harus berupa angka.");
                continue;
            }

            try {
                switch (pilihan) {
                    case 1:
                        return item.hitungHargaTotal(jumlah);
                    case 2:
                        String kodePromo = view.inputKodePromo(sc);
                        if (!kodePromo.equalsIgnoreCase("HEMAT10")) {
                            view.tampilkanPesan("Kode promo tidak valid. Harga normal digunakan.");
                            return item.hitungHargaTotal(jumlah);
                        }
                        return item.hitungHargaTotal(jumlah, kodePromo);
                    case 3:
                        if (!item.bisaPilihUkuran()) {
                            view.tampilkanPesan("Ukuran hanya tersedia untuk minuman.");
                            break;
                        }
                        String pilihanUkuran = view.inputUkuran(sc);
                        EnumUkuran ukuran = EnumUkuran.valueOf(pilihanUkuran.toUpperCase());
                        return item.hitungHargaTotal(jumlah, ukuran);
                    default:
                        view.tampilkanPesan("Pilihan perhitungan tidak tersedia.");
                }
            } catch (IllegalArgumentException e) {
                view.tampilkanPesan("Ukuran tidak valid. Pilih SMALL, MEDIUM, atau LARGE.");
            } catch (InputTidakValidException | StokTidakCukupException e) {
                view.tampilkanPesan(e.getMessage());
                return -1;
            }
        }
    }
}