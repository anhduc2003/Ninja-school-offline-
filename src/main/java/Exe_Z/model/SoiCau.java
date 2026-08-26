package Exe_Z.model;

import java.util.ArrayList;

public class SoiCau {
  public String ketqua;
  
  public String soramdom;
  
  public String time;
  
  public static ArrayList<SoiCau> soicau = new ArrayList<>();
  
  public SoiCau(String name, String tong, String time) {
    this.ketqua = name;
    this.soramdom = tong;
    this.time = time;
  }
  
  public static void clear() {
    soicau = new ArrayList<>();
  }
}
