package Exe_Z.bot.move;

import Exe_Z.bot.IMove;
import Exe_Z.map.TileMap;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.util.NinjaUtils;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class botMovefull implements IMove {

    @Override
    public void move(Char owner) {
        if (owner.isDead) {
            return;
        }
        if (owner.isDontMove()) {
            return;
        }

        Zone zone = owner.zone;
        short preX = owner.x;
        short preY = owner.y;
        int dir = (NinjaUtils.nextBoolean() ? -1 : 1);
        int x = NinjaUtils.nextInt(50, 200) * dir;
        int y = NinjaUtils.nextInt(10, 200) * (NinjaUtils.nextBoolean() ? -1 : 1);

        short newX = (short) (preX + x);
        short newY = (short) (preY + y);
        if (newX < 24) {
            newX = 24;
        }
        if (newX > zone.tilemap.pxw - 24) {
            newX = (short) (zone.tilemap.pxw - 24);
        }
        if (newY < 24) {
            newY = 24;
        }
        if (newY > zone.tilemap.pxh - 24) {
            newY = (short) (zone.tilemap.pxh - 24);
        }
        newY = zone.tilemap.collisionY(newX, newY);
        boolean isJump = NinjaUtils.nextInt(5) == 0;
        if (isJump) {
            newY -= 72; // Nhảy cao hơn khi va chạm
        }
        if (owner.isCrossMap(newX, newY)) {
            newX = preX;
            newY = preY;
        }
        owner.zone.move(owner, newX, newY);
    }
}
