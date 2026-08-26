/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.bot;

import Exe_Z.ability.AbilityCustom;
import Exe_Z.fashion.FashionCustom;
import Exe_Z.map.Map;
import Exe_Z.map.item.ItemMap;
import Exe_Z.map.item.ItemMapFactory;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.util.NinjaUtils;
import Exe_Z.util.TimeUtils;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class Principal extends Bot {

    private int head;
    private int body;
    private int leg;
    private long lastTime, lastTimeDropItem, lastTimeChat;
    static String[] chats = new String[] {
            "Chúc mừng năm mới 2023",
            "Xuân này hơn hẳn mấy xuân qua.",
            "Vạn sự an khang vạn sự lành.",
            "Phúc lộc đưa nhau đến từng nhà. Vài lời cung chúc tân niên mới.",
            "Vạn sự như ý, tỉ sự như mơ, triệu triệu bất ngờ, không chờ cũng đến.",
            "Hoa đào nở, chim én về, mùa xuân lại đến.",
            "Chúc một năm mới: nghìn sự như ý, vạn sự như mơ, triệu sự bất ngờ, tỷ lần hạnh phúc.",
            "Chúc năm mới sức khỏe dẻo dai, công việc thuận lợi thăng tiến dài dài, phi những nước đại tiến tới thành công."
    };

    public Principal(int id, String name, int head, int body, int leg) {
        super(id, name, 150, Char.PK_NORMAL, (byte) 0);
        this.lastTime = this.lastTimeDropItem = System.currentTimeMillis();
        this.head = head;
        this.body = body;
        this.leg = leg;
    }

    @Override
    public void setDefault() {
        super.setDefault();
        FashionCustom fashionCustom = FashionCustom.builder()
                .head((short) head)
                .body((short) body)
                .leg((short) leg)
                .weapon((short) -1)
                .build();
        setFashionStrategy(fashionCustom);
        AbilityCustom abilityCustom = AbilityCustom.builder()
                .hp(2000000)
                .build();
        setAbilityStrategy(abilityCustom);
        setAbility();
        setFashion();
    }

    @Override
    public void updateEveryHalfSecond() {
        super.updateEveryHalfSecond();
        long now = System.currentTimeMillis();
        if (TimeUtils.canDoWithTime(lastTime, 60000)) {
            lastTime = now;
            zone.getService().chat(this.id, "Ta đi khu khác phát tiếp đây, tạm biệt các con");
            Map map = zone.map;
            Zone z = map.rand();
            outZone();
            z.join(this);
            return;
        }
        if (TimeUtils.canDoWithTime(lastTimeDropItem, 10000)) {
            lastTimeDropItem = now;
            int q = NinjaUtils.nextInt(1, 2);
            for (int i = 0; i < q; i++) {
                ItemMap item = ItemMapFactory.getInstance().builder()
                        .id(zone.numberDropItem++)
                        .type(ItemMapFactory.ENVELOPE)
                        .x((short) (x + (i * 6 * (i % 2 == 0 ? 1 : -1))))
                        .y(y)
                        .build();
                zone.addItemMap(item);
                zone.getService().addItemMap(item);
            }
            zone.getService().addEffectAuto((byte) 0, (short) x, y, (byte) 0, (short) 5);
        }
        if (TimeUtils.canDoWithTime(lastTimeChat, 5000)) {
            lastTimeChat = now;
            zone.getService().chat(this.id, (String) NinjaUtils.randomObject(chats));
        }
    }

}
