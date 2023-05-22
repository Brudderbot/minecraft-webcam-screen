package io.brudderbot;

import com.github.sarxos.webcam.Webcam;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.network.packet.server.SendablePacket;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

final class Map_screen {

    private static SendablePacket[] packets = null;

    private Map_screen() {
    }

    public static BufferedImage getscreen() {
        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();
        BufferedImage image = r.createScreenCapture(new Rectangle(0,0,d.width,d.height));
        return image;
    }

    public static SendablePacket[] packets() {
        final LargeGraphics2DFramebuffer framebuffer = new LargeGraphics2DFramebuffer(5 * 128, 3 * 128);
        framebuffer.getRenderer().drawRenderedImage(getscreen(), AffineTransform.getScaleInstance(0.3, 0.3));
        packets = mapPackets(framebuffer);

        return packets;
    }

    /**
     * Creates the maps on the board in the lobby
     */
    public static void create(@NotNull Instance instance, Point maximum) {
        final int maxX = maximum.blockX();
        final int maxY = maximum.blockY();
        final int z = maximum.blockZ();
        for (int i = 0; i < 15; i++) {
            final int x = maxX - i % 5;
            final int y = maxY - i / 5;
            final int id = i;

            final Entity itemFrame = new Entity(EntityType.ITEM_FRAME);
            final ItemFrameMeta meta = (ItemFrameMeta) itemFrame.getEntityMeta();
            itemFrame.setInstance(instance, new Pos(x, y, z, 180, 0));
            meta.setNotifyAboutChanges(false);
            meta.setOrientation(ItemFrameMeta.Orientation.NORTH);
            meta.setInvisible(true);
            meta.setItem(ItemStack.builder(Material.FILLED_MAP)
                    .meta(MapMeta.class, builder -> builder.mapId(id))
                    .build());
            meta.setNotifyAboutChanges(true);
        }
    }

    /**
     * Creates packets for maps that will display an image on the board in the lobby
     */
    private static SendablePacket[] mapPackets(@NotNull LargeGraphics2DFramebuffer framebuffer) {
        final SendablePacket[] packets = new SendablePacket[15];
        for (int i = 0; i < 15; i++) {
            final int x = i % 5;
            final int y = i / 5;
            packets[i] = framebuffer.createSubView(x * 128, y * 128).preparePacket(i);
        }

        return packets;
    }
}