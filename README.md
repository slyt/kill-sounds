# Kill Sounds
OSRS Runelite plugin to play custom sounds when you kill another player.

### Features
- Plays audio when you kill another player
- Ability to add your own `.wav` audio files (`.mp3` not supported)
- Tracks killstreaks and displays as messages. Killstreaks reset on death or plugin restart.

![alt text](kill_sounds_config.png "kill sounds config screenshot")

### Details
It's tricky to detect when you kill another player. Ideally the audio would trigger the instant that you detect the victim has died.

I settled on triggering audio when a player that you have hit in the last 5 seconds shows the death animation. This is to accomodate situations where you deal damage to a player but you stop interacting with them before they die, such as multi-way combat and poison. There is a slight delay between the victim showing 0 hp and the death animation starting, hence why the audio is slightly delayed.

Killstreaks are detected based on the game messages that say something like `"You have ground <name> into tiny pieces and sat on them."`. So if Jagex adds new kill messages, then this functionality will break. These death game messages are more reliable, but show up after the victim has completed the long death animation, so it is not a good trigger for audio.

The least latent want to detect a victim death is using [HitsplatApplied](https://static.runelite.net/api/runelite-api/net/runelite/api/events/HitsplatApplied.html) and [getHealthRatio()](https://static.runelite.net/api/runelite-api/net/runelite/api/Actor.html#getHealthRatio()). However, there are weird edge cases that arise when you kill/attack a player that do not have their health bar shown (e.g. flinching sitations). Hence why I went with death animation audio triggering instead.

