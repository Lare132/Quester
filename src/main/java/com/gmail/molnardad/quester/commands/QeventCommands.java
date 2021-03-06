package com.gmail.molnardad.quester.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.molnardad.quester.QConfiguration;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.commandbase.QCommand;
import com.gmail.molnardad.quester.commandbase.QCommandContext;
import com.gmail.molnardad.quester.commandbase.QCommandLabels;
import com.gmail.molnardad.quester.commandbase.exceptions.QCommandException;
import com.gmail.molnardad.quester.elements.ElementManager;
import com.gmail.molnardad.quester.elements.Qevent;
import com.gmail.molnardad.quester.exceptions.ElementException;
import com.gmail.molnardad.quester.exceptions.QeventException;
import com.gmail.molnardad.quester.exceptions.QuesterException;
import com.gmail.molnardad.quester.lang.QuesterLang;
import com.gmail.molnardad.quester.profiles.ProfileManager;
import com.gmail.molnardad.quester.quests.QuestManager;
import com.gmail.molnardad.quester.utils.SerUtils;

public class QeventCommands {
	
	final QuestManager qMan;
	final ElementManager eMan;
	final ProfileManager profMan;
	final Quester plugin;
	
	public QeventCommands(final Quester plugin) {
		qMan = plugin.getQuestManager();
		eMan = plugin.getElementManager();
		profMan = plugin.getProfileManager();
		this.plugin = plugin;
	}
	
	private Qevent getQevent(final String type, final String occassion, final QCommandContext subContext, final QuesterLang lang) throws QeventException, QCommandException, QuesterException {
		int[] occasion;
		try {
			occasion = SerUtils.deserializeOccasion(occassion, lang);
		}
		catch (final IllegalArgumentException e) {
			throw new QCommandException(e.getMessage());
		}
		if(!eMan.isEvent(type)) {
			subContext.getSender().sendMessage(ChatColor.RED + lang.get("ERROR_EVT_NOT_EXIST"));
			subContext.getSender().sendMessage(
					ChatColor.RED + lang.get("EVT_LIST") + ": " + ChatColor.WHITE
							+ eMan.getEventList());
			throw new QeventException(lang.get("ERROR_EVT_NOT_EXIST"));
		}
		final Qevent evt = eMan.getEventFromCommand(type, subContext);
		if(evt != null) {
			evt.setOccasion(occasion[0], occasion[1]);
		}
		else {
			throw new ElementException(lang.get("ERROR_ELEMENT_FAIL"));
		}
		return evt;
	}
	
	@QCommandLabels({ "run" })
	@QCommand(
			section = "Admin",
			desc = "runs an event",
			min = 1,
			usage = "<event type> [args]",
			permission = QConfiguration.PERM_ADMIN)
	public void run(final QCommandContext context, final CommandSender sender) throws QCommandException, QuesterException {
		if(!(sender instanceof Player)) {
			throw new QCommandException("This command requires player context.");
		}
		final QuesterLang lang = context.getSenderLang();
		final String type = context.getString(0);
		Qevent qevent;
		try {
			qevent = getQevent(type, "0", context.getSubContext(1), lang);
		}
		catch (final QeventException e) {
			return;
		}
		qevent.execute(context.getPlayer(), plugin);
	}
	
	@QCommandLabels({ "runas" })
	@QCommand(
			section = "Admin",
			desc = "runs an event as player",
			min = 3,
			max = 3,
			usage = "<player> <quest id> <event id>",
			permission = QConfiguration.PERM_ADMIN)
	public void runas(final QCommandContext context, final CommandSender sender) throws QCommandException, QuesterException {
		final Player player = Bukkit.getPlayerExact(context.getString(0));
		final QuesterLang lang = context.getSenderLang();
		if(player == null) {
			throw new QCommandException(lang.get("ERROR_CMD_PLAYER_OFFLINE").replaceAll("%p",
					context.getString(0)));
		}
		
		final Qevent qevent;
		final int questId = context.getInt(1);
		final int eventId = context.getInt(2);
		try {
			qevent = qMan.getQuest(questId).getQevent(eventId);
		}
		catch (final Exception e) {
			throw new QeventException(lang.get("ERROR_EVT_NOT_EXIST"));
		}
		qevent.execute(context.getPlayer(), plugin);
	}
	
	@QCommandLabels({ "add", "a" })
	@QCommand(
			section = "QMod",
			desc = "adds an event",
			min = 2,
			usage = "{<occasion>} <event type> [args]")
	public void add(final QCommandContext context, final CommandSender sender) throws QCommandException, QuesterException {
		final QuesterLang lang = context.getSenderLang();
		final String type = context.getString(1);
		Qevent qevent;
		try {
			qevent = getQevent(type, context.getString(0), context.getSubContext(2), lang);
		}
		catch (final QeventException e) {
			return;
		}
		qMan.addQuestQevent(profMan.getProfile(sender.getName()), qevent, lang);
		sender.sendMessage(ChatColor.GREEN
				+ lang.get("EVT_ADD").replaceAll("%type", type.toUpperCase()));
	}
	
	@QCommandLabels({ "set", "s" })
	@QCommand(
			section = "QMod",
			desc = "sets an event",
			min = 3,
			usage = "<evt ID> {<occasion>} <evt type> [args]")
	public void set(final QCommandContext context, final CommandSender sender) throws QCommandException, QuesterException {
		final QuesterLang lang = context.getSenderLang();
		final String type = context.getString(2);
		final int qeventID = context.getInt(0);
		Qevent qevent;
		try {
			qevent = getQevent(type, context.getString(1), context.getSubContext(3), lang);
		}
		catch (final QeventException e) {
			return;
		}
		qMan.setQuestQevent(profMan.getProfile(sender.getName()), qeventID, qevent, lang);
		sender.sendMessage(ChatColor.GREEN
				+ lang.get("EVT_SET").replaceAll("%type", type.toUpperCase()));
	}
	
	@QCommandLabels({ "remove", "r" })
	@QCommand(section = "QMod", desc = "removes event", min = 1, max = 1, usage = "<event ID>")
	public void remove(final QCommandContext context, final CommandSender sender) throws QuesterException {
		qMan.removeQuestQevent(profMan.getProfile(sender.getName()), context.getInt(0),
				context.getSenderLang());
		sender.sendMessage(ChatColor.GREEN
				+ context.getSenderLang().get("EVT_REMOVE").replaceAll("%id", context.getString(0)));
	}
	
	@QCommandLabels({ "list", "l" })
	@QCommand(section = "QMod", max = 0, desc = "event list")
	public void list(final QCommandContext context, final CommandSender sender) throws QuesterException {
		sender.sendMessage(ChatColor.RED + context.getSenderLang().get("EVT_LIST") + ": "
				+ ChatColor.WHITE + eMan.getEventList());
	}
}