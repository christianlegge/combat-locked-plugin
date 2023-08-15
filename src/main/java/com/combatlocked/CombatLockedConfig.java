package com.combatlocked;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import java.awt.*;

@ConfigGroup(CombatLockedConfig.GROUP)
public interface CombatLockedConfig extends Config
{
	String GROUP = "combatlocked";

	@ConfigSection(
			name = "With Levels Available",
			description = "With Levels Available",
			position = 1
	)
	String availableSection = "withLevelsAvailable";

	@ConfigSection(
			name = "With No Levels Available",
			description = "With No Levels Available",
			position = 2
	)
	String notAvailableSection = "withNoLevelsAvailable";

	@ConfigItem(
			keyName = "levelsPerCa",
			name = "Levels Per Combat Achievement",
			description = "How many levels become available for each Combat Achievement earned",
			position = 0
	)
	default int levelsPerCa()
	{
		return 4;
	}

	@ConfigItem(
			keyName = "warnWhenCloseAndAvailable",
			name = "Skill tab warning",
			description = "Show a warning in the skill tab when close to leveling and levels are available",
			section = availableSection,
			position = 1
	)
	default boolean warnWhenCloseAndAvailable() { return true; }

	@Range(
			min = 0,
			max = 100
	)
	@ConfigItem(
			keyName = "warnThresholdAvailable",
			name = "Threshold (%)",
			description = "Percentage of level progress to show warning at",
			section = availableSection,
			position = 2
	)
	default int warnThresholdAvailable() {return 90;}

	@Alpha
	@ConfigItem(
			keyName = "warnColorAvailable",
			name = "Color",
			description = "Color for the skill tab warning",
			section = availableSection,
			position = 3
	)
	default Color warnColorAvailable() {return new Color(255, 255, 0, 100);}

	@ConfigItem(
			keyName = "warnWhenCloseNotAvailable",
			name = "Skill tab warning",
			description = "Show a warning in the skill tab when close to leveling and levels are not available",
			section = notAvailableSection,
			position = 1
	)
	default boolean warnWhenCloseNotAvailable() { return true; }

	@Range(
			min = 0,
			max = 100
	)
	@ConfigItem(
			keyName = "warnThresholdNotAvailable",
			name = "Threshold (%)",
			description = "Percentage of level progress to show warning at",
			section = notAvailableSection,
			position = 2
	)
	default int warnThresholdNotAvailable() {return 85;}

	@Alpha
	@ConfigItem(
			keyName = "warnColorNotAvailable",
			name = "Color",
			description = "Color for the skill tab warning",
			section = notAvailableSection,
			position = 3
	)
	default Color warnColorNotAvailable() {return new Color(255, 0, 0, 100);}
}
