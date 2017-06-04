package pw.codehusky.huskycrates.crate;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.codehusky.huskycrates.HuskyCrates;

import java.util.Random;
import java.util.UUID;

/**
 * Created by lokio on 1/2/2017.
 */
@SuppressWarnings("deprecation")
public class PhysicalCrate {
    public Location<World> location;
    private String crateId;
    public VirtualCrate vc;
    public ArmorStand as = null;
    private HuskyCrates huskyCrates;
    double randomTimeOffset = new Random().nextDouble()*2000;
    public static Vector3d offset = new Vector3d(0.5,1,0.5);
    public PhysicalCrate(Location<World> crateLocation, String crateId, HuskyCrates huskyCrates){
        this.location = crateLocation;
        this.crateId = crateId;
        this.huskyCrates = huskyCrates;
        this.vc = huskyCrates.crateUtilities.getVirtualCrate(crateId);
        createHologram();
    }
    public void createHologram() {
        //System.out.println(as == null);
        if(as == null) {
            for (Entity e : location.getExtent().getEntities()) {
                if (e instanceof ArmorStand) {
                    Vector3d newpos = e.getLocation().copy().sub(offset).getPosition();
                    if (e.getLocation().copy().sub(offset).getPosition().equals(location.getPosition())) {
                        ArmorStand ass = (ArmorStand) e;
                        if (ass.getCreator().isPresent()) {
                            if (ass.getCreator().get().equals(UUID.fromString(huskyCrates.armorStandIdentifier))) {
//                            System.out.println("Found an armor stand");
//                            System.out.println(location);
                                as = ass;
                            }
                        }
                    }
                }
            }
        }
        //System.out.println(as);
        if(as == null || !as.isLoaded()) {
            as = (ArmorStand)  location.getExtent().createEntity(EntityTypes.ARMOR_STAND,location.getPosition());
            as.setLocation(location.copy().add(offset));
            location.getExtent().spawnEntity(as,huskyCrates.genericCause);
        }
        as.setCreator(UUID.fromString(huskyCrates.armorStandIdentifier));
        as.offer(Keys.HAS_GRAVITY,false);
        as.offer(Keys.INVISIBLE,true);
        as.offer(Keys.ARMOR_STAND_MARKER,true);
        as.offer(Keys.CUSTOM_NAME_VISIBLE,true);
        String name = "&cERROR, CHECK CONSOLE!";
        try {
            name = /*crateId*/ huskyCrates.crateUtilities.getVirtualCrate(crateId).displayName;
        }catch(Exception e){
            //e.printStackTrace();
        }
        as.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(name));

    }
    public void runParticles() {
        try {
            createHologram();
            double time = randomTimeOffset + (Sponge.getServer().getRunningTimeTicks() * 0.25);
            double size = 0.8;

            double x = Math.sin(time) * size;
            double y = Math.sin(time * 2) * 0.2 - 0.45;
            double z = Math.cos(time) * size;
            Color clr1 = Color.ofRgb(100,100,100);
            Color clr2 = Color.ofRgb(255, 0, 0);
            if(vc.getOptions().containsKey("clr1")){
                clr1 = (Color) vc.getOptions().get("clr1");
            }
            if(vc.getOptions().containsKey("clr2")){
                clr2 = (Color) vc.getOptions().get("clr2");
            }
            as.getWorld().spawnParticles(
                    ParticleEffect.builder()
                            .type(ParticleTypes.REDSTONE_DUST)
                            .option(ParticleOptions.COLOR, clr1)
                            .build(),
                    as.getLocation()
                            .getPosition()
                            .add(x, y, z));

            x = Math.cos(time + 10) * size;
            y = Math.sin(time * 2 + 10) * 0.2 - 0.55;
            z = Math.sin(time + 10) * size;
            as.getWorld().spawnParticles(
                    ParticleEffect.builder()
                            .type(ParticleTypes.REDSTONE_DUST)
                            .option(ParticleOptions.COLOR, clr2)
                            .build(),
                    as.getLocation()
                            .getPosition()
                            .add(x, y, z));
        }catch (Exception e){

        }
    }
}
