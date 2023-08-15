package com.combatlocked;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CombatLockedConfig.GROUP)
public interface CombatLockedConfig extends Config
{
	String GROUP = "combatlocked";

	@ConfigItem(
		keyName = "levelsPerCa",
		name = "Levels Per Combat Achievement",
		description = "The message to show to the user when they login"
	)
	default int levelsPerCa()
	{
		return 4;
	}
}
