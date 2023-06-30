# PvP Kill Sounds
OSRS Runelite plugin to play custom sounds when you kill another player or get a killstreak in PvP.

Like this addon? [Buy me a ~~beer~~ Asgarnian Ale](https://www.buymeacoffee.com/slyt) 🍺 to show your support and motivate me to make more!

### Features
- Play audio when you kill another player in PvP.
- Play audio when reaching a configurable killstreak threshold.
- Ability to add your own `.wav` audio files (`.mp3` not supported).
- Tracks killstreaks and displays as messages. Killstreaks reset on death or plugin restart.
- Master volume control so you can fine tune sounds to fit game audio levels.

When configuring the path to custom sounds, you can either pass in a path to a directory or a single file.

Windows example:
- Directory: `C:\Users\Durial123\Desktop\custom kill sounds\`
- Single file: `C:\Users\Durial123\Desktop\custom kill sounds\derp.wav`

Linux example: 
- Directory: `/home/Durial123/Desktop/custom-kill-sounds/`
- Directory: `/home/Durial123/Desktop/custom-kill-sounds/hermygerd.wav`


If the path points to a directory, the `.wav` audio files in that directory will be played at random. If the path is invalid or no `.wav` files are found, then the default sounds included with the plugin are played.

![alt text](kill_sounds_config.png "kill sounds config screenshot")

### How sound is triggered
Due to limitations in Runelite API, it's tricky to detect when you kill another player. Ideally the audio would trigger the instant that you detect the victim has died.

I settled on triggering kill audio when a player that you have hit in the last 5 seconds shows the death animation. This is to accomodate situations where you deal damage to a player but you stop interacting with them before they die, such as multi-way combat and poison. There is a _slight_ delay (one tick?) between the victim showing 0 hp and the death animation starting, hence why the audio is slightly delayed.

Killstreaks are detected based on the game messages that say something like `"You have ground <name> into tiny pieces and sat on them."`. So if Jagex adds new kill messages, then this functionality will break. These death are a fairly reliable way to detect a legit kill, but they only show up after the victim has completed the long death animation. This is the how the killstreak audio is triggered.

The least latent want to detect a victim death is using [HitsplatApplied](https://static.runelite.net/api/runelite-api/net/runelite/api/events/HitsplatApplied.html) and [getHealthRatio()](https://static.runelite.net/api/runelite-api/net/runelite/api/Actor.html#getHealthRatio()). However, there are weird edge cases that arise when you kill/attack a player that do not have their health bar shown (e.g. flinching sitations). Hence why I went with death animation triggering instead.

## Reporting Bugs / Feature Requests
Feel free to create a git issue for feature requests or to report bugs.
