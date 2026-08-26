/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.lib.RandomCollection;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public abstract class AbsSelectCard {

    RandomCollection<Card> cards;

    public AbsSelectCard() {
        cards = new RandomCollection<>();
        init();
    }

    protected abstract void init();

    protected void add(Card card) {
        cards.add(card.getRate(), card);
    }

    public void open(Char p) {
        p.setSelectCard(this);
        p.getService().openUI((byte) 38);
    }

    protected abstract boolean isCanSelect(@NotNull Char p);

    protected abstract void selecctCardSuccessful(@NotNull Char p);

    public boolean select(@NotNull Char p, int index) {
        if (isCanSelect(p)) {
            Card[] results = randomCard();
            Card card = results[index];
            results[index] = reward(p, card);
            selecctCardSuccessful(p);
            p.getService().selectCard(results);
            return true;
        }
        return false;
    }

    protected abstract Card reward(@NotNull Char p, Card card);

    private Card[] randomCard() {
        Card[] results = new Card[10];
        for (int i = 0; i < results.length; i++) {
            results[i] = cards.next();
        }
        return results;
    }
}
