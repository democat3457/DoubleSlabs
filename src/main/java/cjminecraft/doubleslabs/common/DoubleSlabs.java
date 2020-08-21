package cjminecraft.doubleslabs.common;

import cjminecraft.doubleslabs.api.SlabSupport;
import cjminecraft.doubleslabs.client.proxy.ClientProxy;
import cjminecraft.doubleslabs.common.init.DSBlocks;
import cjminecraft.doubleslabs.common.init.DSTiles;
import cjminecraft.doubleslabs.common.proxy.IProxy;
import cjminecraft.doubleslabs.server.proxy.ServerProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DoubleSlabs.MODID)
public class DoubleSlabs {
    public static final String MODID = "doubleslabs";

    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    private static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public DoubleSlabs() {
        IEventBus mod = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forge = MinecraftForge.EVENT_BUS;

        mod.addListener(this::commonSetup);

        DSBlocks.BLOCKS.register(mod);
        DSTiles.TILES.register(mod);

        PROXY.addListeners(mod, forge);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        SlabSupport.init(); // TODO remove
    }
}