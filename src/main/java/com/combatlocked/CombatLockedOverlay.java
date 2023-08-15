package com.combatlocked;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import javax.sound.sampled.Line;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class CombatLockedOverlay extends OverlayPanel {
    private final Client client;
    private final CombatLockedConfig config;
    private final CombatLockedPlugin plugin;

    private final static String TOTAL_CAS_STRING = "Total Combat Achievements:";
    private final static String AVAILABLE_LEVELS_STRING = "Available levels:";
    private final static String[] STRINGS = new String[] {
            TOTAL_CAS_STRING,
            AVAILABLE_LEVELS_STRING,
    };

    @Inject
    private CombatLockedOverlay(Client client, CombatLockedConfig config,CombatLockedPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Combat Locked plugin"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {

//        String unspentTiles = addCommasToNumber(plugin.getRemainingTiles());
//        String unlockedTiles = addCommasToNumber(plugin.getTotalTiles());
//        String xpUntilNextTile = addCommasToNumber(plugin.getXpUntilNextTile());

        if (!plugin.getInitialized()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Open the Combat Achievements panel to initialize the plugin. You only have to do this once.").build());
        }
        else {
            panelComponent.getChildren().add(LineComponent.builder().left("Total CAs").right(Integer.toString(plugin.getTotalCas())).build());
            int availableLevels = plugin.getAvailableLevels();
            LineComponent.LineComponentBuilder a = LineComponent.builder().left("Available levels").right(Integer.toString(plugin.getAvailableLevels()));
            if (availableLevels <= 0) {
                a = a.rightColor(Color.red);
            }
            panelComponent.getChildren().add(a.build());
        }

        return super.render(graphics);

//        panelComponent.getChildren().add(LineComponent.builder()
//                .left(UNSPENT_TILES_STRING)
//                .leftColor(getTextColor())
//                .right(unspentTiles)
//                .rightColor(getTextColor())
//                .build());
//
//        if(!(config.enableCustomGameMode() && config.excludeExp())) {
//            panelComponent.getChildren().add(LineComponent.builder()
//                    .left(XP_UNTIL_NEXT_TILE)
//                    .right(xpUntilNextTile)
//                    .build());
//        }
//
//        panelComponent.getChildren().add(LineComponent.builder()
//                .left(UNLOCKED_TILES)
//                .right(unlockedTiles)
//                .build());
//
//        panelComponent.setPreferredSize(new Dimension(
//                getLongestStringWidth(STRINGS, graphics)
//                        + getLongestStringWidth(new String[] {unlockedTiles, unspentTiles}, graphics),
//                0));
//
//        return super.render(graphics);
    }

    /*private Color getTextColor() {
        if(config.enableTileWarnings()) {
            if (plugin.getRemainingTiles() <= 0) {
                return Color.RED;
            } else if (plugin.getRemainingTiles() <= config.warningLimit()) {
                return Color.ORANGE;
            }
        }
        return Color.WHITE;
    }*/

    private int getLongestStringWidth(String[] strings, Graphics2D graphics) {
        int longest = graphics.getFontMetrics().stringWidth("000000");
        for(String i: strings) {
            int currentItemWidth = graphics.getFontMetrics().stringWidth(i);
            if(currentItemWidth > longest) {
                longest = currentItemWidth;
            }
        }
        return longest;
    }

    private String addCommasToNumber(int number) {
        String input = Integer.toString(number);
        StringBuilder output = new StringBuilder();
        for(int x = input.length() - 1; x >= 0; x--) {
            int lastPosition = input.length() - x - 1;
            if(lastPosition != 0 && lastPosition % 3 == 0) {
                output.append(",");
            }
            output.append(input.charAt(x));
        }
        return output.reverse().toString();
    }
}
