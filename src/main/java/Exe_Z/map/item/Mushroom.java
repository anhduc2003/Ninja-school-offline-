/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.item;

import Exe_Z.constants.ItemName;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class Mushroom extends ItemMap {

    public Mushroom(short id) {
        super(id);
        Item item = ItemFactory.getInstance().newItem(ItemName.CAY_NAM);
        item.setQuantity(1);
        item.isLock = true;
        setItem(item);
        this.ownerID = -1;
        this.pickedUp = true;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

}
