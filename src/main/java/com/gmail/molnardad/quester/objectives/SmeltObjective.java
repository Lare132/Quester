package com.gmail.molnardad.quester.objectives;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.molnardad.quester.commandbase.QCommand;
import com.gmail.molnardad.quester.commandbase.QCommandContext;
import com.gmail.molnardad.quester.commandbase.exceptions.QCommandException;
import com.gmail.molnardad.quester.elements.Objective;
import com.gmail.molnardad.quester.elements.QElement;
import com.gmail.molnardad.quester.storage.StorageKey;
import com.gmail.molnardad.quester.utils.SerUtils;

@QElement("SMELT")
public final class SmeltObjective extends Objective {
	
	private final Material material;
	private final short data;
	private final int amount;
	
	public SmeltObjective(final int amt, final Material mat, final int dat) {
		material = mat;
		amount = amt;
		data = (short) dat;
	}
	
	@Override
	public int getTargetAmount() {
		return amount;
	}
	
	@Override
	protected String show(final int progress) {
		final String datStr = data < 0 ? " " : " (data " + data + ") ";
		final String pcs = amount - progress == 1 ? " piece of " : " pieces of ";
		final String mat = material.getId() == 351 ? "dye" : material.name().toLowerCase();
		return "Smelt " + (amount - progress) + pcs + mat + datStr + ".";
	}
	
	@Override
	protected String info() {
		final String dataStr = data < 0 ? "" : ":" + data;
		return material.name() + "[" + material.getId() + dataStr + "]; AMT: " + amount;
	}
	
	@QCommand(min = 2, max = 2, usage = "{<item>} <amount>")
	public static Objective fromCommand(final QCommandContext context) throws QCommandException {
		final int[] itm = SerUtils.parseItem(context.getString(0));
		final Material mat = Material.getMaterial(itm[0]);
		final int dat = itm[1];
		final int amt = context.getInt(1);
		if(amt < 1 || dat < -1) {
			throw new QCommandException(context.getSenderLang().get("ERROR_CMD_ITEM_NUMBERS"));
		}
		return new SmeltObjective(amt, mat, dat);
	}
	
	@Override
	protected void save(final StorageKey key) {
		key.setString("item", SerUtils.serializeItem(material, data));
		if(amount > 1) {
			key.setInt("amount", amount);
		}
	}
	
	protected static Objective load(final StorageKey key) {
		Material mat;
		int dat, amt = 1;
		try {
			final int[] itm = SerUtils.parseItem(key.getString("item", ""));
			mat = Material.getMaterial(itm[0]);
			dat = itm[1];
		}
		catch (final IllegalArgumentException e) {
			return null;
		}
		amt = key.getInt("amount", 1);
		if(amt < 1) {
			amt = 1;
		}
		return new SmeltObjective(amt, mat, dat);
	}
	
	// Custom methods
	
	public boolean check(final ItemStack item) {
		if(item.getTypeId() != material.getId()) {
			return false;
		}
		if(item.getDurability() != data && data >= 0) {
			return false;
		}
		return true;
	}
}
