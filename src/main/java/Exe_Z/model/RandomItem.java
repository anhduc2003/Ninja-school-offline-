package Exe_Z.model;

import Exe_Z.constants.ItemName;
import Exe_Z.event.Event;
import Exe_Z.item.ItemManager;
import Exe_Z.item.ItemTemplate;
import Exe_Z.lib.RandomCollection;
import Exe_Z.util.Log;
import Exe_Z.util.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class RandomItem {

    public static int[] DONG_XU = {50, 30, 20, 10, 10};

    public static  RandomCollection<Integer> KHI_BAO = new RandomCollection<>();
    public static  RandomCollection<Integer> LANG_BAO = new RandomCollection<>();
    public static  RandomCollection<Integer> RUONG_MAY_MAN_2 = new RandomCollection<>();
    public static  RandomCollection<Integer> BANH_KHUC_CAY_CHOCOLATE = new RandomCollection<>();
    public static  RandomCollection<Integer> BANH_KHUC_CAY_DAU_TAY = new RandomCollection<>();
    public static  RandomCollection<Integer> VUA_TUAN_LOC = new RandomCollection<>();
    public static  RandomCollection<Integer> DOI_DIEM_NGUOI_TUYET_XU = new RandomCollection<>();
    public static  RandomCollection<Integer> DOI_DIEM_NGUOI_TUYET_LUONG = new RandomCollection<>();
    public static  RandomCollection<Integer> QUA_TRANG_TRI = new RandomCollection<>();
    public static  RandomCollection<Integer> HOP_QUA_TRANG_TRI = new RandomCollection<>();
    public static  RandomCollection<Integer> BOSS_LANG_CO = new RandomCollection<>();
    public static  RandomCollection<Integer> BOSS_VDMQ = new RandomCollection<>();
    public static  RandomCollection<Integer> BOSS = new RandomCollection<>();
    public static  RandomCollection<Integer> BOSS_EVENT = new RandomCollection<>();
    public static  RandomCollection<Integer> TRUNG_THU = new RandomCollection<>();
    public static  RandomCollection<Integer> NOEL = new RandomCollection<>();
    public static  RandomCollection<Integer> ITEM = new RandomCollection<>();
    public static  RandomCollection<Integer> LANG_CO = new RandomCollection<>();
    public static  RandomCollection<Integer> VDMQ = new RandomCollection<>();
    public static  RandomCollection<Integer> LAT_HINH = new RandomCollection<>();
    public static  RandomCollection<Integer> SACH_VO_CONG_150 = new RandomCollection<>();
    public static  RandomCollection<Integer> RUONG_CHIEN_TRUONG = new RandomCollection<>();
    public static  RandomCollection<Integer> TET = new RandomCollection<>();
    public static  RandomCollection<Integer> BANH_CHUNG = new RandomCollection<>();
    public static  RandomCollection<Integer> RUONG_NGOC = new RandomCollection<>();
    public static  RandomCollection<Integer> BANH_TET = new RandomCollection<>();
    public static  RandomCollection<Integer> BUA_MAY_MAN = new RandomCollection<>();
    public static  RandomCollection<Integer> THAN_TAI = new RandomCollection<>();
    public static  RandomCollection<Integer> WOMAN_DAY = new RandomCollection<>();
    public static  RandomCollection<Integer> HUNG_KING = new RandomCollection<>();
    public static  RandomCollection<Integer> SEA_GAME = new RandomCollection<>();
    public static  RandomCollection<Integer> TRE_XANH_TRAM_DOT = new RandomCollection<>();
    public static  RandomCollection<Integer> TRE_VANG_TRAM_DOT = new RandomCollection<>();
    public static  RandomCollection<Integer> THANH_VAT = new RandomCollection<>();
    public static  RandomCollection<Integer> CUP_VANG = new RandomCollection<>();
    public static  RandomCollection<Integer> LINH_VAT = new RandomCollection<>();
    public static  RandomCollection<Integer> BOSS_LDGT = new RandomCollection<>();
    public static  RandomCollection<Integer> LDGT = new RandomCollection<>();
    public static  RandomCollection<Integer> LANH_DIA_GIA_TOC = new RandomCollection<>();
    public static  RandomCollection<Integer> SUMMER = new RandomCollection<>();
    public static  RandomCollection<Integer> NUOC_DIET_KHUAN = new RandomCollection<>();
    public static  RandomCollection<Integer> THAT_THU_BAO = new RandomCollection<>();
    public static  RandomCollection<Integer> RUONG_HAC_AM = new RandomCollection<>();
    public static  RandomCollection<Integer> LONG_DEN = new RandomCollection<>();
    public static  RandomCollection<Integer> HOP_BANH_THUONG = new RandomCollection<>();
    public static  RandomCollection<Integer> HOP_BANH_THUONG_HANG = new RandomCollection<>();
    public static  RandomCollection<Integer> HANG_VI_THU = new RandomCollection<>();
    public static  RandomCollection<Integer> TUI_QUA_NOEL = new RandomCollection<>();
    public static  RandomCollection<Integer> HOP_QUA_NOEL = new RandomCollection<>();
    public static  RandomCollection<Integer> DIEU_GIAY = new RandomCollection<>();
    public static  RandomCollection<Integer> Halloween = new RandomCollection<>();
    public static  RandomCollection<Integer> HOP_MA_QUY = new RandomCollection<>();
    public static  RandomCollection<Integer> KEO_TAO = new RandomCollection<>();
    public static  RandomCollection<Integer> DIEU_VAI = new RandomCollection<>();
    public static  RandomCollection<Integer> UPYEN = new RandomCollection<>();
    public static  RandomCollection<Integer> UPluong = new RandomCollection<>();
    public static  RandomCollection<Integer> vt = new RandomCollection<>();
    public static  RandomCollection<Integer> SACH_VO_CONG_120 = new RandomCollection<>();

    public static void init() {
//        SEA_GAME.add(1, ItemName.MUI_TEN);
//        SEA_GAME.add(1, ItemName.BONG);
//        SEA_GAME.add(1, ItemName.VIEN_DAN);
//        SEA_GAME.add(1, ItemName.KIEM_BA_CANH);

//        HUNG_KING.add(1, ItemName.DOT_TRE_VANG);
//        HUNG_KING.add(1, ItemName.DOT_TRE_XANH);
//        HUNG_KING.add(1, ItemName.BAP_NGO);
//        HUNG_KING.add(1, ItemName.TANG_THIT);
//        HUNG_KING.add(1, ItemName.KHUC_CA);
//        HUNG_KING.add(1, ItemName.CA_CHUA);
//        HUNG_KING.add(1, ItemName.GO_LIM);

//        SUMMER.add(1, ItemName.TRE);
//        SUMMER.add(1, ItemName.DAY);
//        SUMMER.add(1, ItemName.GIAY2);
//        SUMMER.add(1, ItemName.VAI);
//        SUMMER.add(1, ItemName.KEM_OC_QUE);
//        SUMMER.add(1, ItemName.KEM_SUA);
//        SUMMER.add(1, ItemName.KEM_DAU);
//        SUMMER.add(1, ItemName.KEM_CHOCOLATE);

//        WOMAN_DAY.add(1, ItemName.HOA_HONG_DO);
//        WOMAN_DAY.add(1, ItemName.HOA_HONG_VANG);
//        WOMAN_DAY.add(1, ItemName.HOA_HONG_XANH);
//        
//        //vĩ thú
//        HANG_VI_THU.add(8, ItemName.TRUNG_VI_THU);
//        HANG_VI_THU.add(8, ItemName.NGU_HANH_QUA);
//        HANG_VI_THU.add(8, ItemName.NGU_HANH_HOA);
//        HANG_VI_THU.add(10, ItemName.DA_CAP_8);
//        HANG_VI_THU.add(8, ItemName.DA_CAP_10);
//        HANG_VI_THU.add(8, ItemName.DA_CAP_11);
//        HANG_VI_THU.add(8, ItemName.DA_CAP_9);
//        HANG_VI_THU.add(8, ItemName.YEN);
//        HANG_VI_THU.add(8, ItemName.CHUYEN_TINH_THACH);
//        HANG_VI_THU.add(8, ItemName.TU_TINH_THACH_SO_CAP);
//        HANG_VI_THU.add(8, ItemName.TU_TINH_THACH_TRUNG_CAP);
//        HANG_VI_THU.add(5, ItemName.BAT_BAO);
//        HANG_VI_THU.add(20, ItemName.PHIEU_MAY_MAN);
//        HANG_VI_THU.add(20, ItemName.PHIEU_MAY_MAN);
//
//        // Than tai
//        THAN_TAI.add(30, ItemName.BAO_LI_XI_LON);
//        THAN_TAI.add(70, ItemName.BAO_LI_XI_NHO);

//        BOSS_LANG_TRUYEN_THUYET.add(10, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BOSS_LANG_TRUYEN_THUYET.add(10, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BOSS_LANG_TRUYEN_THUYET.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BOSS_LANG_TRUYEN_THUYET.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BOSS_LANG_TRUYEN_THUYET.add(5, ItemName.MANH_SACH_CO);
//        BOSS_LANG_TRUYEN_THUYET.add(5, ItemName.KHAI_THU_LENH);
//        //BOSS_LANG_TRUYEN_THUYET.add(1, ItemName.RUONG_SVC_11X);
//        BOSS_LANG_TRUYEN_THUYET.add(2, ItemName.CHUYEN_TINH_THACH);
//        BOSS_LANG_TRUYEN_THUYET.add(2, ItemName.RUONG_HAC_AM);
//        BOSS_LANG_TRUYEN_THUYET.add(2, ItemName.KHOA_HAC_AM);
//        BOSS_LANG_TRUYEN_THUYET.add(1, ItemName.RUONG_SVC_10X);
//        BOSS_LANG_TRUYEN_THUYET.add(5, ItemName.HARLEY_DAVIDSON);
//        BOSS_LANG_TRUYEN_THUYET.add(5, ItemName.XE_MAY);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_IKKAKUJUU);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_HIBASHIRI);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_SAIHYOKEN);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_AISU_MEIKU);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_KAMINARI);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_KOKAZE);
//        BOSS_LANG_TRUYEN_THUYET.add(1.5,ItemName.SACH_VO_CONG_KAGE_BUNSHIN);
        

        // Làng cổ
//        LANG_CO.add(1, ItemName.THU_TRANG);
//        LANG_CO.add(1, ItemName.GIAP_THU);
//        LANG_CO.add(1, ItemName.YEN2);
//        LANG_CO.add(1, ItemName.DAY_CUONG);
//        LANG_CO.add(1, ItemName.BO_DIEU_KHIEN);
//        LANG_CO.add(1, ItemName.DONG_CO_V_POWER);
//        LANG_CO.add(1, ItemName.DINH_VI);
//        LANG_CO.add(0.01, ItemName.LONG_KHI);
//        LANG_CO.add(0.1, ItemName.CHUYEN_TINH_THACH);
//        LANG_CO.add(1, ItemName.TU_TINH_THACH_SO_CAP);
//        LANG_CO.add(0.3, ItemName.TU_TINH_THACH_TRUNG_CAP);
//        LANG_CO.add(0.01, ItemName.TU_TINH_THACH_CAO_CAP);
//        LANG_CO.add(1, ItemName.LUC_THANH_HOA);
//        LANG_CO.add(1, ItemName.TU_LINH_LIEN_HOA);
//        LANG_CO.add(1, ItemName.LINH_LANG_HO_DIEP);
//        LANG_CO.add(60, ItemName.YEN);
//        LANG_CO.add(0.1, ItemName.DA_SUC_MANH);
//        LANG_CO.add(0.1, ItemName.DA_SINH_MENH);
//        LANG_CO.add(0.1, ItemName.DA_PHONG_NGU);

        // Vùng đất ma quỷ
//        VDMQ.add(1, ItemName.THU_TRANG);
//        VDMQ.add(1, ItemName.GIAP_THU);
//        VDMQ.add(1, ItemName.YEN2);
//        VDMQ.add(1, ItemName.DAY_CUONG);
//        VDMQ.add(1, ItemName.BO_DIEU_KHIEN);
//        VDMQ.add(1, ItemName.DONG_CO_V_POWER);
//        VDMQ.add(1, ItemName.DINH_VI);
//        VDMQ.add(1, ItemName.BINH_NITRO);
//        VDMQ.add(0.1, ItemName.CHUYEN_TINH_THACH);
//        VDMQ.add(1, ItemName.TU_TINH_THACH_SO_CAP);
//        VDMQ.add(0.5, ItemName.TU_TINH_THACH_TRUNG_CAP);
//        VDMQ.add(1, ItemName.LUC_THANH_HOA);
//        VDMQ.add(1, ItemName.TU_LINH_LIEN_HOA);
//        VDMQ.add(1, ItemName.LINH_LANG_HO_DIEP);
//        VDMQ.add(0.5, ItemName.SACH_VO_CONG_KAGE_BUNSHIN);
//        VDMQ.add(2, ItemName.PHAN_THAN_LENH);
//        VDMQ.add(40, ItemName.DA_CAP_5);
        

        // Item
//        ITEM.add(2, ItemName.DA_CAP_1);
//        ITEM.add(1, ItemName.PHUC_NANG_NHAN_GIA);
//        ITEM.add(1, ItemName.BINH_HP_CUC_TIEU);
//        ITEM.add(1, ItemName.BINH_MP_CUC_TIEU);

        // Noel
//        NOEL.add(1, ItemName.BO);
//        NOEL.add(1, ItemName.KEM);
//        NOEL.add(1, ItemName.DUONG_BOT);
//        NOEL.add(1, ItemName.DAY_KIM_TUYEN_SK);
//        NOEL.add(1, ItemName.TRAI_CHAU);
//        NOEL.add(1, ItemName.CHUONG_VANG);

        // Tết
//        TET.add(1, ItemName.LA_DONG);
//        TET.add(1, ItemName.NEP);
//        TET.add(1, ItemName.DAU_XANH2);
//        TET.add(1, ItemName.LAT_TRE);

        // Trung thu
//        TRUNG_THU.add(1, ItemName.BOT_MI);
//        TRUNG_THU.add(1, ItemName.TRUNG);
//        TRUNG_THU.add(1, ItemName.DUONG);
//        TRUNG_THU.add(1, ItemName.HAT_SEN);
//        TRUNG_THU.add(1, ItemName.DAU_XANH);
//        TRUNG_THU.add(1, ItemName.MUT);

        // Boss event
//        BOSS_EVENT.add(50, ItemName.DA_CAP_9);
//        BOSS_EVENT.add(30, ItemName.DA_CAP_10);
//
//        BOSS_EVENT.add(20, ItemName.LONG_DEN);
//        BOSS_EVENT.add(20, ItemName.HOP_BANH_THUONG_HANG);
//        BOSS_EVENT.add(20, ItemName.HOP_BANH_THUONG);
//
//        BOSS_EVENT.add(10, ItemName.XICH_NHAN_NGAN_LANG);
//        BOSS_EVENT.add(12, ItemName.HAGGIS);
//        BOSS_EVENT.add(10, ItemName.PHIEU_MAY_MAN);
//        BOSS_EVENT.add(5, ItemName.BAT_BAO);
//        BOSS_EVENT.add(3, ItemName.RUONG_BACH_NGAN);
//        // Thánh vật
//        BOSS_EVENT.add(1, ItemName.LAM_SON_DA);
//        BOSS_EVENT.add(0.5, ItemName.TRUC_BACH_THIEN_LU);

        // Cúp vàng
        // BOSS_EVENT.add(2, ItemName.CUP_BAC);
        // BOSS_EVENT.add(1, ItemName.CUP_VANG);
        // linh vật
        // BOSS_EVENT.add(2, ItemName.DE_NGOC);
        // BOSS_EVENT.add(1, ItemName.BO_VANG);
        // BOSS_EVENT.add(1, ItemName.BUOM_VANG);

         // Boss thường
//        BOSS.add(15, ItemName.DA_CAP_6);
//        BOSS.add(10, ItemName.DA_CAP_7);
//        BOSS.add(7, ItemName.DA_CAP_8);
//        BOSS.add(3, ItemName.DA_CAP_9);
//        BOSS.add(10, ItemName.PHIEU_MAY_MAN);
//        BOSS.add(2, ItemName.CHUYEN_TINH_THACH);
//        BOSS.add(3, ItemName.TU_TINH_THACH_SO_CAP);
//        BOSS.add(0.1,ItemName.SACH_VO_CONG_KAGE_BUNSHIN);
//        BOSS.add(1, ItemName.BAT_BAO);
//        BOSS.add(7, ItemName.PHAN_THAN_LENH);
//        BOSS.add(10, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BOSS.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BOSS.add(5, ItemName.TU_TINH_THACH_SO_CAP);
//        BOSS.add(2, ItemName.HARLEY_DAVIDSON);
//        //
//        BOSS.add(1, ItemName.SACH_VO_CONG_IKKAKUJUU);
//        BOSS.add(1, ItemName.SACH_VO_CONG_HIBASHIRI);
//        BOSS.add(1, ItemName.SACH_VO_CONG_SAIHYOKEN);
//        BOSS.add(1, ItemName.SACH_VO_CONG_AISU_MEIKU);
//        BOSS.add(1, ItemName.SACH_VO_CONG_KAMINARI);
//        BOSS.add(1, ItemName.SACH_VO_CONG_KOKAZE);
//        BOSS.add(1, ItemName.SACH_VO_CONG_KAGE_BUNSHIN);//sach9x phan than
//        BOSS.add(1,ItemName.SACH_VO_CONG_PAWARAIKOU);
//        BOSS.add(1,ItemName.SACH_VO_CONG_TOTOGAI);
//        BOSS.add(1,ItemName.SACH_VO_CONG_KITSUKEMAGUMA);
//        BOSS.add(1,ItemName.SACH_VO_CONG_TOTAAIGO);
//        BOSS.add(1,ItemName.SACH_VO_CONG_IKENNOTTO);
//        BOSS.add(1,ItemName.SACH_VO_CONG_OOENJO);
//        //
//        BOSS.add(1,ItemName.SACH_VO_CONG_MAAJIZANGEKI);
//        BOSS.add(1,ItemName.SACH_VO_CONG_BAANINGUFUKIYA);
//        BOSS.add(1,ItemName.SACH_VO_CONG_FURIIZUKATTO);
//        BOSS.add(1,ItemName.SACH_VO_CONG_FUROOZUNKYUUSEN);
//        BOSS.add(1,ItemName.SACH_VO_CONG_BAASUTOSUTOOMU);
//        BOSS.add(1,ItemName.SACH_VO_CONG_KOUGEKITENRAI);
        BOSS.add(0.5, 94);
        BOSS.add(0.1, 95);
        BOSS.add(0.05, 96);
        BOSS.add(0.5, 99);
        BOSS.add(0.1, 100);
        BOSS.add(0.05, 101);
        BOSS.add(0.1, 104);
        BOSS.add(0.1, 105);
        BOSS.add(0.05, 106);
        BOSS.add(0.5, 109);
        BOSS.add(0.1, 110);
        BOSS.add(0.05, 111);
        BOSS.add(0.5, 114);
        BOSS.add(0.1, 115);
        BOSS.add(0.01, 116);
        BOSS.add(0.5, 119);
        BOSS.add(0.1, 120);
        BOSS.add(0.05, 121);
        
        //BOSS.add(2, ItemName.RUONG_HAC_AM);
//        BOSS.add(2, ItemName.KHOA_HAC_AM);
        

        // Boss vùng đất ma quỷ
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_IKKAKUJUU);
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_HIBASHIRI);
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_SAIHYOKEN);
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_AISU_MEIKU);
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_KAMINARI);
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_KOKAZE);
//        BOSS_VDMQ.add(1,ItemName.SACH_VO_CONG_KAGE_BUNSHIN);
//        BOSS_VDMQ.add(10, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BOSS_VDMQ.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BOSS_VDMQ.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BOSS_VDMQ.add(10, ItemName.DA_CAP_10);
//        BOSS_VDMQ.add(2, ItemName.PHAN_THAN_LENH);
//        BOSS_VDMQ.add(1, ItemName.SACH_VO_CONG_KAGE_BUNSHIN);//sach9x phan than
//        BOSS_VDMQ.add(3, ItemName.TU_TINH_THACH_TRUNG_CAP);
//        BOSS_VDMQ.add(3, ItemName.CHUYEN_TINH_THACH);
//        BOSS_VDMQ.add(1, ItemName.RUONG_SVC_10X);
//        BOSS_VDMQ.add(1, ItemName.RUONG_HAC_AM);
//        BOSS_VDMQ.add(1, ItemName.KHOA_HAC_AM);
//        BOSS_VDMQ.add(5, ItemName.HARLEY_DAVIDSON);
        BOSS_VDMQ.add(0.5, 94);
        BOSS_VDMQ.add(0.1, 95);
        BOSS_VDMQ.add(0.05, 96);
        BOSS_VDMQ.add(0.5, 99);
        BOSS_VDMQ.add(0.1, 100);
        BOSS_VDMQ.add(0.05, 101);
        BOSS_VDMQ.add(0.1, 104);
        BOSS_VDMQ.add(0.1, 105);
        BOSS_VDMQ.add(0.05, 106);
        BOSS_VDMQ.add(0.5, 109);
        BOSS_VDMQ.add(0.1, 110);
        BOSS_VDMQ.add(0.05, 111);
        BOSS_VDMQ.add(0.5, 114);
        BOSS_VDMQ.add(0.1, 115);
        BOSS_VDMQ.add(0.01, 116);
        BOSS_VDMQ.add(0.5, 119);
        BOSS_VDMQ.add(0.1, 120);
        BOSS_VDMQ.add(0.05, 121);

        // Khí bảo
//        KHI_BAO.add(20, ItemName.DINH_VI);
//        KHI_BAO.add(20, ItemName.BINH_NITRO);
//        KHI_BAO.add(20, ItemName.DONG_CO_V_POWER);
//        KHI_BAO.add(20, ItemName.BO_DIEU_KHIEN);
//        KHI_BAO.add(20, ItemName.XE_MAY);
//        KHI_BAO.add(0.5, ItemName.HARLEY_DAVIDSON);
//        KHI_BAO.add(1, ItemName.HUYET_SAC_HUNG_LANG);

        // Lang bảo
//        LANG_BAO.add(20, ItemName.YEN2);
//        LANG_BAO.add(20, ItemName.DAY_CUONG);
//        LANG_BAO.add(20, ItemName.THU_TRANG);
//        LANG_BAO.add(20, ItemName.GIAP_THU);
//        LANG_BAO.add(20, ItemName.XICH_NHAN_NGAN_LANG);
//        LANG_BAO.add(1, ItemName.HUYET_SAC_HUNG_LANG);

        // Rương may mắn 2
        // if (DONG_XU[0] > 0) {
        // RUONG_MAY_MAN_2.add(2, ItemName.DONG_XU_VANG);
        // }
        // if (DONG_XU[1] > 0) {
        // rand.add(0.5, ItemName.DONG_XU_XANH);
        // }
        // if (DONG_XU[2] > 0) {
        // RUONG_MAY_MAN_2.add(0.03, ItemName.DONG_XU_XANH_LA);
        // }
        // if (DONG_XU[3] > 0) {
        // RUONG_MAY_MAN_2.add(0.05, ItemName.DONG_XU_TRANG);
        // }
        // if (DONG_XU[4] > 0) {
        // RUONG_MAY_MAN_2.add(0.05, ItemName.DONG_XU_DO);
        // }
        // RUONG_MAY_MAN_2.add(10.8, ItemName.LANG_BAO);
        // RUONG_MAY_MAN_2.add(10, ItemName.KHI_BAO);
        // RUONG_MAY_MAN_2.add(10, ItemName.MAT_NA_SUPER_BROLY);
        // RUONG_MAY_MAN_2.add(10, ItemName.MAT_NA_VEGETA);
        // RUONG_MAY_MAN_2.add(10, ItemName.MAT_NA_ONNA_BUGEISHA);
        // RUONG_MAY_MAN_2.add(10, ItemName.MAT_NA_KUNOICHI);
        // RUONG_MAY_MAN_2.add(30, ItemName.LUC_THANH_HOA);
        // RUONG_MAY_MAN_2.add(1, ItemName.PET_BONG_MA);
        // RUONG_MAY_MAN_2.add(1, ItemName.PET_YEU_TINH);
        // RUONG_MAY_MAN_2.add(5, ItemName.HUYET_NGOC);
        // RUONG_MAY_MAN_2.add(5, ItemName.HUYEN_TINH_NGOC);
        // RUONG_MAY_MAN_2.add(5, ItemName.LUC_NGOC);
        // RUONG_MAY_MAN_2.add(5, ItemName.LAM_TINH_NGOC);
        // Bánh khúc cây chocolate
//        BANH_KHUC_CAY_CHOCOLATE.add(0.05, ItemName.RUONG_HUYEN_BI);
//        BANH_KHUC_CAY_CHOCOLATE.add(0.08, ItemName.RUONG_BACH_NGAN);
//        BANH_KHUC_CAY_CHOCOLATE.add(0.1, ItemName.BAT_BAO);
//        BANH_KHUC_CAY_CHOCOLATE.add(2, ItemName.LONG_KHI);
//        BANH_KHUC_CAY_CHOCOLATE.add(5, ItemName.HOA_TUYET);
//        BANH_KHUC_CAY_CHOCOLATE.add(25, ItemName.MAT_NA_SUPER_BROLY);
//        BANH_KHUC_CAY_CHOCOLATE.add(25, ItemName.MAT_NA_ONNA_BUGEISHA);
//        BANH_KHUC_CAY_CHOCOLATE.add(25, ItemName.HUYEN_TINH_NGOC);
//        BANH_KHUC_CAY_CHOCOLATE.add(25, ItemName.HUYET_NGOC);
//        BANH_KHUC_CAY_CHOCOLATE.add(25, ItemName.LAM_TINH_NGOC);
//        BANH_KHUC_CAY_CHOCOLATE.add(20, ItemName.IK);
//        BANH_KHUC_CAY_CHOCOLATE.add(25, ItemName.LUC_NGOC);
//        BANH_KHUC_CAY_CHOCOLATE.add(20, ItemName.XE_MAY);
//        BANH_KHUC_CAY_CHOCOLATE.add(40, ItemName.XICH_NHAN_NGAN_LANG);
//        BANH_KHUC_CAY_CHOCOLATE.add(50, ItemName.BANH_RANG);
//        BANH_KHUC_CAY_CHOCOLATE.add(50, ItemName.LONG_DEN_TRON);
//        BANH_KHUC_CAY_CHOCOLATE.add(50, ItemName.LONG_DEN_CA_CHEP);
//        BANH_KHUC_CAY_CHOCOLATE.add(60, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BANH_KHUC_CAY_CHOCOLATE.add(50, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BANH_KHUC_CAY_CHOCOLATE.add(60, ItemName.DA_CAP_9);
//        BANH_KHUC_CAY_CHOCOLATE.add(20, ItemName.DA_CAP_10);
//        BANH_KHUC_CAY_CHOCOLATE.add(60, ItemName.MINH_MAN_DAN);
//        BANH_KHUC_CAY_CHOCOLATE.add(60, ItemName.LONG_LUC_DAN);
//        BANH_KHUC_CAY_CHOCOLATE.add(60, ItemName.KHANG_THE_DAN);
//        BANH_KHUC_CAY_CHOCOLATE.add(60, ItemName.SINH_MENH_DAN);
//        BANH_KHUC_CAY_CHOCOLATE.add(10, ItemName.HAC_NGUU);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.LAN_SU_VU);
//        BANH_KHUC_CAY_CHOCOLATE.add(30, ItemName.THONG_LINH_THAO);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.BI_KIP_CUNG);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.BI_KIP_DAO);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.BI_KIP_KIEM_THUAT);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.BI_KIP_KUNAI);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.BI_KIP_QUAT);
//        BANH_KHUC_CAY_CHOCOLATE.add(3, ItemName.BI_KIP_TIEU_THUAT);
//
//        // Bánh khúc cây dâu tây
//        BANH_KHUC_CAY_DAU_TAY.add(0.01, ItemName.RUONG_BACH_NGAN);
//        BANH_KHUC_CAY_DAU_TAY.add(0.1, ItemName.BAT_BAO);
//        BANH_KHUC_CAY_DAU_TAY.add(2, ItemName.LONG_KHI);
//        BANH_KHUC_CAY_DAU_TAY.add(5, ItemName.HOA_TUYET);
//        BANH_KHUC_CAY_DAU_TAY.add(25, ItemName.HUYEN_TINH_NGOC);
//        BANH_KHUC_CAY_DAU_TAY.add(25, ItemName.HUYET_NGOC);
//        BANH_KHUC_CAY_DAU_TAY.add(25, ItemName.LAM_TINH_NGOC);
//        BANH_KHUC_CAY_DAU_TAY.add(25, ItemName.LUC_NGOC);
//        BANH_KHUC_CAY_DAU_TAY.add(10, ItemName.XE_MAY);
//        BANH_KHUC_CAY_DAU_TAY.add(30, ItemName.XICH_NHAN_NGAN_LANG);
//        BANH_KHUC_CAY_DAU_TAY.add(50, ItemName.LONG_DEN_TRON);
//        BANH_KHUC_CAY_DAU_TAY.add(50, ItemName.LONG_DEN_CA_CHEP);
//        BANH_KHUC_CAY_DAU_TAY.add(50, ItemName.LONG_DEN_NGOI_SAO);
//        BANH_KHUC_CAY_DAU_TAY.add(50, ItemName.LONG_DEN_MAT_TRANG);
//        BANH_KHUC_CAY_DAU_TAY.add(60, ItemName.BANH_RANG);
//        BANH_KHUC_CAY_DAU_TAY.add(80, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BANH_KHUC_CAY_DAU_TAY.add(60, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BANH_KHUC_CAY_DAU_TAY.add(90, ItemName.DA_CAP_9);
//        BANH_KHUC_CAY_DAU_TAY.add(30, ItemName.DA_CAP_10);
//        BANH_KHUC_CAY_DAU_TAY.add(80, ItemName.MINH_MAN_DAN);
//        BANH_KHUC_CAY_DAU_TAY.add(80, ItemName.LONG_LUC_DAN);
//        BANH_KHUC_CAY_DAU_TAY.add(80, ItemName.KHANG_THE_DAN);
//        BANH_KHUC_CAY_DAU_TAY.add(80, ItemName.SINH_MENH_DAN);
//        BANH_KHUC_CAY_DAU_TAY.add(3, ItemName.BI_KIP_CUNG);
//        BANH_KHUC_CAY_DAU_TAY.add(3, ItemName.BI_KIP_DAO);
//        BANH_KHUC_CAY_DAU_TAY.add(3, ItemName.BI_KIP_KIEM_THUAT);
//        BANH_KHUC_CAY_DAU_TAY.add(3, ItemName.BI_KIP_KUNAI);
//        BANH_KHUC_CAY_DAU_TAY.add(3, ItemName.BI_KIP_QUAT);
//        BANH_KHUC_CAY_DAU_TAY.add(3, ItemName.BI_KIP_TIEU_THUAT);

        // Tiêu diệt vua tuần lộc
//        VUA_TUAN_LOC.add(0.01, ItemName.RUONG_BACH_NGAN);
//        VUA_TUAN_LOC.add(0.05, ItemName.BAT_BAO);
//        VUA_TUAN_LOC.add(0.5, ItemName.HUYEN_TINH_NGOC);
//        VUA_TUAN_LOC.add(0.5, ItemName.HUYET_NGOC);
//        VUA_TUAN_LOC.add(0.5, ItemName.LAM_TINH_NGOC);
//        VUA_TUAN_LOC.add(0.5, ItemName.LUC_NGOC);
//        VUA_TUAN_LOC.add(110, ItemName.GA_TAY);
//        VUA_TUAN_LOC.add(110, ItemName.TOM_HUM);
//        VUA_TUAN_LOC.add(120, ItemName.DA_CAP_8);
//        VUA_TUAN_LOC.add(100, ItemName.DA_CAP_9);
//        VUA_TUAN_LOC.add(20, ItemName.DA_CAP_10);
//        VUA_TUAN_LOC.add(130, ItemName.MINH_MAN_DAN);
//        VUA_TUAN_LOC.add(130, ItemName.LONG_LUC_DAN);
//        VUA_TUAN_LOC.add(130, ItemName.KHANG_THE_DAN);
//        VUA_TUAN_LOC.add(130, ItemName.SINH_MENH_DAN);
//        VUA_TUAN_LOC.add(10, ItemName.DA_DANH_VONG_CAP_1);
//        VUA_TUAN_LOC.add(8, ItemName.DA_DANH_VONG_CAP_2);
//        VUA_TUAN_LOC.add(6, ItemName.DA_DANH_VONG_CAP_3);
//        VUA_TUAN_LOC.add(5, ItemName.DA_DANH_VONG_CAP_4);
//        VUA_TUAN_LOC.add(5, ItemName.DA_DANH_VONG_CAP_5);

        // Đổi điểm người tuyết xu
//        DOI_DIEM_NGUOI_TUYET_XU.add(0.05, ItemName.BAT_BAO);
//        DOI_DIEM_NGUOI_TUYET_XU.add(10, ItemName.LONG_KHI);
//        DOI_DIEM_NGUOI_TUYET_XU.add(15, ItemName.HOA_TUYET);
//        DOI_DIEM_NGUOI_TUYET_XU.add(110, ItemName.GA_TAY);
//        DOI_DIEM_NGUOI_TUYET_XU.add(110, ItemName.TOM_HUM);
//        DOI_DIEM_NGUOI_TUYET_XU.add(120, ItemName.DA_CAP_6);
//        DOI_DIEM_NGUOI_TUYET_XU.add(100, ItemName.DA_CAP_7);
//        DOI_DIEM_NGUOI_TUYET_XU.add(20, ItemName.DA_CAP_8);
//        DOI_DIEM_NGUOI_TUYET_XU.add(100, ItemName.MINH_MAN_DAN);
//        DOI_DIEM_NGUOI_TUYET_XU.add(100, ItemName.LONG_LUC_DAN);
//        DOI_DIEM_NGUOI_TUYET_XU.add(100, ItemName.KHANG_THE_DAN);
//        DOI_DIEM_NGUOI_TUYET_XU.add(100, ItemName.SINH_MENH_DAN);
//        DOI_DIEM_NGUOI_TUYET_XU.add(1, ItemName.XICH_NHAN_NGAN_LANG);
//        DOI_DIEM_NGUOI_TUYET_XU.add(5, ItemName.BANH_KHUC_CAY_CHOCOLATE);
//        DOI_DIEM_NGUOI_TUYET_XU.add(10, ItemName.BANH_KHUC_CAY_DAU_TAY);
//
//        // Đổi điểm người tuyết lượng
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(0.05, ItemName.BAT_BAO);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(0.01, ItemName.RUONG_BACH_NGAN);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(10, ItemName.LONG_KHI);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(15, ItemName.HOA_TUYET);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(110, ItemName.GA_TAY);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(110, ItemName.TOM_HUM);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(100, ItemName.MINH_MAN_DAN);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(100, ItemName.LONG_LUC_DAN);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(100, ItemName.KHANG_THE_DAN);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(100, ItemName.SINH_MENH_DAN);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(5, ItemName.XE_MAY);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(10, ItemName.BANH_KHUC_CAY_CHOCOLATE);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(10, ItemName.BANH_KHUC_CAY_DAU_TAY);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(30, ItemName.DA_DANH_VONG_CAP_1);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(20, ItemName.DA_DANH_VONG_CAP_2);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(10, ItemName.DA_DANH_VONG_CAP_3);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(8, ItemName.DA_DANH_VONG_CAP_4);
//        DOI_DIEM_NGUOI_TUYET_LUONG.add(5, ItemName.DA_DANH_VONG_CAP_5);
//
//        // Hộp quà trang trí
//        HOP_QUA_TRANG_TRI.add(0.01, ItemName.RUONG_BACH_NGAN);
//        HOP_QUA_TRANG_TRI.add(0.05, ItemName.BAT_BAO);
//        HOP_QUA_TRANG_TRI.add(0.1, ItemName.LONG_KHI);
//        HOP_QUA_TRANG_TRI.add(0.5, ItemName.HOA_TUYET);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.XE_MAY);
//        HOP_QUA_TRANG_TRI.add(10, ItemName.XICH_NHAN_NGAN_LANG);
//        HOP_QUA_TRANG_TRI.add(10, ItemName.HUYEN_TINH_NGOC);
//        HOP_QUA_TRANG_TRI.add(10, ItemName.HUYET_NGOC);
//        HOP_QUA_TRANG_TRI.add(10, ItemName.LAM_TINH_NGOC);
//        HOP_QUA_TRANG_TRI.add(10, ItemName.LUC_NGOC);
//        HOP_QUA_TRANG_TRI.add(40, ItemName.BANH_RANG);
//        HOP_QUA_TRANG_TRI.add(50, ItemName.DA_CAP_8);
//        HOP_QUA_TRANG_TRI.add(40, ItemName.DA_CAP_9);
//        HOP_QUA_TRANG_TRI.add(20, ItemName.DA_CAP_10);
//        HOP_QUA_TRANG_TRI.add(80, ItemName.MINH_MAN_DAN);
//        HOP_QUA_TRANG_TRI.add(80, ItemName.LONG_LUC_DAN);
//        HOP_QUA_TRANG_TRI.add(80, ItemName.KHANG_THE_DAN);
//        HOP_QUA_TRANG_TRI.add(80, ItemName.SINH_MENH_DAN);
//        HOP_QUA_TRANG_TRI.add(20, ItemName.DA_DANH_VONG_CAP_1);
//        HOP_QUA_TRANG_TRI.add(15, ItemName.DA_DANH_VONG_CAP_2);
//        HOP_QUA_TRANG_TRI.add(10, ItemName.DA_DANH_VONG_CAP_3);
//        HOP_QUA_TRANG_TRI.add(8, ItemName.DA_DANH_VONG_CAP_4);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.DA_DANH_VONG_CAP_5);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.BI_KIP_CUNG);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.BI_KIP_DAO);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.BI_KIP_KIEM_THUAT);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.BI_KIP_KUNAI);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.BI_KIP_QUAT);
//        HOP_QUA_TRANG_TRI.add(5, ItemName.BI_KIP_TIEU_THUAT);
//
//        // Quà trang trí
//        QUA_TRANG_TRI.add(0.01, ItemName.RUONG_BACH_NGAN);
//        QUA_TRANG_TRI.add(0.05, ItemName.BAT_BAO);
//        QUA_TRANG_TRI.add(0.1, ItemName.LONG_KHI);
//        QUA_TRANG_TRI.add(0.5, ItemName.HOA_TUYET);
//        QUA_TRANG_TRI.add(5, ItemName.XE_MAY);
//        QUA_TRANG_TRI.add(10, ItemName.XICH_NHAN_NGAN_LANG);
////        QUA_TRANG_TRI.add(10, ItemName.HUYEN_TINH_NGOC);
////        QUA_TRANG_TRI.add(10, ItemName.HUYET_NGOC);
////        QUA_TRANG_TRI.add(10, ItemName.LAM_TINH_NGOC);
////        QUA_TRANG_TRI.add(10, ItemName.LUC_NGOC);
//        QUA_TRANG_TRI.add(40, ItemName.BANH_RANG);
//        QUA_TRANG_TRI.add(50, ItemName.DA_CAP_8);
//        QUA_TRANG_TRI.add(40, ItemName.DA_CAP_9);
//        QUA_TRANG_TRI.add(20, ItemName.DA_CAP_10);
//        QUA_TRANG_TRI.add(80, ItemName.MINH_MAN_DAN);
//        QUA_TRANG_TRI.add(80, ItemName.LONG_LUC_DAN);
//        QUA_TRANG_TRI.add(80, ItemName.KHANG_THE_DAN);
//        QUA_TRANG_TRI.add(80, ItemName.SINH_MENH_DAN);
//        
        //Túi quà noel mở ngọc 
        TUI_QUA_NOEL.add(10, ItemName.HUYEN_TINH_NGOC);
        TUI_QUA_NOEL.add(10, ItemName.HUYET_NGOC);
        TUI_QUA_NOEL.add(10, ItemName.LAM_TINH_NGOC);
        TUI_QUA_NOEL.add(10, ItemName.LUC_NGOC);
//        //HỘP quà noel mở ngọc 
        HOP_QUA_NOEL.add(10, ItemName.HUYEN_TINH_NGOC);
        HOP_QUA_NOEL.add(10, ItemName.HUYET_NGOC);
        HOP_QUA_NOEL.add(10, ItemName.LAM_TINH_NGOC);
        HOP_QUA_NOEL.add(10, ItemName.LUC_NGOC);

        SACH_VO_CONG_150.add(1, ItemName.SACH_VO_CONG_ENKO_BAKUSATSU);
        SACH_VO_CONG_150.add(0.1, ItemName.SACH_VO_CONG_TSUMABENI);
        SACH_VO_CONG_150.add(0.01, ItemName.SACH_VO_CONG_SHABONDAMA);
        SACH_VO_CONG_150.add(1, ItemName.SACH_VO_CONG_KOGORASERU);
        SACH_VO_CONG_150.add(1, ItemName.SACH_VO_CONG_RAIJIN);
        SACH_VO_CONG_150.add(0.1, ItemName.SACH_VO_CONG_KAMIKAZE);

        RUONG_CHIEN_TRUONG.add(80, -1);
        RUONG_CHIEN_TRUONG.add(80, ItemName.MINH_MAN_DAN);
        RUONG_CHIEN_TRUONG.add(80, ItemName.LONG_LUC_DAN);
        RUONG_CHIEN_TRUONG.add(80, ItemName.KHANG_THE_DAN);
        RUONG_CHIEN_TRUONG.add(80, ItemName.SINH_MENH_DAN);
        RUONG_CHIEN_TRUONG.add(70, ItemName.DA_CAP_8);
        RUONG_CHIEN_TRUONG.add(65, ItemName.DA_CAP_9);
        RUONG_CHIEN_TRUONG.add(60, ItemName.DA_CAP_10);
        RUONG_CHIEN_TRUONG.add(15, ItemName.XICH_NHAN_NGAN_LANG);
        RUONG_CHIEN_TRUONG.add(10, ItemName.SON_TINH);
        RUONG_CHIEN_TRUONG.add(10, ItemName.THUY_TINH);
        RUONG_CHIEN_TRUONG.add(10, ItemName.DA_CAP_11);
        RUONG_CHIEN_TRUONG.add(10, ItemName.DA_CAP_12);
        RUONG_CHIEN_TRUONG.add(2, ItemName.BAT_BAO);
        RUONG_CHIEN_TRUONG.add(0.2, ItemName.RUONG_BACH_NGAN);
        RUONG_CHIEN_TRUONG.add(0.1, ItemName.HARLEY_DAVIDSON);

//        RUONG_HAC_AM.add(50, ItemName.DA_CAP_9);
//        RUONG_HAC_AM.add(40, ItemName.DA_CAP_10);
//        RUONG_HAC_AM.add(10, ItemName.DA_CAP_11);
//        RUONG_HAC_AM.add(15, ItemName.GIAY_VANG);
//        RUONG_HAC_AM.add(10, ItemName.GIAY_BAC);
//        RUONG_HAC_AM.add(1, ItemName.BAT_BAO);
//        RUONG_HAC_AM.add(0.1, ItemName.RUONG_BACH_NGAN);
//        RUONG_HAC_AM.add(0.05, ItemName.HARLEY_DAVIDSON);

        // Bánh chưng
//        BANH_CHUNG.add(0.01, ItemName.RUONG_HUYEN_BI);
//        BANH_CHUNG.add(0.05, ItemName.RUONG_BACH_NGAN);
//        BANH_CHUNG.add(0.1, ItemName.BAT_BAO);
//        BANH_CHUNG.add(2, ItemName.LONG_KHI);
//        BANH_CHUNG.add(5, ItemName.HOA_TUYET);
//        BANH_CHUNG.add(40, ItemName.IK);
//        BANH_CHUNG.add(20, ItemName.HUYEN_TINH_NGOC);
//        BANH_CHUNG.add(20, ItemName.HUYET_NGOC);
//        BANH_CHUNG.add(20, ItemName.LAM_TINH_NGOC);
//        BANH_CHUNG.add(20, ItemName.LUC_NGOC);
//        BANH_CHUNG.add(20, ItemName.XE_MAY);
//        BANH_CHUNG.add(20, ItemName.MAT_NA_SUPER_BROLY);
//        BANH_CHUNG.add(20, ItemName.MAT_NA_ONNA_BUGEISHA);
//        BANH_CHUNG.add(40, ItemName.XICH_NHAN_NGAN_LANG);
//        BANH_CHUNG.add(40, ItemName.BANH_RANG);
//        BANH_CHUNG.add(45, ItemName.LONG_DEN_TRON);
//        BANH_CHUNG.add(45, ItemName.LONG_DEN_CA_CHEP);
//        BANH_CHUNG.add(45, ItemName.LONG_DEN_NGOI_SAO);
//        BANH_CHUNG.add(45, ItemName.LONG_DEN_MAT_TRANG);
//        BANH_CHUNG.add(50, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BANH_CHUNG.add(30, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BANH_CHUNG.add(40, ItemName.DA_CAP_9);
//        BANH_CHUNG.add(20, ItemName.DA_CAP_10);
//        BANH_CHUNG.add(45, ItemName.MINH_MAN_DAN);
//        BANH_CHUNG.add(45, ItemName.LONG_LUC_DAN);
//        BANH_CHUNG.add(45, ItemName.KHANG_THE_DAN);
//        BANH_CHUNG.add(45, ItemName.SINH_MENH_DAN);
//        BANH_CHUNG.add(3, ItemName.BI_KIP_CUNG);
//        BANH_CHUNG.add(3, ItemName.BI_KIP_DAO);
//        BANH_CHUNG.add(3, ItemName.BI_KIP_KIEM_THUAT);
//        BANH_CHUNG.add(3, ItemName.BI_KIP_KUNAI);
//        BANH_CHUNG.add(3, ItemName.BI_KIP_QUAT);
//        BANH_CHUNG.add(3, ItemName.BI_KIP_TIEU_THUAT);
//        BANH_CHUNG.add(10, ItemName.GIAY_RACH);
//        BANH_CHUNG.add(6, ItemName.GIAY_BAC);
//        BANH_CHUNG.add(3, ItemName.GIAY_VANG);
//        BANH_CHUNG.add(10, ItemName.HAC_NGUU);
//        BANH_CHUNG.add(3, ItemName.PET_UNG_LONG);
//        BANH_CHUNG.add(3, ItemName.BACH_HO);
//        BANH_CHUNG.add(3, ItemName.LAN_SU_VU);
//        BANH_CHUNG.add(40, ItemName.THONG_LINH_THAO);

        // Bánh tét
//        BANH_TET.add(0.01, ItemName.RUONG_BACH_NGAN);
//        BANH_TET.add(0.1, ItemName.BAT_BAO);
//        BANH_TET.add(2, ItemName.LONG_KHI);
//        BANH_TET.add(5, ItemName.HOA_TUYET);
//        BANH_TET.add(25, ItemName.HUYEN_TINH_NGOC);
//        BANH_TET.add(25, ItemName.HUYET_NGOC);
//        BANH_TET.add(25, ItemName.LAM_TINH_NGOC);
//        BANH_TET.add(25, ItemName.LUC_NGOC);
//        BANH_TET.add(10, ItemName.XE_MAY);
//        BANH_TET.add(30, ItemName.XICH_NHAN_NGAN_LANG);
//        BANH_TET.add(50, ItemName.LONG_DEN_TRON);
//        BANH_TET.add(50, ItemName.LONG_DEN_CA_CHEP);
//        BANH_TET.add(50, ItemName.LONG_DEN_NGOI_SAO);
//        BANH_TET.add(50, ItemName.LONG_DEN_MAT_TRANG);
//        BANH_TET.add(60, ItemName.BANH_RANG);
//        BANH_TET.add(80, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        BANH_TET.add(60, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        BANH_TET.add(90, ItemName.DA_CAP_9);
//        BANH_TET.add(30, ItemName.DA_CAP_10);
//        BANH_TET.add(80, ItemName.MINH_MAN_DAN);
//        BANH_TET.add(80, ItemName.LONG_LUC_DAN);
//        BANH_TET.add(80, ItemName.KHANG_THE_DAN);
//        BANH_TET.add(80, ItemName.SINH_MENH_DAN);
//        BANH_TET.add(3, ItemName.BI_KIP_CUNG);
//        BANH_TET.add(3, ItemName.BI_KIP_DAO);
//        BANH_TET.add(3, ItemName.BI_KIP_KIEM_THUAT);
//        BANH_TET.add(3, ItemName.BI_KIP_KUNAI);
//        BANH_TET.add(3, ItemName.BI_KIP_QUAT);
//        BANH_TET.add(3, ItemName.BI_KIP_TIEU_THUAT);
//        BANH_TET.add(10, ItemName.GIAY_RACH);

        // Bùa may mắn
//        BUA_MAY_MAN.add(0.005, ItemName.RUONG_HUYEN_BI);
//        BUA_MAY_MAN.add(0.01, ItemName.RUONG_BACH_NGAN);
//        BUA_MAY_MAN.add(0.05, ItemName.BAT_BAO);
//        BUA_MAY_MAN.add(0.1, ItemName.LONG_KHI);
//        BUA_MAY_MAN.add(0.5, ItemName.HOA_TUYET);
//        BUA_MAY_MAN.add(1, ItemName.BANH_TRUNG_THU_BANG_HOA);
//        BUA_MAY_MAN.add(1, ItemName.BANH_TRUNG_THU_PHONG_LOI);
//        BUA_MAY_MAN.add(5, ItemName.THUOC_CAI_TIEN);
//        BUA_MAY_MAN.add(5, ItemName.XE_MAY);
//        BUA_MAY_MAN.add(25, ItemName.XICH_NHAN_NGAN_LANG);
//        BUA_MAY_MAN.add(16, ItemName.HUYEN_TINH_NGOC);
//        BUA_MAY_MAN.add(16, ItemName.HUYET_NGOC);
//        BUA_MAY_MAN.add(16, ItemName.LAM_TINH_NGOC);
//        BUA_MAY_MAN.add(16, ItemName.LUC_NGOC);
//        BUA_MAY_MAN.add(20, ItemName.MAT_NA_SUPER_BROLY);
//        BUA_MAY_MAN.add(20, ItemName.MAT_NA_ONNA_BUGEISHA);
//        BUA_MAY_MAN.add(30, ItemName.BANH_RANG);
//        BUA_MAY_MAN.add(40, ItemName.DA_CAP_8);
//        BUA_MAY_MAN.add(40, ItemName.DA_CAP_9);
//        BUA_MAY_MAN.add(20, ItemName.DA_CAP_10);
//        BUA_MAY_MAN.add(35, ItemName.LONG_DEN_TRON);
//        BUA_MAY_MAN.add(35, ItemName.LONG_DEN_CA_CHEP);
//        BUA_MAY_MAN.add(40, ItemName.MINH_MAN_DAN);
//        BUA_MAY_MAN.add(40, ItemName.LONG_LUC_DAN);
//        BUA_MAY_MAN.add(40, ItemName.KHANG_THE_DAN);
//        BUA_MAY_MAN.add(40, ItemName.SINH_MENH_DAN);
//        BUA_MAY_MAN.add(20, ItemName.DA_DANH_VONG_CAP_1);
//        BUA_MAY_MAN.add(15, ItemName.DA_DANH_VONG_CAP_2);
//        BUA_MAY_MAN.add(10, ItemName.DA_DANH_VONG_CAP_3);
//        BUA_MAY_MAN.add(8, ItemName.DA_DANH_VONG_CAP_4);
//        BUA_MAY_MAN.add(5, ItemName.DA_DANH_VONG_CAP_5);
//        BUA_MAY_MAN.add(20, ItemName.VIEN_LINH_HON_CAP_1);
//        BUA_MAY_MAN.add(15, ItemName.VIEN_LINH_HON_CAP_2);
//        BUA_MAY_MAN.add(8, ItemName.VIEN_LINH_HON_CAP_3);
//        BUA_MAY_MAN.add(4, ItemName.VIEN_LINH_HON_CAP_4);
//        BUA_MAY_MAN.add(2, ItemName.VIEN_LINH_HON_CAP_5);
//        BUA_MAY_MAN.add(5, ItemName.BI_KIP_CUNG);
//        BUA_MAY_MAN.add(5, ItemName.BI_KIP_DAO);
//        BUA_MAY_MAN.add(5, ItemName.BI_KIP_KIEM_THUAT);
//        BUA_MAY_MAN.add(5, ItemName.BI_KIP_KUNAI);
//        BUA_MAY_MAN.add(5, ItemName.BI_KIP_QUAT);
//        BUA_MAY_MAN.add(5, ItemName.BI_KIP_TIEU_THUAT);
//        BUA_MAY_MAN.add(10, ItemName.GIAY_RACH);
//        BUA_MAY_MAN.add(6, ItemName.GIAY_BAC);
//        BUA_MAY_MAN.add(3, ItemName.GIAY_VANG);
//        BUA_MAY_MAN.add(3, ItemName.HAGGIS);
//        BUA_MAY_MAN.add(10, ItemName.HAC_NGUU);
        // BUA_MAY_MAN.add(5, ItemName.PET_UNG_LONG);
        // BUA_MAY_MAN.add(5, ItemName.BACH_HO);
//        BUA_MAY_MAN.add(5, ItemName.LAN_SU_VU);
        // BUA_MAY_MAN.add(3, ItemName.PET_BONG_MA);
        // BUA_MAY_MAN.add(3, ItemName.PET_YEU_TINH);
        // BUA_MAY_MAN.add(3, ItemName.MAT_NA_KUMA);
        // BUA_MAY_MAN.add(3, ItemName.MAT_NA_INU);
//        BUA_MAY_MAN.add(20, ItemName.THONG_LINH_THAO);

//        BUA_MAY_MAN.add(10, ItemName.MANH_NON_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_AO_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_QUAN_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_GANG_TAY_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_GIAY_JIRAI_);
//
//        BUA_MAY_MAN.add(10, ItemName.MANH_PHU_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_DAY_CHUYEN_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_NGOC_BOI_JIRAI_);
//        BUA_MAY_MAN.add(10, ItemName.MANH_NHAN_JIRAI_);
//
//        BUA_MAY_MAN.add(10, ItemName.MANH_NON_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_AO_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_QUAN_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_GANG_TAY_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_GIAY_JUMITO);
//
//        BUA_MAY_MAN.add(10, ItemName.MANH_PHU_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_DAY_CHUYEN_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_NGOC_BOI_JUMITO);
//        BUA_MAY_MAN.add(10, ItemName.MANH_NHAN_JUMITO);

        // Bánh chưng
//        TRE_VANG_TRAM_DOT.add(30, ItemName.HOAN_LUONG_CHI_THAO);
//        TRE_VANG_TRAM_DOT.add(0.01, ItemName.RUONG_HUYEN_BI);
//        TRE_VANG_TRAM_DOT.add(0.05, ItemName.RUONG_BACH_NGAN);
//        TRE_VANG_TRAM_DOT.add(0.1, ItemName.BAT_BAO);
//        TRE_VANG_TRAM_DOT.add(2, ItemName.LONG_KHI);

        if (Event.isTrungThu()) {
//            TRE_VANG_TRAM_DOT.add(1, ItemName.BANH_TRUNG_THU_BANG_HOA);
//            TRE_VANG_TRAM_DOT.add(1, ItemName.BANH_TRUNG_THU_PHONG_LOI);
        }

//        TRE_VANG_TRAM_DOT.add(10, ItemName.HOA_TUYET);
//        TRE_VANG_TRAM_DOT.add(40, ItemName.IK);
//        TRE_VANG_TRAM_DOT.add(25, ItemName.HUYEN_TINH_NGOC);
//        TRE_VANG_TRAM_DOT.add(25, ItemName.HUYET_NGOC);
//        TRE_VANG_TRAM_DOT.add(25, ItemName.LAM_TINH_NGOC);
//        TRE_VANG_TRAM_DOT.add(25, ItemName.LUC_NGOC);
//        TRE_VANG_TRAM_DOT.add(20, ItemName.XE_MAY);
//        TRE_VANG_TRAM_DOT.add(20, ItemName.MAT_NA_SUPER_BROLY);
//        TRE_VANG_TRAM_DOT.add(20, ItemName.MAT_NA_ONNA_BUGEISHA);
//        TRE_VANG_TRAM_DOT.add(40, ItemName.XICH_NHAN_NGAN_LANG);
//        TRE_VANG_TRAM_DOT.add(40, ItemName.BANH_RANG);
//        TRE_VANG_TRAM_DOT.add(45, ItemName.LONG_DEN_TRON);
//        TRE_VANG_TRAM_DOT.add(45, ItemName.LONG_DEN_CA_CHEP);
//        TRE_VANG_TRAM_DOT.add(40, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        TRE_VANG_TRAM_DOT.add(30, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        TRE_VANG_TRAM_DOT.add(40, ItemName.DA_CAP_9);
//        TRE_VANG_TRAM_DOT.add(20, ItemName.DA_CAP_10);
//        TRE_VANG_TRAM_DOT.add(50, ItemName.MINH_MAN_DAN);
//        TRE_VANG_TRAM_DOT.add(50, ItemName.LONG_LUC_DAN);
//        TRE_VANG_TRAM_DOT.add(50, ItemName.KHANG_THE_DAN);
//        TRE_VANG_TRAM_DOT.add(50, ItemName.SINH_MENH_DAN);
//        TRE_VANG_TRAM_DOT.add(3, ItemName.BI_KIP_CUNG);
//        TRE_VANG_TRAM_DOT.add(3, ItemName.BI_KIP_DAO);
//        TRE_VANG_TRAM_DOT.add(3, ItemName.BI_KIP_KIEM_THUAT);
//        TRE_VANG_TRAM_DOT.add(3, ItemName.BI_KIP_KUNAI);
//        TRE_VANG_TRAM_DOT.add(3, ItemName.BI_KIP_QUAT);
//        TRE_VANG_TRAM_DOT.add(3, ItemName.BI_KIP_TIEU_THUAT);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.HAC_NGUU);
//        TRE_VANG_TRAM_DOT.add(40, ItemName.THONG_LINH_THAO);
        // TRE_VANG_TRAM_DOT.add(2, ItemName.BACH_HO);
        // TRE_VANG_TRAM_DOT.add(2, ItemName.PET_UNG_LONG);
        // TRE_VANG_TRAM_DOT.add(3, ItemName.MAT_NA_KUMA);
        // TRE_VANG_TRAM_DOT.add(3, ItemName.MAT_NA_INU);
        // TRE_VANG_TRAM_DOT.add(10, ItemName.KHAU_TRANG);
        // TRE_VANG_TRAM_DOT.add(3, ItemName.PET_BONG_MA);
        // TRE_VANG_TRAM_DOT.add(3, ItemName.PET_YEU_TINH);
//        TRE_VANG_TRAM_DOT.add(2, ItemName.HAKAIRO_YOROI);
//
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_NON_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_AO_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_QUAN_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_GANG_TAY_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_GIAY_JIRAI_);
//
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_PHU_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_DAY_CHUYEN_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_NGOC_BOI_JIRAI_);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_NHAN_JIRAI_);
//
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_NON_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_AO_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_QUAN_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_GANG_TAY_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_GIAY_JUMITO);
//
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_PHU_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_DAY_CHUYEN_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_NGOC_BOI_JUMITO);
//        TRE_VANG_TRAM_DOT.add(10, ItemName.MANH_NHAN_JUMITO);
        if (Event.isTrungThu()) {
//            TRE_VANG_TRAM_DOT.add(10, ItemName.MAT_NA_THO);
//            TRE_VANG_TRAM_DOT.add(10, ItemName.MAT_NA_THO_NU);
        }

        // Bánh tét
//        TRE_XANH_TRAM_DOT.add(0.01, ItemName.RUONG_BACH_NGAN);
//        TRE_XANH_TRAM_DOT.add(0.1, ItemName.BAT_BAO);
//        TRE_XANH_TRAM_DOT.add(2, ItemName.LONG_KHI);
//        TRE_XANH_TRAM_DOT.add(10, ItemName.HOA_TUYET);
//        TRE_XANH_TRAM_DOT.add(15, ItemName.HUYEN_TINH_NGOC);
//        TRE_XANH_TRAM_DOT.add(15, ItemName.HUYET_NGOC);
//        TRE_XANH_TRAM_DOT.add(15, ItemName.LAM_TINH_NGOC);
//        TRE_XANH_TRAM_DOT.add(15, ItemName.LUC_NGOC);
//        TRE_XANH_TRAM_DOT.add(15, ItemName.XE_MAY);
//        TRE_XANH_TRAM_DOT.add(30, ItemName.XICH_NHAN_NGAN_LANG);
//        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_TRON);
//        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_CA_CHEP);
//        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_NGOI_SAO);
//        TRE_XANH_TRAM_DOT.add(70, ItemName.LONG_DEN_MAT_TRANG);
//        TRE_XANH_TRAM_DOT.add(60, ItemName.BANH_RANG);
//        TRE_XANH_TRAM_DOT.add(80, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO);
//        TRE_XANH_TRAM_DOT.add(40, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        TRE_XANH_TRAM_DOT.add(80, ItemName.DA_CAP_9);
//        TRE_XANH_TRAM_DOT.add(30, ItemName.DA_CAP_10);
//        TRE_XANH_TRAM_DOT.add(80, ItemName.MINH_MAN_DAN);
//        TRE_XANH_TRAM_DOT.add(80, ItemName.LONG_LUC_DAN);
//        TRE_XANH_TRAM_DOT.add(80, ItemName.KHANG_THE_DAN);
//        TRE_XANH_TRAM_DOT.add(80, ItemName.SINH_MENH_DAN);
//        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_CUNG);
//        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_DAO);
//        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_KIEM_THUAT);
//        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_KUNAI);
//        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_QUAT);
//        TRE_XANH_TRAM_DOT.add(3, ItemName.BI_KIP_TIEU_THUAT);
//        TRE_XANH_TRAM_DOT.add(20, ItemName.HOAN_LUONG_CHI_THAO);
//        TRE_XANH_TRAM_DOT.add(10, ItemName.GIAY_RACH);
        if (Event.isTrungThu()) {
//            TRE_XANH_TRAM_DOT.add(1, ItemName.MAT_NA_THO);
//            TRE_XANH_TRAM_DOT.add(1, ItemName.MAT_NA_THO_NU);
        }

//        THANH_VAT.add(1, ItemName.MAT_NA_HO);
//        THANH_VAT.add(1, ItemName.LAN_SU_VU);
//        THANH_VAT.add(1, ItemName.MAT_NA_SUPER_BROLY);
//        THANH_VAT.add(1, ItemName.MAT_NA_ONNA_BUGEISHA);
//        THANH_VAT.add(1, ItemName.PHUONG_HOANG_BANG);
//        THANH_VAT.add(1, ItemName.PET_UNG_LONG);
//
//        CUP_VANG.add(1, ItemName.MAT_NA_HO);
//        CUP_VANG.add(1, ItemName.LAN_SU_VU);
//        CUP_VANG.add(1, ItemName.MAT_NA_INU);
//        CUP_VANG.add(1, ItemName.MAT_NA_KUMA);
//        CUP_VANG.add(1, ItemName.PHUONG_HOANG_BANG);
//        CUP_VANG.add(1, ItemName.PET_BORU);

        // LINH_VAT.add(1, ItemName.MAT_NA_HO);
//        LINH_VAT.add(1, ItemName.LAN_SU_VU);
//        LINH_VAT.add(1, ItemName.THIEN_NGUYET_CHI_NU);
//        LINH_VAT.add(1, ItemName.NHAT_TU_LAM_PHONG);
//        LINH_VAT.add(1, ItemName.PHUONG_HOANG_BANG);
//        // LINH_VAT.add(1, ItemName.PET_UNG_LONG);
//        LINH_VAT.add(0.8, ItemName.PET_BONG_MA);
//        LINH_VAT.add(0.8, ItemName.PET_YEU_TINH);
//        LINH_VAT.add(0.5, ItemName.HAKAIRO_YOROI);
//
//        BOSS_LDGT.add(30, ItemName.DA_CAP_8);
//        BOSS_LDGT.add(20, ItemName.DA_CAP_9);
//        BOSS_LDGT.add(10, ItemName.DA_CAP_10);

        // truong linh hon
//        BOSS_LDGT.add(50, 893);
//        BOSS_LDGT.add(10, 890);
//        BOSS_LDGT.add(10, 891);
//        BOSS_LDGT.add(10, 892);
//
//        BOSS_LDGT.add(10, 880);
//        BOSS_LDGT.add(8, 881);
//        BOSS_LDGT.add(6, 882);
//        BOSS_LDGT.add(2, 883);
//        BOSS_LDGT.add(1, 884);
//
//        LANH_DIA_GIA_TOC.add(100, 893);
//        LANH_DIA_GIA_TOC.add(10, 890);
//        LANH_DIA_GIA_TOC.add(10, 891);
//        LANH_DIA_GIA_TOC.add(10, 892);
//
//        LANH_DIA_GIA_TOC.add(10, 880);
//        LANH_DIA_GIA_TOC.add(7, 881);
//        LANH_DIA_GIA_TOC.add(5, 882);
//        LANH_DIA_GIA_TOC.add(2, 883);
//        LANH_DIA_GIA_TOC.add(1, 884);

        NUOC_DIET_KHUAN.add(1, ItemName.PHUC_NANG_NHAN_GIA);

//        THAT_THU_BAO.add(0.5, ItemName.BAT_BAO);
//        THAT_THU_BAO.add(0.1, ItemName.RUONG_BACH_NGAN);
//        THAT_THU_BAO.add(15, ItemName.DA_CAP_8);
//        THAT_THU_BAO.add(10, ItemName.DA_CAP_9);
//        THAT_THU_BAO.add(8, ItemName.DA_CAP_10);
//        THAT_THU_BAO.add(6, ItemName.DA_CAP_11);
//        THAT_THU_BAO.add(5, ItemName.DA_CAP_12);
//        THAT_THU_BAO.add(15, ItemName.MINH_MAN_DAN);
//        THAT_THU_BAO.add(15, ItemName.LONG_LUC_DAN);
//        THAT_THU_BAO.add(15, ItemName.KHANG_THE_DAN);
//        THAT_THU_BAO.add(15, ItemName.SINH_MENH_DAN);

//        LONG_DEN.add(30, ItemName.DA_CAP_5);
//        LONG_DEN.add(25, ItemName.DA_CAP_6);
//        LONG_DEN.add(15, ItemName.DA_CAP_7);
//        LONG_DEN.add(10, ItemName.DA_CAP_8);
//
//        LONG_DEN.add(1, ItemName.BI_KIP_CUNG);
//        LONG_DEN.add(1, ItemName.BI_KIP_DAO);
//        LONG_DEN.add(1, ItemName.BI_KIP_KIEM_THUAT);
//        LONG_DEN.add(1, ItemName.BI_KIP_KUNAI);
//        LONG_DEN.add(1, ItemName.BI_KIP_QUAT);
//        LONG_DEN.add(1, ItemName.BI_KIP_TIEU_THUAT);
//
//        LONG_DEN.add(10, ItemName.GA_TAY);
//        LONG_DEN.add(10, ItemName.TOM_HUM);
//        LONG_DEN.add(5, ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG);
//        LONG_DEN.add(5, ItemName.DIA_LANG_THAO);
//        LONG_DEN.add(5, ItemName.TAM_LUC_DIEP);
//        LONG_DEN.add(0.1, ItemName.CHIM_TINH_ANH);
//        LONG_DEN.add(10, ItemName.HOAN_COT_CHI_CHU_SO_CAP);
//        LONG_DEN.add(1, ItemName.BAO_HIEM_TRUNG_CAP);
//        LONG_DEN.add(1, ItemName.BAO_HIEM_CAO_CAP);
//
//        LONG_DEN.add(1, ItemName.LINH_LANG_HO_DIEP);
//        LONG_DEN.add(1, ItemName.CHUYEN_TINH_THACH);
//        LONG_DEN.add(1, ItemName.BO_CAI_THIEN_GIAM_XOC);
//        LONG_DEN.add(1, ItemName.BO_CAI_THIEN_DONG_CO);
//        LONG_DEN.add(1, ItemName.BO_CAI_THIEN_DANH_LUA);
//        LONG_DEN.add(0.1, ItemName.KHI_BAO);
//        LONG_DEN.add(0.1, ItemName.LANG_BAO);
//        LONG_DEN.add(0.05, ItemName.XE_MAY);
//        LONG_DEN.add(0.1, ItemName.LONG_DEN_CA_CHEP);
//        LONG_DEN.add(0.1, ItemName.LONG_DEN_TRON);
//        LONG_DEN.add(0.01, ItemName.LONG_DEN_NGOI_SAO);
//        LONG_DEN.add(1, ItemName.BANH_RANG);
//        LONG_DEN.add(1, ItemName.IK);
//        LONG_DEN.add(1, ItemName.THUOC_CAI_TIEN);
//        LONG_DEN.add(0.001, ItemName.HARLEY_DAVIDSON);
//        LONG_DEN.add(0.001, ItemName.THAI_DUONG_VO_CUC_KIEM);
//        LONG_DEN.add(0.001, ItemName.THAI_DUONG_TANG_HON_DAO);
//        LONG_DEN.add(0.001, ItemName.THAI_DUONG_CHIEN_LUC_DAO);
//        LONG_DEN.add(0.001, ItemName.THAI_DUONG_HOANG_PHONG_PHIEN);
//        LONG_DEN.add(0.001, ItemName.THAI_DUONG_THIEN_HOA_TIEU);
//        LONG_DEN.add(0.001, ItemName.THAI_DUONG_BANG_THAN_CUNG);
        
        loadItems("item_roi/loai_khac/RUONG_NGOC.json", "RUONG_NGOC");
        loadItems("item_roi/loai_khac/LANG_BAO.json", "LANG_BAO");
        loadItems("item_roi/loai_khac/KHI_BAO.json", "KHI_BAO");
        loadItems("item_roi/loai_khac/VUA_TUAN_LOC.json", "VUA_TUAN_LOC");
        loadItems("item_roi/loai_khac/SACH_VO_CONG_120.json", "SACH_VO_CONG_120");
        loadItems("item_roi/loai_khac/RUONG_CHIEN_TRUONG.json", "RUONG_CHIEN_TRUONG");
        loadItems("item_roi/loai_khac/RUONG_HAC_AM.json", "RUONG_HAC_AM");
        loadItems("item_roi/loai_khac/LINH_VAT.json", "LINH_VAT");
        loadItems("item_roi/loai_khac/THAT_THU_BAO.json", "THAT_THU_BAO");

        loadItems("item_roi/event_SumMer/SUMMER.json", "SUMMER");
        loadItems("item_roi/event_SumMer/DIEU_VAI.json", "DIEU_VAI");
        loadItems("item_roi/event_SumMer/DIEU_GIAY.json", "DIEU_GIAY");

        loadItems("item_roi/map_langtruyenthuyet/BOSS_LANG_TRUYEN_THUYET.json", "BOSS_LANG_TRUYEN_THUYET");
        loadItems("item_roi/map_langtruyenthuyet/LANG_TRUYEN_THUYET.json", "LANG_TRUYEN_THUYET");

        loadItems("item_roi/map_langco/BOSS_LANG_CO.json", "BOSS_LANG_CO");
        loadItems("item_roi/map_langco/LANG_CO.json", "LANG_CO");

        loadItems("item_roi/event_Noel/NOEL.json", "NOEL");
        loadItems("item_roi/event_Noel/BANH_KHUC_CAY_CHOCOLATE.json", "BANH_KHUC_CAY_CHOCOLATE");
        loadItems("item_roi/event_Noel/BANH_KHUC_CAY_DAU_TAY.json", "BANH_KHUC_CAY_DAU_TAY");
        loadItems("item_roi/event_Noel/DOI_DIEM_NGUOI_TUYET_LUONG.json", "DOI_DIEM_NGUOI_TUYET_LUONG");
        loadItems("item_roi/event_Noel/DOI_DIEM_NGUOI_TUYET_XU.json", "DOI_DIEM_NGUOI_TUYET_XU");
        loadItems("item_roi/event_Noel/HOP_QUA_TRANG_TRI.json", "HOP_QUA_TRANG_TRI");
        loadItems("item_roi/event_Noel/QUA_TRANG_TRI.json", "QUA_TRANG_TRI");

        loadItems("item_roi/map_thuong/MAP.json", "MAP");
        loadItems("item_roi/map_thuong/BOSS.json", "BOSS");
        loadItems("item_roi/map_thuong/BOSS_EVENT.json", "BOSS_EVENT");
        loadItems("item_roi/map_thuong/UPYEN.json", "UPYEN");
        loadItems("item_roi/map_thuong/UPluong.json", "UPluong");
        loadItems("item_roi/map_thuong/MAP_VITHU.json", "MAP_VITHU");

        loadItems("item_roi/event_LunarNewYear/TET.json", "TET");
        loadItems("item_roi/event_LunarNewYear/BANH_CHUNG.json", "BANH_CHUNG");
        loadItems("item_roi/event_LunarNewYear/BANH_TET.json", "BANH_TET");
        loadItems("item_roi/event_LunarNewYear/BUA_MAY_MAN.json", "BUA_MAY_MAN");

        loadItems("item_roi/event_TrungThu/TRUNG_THU.json", "TRUNG_THU");
        loadItems("item_roi/event_TrungThu/HOP_BANH_THUONG.json", "HOP_BANH_THUONG");
        loadItems("item_roi/event_TrungThu/HOP_BANH_THUONG_HANG.json", "HOP_BANH_THUONG_HANG");
        loadItems("item_roi/event_TrungThu/LONG_DEN.json", "LONG_DEN");

        loadItems("item_roi/map_VDMQ/VDMQ.json", "VDMQ");
        loadItems("item_roi/map_VDMQ/BOSS_VDMQ.json", "BOSS_VDMQ");

        loadItems("item_roi/event_Halloween/Halloween.json", "Halloween");
        loadItems("item_roi/event_Halloween/HOP_MA_QUY.json", "HOP_MA_QUY");
        loadItems("item_roi/event_Halloween/KEO_TAO.json", "KEO_TAO");

        loadItems("item_roi/map_LDGT/BOSS_LDGT.json", "BOSS_LDGT");
        loadItems("item_roi/map_LDGT/LDGT.json", "LDGT");
    }
    
    private static void loadItems(String filename, String itemType) {
        StringBuilder objStr = new StringBuilder();
        try (FileReader frd = new FileReader(filename); BufferedReader brd = new BufferedReader(frd)) {
            String line;
            while ((line = brd.readLine()) != null) {
                objStr.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        clear(itemType);

        if (itemType.equals("Lat_Hinh_Thuong")) {
            SelectCard.getInstance().init();
        } 
//        else 
//            if (itemType.equals("Lat_Hinh_VIP")) {
//            SelectCardVIP.getInstance().init();
//        } 
           else {
            try {
                JSONArray js = (JSONArray) JSONValue.parse(objStr.toString());
                for (int i = 0; i < js.size(); i++) {
                    JSONObject job1 = (JSONObject) JSONValue.parse(js.get(i).toString());
                    double percent = Double.parseDouble(job1.get("percent").toString());
                    int id = Integer.parseInt(job1.get("id").toString());

                    switch (itemType) {
                        case "RUONG_NGOC" ->
                            RUONG_NGOC.add(percent, id);
                        case "SUMMER" ->
                            SUMMER.add(percent, id);
                        case "DIEU_VAI" ->
                            DIEU_VAI.add(percent, id);
                        case "DIEU_GIAY" ->
                            DIEU_GIAY.add(percent, id);
                        case "BOSS_LANG_CO" ->
                            BOSS_LANG_CO.add(percent, id);
                        case "LANG_CO" ->
                            LANG_CO.add(percent, id);
                        case "NOEL" ->
                            NOEL.add(percent, id);
                        case "BANH_KHUC_CAY_CHOCOLATE" ->
                            BANH_KHUC_CAY_CHOCOLATE.add(percent, id);
                        case "BANH_KHUC_CAY_DAU_TAY" ->
                            BANH_KHUC_CAY_DAU_TAY.add(percent, id);
                        case "MAP" ->
                            ITEM.add(percent, id);
                        case "BOSS" ->
                            BOSS.add(percent, id);
                        case "BOSS_EVENT" ->
                            BOSS_EVENT.add(percent, id);
                        case "UPYEN" ->
                            UPYEN.add(percent, id);
                        case "UPluong" ->
                            UPluong.add(percent, id);
                        case "MAP_VITHU" ->
                            vt.add(percent, id);
                        case "TET" ->
                            TET.add(percent, id);
                        case "BANH_CHUNG" ->
                            BANH_CHUNG.add(percent, id);
                        case "BANH_TET" ->
                            BANH_TET.add(percent, id);
                        case "TRUNG_THU" ->
                            TRUNG_THU.add(percent, id);
                        case "HOP_BANH_THUONG" ->
                            HOP_BANH_THUONG.add(percent, id);
                        case "HOP_BANH_THUONG_HANG" ->
                            HOP_BANH_THUONG_HANG.add(percent, id);
                        case "VDMQ" ->
                            VDMQ.add(percent, id);
                        case "BOSS_VDMQ" ->
                            BOSS_VDMQ.add(percent, id);
                        case "KHI_BAO" ->
                            KHI_BAO.add(percent, id);
                        case "LANG_BAO" ->
                            LANG_BAO.add(percent, id);
                        case "VUA_TUAN_LOC" ->
                            VUA_TUAN_LOC.add(percent, id);
                        case "DOI_DIEM_NGUOI_TUYET_XU" ->
                            DOI_DIEM_NGUOI_TUYET_XU.add(percent, id);
                        case "DOI_DIEM_NGUOI_TUYET_LUONG" ->
                            DOI_DIEM_NGUOI_TUYET_LUONG.add(percent, id);
                        case "HOP_QUA_TRANG_TRI" ->
                            HOP_QUA_TRANG_TRI.add(percent, id);
                        case "QUA_TRANG_TRI" ->
                            QUA_TRANG_TRI.add(percent, id);
                        case "SACH_VO_CONG_120" ->
                            SACH_VO_CONG_120.add(percent, id);
                        case "RUONG_CHIEN_TRUONG" ->
                            RUONG_CHIEN_TRUONG.add(percent, id);
                        case "RUONG_HAC_AM" ->
                            RUONG_HAC_AM.add(percent, id);
                        case "BUA_MAY_MAN" ->
                            BUA_MAY_MAN.add(percent, id);
                        case "LINH_VAT" ->
                            LINH_VAT.add(percent, id);
                        case "THAT_THU_BAO" ->
                            THAT_THU_BAO.add(percent, id);
                        case "LONG_DEN" ->
                            LONG_DEN.add(percent, id);
                        case "Halloween" ->
                            Halloween.add(percent, id);
                        case "HOP_MA_QUY" ->
                            HOP_MA_QUY.add(percent, id);
                        case "KEO_TAO" ->
                            KEO_TAO.add(percent, id);
                        case "BOSS_LDGT" ->
                            BOSS_LDGT.add(percent, id);
                        case "LDGT" ->
                            LDGT.add(percent, id);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear(String type) {
        switch (type) {
            case "RUONG_NGOC" ->
                RUONG_NGOC = new RandomCollection<>();
            case "SUMMER" ->
                SUMMER = new RandomCollection<>();
            case "DIEU_VAI" ->
                DIEU_VAI = new RandomCollection<>();
            case "DIEU_GIAY" ->
                DIEU_GIAY = new RandomCollection<>();
            case "BOSS_LANG_CO" ->
                BOSS_LANG_CO = new RandomCollection<>();
            case "LANG_CO" ->
                LANG_CO = new RandomCollection<>();
            case "NOEL" ->
                NOEL = new RandomCollection<>();
            case "BANH_KHUC_CAY_CHOCOLATE" ->
                BANH_KHUC_CAY_CHOCOLATE = new RandomCollection<>();
            case "BANH_KHUC_CAY_DAU_TAY" ->
                BANH_KHUC_CAY_DAU_TAY = new RandomCollection<>();
            case "MAP" ->
                ITEM = new RandomCollection<>();
            case "BOSS" ->
                BOSS = new RandomCollection<>();
            case "BOSS_EVENT" ->
                BOSS_EVENT = new RandomCollection<>();
            case "UPYEN" ->
                UPYEN = new RandomCollection<>();
            case "UPluong" ->
                UPluong = new RandomCollection<>();
            case "MAP_VITHU" ->
                vt = new RandomCollection<>();
            case "TET" ->
                TET = new RandomCollection<>();
            case "BANH_CHUNG" ->
                BANH_CHUNG = new RandomCollection<>();
            case "BANH_TET" ->
                BANH_TET = new RandomCollection<>();
            case "TRUNG_THU" ->
                TRUNG_THU = new RandomCollection<>();
            case "HOP_BANH_THUONG" ->
                HOP_BANH_THUONG = new RandomCollection<>();
            case "HOP_BANH_THUONG_HANG" ->
                HOP_BANH_THUONG_HANG = new RandomCollection<>();
            case "VDMQ" ->
                VDMQ = new RandomCollection<>();
            case "BOSS_VDMQ" ->
                BOSS_VDMQ = new RandomCollection<>();
            case "KHI_BAO" ->
                KHI_BAO = new RandomCollection<>();
            case "LANG_BAO" ->
                LANG_BAO = new RandomCollection<>();
            case "VUA_TUAN_LOC" ->
                VUA_TUAN_LOC = new RandomCollection<>();
            case "DOI_DIEM_NGUOI_TUYET_XU" ->
                DOI_DIEM_NGUOI_TUYET_XU = new RandomCollection<>();
            case "DOI_DIEM_NGUOI_TUYET_LUONG" ->
                DOI_DIEM_NGUOI_TUYET_LUONG = new RandomCollection<>();
            case "HOP_QUA_TRANG_TRI" ->
                HOP_QUA_TRANG_TRI = new RandomCollection<>();
            case "QUA_TRANG_TRI" ->
                QUA_TRANG_TRI = new RandomCollection<>();
            case "SACH_VO_CONG_120" ->
                SACH_VO_CONG_120 = new RandomCollection<>();
            case "RUONG_CHIEN_TRUONG" ->
                RUONG_CHIEN_TRUONG = new RandomCollection<>();
            case "RUONG_HAC_AM" ->
                RUONG_HAC_AM = new RandomCollection<>();
            case "BUA_MAY_MAN" ->
                BUA_MAY_MAN = new RandomCollection<>();
            case "LINH_VAT" ->
                LINH_VAT = new RandomCollection<>();
            case "THAT_THU_BAO" ->
                THAT_THU_BAO = new RandomCollection<>();
            case "LONG_DEN" ->
                LONG_DEN = new RandomCollection<>();
            case "Halloween" ->
                Halloween = new RandomCollection<>();
            case "HOP_MA_QUY" ->
                HOP_MA_QUY = new RandomCollection<>();
            case "KEO_TAO" ->
                KEO_TAO = new RandomCollection<>();
            case "BOSS_LDGT" ->
                BOSS_LDGT= new RandomCollection<>();
            case "LDGT" ->
                LDGT = new RandomCollection<>();
            case "Lat_Hinh_Thuong" ->
                SelectCard.getInstance().cards = new RandomCollection<>();
//            case "Lat_Hinh_VIP" ->
//                SelectCardVIP.getInstance().cards = new RandomCollection<>();
        }
    }

    public static void abc(String url) {
        try {
            File file = new File(url);
            if (!file.isDirectory()) {
                return;
            }
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            long now = System.currentTimeMillis();
            long min_time = 1 * 60 * 1000;
            for (File file1 : files) {
                if (file1.isFile()) {
                    BasicFileAttributes fab = Files.readAttributes(file1.toPath(), BasicFileAttributes.class);
                    long time_edit = fab.lastModifiedTime().toMillis();
                    if (now - time_edit < min_time) {
                        loadItems(url + "/" + file1.getName(), file1.getName().split(".json")[0]);
                        System.out.println("LOAD OK " + file1.getName().split(".json")[0]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void useTest(RandomCollection<Integer> rc, int times) {
        HashMap<Integer, Integer> result = rc.test(times);
        for (Map.Entry<Integer, Integer> e : result.entrySet()) {
            ItemTemplate template = ItemManager.getInstance().getItemTemplate(e.getKey());
            int value = e.getValue();
            double rate = ((double) value / times * 100);
            Log.info(String.format("name: %s rate: %.4f%% (%d)", StringUtils.removeAccent(template.name), rate, value));
        }
    }

}
