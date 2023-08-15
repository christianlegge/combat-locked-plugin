package com.combatlocked;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Combat Locked"
)
public class CombatLockedPlugin extends Plugin
{
	private static final Pattern CA_MESSAGE_PATTERN = Pattern.compile("Congratulations, you've completed an? (?<tier>\\w+) combat task: <col=[0-9a-f]+>(?<task>(.+))</col>");
	private int availableLevels;
	private int totalCas;
	private boolean initialized;
	private String profileKey;

	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private CombatLockedOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private CombatLockedConfig config;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		initialized = false;
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.GAMEMESSAGE)  {
			return;
		}

		log.debug("parsing game message");

		String chatMessage = event.getMessage();

		log.debug(chatMessage);

		if (chatMessage.contains("combat task")) {
			log.debug("contains combat task");
			final Matcher m = CA_MESSAGE_PATTERN.matcher(chatMessage);
			log.debug(m.toString());
			if (m.find()) {
				log.debug("success, saving");
				this.totalCas++;
				saveCasConfig();
			}
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
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) {
		final String profileKey = configManager.getRSProfileKey();

		log.debug(profileKey);
		if (profileKey == null || profileKey.equals(this.profileKey)) {
			return;
		}
		this.profileKey = profileKey;

		loadCasConfig();
	}

	@Provides
	CombatLockedConfig provideConfig(ConfigManager configManager)
	{
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
		}
		else {
			this.initialized = true;
			this.totalCas = Integer.parseInt(casConfig);
		}
	}

	private void  updateAvailableLevels() {
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
}
