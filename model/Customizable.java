package model;

import java.util.List;

public interface Customizable {
    void customize(List<Integer> customizations);
    void customize(int[] customizations);
}

/*
================================================================================
PENJELASAN KODE `Customizable.java`:
- Kontrak antarmuka bagi item yang mendukung kustomisasi melalui List generik maupun array.
================================================================================
*/