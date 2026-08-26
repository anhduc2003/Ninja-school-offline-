/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.lib;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class Resource {

    private long timeRemoveResource;
    private byte[] data;
    private long createdAt;

    public Resource(byte[] data, long timeRemoveResource) {
        this.data = data;
        this.timeRemoveResource = timeRemoveResource;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        //return (System.currentTimeMillis() - createdAt) > timeRemoveResource;
        return false;
    }

    public byte[] getData() {
        return this.data;
    }
}
