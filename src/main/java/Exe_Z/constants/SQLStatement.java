/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.constants;

/**
 *
 * @author Admin
 */
public class SQLStatement {

    public static final String GET_ALL_NPC = "SELECT * FROM `npc`;";
    public static final String GET_ALL_MONSTER = "SELECT * FROM `monster`;";
    public static final String GET_ALL_MAP = "SELECT * FROM `map`;";
    public static final String GET_ALL_TASK_TEMPLATE = "SELECT * FROM `task_template`;";
    public static final String LOAD_CLASS = "SELECT * FROM `clazz`;";
    public static final String LOAD_SKILL_TEMPLATE = "SELECT * FROM `skill_template` WHERE `class` = ?;";
    public static final String LOAD_SKILL = "SELECT * FROM `skill` WHERE `template_id` = ? ORDER BY `point` ASC;";
     public static final String UPDATE_TOP_VXMM = "UPDATE `players` SET `topvxmm` = ? WHERE `id` = ? LIMIT 1;";
    
    
    public static final String SAVE_DATA_PLAYER = "UPDATE `players` SET `xu` = ?, `xuInBox` = ?, `yen` = ?, `point` = ?, `spoint` = ?, `saveCoordinate` = ?, `numberCellBag` = ?, `numberCellBox` = ?, `last_logout_time` = ?, `clan` = ?, `taskId` = ?, `rewardPB` = ?, `message` = ?, `data` = ?, `skill` = ?, `potential` = ?, `map` = ?, `equiped` = ?, `bag` = ?, `box` = ?, `mount` = ?, `task` = ?, `fashion` = ?, `class` = ?, `effect` = ?, `friends` = ?, `head2` = ?, `weapon` = ?, `body` = ?, `leg` = ?, `enemies` = ?, `onCSkill` = ?, `onKSkill` = ?, `onOSkill` = ?, `mask_box` = ?, `collection_box` = ?, `bijuu` = ?, `online` = ?, `napdau` = ?, `fancung` = ?, `loantin` = ?, `rewardMOC` = ?,`topvxmm` = ?,`topBoss` = ?,`thannong` = ? WHERE `id` = ? LIMIT 1";
    public static final String GET_GIFT_CODE = "SELECT * FROM `gift_codes` WHERE `code` = ? AND (`server_id` = ? OR `server_id` = 0) AND (expires_at IS NULL OR expires_at > now()) LIMIT 1;";
    public static final String UPDATE_GIFT_CODE = "UPDATE `gift_codes` SET `status` = 1, `updated_at` = ? WHERE `id` = ? LIMIT 1;";
    public static final String INSERT_USED_GIFT_CODE = "INSERT INTO `gift_code_histories`(`player_id`,`user_id`, `gift_code`, `updated_at`) VALUES (?, ?, ?, ?)";
    public static final String CHECK_EXIST_USED_GIFT_CODE = "SELECT * FROM `gift_code_histories` WHERE `gift_code` = ? AND (`player_id` = ? OR `user_id` = ?) LIMIT 1;";
    public static final String GET_USER = "SELECT `status`, `id`, `luong`, `balance`, `tongnap`, `loantin`, `fancung`, `last_attendance_at`, `received_first_gift`, `password`, `ip_address`, `level_reward`, `role`, `online`, `ban_until`, `activated`, `date_khaimo` FROM `users` WHERE `username` = ?;";
    public static final String GET_USER_ROLES = "SELECT * FROM `model_has_roles` WHERE `model_id` = ?;";
    public static final String CHECK_USERNAME = "SELECT * FROM `users` WHERE `username` = ? LIMIT 1;";
    public static final String GET_ID_USERNAME = "SELECT `id` FROM `users` WHERE `username` = ? LIMIT 1;";
    public static final String UPDATE_USERNAME = "UPDATE `users` SET `username` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_AMOUNT_UNPAID = "UPDATE `users` SET `amount_unpaid` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_GOLD = "UPDATE `users` SET `luong` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_COIN = "UPDATE `players` SET `xu` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_YEN = "UPDATE `players` SET `yen` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_VND = "UPDATE `users` SET `coin` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_GOLD1 = "UPDATE `users` SET `coin` = ? WHERE `id` = ? LIMIT 1;";

    public static final String ADD_GOLD = "UPDATE `users` SET `luong` = `luong` + ? WHERE `id` = ? LIMIT 1;";
    public static final String ADD_GOLD1 = "UPDATE `users` SET `coin` = `coin` + ? WHERE `id` = ? LIMIT 1;";
    public static final String ADD_COIN = "UPDATE `players` SET `xu` = `xu` + ? WHERE `id` = ? LIMIT 1;";
    public static final String ADD_YEN = "UPDATE `players` SET `yen` = `yen` + ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_MESSAGE = "UPDATE `players` SET `message` = ? WHERE `id` = ? LIMIT 1;";
    public static final String INSERT_ITEM_TO_STALL = "INSERT INTO `shinwa`(`server_id`, `seller`, `item`, `price`, `status`, `time`) VALUES (?,?,?,?,?,?)";
    public static final String GET_ALL_STORE = "SELECT * FROM `stores`";
    public static final String GET_STORE_DATA = "SELECT * FROM `store_data` WHERE `store` = ?";
    public static final String CHECK_PLAYER_NAME = "SELECT * FROM `players` WHERE `name` = ? LIMIT 1;";
    public static final String UPDATE_CLAN_MAIN_PLAYER_NAME = "UPDATE `clan` SET `main_name` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_CLAN_ASS_PLAYER_NAME = "UPDATE `clan` SET `assist_name` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_CLAN_MEM_PLAYER_NAME = "UPDATE `clan_member` SET `name` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_PLAYER_NAME = "UPDATE `players` SET `name` = ? WHERE `id` = ? LIMIT 1;";
    public static final String DELETE_CLAN = "DELETE FROM `clan` WHERE `id` = ? LIMIT 1;";
    public static final String CheckClanName = "SELECT * FROM `clan` WHERE `name` = ? LIMIT 1;";
    public static final String UpdateClanName = "UPDATE `clan` SET `name` = ? WHERE `id` = ? LIMIT 1;";

    public static final String UPDATE_PRODUCT = "UPDATE `shinwa` SET `status` = ?, `time` = ? WHERE `id` = ? LIMIT 1;";
    public static final String LOAD_EVENT_POINT = "SELECT `event_points`.*, `players`.`name` FROM `event_points`, `players` WHERE `event_points`.`event_id` = ? AND `event_points`.`server_id` = ? AND `players`.`id` = `event_points`.`player_id`;";
    
    public static final String INSERT_EVENT_POINT = "INSERT INTO `event_points`(`event_id`, `player_id`, `point`, `server_id`) VALUES (?,?,?,?)";
    public static final String UPDATE_EVENT_POINT = "UPDATE `event_points` SET `point` = ? WHERE `id` = ? LIMIT 1;";
    public static final String LOAD_PLAYER = "SELECT * FROM `players` WHERE `id` = ? LIMIT 1;";
    public static final String CHECK_NAME = "SELECT * FROM `players` WHERE `name` = ? LIMIT 1;";
    public static final String UPDATE_NAME = "UPDATE `players` SET `name` = ? WHERE `id` = ? LIMIT 1;";
    
    public static final String SELECT_PLAYER_BY_NAME = "SELECT * FROM `players` WHERE `name` = ? LIMIT 1;";
    public static final String UPDATE_USERS_STATUS = "UPDATE `users` SET `status` = ?, `activated` = ? WHERE `id` = ? LIMIT 1;";
    public static final String FIND_ITEM_BY_ID = "SELECT * FROM `item` WHERE `id` = ?;";
    public static final String DELETE_ITEM_BY_ID = "DELETE FROM `store_data` WHERE `item_id` = ?";
    public static final String LOAD_ITEM_BY_NAME = "SELECT * FROM `item` WHERE `name` LIKE CONCAT('%',?,'%');";
    public static final String UPDATE_PLAYER_ONLINE_STATUS = "UPDATE `players` SET `online` = ? WHERE `user_id` = ? LIMIT 1;";
    public static final String UPDATE_USERS_ONLINE_STATUS = "UPDATE `users` SET `online` = ? WHERE `id` = ? LIMIT 1;";
    public static final String UPDATE_PLAYER_XU_STATUS = "UPDATE `players` SET `xu` = ? WHERE `user_id` = ? LIMIT 1;";
    public static final String ADD_ITEM_INTO_SHOPDATA = "INSERT INTO `store_data` (`item_id`, `sys`, `store`, `lock`, `coin`, `gold`, `yen`, `expire`, `options`) VALUES (?, 0, ?, 1, ?, ?, ?, 2592000000, '[]');";
    public static final String READ_LIMIT_DOITEN = "SELECT `limit_doiten` FROM `players` WHERE `id` = ?";
    
    public static final String ACTIVE_ACCOUNT = "UPDATE `users` SET `activated` = ?,`kh` = ?,`role` = ? WHERE `username` = ? LIMIT 1;";
    public static final String LOCK_ACCOUNT = "UPDATE `users` SET `status` = ? WHERE `username` = ? LIMIT 1;";
    public static final String BANUNI_ACCOUNT = "UPDATE `users` SET `ban_until` = ? WHERE `username` = ? LIMIT 1;";
    public static final String SET_A = "INSERT INTO `model_has_roles`(`role_id`,`model_type`, `model_id`) VALUES (?, ?, ?)";
    public static final String LOAD_EXP_SERVER = "SELECT `value` FROM `options` WHERE `key` = 'expserver' LIMIT 1;";
    public static final String LOAD_LVNJTL_SERVER = "SELECT `value` FROM `options` WHERE `key` = 'levelnjtl' LIMIT 1;";
    public static final String LOAD_NOTIFY = "SELECT * FROM `options` WHERE `key` = ?   LIMIT 1;";
    public static final String DELETE_PRODUCT = "DELETE FROM `shinwa` WHERE `id` = ? LIMIT 1;";
}
