package io.brudderbot;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

public class Main {

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        instanceContainer.setGenerator(unit ->
                unit.modifier().fillHeight(0, 42, Block.BARRIER));
        // Add an event callback to specify the spawning instance (and the spawn position)
        Instance instance = instanceContainer;
        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        Map_cam.create(instance, new Pos(0, 44, 0));
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(-1.5, 42, -2));
        });
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            scheduler.submitTask(() -> {
                player.sendPackets(Map_screen.packets());
                return TaskSchedule.seconds(0);
            });
        });
        globalEventHandler.addListener(PlayerMoveEvent.class, event -> {
            final Player player = event.getPlayer();
            player.teleport(new Pos(-1.5, 42, -2));
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);

    }
}