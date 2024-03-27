package net.nussi.dedicated_applied_energistics.providers.virtualdisk;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.misc.RedisHelper;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertResponse;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.*;

public class VirtualDiskHost {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;

    private Jedis redis;
    private String channel;

    public MEStorage storage;
    private VirtualDiskInfo info;


    private final Queue<InsertPair> insertQueue = new LinkedList<>();
    private final Queue<ExtractPair> extractQueue = new LinkedList<>();
    private final Queue<AvailableStacksPair> availableStacksQueue = new LinkedList<>();
    private final Queue<DescriptionPair> descriptionQueue = new LinkedList<>();

    public VirtualDiskHost(MEStorage storage, int priority, InterDimensionalInterfaceBlockEntity instance) {
        this.storage = storage;
        this.info = new VirtualDiskInfo(instance.getUuid(), priority);
        this.instance = instance;
        this.channel = RedisHelper.getPath(storage, instance);
    }

    public void onStart() {
        this.redis = instance.getRedis();

        this.updateRedis();

        LOGGER.warn("Started virtual drive host " + channel);
    }

    public void onStop() {
        this.removeRedis();
        LOGGER.warn("Stopped virtual drive host " + channel);
    }

    private void updateRedis() {
        this.redis.hset(VirtualDiskInfo.REDIS_PATH, channel, this.info.toString());
    }

    private void removeRedis() {
        this.redis.hdel(VirtualDiskInfo.REDIS_PATH, channel);
    }


    public void onTick() {
//        LOGGER.info("Ticking");

        while (insertQueue.size() > 0) {
            InsertPair pair = insertQueue.poll();
            InsertRequest request = pair.getRequest();
            long data = this.storage.insert(request.what, request.amount, request.mode, IActionSource.ofMachine(instance));
            InsertResponse response = new InsertResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (extractQueue.size() > 0) {
            ExtractPair pair = extractQueue.poll();
            ExtractRequest request = pair.getRequest();
            long data = this.storage.insert(request.what, request.amount, request.mode, IActionSource.ofMachine(instance));
            ExtractResponse response = new ExtractResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (availableStacksQueue.size() > 0) {
            AvailableStacksPair pair = availableStacksQueue.poll();
            AvailableStacksRequest request = pair.getRequest();
            var data = this.storage.getAvailableStacks();
            AvailableStacksResponse response = new AvailableStacksResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (descriptionQueue.size() > 0) {
            DescriptionPair pair = descriptionQueue.poll();
            DescriptionRequest request = pair.getRequest();
            var data = this.storage.getDescription().getString();
            DescriptionResponse response = new DescriptionResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualDiskHost virtualDiskHost) {
            return virtualDiskHost.channel.equals(this.channel);
        }
        return false;
    }

    public String getChannel() {
        return channel;
    }


}
