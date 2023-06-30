package com.example.killsounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
// audio file player imports
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@Slf4j
@PluginDescriptor(
	name = "PvP Kill Sounds"
)
public class KillSoundsPlugin extends Plugin
{

	Integer killStreak = 0;
	HashMap<String, Integer> killCounts = new HashMap<>();
	List<Integer> soundIds = new ArrayList<>();
	List<String> defaultKillingBlowSounds;
	HashMap<String, Long> lastHitTimeMap = new HashMap<>();
	String customSoundLocation = "";


	@Inject
	private Client client;

	@Inject
	private KillSoundsConfig config;

	@Provides // This is a Guice thing that makes it so you can use the config outside of this class
	// Guice is a dependency injection framework for Java
	KillSoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(KillSoundsConfig.class);
	}


	private static final ImmutableList<String> KILL_MESSAGES = ImmutableList.of(
		"into tiny pieces and sat on them", // You have ground <name> into tiny pieces and sat on them.
		"you have obliterated",
		"falls before your might",
		"A humiliating defeat for",
		"With a crushing blow you",
		"thinking challenging you",
		"Can anyone defeat you? Certainly",
		"was no match for you",
		"You were clearly a better fighter than",
		"RIP",
		"You have defeated",
		"What an embarrassing performance by",
		"was no match for your awesomeness",
		"cleaned the floor with", // You have cleaned the floor with <name>.
		"you just killed", // Be proud of yourself - you just  killed <name>.
		"were an orange",
		"have obliterated",
		"have stomped",// You have stomped <name> into the floor and trod on him.
		"You win," // You win, <name> loses, 'nuff said.
		); 


	public void playSoundResource(String filepath){
		log.debug("Playing sound: " + filepath);
		try {
			InputStream inputStream = getClass().getResourceAsStream(filepath);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);

			// Adjust the volume
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float volume = (float) config.volumeAmount() / 100.0f; // Set the volume level here (0.0 to 1.0)
			float minVolume = gainControl.getMinimum();
			float maxVolume = gainControl.getMaximum();
			float range = maxVolume - minVolume;
			float gain = (range * volume) + minVolume;
			gainControl.setValue(gain);

			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void playSoundFile(String filepath){
		log.debug("Playing sound: " + filepath);
		try {
			File audioFile = new File(filepath);
			InputStream inputStream = new BufferedInputStream(new FileInputStream(audioFile)); // prevents "mark/reset not supported" error
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);

			// Adjust the volume
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float volume = (float) config.volumeAmount() / 100.0f; // Set the volume level here (0.0 to 1.0)
			float minVolume = gainControl.getMinimum();
			float maxVolume = gainControl.getMaximum();
			float range = maxVolume - minVolume;
			float gain = (range * volume) + minVolume;
			gainControl.setValue(gain);

			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> loadDefaultKillingBlowSounds(){
		List<String> filenames = new ArrayList<>();
		String resourcePath = "./resources/killingBlow/";
		filenames.add(resourcePath + "fatality.wav");
		filenames.add(resourcePath + "get_wrecked.wav");

		return filenames;
	}

	public List<String> loadCustomKillingBlowSounds(String directoryPath) {
        List<String> filenames = new ArrayList<>();
		File directory = new File(directoryPath);

		if (!directory.exists()) {
			return filenames;
		}

		if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".wav")) {
                        filenames.add(file.getAbsolutePath());
                    }
                }
            }
        }
		else if(directory.isFile() && directory.getName().toLowerCase().endsWith(".wav")){ // Handle condition that the user configured a single file instead of a filepath
			filenames.add(directory.getAbsolutePath());
		}

		return filenames;

    }

	public String getRandomString(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(stringList.size());
        return stringList.get(randomIndex);
    }

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Kill Sounds started!");
		if (config.enableAudioGreeting()){
			playSoundResource("./resources/" + "kill_sounds_initiated.wav");
		}
		defaultKillingBlowSounds = loadDefaultKillingBlowSounds();
		log.debug("Loaded defaultKillingBlowSounds: "+ defaultKillingBlowSounds.toString());
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Kill Sounds stopped!");
		killStreak = 0;
	}
	

	@Subscribe
	public void onChatMessage(ChatMessage event){	
		if (event.getType() != ChatMessageType.GAMEMESSAGE){return;} // Only look for game messages
		
		String chatMessage = event.getMessage();
		if (KILL_MESSAGES.stream().anyMatch(chatMessage::contains))
		{
			killStreak++;
			if (config.enableKillstreakMessages()){
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Killstreak: " + killStreak, null);
			}

			if (config.muteKillstreakSounds()){return;} // Don't play sounds if they are muted

			

			// Play sounds below dependent on killstreak
			String killstreakDirectory = "./resources/killstreakSounds/";
			if (killStreak == config.killstreak1Threshold()){
				// if we can't find the file or directory, then play default sound
				List<String> customSoundLocation = loadCustomKillingBlowSounds(config.killstreak1SoundLocation());
				if (customSoundLocation.size() > 0){
					String killstreak1Sound = getRandomString(customSoundLocation);
					playSoundFile(killstreak1Sound);
				}else{
					playSoundResource(killstreakDirectory + "1_killing_spree.wav");
				}
			}else if (killStreak == config.killstreak2Threshold()){
				List<String> customSoundLocation = loadCustomKillingBlowSounds(config.killstreak2SoundLocation());
				if (customSoundLocation.size() > 0){
					String killstreak1Sound = getRandomString(customSoundLocation);
					playSoundFile(killstreak1Sound);
				}else{
					playSoundResource(killstreakDirectory + "2_rampage.wav");
				}
			}else if (killStreak == config.killstreak3Threshold()){
				List<String> customSoundLocation = loadCustomKillingBlowSounds(config.killstreak3SoundLocation());
				if (customSoundLocation.size() > 0){
					String killstreak1Sound = getRandomString(customSoundLocation);
					playSoundFile(killstreak1Sound);
				}else{
					playSoundResource(killstreakDirectory + "3_unstoppable.wav");
				}
			}else if (killStreak == config.killstreak4Threshold()){
				List<String> customSoundLocation = loadCustomKillingBlowSounds(config.killstreak4SoundLocation());
				if (customSoundLocation.size() > 0){
					String killstreak1Sound = getRandomString(customSoundLocation);
					playSoundFile(killstreak1Sound);
				}else{
					playSoundResource(killstreakDirectory + "4_dominating.wav");
				}
			}else if (killStreak == config.killstreak5Threshold()){
				List<String> customSoundLocation = loadCustomKillingBlowSounds(config.killstreak5SoundLocation());
				if (customSoundLocation.size() > 0){
					String killstreak1Sound = getRandomString(customSoundLocation);
					playSoundFile(killstreak1Sound);
				}else{
					playSoundResource(killstreakDirectory + "5_godlike.wav");
				}
			}else if (killStreak == config.killstreak6Threshold()){
				List<String> customSoundLocation = loadCustomKillingBlowSounds(config.killstreak6SoundLocation());
				if (customSoundLocation.size() > 0){
					String killstreak1Sound = getRandomString(customSoundLocation);
					playSoundFile(killstreak1Sound);
				}else{
					playSoundResource(killstreakDirectory + "6_legendary.wav");
				}
			}else{return;}

		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) // Restart killstreak on death
	{
		Actor actor = actorDeath.getActor();

		if (actor instanceof NPC){return;}
		Player player = (Player) actor;

		if (player == client.getLocalPlayer()){ // You died
			if (killStreak > 0){
				log.debug(killStreak + " killstreak ended!");
				if (config.enableKillstreakMessages()){
					client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", killStreak + " killstreak ended!", null);
				}
				killStreak = 0;
			}
		}

	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied){
		Actor actor = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (actor instanceof NPC){return;} 				// Only consider damage from players
		if (!hitsplat.isMine()){return;} 				// Only consider damage from player
		if (actor == client.getLocalPlayer()){return;} 	// Don't count hitsplats on yourself

		// Add play to hashmap so that we can check if we've dealt damage to this player recently
		String playerName = actor.getName();
		lastHitTimeMap.put(playerName, System.currentTimeMillis());
	}


	@Subscribe
	public void onAnimationChanged(AnimationChanged e){
		if (!(e.getActor() instanceof Player)){return;} // Only care about Players
			
		Player animatorActor = (Player) e.getActor();
		int animationID = animatorActor.getAnimation();
		if (animationID != 836){return;} // Only play sound for death animations
		
		
		String playerName = animatorActor.getName();
		if(!lastHitTimeMap.containsKey(playerName)){return;} // We've never hit this player


		long lastHitTime = lastHitTimeMap.get(playerName);
		long currentTime = System.currentTimeMillis();
		long timeSinceLastHit = currentTime - lastHitTime;
		// Print time of last hit in seconds, e.g. 3.5s and include playerName
		log.debug("You last hit " + playerName +" " +  timeSinceLastHit/1000.0 + " seconds ago");

		if (timeSinceLastHit > 5000){return;}  // Play sound if we've dealt damage to this player in the last 5 seconds and they're dying

		if (config.muteKillSounds()){return;} // Don't play sounds if they are muted
		// Play sounds
		List<String> customKillingBlowSounds = loadCustomKillingBlowSounds(config.customSoundLocation());
		if (customKillingBlowSounds.size() > 0){
			String killingBlowSound = getRandomString(customKillingBlowSounds);
			playSoundFile(killingBlowSound);
		}else{
			String killBlowSound = getRandomString(defaultKillingBlowSounds); // TODO: Errors if empty string returned
			playSoundResource(killBlowSound);
		}
	}

}
