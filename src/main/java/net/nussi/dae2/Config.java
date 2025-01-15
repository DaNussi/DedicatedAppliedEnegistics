package net.nussi.dae2;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Dae2.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> RABBITMQ_URI = BUILDER.comment("The URI of the RabbitMQ server to connect to").define("rabbitmqUri", "amqp://guest:guest@localhost:5672");

    public static final ModConfigSpec SPEC = BUILDER.build();


    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {

    }
}
