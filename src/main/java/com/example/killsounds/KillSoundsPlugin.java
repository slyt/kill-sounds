package com.example.killsounds;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
// audio file player imports
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Kill Sounds"
)
public class KillSoundsPlugin extends Plugin
{

	Integer killStreak = 0;
	HashMap<String, Integer> killCounts = new HashMap<>();
	List<Integer> soundIds;

	@Inject
	private Client client;

	@Inject
	private KillSoundsConfig config;

	private static final ImmutableList<String> KILL_MESSAGES = ImmutableList.of("into tiny pieces and sat on them", "you have obliterated",
		"falls before your might", "A humiliating defeat for", "With a crushing blow you", "thinking challenging you",
		"Can anyone defeat you? Certainly", "was no match for you", "You were clearly a better fighter than", "RIP",
		"You have defeated", "What an embarrassing performance by", "was no match for your awesomeness");


	@Override
	protected void startUp() throws Exception
	{
		log.info("Kill Sounds started!");
		// play fatality.wav
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.getClass().getResourceAsStream("fatality.wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "v0.0.0 Kill Sounds says " + config.greeting(), null);
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

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("%s (%d) has died", player.getName(), player.getCombatLevel()), null);
	
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
		{
			return;
		}

		String chatMessage = event.getMessage();
		if (KILL_MESSAGES.stream().anyMatch(chatMessage::contains))
		{
			log.info("Detected kill via chat message!: \"" + chatMessage +"\"");
		}


		// TODO: make regex to detect all kill messages
		// String killMessage = "You win, Fuungi724 loses, 'nuff said.";
		// if (event.getType() == ChatMessageType.GAMEMESSAGE && event.getMessage().contains(killMessage)){
		// 	client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Chat message detected!", null);
		// }
		

		// if (event.getMessage().contains("You have defeated")){
		// 	client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Chat message detected that you killed someone!", null);
		// }
	}

	@Subscribe
	public void onPlayerLootReceived(final PlayerLootReceived playerLootReceived) throws IOException { // This appears to be the preferred way to detect pvp kills
		
		final Player victim = playerLootReceived.getPlayer();
		//final Collection<ItemStack> items = playerLootReceived.getItems();
		final String victimName = victim.getName();
		final int victimCombat = victim.getCombatLevel();
		log.info("Detected playerLootReceived from victim " + victimName + " - level " + victimCombat);
		killStreak++;
		log.info("Killstreak +1; Total Kills: " + killStreak);

	
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
			log.info(killStreak + " killstreak ended!");
			killStreak = 0;
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

			// detect if we're interacting with this player
			if (client.getLocalPlayer().getInteracting() != animatorActor)
			{
				return;
			} else{
				log.info("You're interacting with " + playerName); // This works, "You're interacting with HJYJHGJTHGJY!" in chat
			}

			if (animatorActor.getHealthRatio() != 0) // Only look for dead animators
			{
				return;
			} else{
				log.info(playerName + " has no health!");
			}

			if (animationID != 836) // Only look for death animations
			{
				return;
			} else{
				log.info(playerName + " is animating death!");
				log.info("soundIds list:" + soundIds.toString());
			}

			if (animationID == 836) // player death animation
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Death animation detected that player died: " + playerName, null);
			}
		}
	}

	@Provides
	KillSoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(KillSoundsConfig.class);
	}
}
