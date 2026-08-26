/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.constants.ItemName;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.util.NinjaUtils;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class SelectCardHalloween extends AbsSelectCard {

    private int count;

    public SelectCardHalloween(int count) {
        this.count = count;
    }

    @Override
    protected void init() {
        add(Card.builder().id(ItemName.H).rate(20).quantity(100).build());
        add(Card.builder().id(ItemName.A).rate(20).quantity(100).build());
        add(Card.builder().id(ItemName.L).rate(40).quantity(100).build());
        add(Card.builder().id(ItemName.O).rate(20).quantity(100).build());
        add(Card.builder().id(ItemName.W).rate(20).quantity(100).build());
        add(Card.builder().id(ItemName.E).rate(40).quantity(100).build());
        add(Card.builder().id(ItemName.N).rate(20).quantity(100).build());
        add(Card.builder().id(ItemName.KEO_TAO).rate(2).quantity(10).build());
        add(Card.builder().id(ItemName.HOP_MA_QUY).rate(1).quantity(10).build());
        add(Card.builder().id(ItemName.BAT_BAO).rate(1).quantity(1).build());
        add(Card.builder().id(ItemName.GAY_PHEP).rate(0.1).quantity(1).build());
        add(Card.builder().id(ItemName.CHOI_BAY).rate(0.05).quantity(1).build());
        add(Card.builder().id(ItemName.RUONG_BACH_NGAN).rate(0.01).quantity(1).build());
        add(Card.builder().id(ItemName.RUONG_HUYEN_BI).rate(0.001).quantity(1).build());
    }

    @Override
    protected Card reward(@NotNull Char p, Card card) {
        Item item = ItemFactory.getInstance().newItem(card.getId());
        item.setQuantity(card.getQuantity());
        p.addItemToBag(item);
        return card;
    }

    @Override
    protected boolean isCanSelect(Char p) {
        if (count <= 0) {
            p.serverDialog("Bạn đã hết lượt lật hình!");
            return false;
        }
        return true;
    }

    @Override
    protected void selecctCardSuccessful(Char p) {
        count--;
        if (count == 0) {
            NinjaUtils.setTimeout(() -> {
                p.getService().endDlg(true);
            }, 5000);
        }
    }

}
