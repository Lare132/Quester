package com.gmail.molnardad.quester.listeners;

import java.util.List;

import me.ThaH3lper.com.Api.BossDeathEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.molnardad.quester.ActionSource;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.elements.Objective;
import com.gmail.molnardad.quester.objectives.BossObjective;
import com.gmail.molnardad.quester.profiles.PlayerProfile;
import com.gmail.molnardad.quester.profiles.ProfileManager;
import com.gmail.molnardad.quester.quests.Quest;

public class BossDeathListener implements Listener {
	
	private final ProfileManager profMan;
	
	public BossDeathListener(final Quester plugin) {
		profMan = plugin.getProfileManager();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBossDeath(final BossDeathEvent event) {
		final Player player = event.getPlayer();
		if(player == null) {
			return;
		}
		final PlayerProfile prof = profMan.getProfile(player.getName());
		final Quest quest = prof.getQuest();
		if(quest != null) {
			if(!quest.allowedWorld(player.getWorld().getName().toLowerCase())) {
				return;
			}
			final List<Objective> objs = quest.getObjectives();
			for(int i = 0; i < objs.size(); i++) {
				if(objs.get(i).getType().equalsIgnoreCase("BOSS")) {
					final BossObjective obj = (BossObjective) objs.get(i);
					if(!profMan.isObjectiveActive(prof, i)) {
						continue;
					}
					if(obj.nameCheck(event.getBossName())) {
						profMan.incProgress(player, ActionSource.listenerSource(event), i);
						return;
					}
				}
			}
		}
	}
}
