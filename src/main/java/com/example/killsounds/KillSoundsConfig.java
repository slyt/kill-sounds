package com.example.killsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("killsounds")
public interface KillSoundsConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
		keyName = "customSoundLocation",
		name = "Custom Sound Location",
		description = "Aboslute path to custom sound file(s)"
	)default String customSoundLocation()
	{
		return "./resources/customSounds/";
	}

	@ConfigItem(
		keyName = "enableCustomSounds",
		name = "Custom sounds enabled",
		description = "If checked, custom sounds are used."
	)default boolean enableCustomSounds()
	{
		return false;
	}


}
