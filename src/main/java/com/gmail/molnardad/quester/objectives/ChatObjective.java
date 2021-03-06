package com.gmail.molnardad.quester.objectives;

import com.gmail.molnardad.quester.commandbase.QCommand;
import com.gmail.molnardad.quester.commandbase.QCommandContext;
import com.gmail.molnardad.quester.commandbase.exceptions.QCommandException;
import com.gmail.molnardad.quester.elements.Objective;
import com.gmail.molnardad.quester.elements.QElement;
import com.gmail.molnardad.quester.storage.StorageKey;

@QElement("CHAT")
public final class ChatObjective extends Objective {
	
	private final String regex;
	private final boolean inverted;
	
	public ChatObjective(final String regex, final boolean inverted) {
		this.regex = regex;
		this.inverted = inverted;
	}
	
	@Override
	public int getTargetAmount() {
		return 1;
	}
	
	@Override
	protected String show(final int progress) {
		final String type = inverted ? "doesn't match" : "matches";
		return "Say anything that " + type + " \"" + regex + "\".";
	}
	
	@Override
	protected String info() {
		final String flags = inverted ? " (-i)" : "";
		return "\"" + regex + "\" " + flags;
	}
	
	@QCommand(min = 1, max = 1, usage = "<regex> (-i)")
	public static Objective fromCommand(final QCommandContext context) throws QCommandException {
		
		return new ChatObjective(context.getString(0), context.hasFlag('i'));
	}
	
	@Override
	protected void save(final StorageKey key) {
		key.setString("regex", regex);
		if(inverted) {
			key.setBoolean("inverted", inverted);
		}
	}
	
	protected static Objective load(final StorageKey key) {
		final String regex = key.getString("regex", null);
		if(regex == null) {
			return null;
		}
		return new ChatObjective(regex, key.getBoolean("inverted", false));
	}
	
	public boolean matches(final String message) {
		return message.matches(regex) != inverted;
	}
}
