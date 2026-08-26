package Exe_Z.RandomItem.Event;

import Exe_Z.constants.ItemName;
import Exe_Z.event.Event;
import Exe_Z.item.ItemManager;
import Exe_Z.item.ItemTemplate;
import Exe_Z.lib.RandomCollection;
import Exe_Z.util.Log;
import Exe_Z.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RandomItemGioTo {
    public static final RandomCollection<Integer> GIO_TO = new RandomCollection<>();
    public static final RandomCollection<Integer> BOSS_TUONG_GIAC= new RandomCollection<>();
    public static final RandomCollection<Integer> TRE_XANH_TRAM_DOT= new RandomCollection<>();
    public static final RandomCollection<Integer> TRE_VANG_TRAM_DOT= new RandomCollection<>();
    public static final RandomCollection<Integer> TIN_VAT= new RandomCollection<>();
    
    public static void init() {
        GIO_TO.add(10, ItemName.DOT_TRE_XANH);
        GIO_TO.add(4, ItemName.DOT_TRE_VANG);
        
        TRE_XANH_TRAM_DOT.add(0.01, ItemName.RUONG_BACH_NGAN);
        TRE_XANH_TRAM_DOT.add(0.1, ItemName.BAT_BAO);
        TRE_XANH_TRAM_DOT.add(2, ItemName.LONG_KHI);
        TRE_XANH_TRAM_DOT.add(10, ItemName.HOA_TUYET);
        TRE_XANH_TRAM_DOT.add(10, ItemName.XE_MAY);
        TRE_XANH_TRAM_DOT.add(35, ItemName.XICH_NHAN_NGAN_LANG);
        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_TRON);
        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_CA_CHEP);
        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_NGOI_SAO);
        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_MAT_TRANG);
        TRE_XANH_TRAM_DOT.add(60, ItemName.BANH_RANG);
        TRE_XANH_TRAM_DOT.add(80, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
        TRE_XANH_TRAM_DOT.add(40, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
        TRE_XANH_TRAM_DOT.add(80, ItemName.DA_CAP_7);
        TRE_XANH_TRAM_DOT.add(30, ItemName.DA_CAP_8);
        TRE_XANH_TRAM_DOT.add(80, ItemName.MINH_MAN_DAN);
        TRE_XANH_TRAM_DOT.add(80, ItemName.LONG_LUC_DAN);
        TRE_XANH_TRAM_DOT.add(80, ItemName.KHANG_THE_DAN);
        TRE_XANH_TRAM_DOT.add(80, ItemName.SINH_MENH_DAN);
        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_CUNG);
        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_DAO);
        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_KIEM_THUAT);
        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_KUNAI);
        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_QUAT);
        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_TIEU_THUAT);
        TRE_XANH_TRAM_DOT.add(20, ItemName.HOAN_LUONG_CHI_THAO);
        TRE_XANH_TRAM_DOT.add(10, ItemName.GIAY_RACH);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_NON_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_AO_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_QUAN_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_GANG_TAY_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_GIAY_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_PHU_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_DAY_CHUYEN_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_NGOC_BOI_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_NHAN_JIRAI_);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_NON_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_AO_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_QUAN_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_GANG_TAY_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_GIAY_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_PHU_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_DAY_CHUYEN_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_NGOC_BOI_JUMITO);
        TRE_XANH_TRAM_DOT.add(15, ItemName.MANH_NHAN_JUMITO);
        TRE_XANH_TRAM_DOT.add(30, ItemName.DA_DANH_VONG_CAP_1);
        TRE_XANH_TRAM_DOT.add(10, ItemName.HUYEN_TINH_NGOC);
        TRE_XANH_TRAM_DOT.add(10, ItemName.HUYET_NGOC);
        TRE_XANH_TRAM_DOT.add(10, ItemName.LAM_TINH_NGOC);
        TRE_XANH_TRAM_DOT.add(10, ItemName.LUC_NGOC);

        // item receive from gold item
        TRE_VANG_TRAM_DOT.add(25, ItemName.HOAN_LUONG_CHI_THAO);
        TRE_VANG_TRAM_DOT.add(0.01, ItemName.RUONG_HUYEN_BI);
        TRE_VANG_TRAM_DOT.add(0.05, ItemName.RUONG_BACH_NGAN);
        TRE_VANG_TRAM_DOT.add(0.1, ItemName.BAT_BAO);
        TRE_VANG_TRAM_DOT.add(2, ItemName.LONG_KHI);
        TRE_VANG_TRAM_DOT.add(15, ItemName.HOA_TUYET);
        TRE_VANG_TRAM_DOT.add(45, ItemName.IK);
        TRE_VANG_TRAM_DOT.add(15, ItemName.HUYEN_TINH_NGOC);
        TRE_VANG_TRAM_DOT.add(15, ItemName.HUYET_NGOC);
        TRE_VANG_TRAM_DOT.add(15, ItemName.LAM_TINH_NGOC);
        TRE_VANG_TRAM_DOT.add(15, ItemName.LUC_NGOC);
        TRE_VANG_TRAM_DOT.add(25, ItemName.XE_MAY);
        TRE_VANG_TRAM_DOT.add(25, ItemName.MAT_NA_SUPER_BROLY);
        TRE_VANG_TRAM_DOT.add(25, ItemName.MAT_NA_ONNA_BUGEISHA);
        TRE_VANG_TRAM_DOT.add(50, ItemName.XICH_NHAN_NGAN_LANG);
        TRE_VANG_TRAM_DOT.add(50, ItemName.BANH_RANG);
        TRE_VANG_TRAM_DOT.add(50, ItemName.LONG_DEN_TRON);
        TRE_VANG_TRAM_DOT.add(50, ItemName.LONG_DEN_CA_CHEP);
        TRE_VANG_TRAM_DOT.add(50, ItemName.LONG_DEN_NGOI_SAO);
        TRE_VANG_TRAM_DOT.add(50, ItemName.LONG_DEN_MAT_TRANG);
        TRE_VANG_TRAM_DOT.add(45, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
        TRE_VANG_TRAM_DOT.add(40, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
        TRE_VANG_TRAM_DOT.add(60, ItemName.DA_CAP_8);
        TRE_VANG_TRAM_DOT.add(40, ItemName.DA_CAP_9);
        TRE_VANG_TRAM_DOT.add(20, ItemName.DA_CAP_10);
        TRE_VANG_TRAM_DOT.add(55, ItemName.MINH_MAN_DAN);
        TRE_VANG_TRAM_DOT.add(55, ItemName.LONG_LUC_DAN);
        TRE_VANG_TRAM_DOT.add(55, ItemName.KHANG_THE_DAN);
        TRE_VANG_TRAM_DOT.add(55, ItemName.SINH_MENH_DAN);
        TRE_VANG_TRAM_DOT.add(20, ItemName.BI_KIP_CUNG);
        TRE_VANG_TRAM_DOT.add(15, ItemName.BI_KIP_DAO);
        TRE_VANG_TRAM_DOT.add(5, ItemName.BI_KIP_KIEM_THUAT);
        TRE_VANG_TRAM_DOT.add(8, ItemName.BI_KIP_KUNAI);
        TRE_VANG_TRAM_DOT.add(10, ItemName.BI_KIP_QUAT);
        TRE_VANG_TRAM_DOT.add(2, ItemName.BI_KIP_TIEU_THUAT);
        TRE_VANG_TRAM_DOT.add(10, ItemName.HAC_NGUU);
        TRE_VANG_TRAM_DOT.add(50, ItemName.THONG_LINH_THAO);
        TRE_VANG_TRAM_DOT.add(2, ItemName.HAKAIRO_YOROI);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_NON_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_AO_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_QUAN_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_GANG_TAY_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_GIAY_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_PHU_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_DAY_CHUYEN_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_NGOC_BOI_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_NHAN_JIRAI_);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_NON_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_AO_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_QUAN_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_GANG_TAY_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_GIAY_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_PHU_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_DAY_CHUYEN_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_NGOC_BOI_JUMITO);
        TRE_VANG_TRAM_DOT.add(15, ItemName.MANH_NHAN_JUMITO);
        TRE_VANG_TRAM_DOT.add(30, ItemName.DA_DANH_VONG_CAP_1);
        TRE_VANG_TRAM_DOT.add(20, ItemName.DA_DANH_VONG_CAP_2);
        TRE_VANG_TRAM_DOT.add(1, ItemName.MAT_NA_THANH_GIONG_);
        TRE_VANG_TRAM_DOT.add(1, ItemName.SON_TINH);
        
        
        
        TIN_VAT.add(50, ItemName.LONG_DEN_TRON);
        TIN_VAT.add(50, ItemName.LONG_DEN_CA_CHEP);
        TIN_VAT.add(50, ItemName.LONG_DEN_NGOI_SAO);
        TIN_VAT.add(50, ItemName.LONG_DEN_MAT_TRANG);
        TIN_VAT.add(45, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
        TIN_VAT.add(40, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
        TIN_VAT.add(60, ItemName.DA_CAP_4);
        TIN_VAT.add(40, ItemName.DA_CAP_5);
        TIN_VAT.add(20, ItemName.DA_CAP_6);
        TIN_VAT.add(40, ItemName.TUI_QUA_NOEL);
        TIN_VAT.add(30, ItemName.DA_DANH_VONG_CAP_1);
        TIN_VAT.add(1, ItemName.HAJIRO);
        TIN_VAT.add(1, ItemName.SHIRAIJI);
        TIN_VAT.add(1, ItemName.TU_TINH_THACH_SO_CAP);
        TIN_VAT.add(0.1, ItemName.TU_TINH_THACH_TRUNG_CAP);
        TIN_VAT.add(10, ItemName.HAGGIS);
        TIN_VAT.add(0.1, ItemName.BACH_HO);
        TIN_VAT.add(1, ItemName.KHI_BAO);
        TIN_VAT.add(1, ItemName.LANG_BAO);
        TIN_VAT.add(1, ItemName.SON_TINH);
        TIN_VAT.add(0.1, ItemName.MAT_NA_THANH_GIONG_);
        
        BOSS_TUONG_GIAC.add(10, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
        BOSS_TUONG_GIAC.add(10, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
        BOSS_TUONG_GIAC.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
        BOSS_TUONG_GIAC.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
        BOSS_TUONG_GIAC.add(5, ItemName.MANH_SACH_CO);
        BOSS_TUONG_GIAC.add(2, ItemName.KHAI_THU_LENH);
        BOSS_TUONG_GIAC.add(2, ItemName.CHUYEN_TINH_THACH);
        BOSS_TUONG_GIAC.add(0.01, ItemName.RUONG_SVC_10X);
        BOSS_TUONG_GIAC.add(5, ItemName.HARLEY_DAVIDSON);
        BOSS_TUONG_GIAC.add(5, ItemName.XE_MAY);
        BOSS_TUONG_GIAC.add(30, ItemName.DA_DANH_VONG_CAP_1);
        BOSS_TUONG_GIAC.add(20, ItemName.DA_DANH_VONG_CAP_2);
        BOSS_TUONG_GIAC.add(10, ItemName.DA_DANH_VONG_CAP_3);
        BOSS_TUONG_GIAC.add(30, ItemName.NHAM_THACH_);
        BOSS_TUONG_GIAC.add(3, ItemName.TU_TINH_THACH_SO_CAP);
        BOSS_TUONG_GIAC.add(30, ItemName.PHA_LE);
        BOSS_TUONG_GIAC.add(30, ItemName.BAO_HIEM_SO_CAP);
        BOSS_TUONG_GIAC.add(30, ItemName.DOT_TRE_XANH);
        BOSS_TUONG_GIAC.add(30, ItemName.DOT_TRE_VANG);
    }
    public static void useTest(RandomCollection<Integer> rc, int times) {
        HashMap<Integer, Integer> result = rc.test(times);
        for (Map.Entry<Integer, Integer> e : result.entrySet()) {
            ItemTemplate template = ItemManager.getInstance().getItemTemplate(e.getKey());
            int value = e.getValue();
            double rate = ((double) value / times * 100);
            System.out.println(String.format("name: %s rate: %.4f%% (%d)", StringUtils.removeAccent(template.name), rate, value));
        }
    }
}
