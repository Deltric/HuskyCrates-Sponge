package com.codehusky.huskycrates.util;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.Key;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class Util {
    /**
     * Garbage method I have to do since API7 decided to eat the inventory api.
     * @param inventory
     * @return an inventory that i actually want
     */
    public static Inventory getHotbarFirst(Inventory inventory) {
        return inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                .union(inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));
    }

    /**
     * This will log and alert the console + admin players on the server of something.
     * @param message
     */
    public static void alertAdmins(Text message, String logMessage){
        for(Player p: Sponge.getServer().getOnlinePlayers()){
            if(p.hasPermission("huskycrates.admin")) {
                p.sendMessage(message);
                p.playSound(SoundTypes.ENTITY_CAT_HURT, p.getPosition(), 1);
            }
        }
        //Sponge.getServer().getConsole().sendMessage(message);
        System.out.println("[" + DateFormat.getDateTimeInstance().format(new Date()) + "] " + logMessage);
    }

    public static void dupeLog(Player badPlayer, ItemStack stack) {
        try {
            String p = HuskyCrates.dupeLogPath.toString();
            String content = "Date : " + DateFormat.getDateTimeInstance().format(new Date()) + "\nName : " + badPlayer.getName() + "\nUUID : " + badPlayer.getUniqueId().toString() + "\nKey Type : " + ((Key.extractKeyId(stack) != null)? Key.extractKeyId(stack):"NO KEY ID") + "\nKey Stack Quantity : " + stack.getQuantity() + "\n--------------------------------" + "\n";
            FileWriter fw = new FileWriter(p, true);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            System.out.println("Unable to find 'dupealert' log to write to!");
            e.printStackTrace();
        }
    }
    public static void alertAdminsDupe(Player badPlayer, ItemStack stack){
        String mainMessage =  "Player " + badPlayer.getName() + " (" + badPlayer.getUniqueId().toString() + ") tried to use a stack of " + stack.getQuantity() + " " + ((Key.extractKeyId(stack) != null)? Key.extractKeyId(stack):"NO KEY ID") + " key(s).";
        alertAdmins(Text.of(TextColors.RED,"[HuskyCrates] ",TextColors.YELLOW,"Key Duplication Alert!\n",TextColors.RED,mainMessage),"[DUPE ALERT] " + mainMessage);
        dupeLog(badPlayer, stack);
    }

    public static String getJDBC() {
        CommentedConfigurationNode db = HuskyCrates.instance.mainConfig.getNode("virtualkeydatabase");
        if (db.getNode("useRemoteDatabase").getBoolean()){
            return String.format("jdbc:%s://%s:%d/%s?user=%s&password=%s",
                    db.getNode("type").getString(),
                    db.getNode("host").getString(),
                    db.getNode("port").getInt(),
                    db.getNode("database").getString(),
                    db.getNode("username").getString(),
                    db.getNode("password").getString());
        }else{
            return null;
        }
    }
}
