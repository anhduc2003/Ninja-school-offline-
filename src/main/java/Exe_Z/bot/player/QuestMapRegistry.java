package Exe_Z.bot.player;

import Exe_Z.constants.MapName;
import Exe_Z.constants.TaskName;

import java.util.HashMap;
import java.util.Map;

public class QuestMapRegistry {

    private static final Map<Integer, int[]> QUEST_MAPS = new HashMap<>();

    static {
        QUEST_MAPS.put(TaskName.NV_DIET_SEN_TRU_COC, new int[]{MapName.DONG_HACHI, MapName.DONG_HACHI, MapName.DONG_HACHI});
        QUEST_MAPS.put(TaskName.NV_HAI_THUOC_CUU_NGUOI, new int[]{MapName.RUNG_TRUC_UTRA, MapName.RUNG_TRUC_UTRA, MapName.RUNG_TRUC_UTRA});
        QUEST_MAPS.put(TaskName.NV_KHAM_PHA_XA_LANG, new int[]{MapName.LANG_KOJIN, MapName.LANG_KOJIN, MapName.LANG_KOJIN});
        QUEST_MAPS.put(TaskName.NV_BAI_HOC_DAU_TIEN, new int[]{MapName.TRUONG_HIROSAKI, MapName.TRUONG_HIROSAKI, MapName.TRUONG_HIROSAKI});
        QUEST_MAPS.put(TaskName.NV_THU_THAP_NGUYEN_LIEU, new int[]{MapName.RUNG_DAO_SAKURA, MapName.RUNG_DAO_SAKURA, MapName.RUNG_DAO_SAKURA});
        QUEST_MAPS.put(TaskName.NV_TRUYEN_TAI_TIN_TUC, new int[]{MapName.LANG_KOJIN, MapName.LANG_KOJIN, MapName.LANG_KOJIN});
        QUEST_MAPS.put(TaskName.NV_REN_LUYEN_THE_LUC, new int[]{MapName.TRUONG_HARUNA, MapName.TRUONG_HARUNA, MapName.TRUONG_HARUNA});
        QUEST_MAPS.put(TaskName.NV_DUA_JAIAN_TRO_VE, new int[]{MapName.TRUONG_HARUNA, MapName.TRUONG_HARUNA, MapName.TRUONG_HARUNA});
        QUEST_MAPS.put(TaskName.NV_TIM_NGUYEN_LIEU_LAM_THUOC, new int[]{MapName.RUNG_MISHIMA, MapName.RUNG_MISHIMA, MapName.RUNG_MISHIMA});
        QUEST_MAPS.put(TaskName.NV_LAY_NUOC_HANG_SAU, new int[]{MapName.HANG_HA, MapName.HANG_HA, MapName.HANG_HA});
        QUEST_MAPS.put(TaskName.NV_VUOT_QUA_THU_THACH, new int[]{MapName.RUNG_DAO_SAKURA, MapName.RUNG_DAO_SAKURA, MapName.RUNG_DAO_SAKURA});
        QUEST_MAPS.put(TaskName.NV_THU_THAP_CHIA_KHOA, new int[]{MapName.HANG_MEIRO, MapName.HANG_MEIRO, MapName.HANG_MEIRO});
        QUEST_MAPS.put(TaskName.NV_TRUY_TIM_BAO_VAT, new int[]{MapName.HANG_MEIRO, MapName.HANG_MEIRO, MapName.HANG_MEIRO});
        QUEST_MAPS.put(TaskName.NV_REN_LUYEN, new int[]{MapName.RUNG_DAO_SAKURA, MapName.RUNG_DAO_SAKURA, MapName.RUNG_DAO_SAKURA});
        QUEST_MAPS.put(TaskName.NV_THU_THAP_TINH_THE_BANG, new int[]{MapName.HANG_HA, MapName.HANG_HA, MapName.HANG_HA});
        QUEST_MAPS.put(TaskName.NV_THU_THAP_XAC_DOI_LUA, new int[]{MapName.RUNG_TRUC_UTRA, MapName.RUNG_TRUC_UTRA, MapName.RUNG_TRUC_UTRA});
        QUEST_MAPS.put(TaskName.NV_KIEN_TRI_DIET_AC, new int[]{MapName.DONG_HACHI, MapName.DONG_HACHI, MapName.DONG_HACHI});
        QUEST_MAPS.put(TaskName.NV_DU_TRU_LUONG_THUC, new int[]{MapName.RUNG_MISHIMA, MapName.RUNG_MISHIMA, MapName.RUNG_MISHIMA});
        QUEST_MAPS.put(TaskName.NV_TUAN_HOAN, new int[]{MapName.HANG_HA, MapName.HANG_HA, MapName.HANG_HA});
    }

    public static int getMapId(int taskId, int index) {
        int[] maps = QUEST_MAPS.get(taskId);
        if (maps == null || maps.length == 0) {
            return -1;
        }
        if (index >= 0 && index < maps.length) {
            return maps[index];
        }
        return maps[0];
    }
}
