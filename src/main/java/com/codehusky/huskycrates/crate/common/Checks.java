package com.codehusky.huskycrates.crate.common;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Item;
import com.codehusky.huskycrates.crate.virtual.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.util.*;

public class Checks {
    public Map<Integer, ItemStack> inventory  = new HashMap<>();


    public void tryCrateFromPhysicalCrate(PhysicalCrate physicalCrate, Player player) {
        if(physicalCrate.getCrate().isFree()){
            //////////////////////////
            // ATTEMPT 0
            // Try Free
            //////////////////////////
            if(!physicalCrate.getCrate().isTimedOut(player.getUniqueId())) {
                physicalCrate.getCrate().launchView(physicalCrate.getCrate(), player, physicalCrate.getLocation());
                return;
            }else{
                player.sendMessage(physicalCrate.getCrate().getMessages().format(Crate.Messages.Type.RejectionCooldown,player));
            }
        }else if(!physicalCrate.getCrate().isTimedOut(player.getUniqueId())){

            /////////////////////////
            // ATTEMPT 1
            // Try Physical Key
            /////////////////////////

            Optional<ItemStack> pItemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
            if (pItemInHand.isPresent()) {
                if(handleKeyItem(physicalCrate.getCrate(),pItemInHand.get(),player)){
                    physicalCrate.getCrate().launchView(physicalCrate.getCrate(), player, physicalCrate.getLocation());
                    return;
                }
            }

            /////////////////////////
            // ATTEMPT 2
            // Try Virtual Key
            /////////////////////////

            if(physicalCrate.getCrate().testVirtualKey(player.getUniqueId())){
                physicalCrate.getCrate().consumeVirtualKeys(player.getUniqueId());
                physicalCrate.getCrate().launchView(physicalCrate.getCrate(), player, physicalCrate.getLocation());
                return;
            }

            player.sendMessage(physicalCrate.getCrate().getMessages().format(Crate.Messages.Type.RejectionNeedKey,player));
        }else{
            player.sendMessage(physicalCrate.getCrate().getMessages().format(Crate.Messages.Type.RejectionCooldown,player));
        }
        player.playSound(SoundTypes.ENTITY_CREEPER_DEATH, player.getPosition(), 1.0);
        if (physicalCrate.getCrate().getRejectEffect() != null) {
            HuskyCrates.registry.runClientEffect(physicalCrate.getCrate().getRejectEffect(), physicalCrate.getLocation(),player);
        }
    }

    public void tryCrateFromCrate(Crate crate, Player player) {
        if(crate.isFree()){
            //////////////////////////
            // ATTEMPT 0
            // Try Free
            //////////////////////////
            if(!crate.isTimedOut(player.getUniqueId())) {
                crate.launchView(crate, player, player.getLocation());
                return;
            }else{
                player.sendMessage(crate.getMessages().format(Crate.Messages.Type.RejectionCooldown,player));
            }
        }else if(!crate.isTimedOut(player.getUniqueId())){

            /////////////////////////
            // ATTEMPT 1
            // Try Physical Key
            /////////////////////////


            Optional<ItemStack> pItemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
            if (pItemInHand.isPresent()) {
                if(handleKeyItem(crate,pItemInHand.get(),player)){
                    crate.launchView(crate, player, player.getLocation());
                    return;
                }
            }


            /////////////////////////
            // ATTEMPT 2
            // Try Virtual Key
            /////////////////////////

            if(crate.testVirtualKey(player.getUniqueId())){
                crate.consumeVirtualKeys(player.getUniqueId());
                crate.launchView(crate, player, player.getLocation());
                return;
            }

            player.sendMessage(crate.getMessages().format(Crate.Messages.Type.RejectionNeedKey,player));
        }else{
            player.sendMessage(crate.getMessages().format(Crate.Messages.Type.RejectionCooldown,player));
        }
        player.playSound(SoundTypes.ENTITY_CREEPER_DEATH, player.getPosition(), 1.0);
    }

    public boolean handleKeyItem(Crate crate, ItemStack stack, Player player){
        String keyID = Key.extractKeyId(stack);
        if (keyID != null && HuskyCrates.registry.isKey(keyID)) {
            Key key = HuskyCrates.registry.getKey(keyID);
            boolean ignoreCompatability = key.canLaunchCrate() && key.crateToLaunch().getId().equals(crate.getId());
            if (key.testKey(stack) || ignoreCompatability) {

                if (crate.testKey(stack) || ignoreCompatability) {

                    int toConsume = 1; //assume local key
                    if(crate.getAcceptedKeys().containsKey(keyID)){
                        toConsume = crate.getAcceptedKeys().get(keyID);
                    }
                    if(!HuskyCrates.KEY_SECURITY || HuskyCrates.registry.validateSecureKey(stack,toConsume)) {
                        if (stack.getQuantity() > toConsume) {
                            player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.builder().from(stack).quantity(stack.getQuantity() - toConsume).build());
                        } else if(stack.getQuantity() == toConsume) {
                            player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.empty());
                        }else{
                            player.sendMessage(crate.getMessages().format(Crate.Messages.Type.RejectionNeedKey,player));
                            return false;
                        }
                        
                        return true;
                    }else{
                        player.playSound(SoundTypes.ENTITY_CAT_HISS,player.getPosition(),1.0);
                        player.sendMessage(Text.of(TextColors.RED,"Caught you!\nYou attempted to use fake keys (stack of " + stack.getQuantity() + ") with this crate!\nThis will be reported to admins."));
                        Util.alertAdminsDupe(player,stack);
                        List<PotionEffect> pe = player.getOrElse(Keys.POTION_EFFECTS,new ArrayList<>());
                        pe.add(PotionEffect.of(PotionEffectTypes.BLINDNESS,0,40));
                        player.offer(Keys.POTION_EFFECTS,pe);
                        return false;
                    }
                }
            }
        }
        return false;
    }



    public int keysInInventory(ItemStack stack, Player player){

        Inventory playerInventory = Util.getHotbarFirst(player.getInventory());
        inventory = new HashMap<>();
        int i = 0;
        int keyAmount = 0;
        int keyAmountS = 0;
        for (Inventory slot : playerInventory.slots())
        {
            Optional<ItemStack> itemslot = slot.peek();
            if (itemslot.isPresent()){
                if(Key.extractKeyId(itemslot.get()) != null && HuskyCrates.registry.isKey(Key.extractKeyId(itemslot.get()))){
                    if(Key.extractKeyId(itemslot.get()).equals(Key.extractKeyId(stack))){
                        keyAmountS = keyAmount + itemslot.get().getQuantity();
                        keyAmount = keyAmountS;

                        i++;
                    }
                }
            }
        }
        return keyAmount;
    }

    public boolean processKeysInInventory(ItemStack stack, Player player, int consumeAmount) {
        Inventory playerInventory = Util.getHotbarFirst(player.getInventory());
        Iterable<Slot> slotIter = playerInventory.slots();
        int i = 0;
        int keysConsumed = 0;
        int keysSeen = 0;
        for (Slot slot : slotIter) {
            {
                Optional<ItemStack> itemslot = slot.peek();
                if (itemslot.isPresent()) {
                    if (Key.extractKeyId(itemslot.get()) != null && HuskyCrates.registry.isKey(Key.extractKeyId(itemslot.get()))) {
                        if (Key.extractKeyId(itemslot.get()).equals(Key.extractKeyId(stack))) {

                            if (keysConsumed == consumeAmount) {
                                break;
                            }
                            if (itemslot.get().getQuantity() > consumeAmount) {
                                HuskyCrates.registry.consumeSecureKey(itemslot.get(), consumeAmount);
                                slot.set(ItemStack.builder().from(itemslot.get()).quantity(itemslot.get().getQuantity() - consumeAmount).build());
                                break;

                            } else if (itemslot.get().getQuantity() < consumeAmount) {
                                keysConsumed = keysSeen + itemslot.get().getQuantity();
                                keysSeen = keysConsumed;

                                HuskyCrates.registry.consumeSecureKey(itemslot.get(), itemslot.get().getQuantity());
                                slot.set(ItemStack.empty());
                                if (keysConsumed == consumeAmount) {
                                    System.out.println("Broke For Loop In Less-Than-Check");
                                    break;
                                }
                            } else if (itemslot.get().getQuantity() == consumeAmount - keysConsumed) {
                                HuskyCrates.registry.consumeSecureKey(itemslot.get(), consumeAmount - keysConsumed);
                                slot.set(ItemStack.empty());
                                break;
                            }

                            i++;
                        }
                    }
                }
            }
        }

        return false;
    }
    public boolean validateFutureUseSecureKeys(ItemStack stack, Player player, int consumeAmount){
        Inventory playerInventory = Util.getHotbarFirst(player.getInventory());
        Iterable<Slot> slotIter = playerInventory.slots();
        int i = 0;
        int keysConsumed = 0;
        int keysSeen = 0;
        for (Slot slot : slotIter) {
            {
                Optional<ItemStack> itemslot = slot.peek();
                if (itemslot.isPresent()){
                    if(Key.extractKeyId(itemslot.get()) != null && HuskyCrates.registry.isKey(Key.extractKeyId(itemslot.get()))){
                        if(Key.extractKeyId(itemslot.get()).equals(Key.extractKeyId(stack))){

                            //HuskyCrates.registry.validateSecureKey(itemslot.get(),itemslot.get().getQuantity());
                            //if(keysConsumed == consumeAmount){
                            //    break;
                            //}
                            if (itemslot.get().getQuantity() > consumeAmount) {
                                System.out.println("---------------------------");
                                System.out.println(HuskyCrates.registry.validateSecureKey(itemslot.get(),consumeAmount));
                                System.out.println("---------------------------");
                                return HuskyCrates.registry.validateSecureKey(itemslot.get(),consumeAmount);

                            } else if(itemslot.get().getQuantity() < consumeAmount) {
                                keysConsumed = keysSeen + itemslot.get().getQuantity();
                                keysSeen = keysConsumed;


                                System.out.println("---------------------------");
                                System.out.println("How Many Times Do We Get Here Per Loop?");
                                System.out.println(keysSeen+" is equal to the keysSeen variable");
                                System.out.println("---------------------------");
                                System.out.println(consumeAmount+" is equal to the consumeAmount variable");
                                System.out.println("---------------------------");


                                if(keysConsumed == consumeAmount){
                                    System.out.println("Broke For Loop In Less-Than-Check");
                                    return HuskyCrates.registry.validateSecureKey(itemslot.get(),itemslot.get().getQuantity());
                                }
                            } else if(itemslot.get().getQuantity() == consumeAmount-keysConsumed) {
                                return HuskyCrates.registry.validateSecureKey(itemslot.get(),consumeAmount-keysConsumed);
                            }

                            i++;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*        inventory = new HashMap<>();
        for (Inventory slot : playerInventory.slots())
        {
            Optional<ItemStack> itemslot = slot.peek();
            if (itemslot.isPresent()){
                if(Key.extractKeyId(itemslot.get()) != null && HuskyCrates.registry.isKey(Key.extractKeyId(itemslot.get()))){
                    if(Key.extractKeyId(itemslot.get()).equals(Key.extractKeyId(stack))){


                        if(keysConsumed == consumeAmount){
                            break;
                        }
                        if (itemslot.get().getQuantity() > consumeAmount) {

                            player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.builder().from(stack).quantity(stack.getQuantity() - consumeAmount).build());
                            break;
                        } else if(itemslot.get().getQuantity() < consumeAmount) {
                            keysConsumed = keysSeen + itemslot.get().getQuantity();
                            keysSeen = keysConsumed;
                            playerInventory.qu
                            player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.empty());
                            if(keysConsumed == consumeAmount){
                                System.out.println("Broke For Loop In Less-Than-Check");
                                break;
                            }
                        }

                        i++;
                    }
                }
            }
        }*/
}