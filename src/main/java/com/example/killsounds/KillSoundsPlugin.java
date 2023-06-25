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
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
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
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "v0.0.0 Kill Sounds started", null);
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned playerDespawned){
		final Player player = playerDespawned.getPlayer();
		//String playerName = player.getName();
		//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("%s has despawned", playerName), null);
		
		// Only care about dead Players
		if (player.getHealthRatio() != 0)
		{
			return;
		}

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("onPlayerDespawned: %s (%d) has died", player.getName(), player.getCombatLevel()), null);
	
		// detect if you killed the player
		if (client.getLocalPlayer().getInteracting() == player // Your attacking the player
		 && player.getHealthRatio() == 0) // player has 0 health
		 {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You killed " + player.getName(), null);
		}

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned){ //detect when you kill an NPC
		final NPC npc = npcDespawned.getNpc();
		int id = npc.getId();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", npc.getName() + " (" + id + ") despawned...", null);
		
		if (client.getLocalPlayer().getInteracting() == npc // Your attacking the player
		 && npc.getHealthRatio() == 0) // player has 0 health
		 {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You killed " + npc.getName(), null);
		}

	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{

		// log.info("Chat message type: " + event.getType());
		// log.info("Message: " + event.getMessage());
		// log.info("Sender: " + event.getSender());
		// log.info("Name: " + event.getName());

		
		if (event.getType() != ChatMessageType.GAMEMESSAGE) // Only look for game messages
		{return;	}

		String chatMessage = event.getMessage();
		if (KILL_MESSAGES.stream().anyMatch(chatMessage::contains))
		{
			log.info("Detected kill via chat message!: \"" + chatMessage +"\"");
			killStreak++;
			log.info("Killstreak +1; Total Kills: " + killStreak);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Killstreak +1; Total Kills: " + killStreak, null);
		}
	}

	@Subscribe
	public void onPlayerLootReceived(final PlayerLootReceived playerLootReceived) throws IOException { // This appears to be the preferred way to detect pvp kills
		
		final Player victim = playerLootReceived.getPlayer();
		//final Collection<ItemStack> items = playerLootReceived.getItems();
		final String victimName = victim.getName();
		final int victimCombat = victim.getCombatLevel();
		log.info("Detected playerLootReceived from victim " + victimName + " - level " + victimCombat);
	
		if (killCounts.containsKey(victimName)) {
			Integer victimKillCount = killCounts.get(victimName);
			victimKillCount++;
			killCounts.put(victimName, victimKillCount);
			log.info("You have killed " + victimName + " (" + victimCombat +") " + victimKillCount + " times!");
		} else {
			killCounts.put(victimName, 1);
			log.info("You have killed " + victimName + " " + 1 + " time!");
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) // Restart killstreak on death
	{
		Actor actor = actorDeath.getActor();

		if (actor instanceof NPC){
			return;
		}
		Player player = (Player) actor;

		if (player == client.getLocalPlayer()){
			if (killStreak > 0){
				log.info(killStreak + " killstreak ended!");
				client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", killStreak + " killstreak ended!", null);
				killStreak = 0;
			}
		}

	}


	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event){
		Integer soundId = event.getSoundId();
		
		soundIds.add(soundId);
		log.info("Sound effect played: " + soundId);
	}


	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		// if (!(e.getActor() instanceof  NPC) || !(e.getActor() instanceof Player)) // Only look for Player and NPC animators
		// 	return;
		
		if (e.getActor() instanceof NPC){

			NPC animatorActor = (NPC) e.getActor();
			int npcId = animatorActor.getId();

			String npcName = animatorActor.getName();
			int npcLevel = animatorActor.getCombatLevel();
			int animationID = animatorActor.getAnimation();

						
			if (npcId == -1 || animationID == -1)
			return; // Don't bother with null NPC and idle animations

			switch (animationID)
			{
				case 6182: // Goblin death animation
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You killed a goblin " + npcName, null);
					break; // Apparently break statements are important in Java! Include in every case!
				case 6183: // Gobline being attacked animation
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You attacked a goblin  " + npcName, null);
					break;
				case 836: // player death animation
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You killed! " + npcName, null);
				case -1:
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", " npcName: " + npcName + " npcId: " + npcId + " npcLevel: " + npcLevel + " animationID: " + animationID, null);
					break;
			}
		}

		if (e.getActor() instanceof Player){
			Player animatorActor = (Player) e.getActor();
			//int playerId = animatorActor.getId();

			String playerName = animatorActor.getName();
			//int playerLevel = animatorActor.getCombatLevel();
			int animationID = animatorActor.getAnimation();

			if (animationID == 836) // player death animation
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Death animation detected that player died: " + playerName, null);
			}

			// detect if we're interacting with this player
			if (client.getLocalPlayer().getInteracting() == null){return;}
			if (client.getLocalPlayer().getInteracting() != animatorActor){return;} // TODO: Fix null pointer exception here
			log.info("You're interacting with " + playerName); // This works, "You're interacting with HJYJHGJTHGJY!" in chat


			if (animatorActor.getHealthRatio() != 0){return;} // Only look for dead animators
			log.info(playerName + " has no health!");

			if (config.enableCustomSounds()){
				playSoundFile(config.customSoundLocation().strip());
				//playSoundFile("./resources/customSounds/you_dead.wav");
			}else{	
				String killBlowSound = getRandomString(killBlowSounds); // TODO: Error if empty string returned
				playSoundResource("./resources/killingBlow/" + killBlowSound);
			}

			if (animationID != 836){return;} // Only look for death animations
			log.info(playerName + " is animating death!");
			

		}
	}


}
