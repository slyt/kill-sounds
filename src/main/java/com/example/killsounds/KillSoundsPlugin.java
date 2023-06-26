package com.example.killsounds;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
// audio file player imports
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigItem;

@Slf4j
@PluginDescriptor(
	name = "Kill Sounds"
)
public class KillSoundsPlugin extends Plugin
{

	Integer killStreak = 0;
	HashMap<String, Integer> killCounts = new HashMap<>();
	List<Integer> soundIds = new ArrayList<>();
	List<String> killBlowSounds;
	HashMap<String, Long> lastHitTimeMap = new HashMap<>();


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
		log.info("Playing sound: " + filepath);
		try {
			InputStream inputStream = getClass().getResourceAsStream(filepath);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void playSoundFile(String filepath){
		log.info("Playing sound: " + filepath);
		try {
			File audioFile = new File(filepath);
			InputStream inputStream = new BufferedInputStream(new FileInputStream(audioFile)); // prevents "mark/reset not supported" error
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> loadFilenames(String directoryPath) {
        List<String> filenames = new ArrayList<>();

        // try {
            Path directory = Paths.get(directoryPath);

			System.out.println("Directory path: " + directoryPath);
    		System.out.println("Is directory? " + Files.isDirectory(directory)); // Why isn't this true?
			// String classpath = System.getProperty("java.class.path");
			// System.out.println("Classpath: " + classpath);


			// hardcode filenames for from ./resoureces/killingBlow
			filenames.add("eat_shit_and_die.wav");
			filenames.add("fatality.wav");
			filenames.add("get_fucked.wav");
			filenames.add("get_wrecked.wav");
			filenames.add("see_you_in_lumby.wav");


            //if (Files.isDirectory(directory)) {
                // Files.list(directory)
                //         .filter(Files::isRegularFile)
                //         .map(Path::getFileName)
                //         .map(Path::toString)
                //         .forEach(filenames::add);
            //}
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

		// print filenames to console
		for (String filename : filenames) {
			log.info("Filename: " + filename);
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
		log.info("Kill Sounds started!");
		playSoundResource("./resources/" + "kill_sounds_initiated.wav");
		killBlowSounds = loadFilenames("./resources/killingBlow");
		log.info(killBlowSounds.toString());
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Kill Sounds stopped!");
	}
	

	@Subscribe
	public void onChatMessage(ChatMessage event){	
		if (event.getType() != ChatMessageType.GAMEMESSAGE){return;} // Only look for game messages
		
		String chatMessage = event.getMessage();
		if (KILL_MESSAGES.stream().anyMatch(chatMessage::contains))
		{
			killStreak++;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Killstreak: " + killStreak, null);
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
				log.info(killStreak + " killstreak ended!");
				client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", killStreak + " killstreak ended!", null);
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
			log.info("You last hit " + playerName +" " +  timeSinceLastHit/1000.0 + " seconds ago");

			if (timeSinceLastHit < 5000){ // If we've dealt damage to this player in the last 5 seconds
				// Play sounds
				if (config.enableCustomSounds()){
					playSoundFile(config.customSoundLocation().strip());
					//playSoundFile("./resources/customSounds/you_dead.wav");
				}else{	
					String killBlowSound = getRandomString(killBlowSounds); // TODO: Errors if empty string returned
					playSoundResource("./resources/killingBlow/" + killBlowSound);
			}
			}
	}

}
