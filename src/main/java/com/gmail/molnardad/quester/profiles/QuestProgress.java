package com.gmail.molnardad.quester.profiles;

import java.util.Arrays;
import java.util.List;

import com.gmail.molnardad.quester.Quest;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.elements.Objective;
import com.gmail.molnardad.quester.managers.QConfiguration;
import com.gmail.molnardad.quester.storage.StorageKey;
import com.gmail.molnardad.quester.utils.Util;

public class QuestProgress {
	
	final Quest quest;
	int[] progress;
	ObjectiveStatus[] objectiveStatuses;
	
	public enum ObjectiveStatus {
		INACTIVE,
		ACTIVE,
		COMPLETED,
		DISABLED;
	}
	
	QuestProgress(Quest quest) {
		if(quest == null) {
			throw new NullPointerException("Cannot create QuestProgress for null Quest.");
		}
		this.quest = quest;
		progress = new int[quest.getObjectives().size()];
		objectiveStatuses = new ObjectiveStatus[quest.getObjectives().size()];
		for(int i=0; i < objectiveStatuses.length; i++) {
			objectiveStatuses[i] = ObjectiveStatus.INACTIVE;
		}
		updateObjectives();
	}
	
	void addToProgress(int objectiveID, int toAdd) {
		if(objectiveID >= 0 || objectiveID < progress.length) {
		}
	}
	
	public ObjectiveStatus[] getObjectiveStatuses() {
		return Arrays.copyOf(objectiveStatuses, objectiveStatuses.length);
	}
	
	public ObjectiveStatus getObjectiveStatus(int objectiveID) {
		if(objectiveID < 0 || objectiveID >= objectiveStatuses.length) {
			return null;
		}
		return objectiveStatuses[objectiveID];
	}
	
	private void updateObjectives() {
		List<Objective> objectives = quest.getObjectives();
		for(int i=0; i<objectives.size(); i++) {
			if(objectives.get(i).isComplete(progress[i])) {
				objectiveStatuses[i] = ObjectiveStatus.COMPLETED;
				continue;
			}
		}
		objectives:
		for(int i=0; i<objectives.size(); i++) {
			if(objectiveStatuses[i] != ObjectiveStatus.COMPLETED) {
				for(int p : objectives.get(i).getPrerequisites()) {
					if(objectiveStatuses[p] != ObjectiveStatus.COMPLETED) {
						objectiveStatuses[i] = ObjectiveStatus.INACTIVE;
						continue objectives;
					}
				}
				objectiveStatuses[i] = ObjectiveStatus.ACTIVE;
			}
		}
	}
	
	public int getSize() {
		return progress.length;
	}
	
	public Quest getQuest() {
		return quest;
	}
	
	public boolean setProgress(int objectiveID, int newValue) {
		if(getObjectiveStatus(objectiveID) == ObjectiveStatus.ACTIVE) {
			
			return true;
		}
		return false;
	}
	
	public int[] getProgress() {
		return progress;
	}
	
	public int getCurrentObjectiveID() {
		for(int i=0; i<objectiveStatuses.length; i++) {
			if(objectiveStatuses[i] == ObjectiveStatus.ACTIVE) {
				return i;
			}
		}
		return -1;
	}
	
	public static QuestProgress getEmptyProgress(Quest quest) {
		return new QuestProgress(quest);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof QuestProgress) {
			QuestProgress prg = (QuestProgress) obj;
			return this.quest.equals(prg.quest);
		}
		return false;
	}
	
	void serialize(StorageKey key) {
		key.setString("progress", Util.implodeInt(progress, "|"));
	}
	
	static QuestProgress deserialize(StorageKey key, Quest quest) {
		String progressString = null;
		if(!key.getSubKeys().isEmpty()) {
			progressString = key.getString("progress");
		}
		else if(key.getString("") != null) {
			progressString = key.getString("");
		}
		
		if(progressString == null) {
			if(QConfiguration.verbose) {
				Quester.log.info("Invalid or missing progress for quest '" + key + "' in profile.");
			}
		}
		QuestProgress prog = null;
		try {
			prog = new QuestProgress(quest);
			String[] strs = progressString.split("\\|");
			if(strs.length != 1 || !strs[0].isEmpty()) {
				for(int i=0; i < strs.length; i++) {
					prog.setProgress(i, Integer.parseInt(strs[i]));
				}
			}		
		} catch (Exception e) {
			return null;
		}
		
		return prog;
	}
}
