/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.item;

import Exe_Z.item.Item;
import Exe_Z.util.TimeUtils;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Administrator
 */
@Getter
@Setter
public class ItemMap {

    protected short id;
    protected Item item;
    protected int ownerID;
    protected short x;
    protected short y;
    protected boolean pickedUp;
    protected int requireItemID = -1;
    protected long createdAt;

    public Lock lock = new ReentrantLock();

    public ItemMap(short id) {
        this.id = id;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return TimeUtils.canDoWithTime(createdAt, 30000);
    }

    public boolean isCanPickup() {
        return TimeUtils.canDoWithTime(createdAt, 20000);
    }
    
    public int getItemID() {
        return item.id;
    }

}
