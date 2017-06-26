package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskygui.HuskyGUI;
import pw.codehusky.huskygui.components.Action;
import pw.codehusky.huskygui.components.GUIRunnable;
import pw.codehusky.huskygui.components.RunnableAction;
import pw.codehusky.huskygui.components.page.Element;
import pw.codehusky.huskygui.components.page.Page;
import pw.codehusky.huskygui.components.page.elements.ActionElement;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class Husky implements CommandExecutor {
    private pw.codehusky.huskycrates.HuskyCrates plugin;
    public Husky(pw.codehusky.huskycrates.HuskyCrates ins){
        plugin = ins;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Player plr = (Player)src;
            if(!plr.getUniqueId().toString().toLowerCase().equals("20db6d8a-d993-4dc5-a30e-8b633afaa438") && !plr.hasPermission("huskycrates.tester")){
                return CommandResult.empty();
            }
            src.sendMessage(Text.of(TextColors.GOLD,"bark bark!"));
            src.sendMessage(Text.of(TextColors.GRAY, TextStyles.ITALIC,"Running HuskyCrates v" + HuskyCrates.instance.pC.getVersion().get() + " (BETA)"));




            /*--------------
              GUI Testing
            --------------*/
            HuskyGUI test = new HuskyGUI();


            /*--------------
              Test state.
            --------------*/
            Page testPage = new Page("testState");
            Element testElement = new Element();
            testElement.setDisplayItem(ItemStack.builder().itemType(ItemTypes.DIAMOND).add(Keys.DISPLAY_NAME,Text.of("apple xd")).build());

            ActionElement testClickable = new ActionElement();
            testClickable.setDisplayItem(ItemStack.builder().itemType(ItemTypes.COMMAND_BLOCK).add(Keys.DISPLAY_NAME,Text.of("Onward!")).build());
            testClickable.setAction(new Action(test,plr,false,false,"onward"));

            ActionElement close = new ActionElement();
            close.setDisplayItem(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME,Text.of("Close Test")).build());
            close.setAction(new Action(test,plr,true,false,""));

            testPage.putElement(0,testElement);
            testPage.putElement(4,close);
            testPage.putElement(8,testClickable);


            /*--------------
              onward
            --------------*/
            Page onward = new Page("onward");
            onward.setParent("testState");
            onward.setInventoryDimension(InventoryDimension.of(9,3));
            onward.fillEmptyWithItem = true;

            ActionElement genericStop = new ActionElement();
            genericStop.setDisplayItem(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME,Text.of("Back")).build());
            genericStop.setAction(new Action(test,plr,false,true,"onward"));
            onward.putElement(13,genericStop);


            ActionElement soundTest = new ActionElement();
            soundTest.setDisplayItem(ItemStack.builder().itemType(ItemTypes.NOTEBLOCK).add(Keys.DISPLAY_NAME,Text.of("Test runnable")).build());
            RunnableAction ra = new RunnableAction(test,plr,false,true,"onward");
            ra.setRunnable(context -> {
                context.observer.playSound(SoundTypes.ENTITY_CREEPER_DEATH,context.observer.getLocation().getPosition(),1.0);
            });
            soundTest.setAction(ra);

            onward.putElement(13,genericStop);
            onward.putElement(4,soundTest);

            /*--------------
              Finalization
            --------------*/
            test.addDefaultState(testPage);
            test.addState(onward);
            test.launchForPlayer(plr);
        }

        return CommandResult.success();
    }

}
