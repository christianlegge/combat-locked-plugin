package com.combatlocked;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
        name = "Combat Locked"
)
public class CombatLockedPlugin extends Plugin {
    private static final Skill[] SKILLS = {
            Skill.ATTACK,
            Skill.STRENGTH,
            Skill.DEFENCE,
            Skill.RANGED,
            Skill.PRAYER,
            Skill.MAGIC,
            Skill.RUNECRAFT,
            Skill.CONSTRUCTION,
            Skill.HITPOINTS,
            Skill.AGILITY,
            Skill.HERBLORE,
            Skill.THIEVING,
            Skill.CRAFTING,
            Skill.FLETCHING,
            Skill.SLAYER,
            Skill.HUNTER,
            Skill.MINING,
            Skill.SMITHING,
            Skill.FISHING,
            Skill.COOKING,
            Skill.FIREMAKING,
            Skill.WOODCUTTING,
            Skill.FARMING
    };
    private static final int SCRIPTID_STATS_SETLEVEL = 394;
    private static final Pattern CA_MESSAGE_PATTERN = Pattern.compile("Congratulations, you've completed an? (?<tier>\\w+) combat task: <col=[0-9a-f]+>(?<task>(.+))</col>");
    private int availableLevels;
    private int totalCas;
    private boolean initialized;
    private String profileKey;
    private final Widget[] warnings = new Widget[24];
    private Widget currentWidget;

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private CombatLockedOverlay overlay;
    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    @Inject
    private CombatLockedConfig config;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
        initialized = false;
        if (client.getGameState() == GameState.LOGGED_IN) {
			this.profileKey = configManager.getRSProfileKey();
            loadCasConfig();
            clientThread.invoke(this::drawWarnings);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        clientThread.invoke(this::removeWarnings);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String chatMessage = event.getMessage();

        if (chatMessage.contains("combat task")) {
            final Matcher m = CA_MESSAGE_PATTERN.matcher(chatMessage);
            if (m.find()) {
                this.totalCas++;
                saveCasConfig();
            }
        }

    }

    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        if (event.getScriptId() != SCRIPTID_STATS_SETLEVEL) {
            return;
        }
        this.currentWidget = event.getScriptEvent().getSource();
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() == SCRIPTID_STATS_SETLEVEL && currentWidget != null) {
            drawWarning(currentWidget);
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() != 717) {
            return;
        }
        Widget caWidget = client.getWidget(717, 14);
        if (caWidget != null) {
            Widget totalWidget = caWidget.getChild(3);
            if (totalWidget != null) {
                this.totalCas = Integer.parseInt(totalWidget.getText());
                this.initialized = true;
                saveCasConfig();
                updateAvailableLevels();
            }
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        updateAvailableLevels();
        Skill skill = event.getSkill();
        int idx = Arrays.asList(SKILLS).indexOf(skill);
        if (warnings[idx] != null) {
            warnings[idx].setHidden(!shouldWarn(skill));
        }
    }

    @Subscribe
    public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) {
        final String profileKey = configManager.getRSProfileKey();

        if (profileKey == null || profileKey.equals(this.profileKey)) {
            return;
        }
        this.profileKey = profileKey;

        loadCasConfig();
    }

    @Provides
    CombatLockedConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CombatLockedConfig.class);
    }

    int getAvailableLevels() {
        return availableLevels;
    }

    int getTotalCas() {
        return totalCas;
    }

    boolean getInitialized() {
        return initialized;
    }

    private void drawWarnings() {
        Widget skillsContainer = client.getWidget(WidgetInfo.SKILLS_CONTAINER);
        if (skillsContainer == null) {
            return;
        }

        for (Widget skillTile : skillsContainer.getStaticChildren()) {
            drawWarning(skillTile);
        }
    }

    private void drawWarning(Widget skillTile) {
        int idx = WidgetInfo.TO_CHILD(skillTile.getId()) - 1;
        if (idx >= 23 || warnings[idx] != null) {
            return;
        }
        Skill skill = SKILLS[idx];

        Widget box = skillTile.createChild(-1, WidgetType.RECTANGLE);
        box.setWidthMode(WidgetSizeMode.MINUS);
        box.setHeightMode(WidgetSizeMode.MINUS);
        box.setOriginalHeight(4);
        box.setOriginalWidth(4);
        box.setPos(0, 0, WidgetPositionMode.ABSOLUTE_CENTER, WidgetPositionMode.ABSOLUTE_CENTER);
        box.setFilled(true);
        box.setOpacity(140);
        box.setTextColor(Color.RED.getRGB());

        warnings[idx] = box;

        updateWarning(skill);

    }

    private void updateWarning(Skill skill) {
        int idx = Arrays.asList(SKILLS).indexOf(skill);
        Widget w = warnings[idx];
        w.setHidden(!shouldWarn(skill));
        w.revalidate();
    }

    private void removeWarnings() {
        for (int i = 0; i < warnings.length; i++) {
            removeWarning(warnings[i]);
            warnings[i] = null;
        }
    }

    private void removeWarning(Widget w) {
        if (w == null) {
            return;
        }
        Widget parent = w.getParent();
        Widget[] children = parent.getChildren();
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i] == w) {
                children[i] = null;
            }
        }
    }

    private void saveCasConfig() {
        if (this.profileKey == null || !this.initialized) {
            return;
        }
        configManager.setConfiguration(CombatLockedConfig.GROUP, this.profileKey, "totalCas", Integer.toString(this.totalCas));
    }

    private void loadCasConfig() {
        if (this.profileKey == null) {
            return;
        }
        String casConfig = configManager.getConfiguration(CombatLockedConfig.GROUP, this.profileKey, "totalCas");
        if (casConfig == null) {
            this.initialized = false;
        } else {
            this.initialized = true;
            this.totalCas = Integer.parseInt(casConfig);
        }
    }

    private void updateAvailableLevels() {
        Skill[] skills = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC, Skill.PRAYER};
        int totalLevels = 0;
        for (Skill s : skills) {
            totalLevels += client.getRealSkillLevel(s);
        }
        this.availableLevels = this.totalCas * 4 - totalLevels + skills.length;
        if (Arrays.asList(skills).contains(Skill.HITPOINTS)) {
            this.availableLevels += 9;
        }
    }

    private boolean shouldWarn(Skill skill) {
        int xp = client.getSkillExperience(skill);
        int level = client.getRealSkillLevel(skill);
        return level < 99 && reverseLerp(Experience.getXpForLevel(level), Experience.getXpForLevel(level + 1), xp) >= 0.85;
    }

    private float reverseLerp(int a, int b, int x) {
        return (x - a) / (float) (b - a);
    }
}
