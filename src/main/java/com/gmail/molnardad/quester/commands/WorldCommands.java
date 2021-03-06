package com.gmail.molnardad.quester.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.gmail.molnardad.quester.QConfiguration;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.commandbase.QCommand;
import com.gmail.molnardad.quester.commandbase.QCommandContext;
import com.gmail.molnardad.quester.commandbase.QCommandLabels;
import com.gmail.molnardad.quester.commandbase.exceptions.QCommandException;
import com.gmail.molnardad.quester.exceptions.QuesterException;
import com.gmail.molnardad.quester.profiles.ProfileManager;
import com.gmail.molnardad.quester.quests.QuestManager;

public class WorldCommands {
	
	final QuestManager qMan;
	final ProfileManager profMan;
	
	public WorldCommands(final Quester plugin) {
		qMan = plugin.getQuestManager();
		profMan = plugin.getProfileManager();
	}
	
	@QCommandLabels({ "add", "a" })
	@QCommand(section = "QMod", desc = "adds quest world", min = 1, max = 1, usage = "{<world>}")
	public void set(final QCommandContext context, final CommandSender sender) throws QuesterException, QCommandException {
		World world = null;
		if(context.getString(0).equalsIgnoreCase(QConfiguration.worldLabelThis)) {
			if(context.getPlayer() != null) {
				world = context.getPlayer().getWorld();
			}
			else {
				throw new QCommandException(context.getSenderLang().get("ERROR_CMD_WORLD_THIS")
						.replaceAll("%this", QConfiguration.worldLabelThis));
			}
		}
		else {
			world = sender.getServer().getWorld(context.getString(0));
		}
		if(world == null) {
			throw new QCommandException(context.getSenderLang().get("ERROR_CMD_WORLD_INVALID"));
		}
		qMan.addQuestWorld(profMan.getProfile(sender.getName()), world.getName(),
				context.getSenderLang());
		sender.sendMessage(ChatColor.GREEN + context.getSenderLang().get("Q_WORLD_ADDED"));
	}
	
	@QCommandLabels({ "remove", "r" })
	@QCommand(section = "QMod", desc = "removes quest world", min = 1, max = 1, usage = "{<world>}")
	public void remove(final QCommandContext context, final CommandSender sender) throws QuesterException, QCommandException {
		String worldName = context.getString(0);
		if(context.getString(0).equalsIgnoreCase(QConfiguration.worldLabelThis)) {
			if(context.getPlayer() != null) {
				worldName = context.getPlayer().getWorld().getName();
			}
			else {
				throw new QCommandException(context.getSenderLang().get("ERROR_CMD_WORLD_THIS")
						.replaceAll("%this", QConfiguration.worldLabelThis));
			}
		}
		if(qMan.removeQuestWorld(profMan.getProfile(sender.getName()), worldName,
				context.getSenderLang())) {
			sender.sendMessage(ChatColor.GREEN + context.getSenderLang().get("Q_WORLD_REMOVED"));
		}
		else {
			throw new QCommandException(context.getSenderLang().get("ERROR_WORLD_NOT_ASSIGNED"));
		}
	}
}
