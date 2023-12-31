package com.example.killsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("killsounds")
public interface KillSoundsConfig extends Config
{

	@ConfigItem(
		keyName = "customSoundLocation",
		name = "Custom kill sound location",
		description = "Aboslute path to custom sound directory. Directory must contain .wav filetypes. If there are multiple sound files in the directory, then one will be played at random during a kill in PvP.",
		position = 15
	)default String customSoundLocation()
	{
		return "/path/to/custom/wav/files/";
	}


	@ConfigItem(
		keyName = "muteKillSounds",
		name = "Mute kill sounds",
		description = "If checked, don't play sounds when a kill is made in PvP.",
		position = 12
	)default boolean muteKillSounds()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 100
	)
	@ConfigItem(
		keyName = "volumeAmount",
		name = "Master Volume",
		description = "How loud to play kill and killstreak souds.",
		position = 2
	)default int volumeAmount()
	{
		return 75;
	}

	@ConfigItem(
		keyName = "enableAudioGreeting",
		name = "Play audio greeting",
		description = "If checked, play the \"Kill sounds initiated!\" sound when the plugin is started.",
		position = 5
	)default boolean enableAudioGreeting()
	{
		return false;
	}

	@ConfigSection(
		name = "Killstreaks",
		description = "Settings for Killstreaks (consecutive kills)",
		position = 50
	)
	String killstreakSection = "Killstreak options";

	@ConfigItem(
		keyName = "enableKillstreakMessages",
		name = "Enable killstreak chat messages",
		description = "If checked, show killstreak count in chat",
		position = 55,
		section = killstreakSection
	)default boolean enableKillstreakMessages()
	{
		return true;
	}


	@ConfigItem(
		keyName = "muteKillstreakSounds",
		name = "Mute killstreak sounds",
		description = "If checked, sounds are not played during killstreaks in PvP.",
		position = 55,
		section = killstreakSection
	)default boolean muteKillstreakSounds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "killstreak1Threshold",
		name = "Killstreak 1 threshold",
		description = "The number of kills required to trigger the killstreak 1 sound. 0 is disabled.",
		position = 60,
		section = killstreakSection
	)default int killstreak1Threshold()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "killstreak1SoundLocation",
		name = "Killstreak 1 sound location",
		description = "Aboslute path to a .wav file.",
		position = 65,
		section = killstreakSection
	)default String killstreak1SoundLocation()
	{
		return "/path/to/custom/sounds/killstreak1.wav";
	}

	@ConfigItem(
		keyName = "killstreak2Threshold",
		name = "Killstreak 2 threshold",
		description = "The number of kills required to trigger the killstreak 2 sound. 0 is disabled.",
		position = 70,
		section = killstreakSection
	)default int killstreak2Threshold()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "killstreak2SoundLocation",
		name = "Killstreak 2 sound location",
		description = "Aboslute path to a .wav file.",
		position = 75,
		section = killstreakSection
	)default String killstreak2SoundLocation()
	{
		return "/path/to/custom/sounds/killstreak2.wav";
	}

	@ConfigItem(
		keyName = "killstreak3Threshold",
		name = "Killstreak 3 threshold",
		description = "The number of kills required to trigger the killstreak 3 sound. 0 is disabled.",
		position = 80,
		section = killstreakSection
	)default int killstreak3Threshold()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "killstreak3SoundLocation",
		name = "Killstreak 3 sound location",
		description = "Aboslute path to a .wav file.",
		position = 85,
		section = killstreakSection
	)default String killstreak3SoundLocation()
	{
		return "/path/to/custom/sounds/killstreak3.wav";
	}

	@ConfigItem(
		keyName = "killstreak4Threshold",
		name = "Killstreak 4 threshold",
		description = "The number of kills required to trigger the killstreak 4 sound. 0 is disabled.",
		position = 90,
		section = killstreakSection
	)default int killstreak4Threshold()
	{
		return 6;
	}

	@ConfigItem(
		keyName = "killstreak4SoundLocation",
		name = "Killstreak 4 sound location",
		description = "Aboslute path to a .wav file.",
		position = 95,
		section = killstreakSection
	)default String killstreak4SoundLocation()
	{
		return "/path/to/custom/sounds/killstreak4.wav";
	}

	@ConfigItem(
		keyName = "killstreak5Threshold",
		name = "Killstreak 5 threshold",
		description = "The number of kills required to trigger the killstreak 5 sound. 0 is disabled.",
		position = 100,
		section = killstreakSection
	)default int killstreak5Threshold()
	{
		return 7;
	}

	@ConfigItem(
		keyName = "killstreak5SoundLocation",
		name = "Killstreak 5 sound location",
		description = "Aboslute path to a .wav file.",
		position = 105,
		section = killstreakSection
	)default String killstreak5SoundLocation()
	{
		return "/path/to/custom/sounds/killstreak5.wav";
	}

	@ConfigItem(
		keyName = "killstreak6Threshold",
		name = "Killstreak 6 threshold",
		description = "The number of kills required to trigger the killstreak 6 sound. 0 is disabled.",
		position = 110,
		section = killstreakSection
	)default int killstreak6Threshold()
	{
		return 8;
	}

	@ConfigItem(
		keyName = "killstreak6SoundLocation",
		name = "Killstreak 6 sound location",
		description = "Aboslute path to a .wav file.",
		position = 115,
		section = killstreakSection
	)default String killstreak6SoundLocation()
	{
		return "/path/to/custom/sounds/killstreak6.wav";
	}
}
