package model;

public enum EnumUkuran {
    SMALL(1.0),
    MEDIUM(1.25),
    LARGE(1.5);

    private final double kalihrg;

    EnumUkuran(double pengaliHarga) {
        this.kalihrg = pengaliHarga;
    }
    
    public double getPengaliHarga() {
        return kalihrg;
    }
}

/*
================================================================================
PENJELASAN KODE `EnumUkuran.java`:
- Menyimpan data pilihan ukuran dan pengali harganya tanpa memanggil fungsi `valueOf` 
  yang berisiko menimbulkan `IllegalArgumentException`.
================================================================================
*/