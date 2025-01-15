package net.nussi.dae2.common;

import com.mojang.logging.LogUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.nussi.dae2.Config;
import net.nussi.dae2.Dae2;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;

@EventBusSubscriber(modid = Dae2.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConnectionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ConnectionManager INSTANCE;

    private final String uri;
    private final ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;

    public ConnectionManager(String uri) throws Exception {
        this.uri = uri;

        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setUri(uri);
        LOGGER.info("Connection to RabbitMQ server established");

        this.connection = this.connectionFactory.newConnection();
        this.channel = this.connection.createChannel();

    }

    public Channel getChannel() {
        if (!connection.isOpen()) {
            try {
                this.connection = this.connectionFactory.newConnection();
                LOGGER.warn("Connection was closed, but was re-opened");
            } catch (Exception e) {
                LOGGER.error("Connection was closed, but could not be re-opened", e);
            }
        }

        if (!channel.isOpen()) {
            try {
                this.channel = this.connection.createChannel();
                LOGGER.warn("Channel was closed, but was re-opened");
            } catch (Exception e) {
                LOGGER.error("Channel was closed, but could not be re-opened", e);
            }
        }

        return this.channel;
    }

    // TODO: Implement a method to close the connection and channel
    public void close() {
        try {
            this.channel.close();
        } catch (Exception e) {
            LOGGER.error("Channel could not be closed", e);
        }

        try {
            this.connection.close();
        } catch (Exception e) {
            LOGGER.error("Connection could not be closed", e);
        }
    }


    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) throws Exception {
        INSTANCE = new ConnectionManager(Config.RABBITMQ_URI.get());
    }


}