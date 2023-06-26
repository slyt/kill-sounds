package com.example.killsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("killsounds")
public interface KillSoundsConfig extends Config
{

	@ConfigItem(
		keyName = "customSoundLocation",
		name = "Custom Sound Directory",
		description = "Aboslute path to custom sound directory. Directory must contain .wav filetypes."
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

	@ConfigItem(
		keyName = "enableAudioGreeting",
		name = "Play audio greeting",
		description = "If checked, play the \"Kill sounds initiated!\" sound when the plugin is started."
	)default boolean enableAudioGreeting()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableKillstreakMessages",
		name = "Enable killstreak messages",
		description = "If checked, show killstreak count in chat"
	)default boolean enableKillstreakMessages()
	{
		return false;
	}

}
