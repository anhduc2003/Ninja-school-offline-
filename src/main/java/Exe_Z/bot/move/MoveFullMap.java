package Exe_Z.bot.move;

import Exe_Z.bot.IMove;
import Exe_Z.map.TileMap;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.util.NinjaUtils;
import lombok.Setter;

@Setter
public class MoveFullMap implements IMove {

    protected Char target;

    public MoveFullMap(Char target) {
        this.target = target;
    }

    @Override
    public void move(Char owner) {
        if (target == null) {
            return;
        }
        if (owner.isDead || target.isDead) {
            return;
        }
        if (owner.isDontMove()) {
            return;
        }
        Zone zone = owner.zone;
        owner.preX = owner.x;
        owner.preY = owner.y;
        int dir = target.x > owner.x ? 1 : -1;
        int x = NinjaUtils.nextInt(100, 180) * dir;
        owner.x += (short) x;
        if (owner.x < 24) {
            owner.x = 24;
        }
        if (owner.x > owner.zone.tilemap.pxw - 24) {
            owner.x = (short) (owner.zone.tilemap.pxw - 24);
        }
        if (owner.isCrossMap(owner.x, owner.y)) {
            owner.x -= (short) (x * 2);
        }
        short tempX = (short) (owner.x + 11);
        short tempY = (short) (owner.y - 16);
        if (zone.tilemap.isInWaypoint(tempX, tempY)) {
            owner.x = owner.preX;
            owner.y = owner.preY;
        }
        owner.y -= 24;
        owner.y = owner.zone.tilemap.collisionY(owner.x, owner.y);
        if (Math.abs(owner.x - target.x) < 100) {
            if (target.y < owner.y) {
                owner.y -= 72;
            } else if (target.y > owner.y) {
                owner.y = target.y;
            }
        }
        if (!owner.zone.tilemap.tileTypeAt(tempX, tempY, TileMap.T_TOP)) {
            owner.y = owner.zone.tilemap.collisionY(owner.x, owner.y);
        }
        if (owner.isCrossMap(owner.x, owner.y)) {
            owner.x = owner.preX;
            owner.y = owner.preY;
        }
        if (owner.clone != null && owner.clone.isNhanBan && !owner.clone.isDead) {
            owner.clone.move((short) (NinjaUtils.nextInt(-50, 50) + owner.x), owner.y);
        }
        owner.zone.getService().playerMove(owner);
    }

}
