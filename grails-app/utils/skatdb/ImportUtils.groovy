package skatdb

import groovy.json.JsonSlurper;

import java.util.ArrayList;
import java.util.Date;


import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

abstract class ImportUtils {

	def doWithTransaction = { def closure ->
		Transaction tr = session.beginTransaction()
		try {
			closure()
			tr.commit()
		} catch(Throwable t) {
			tr.rollback()
			throw new HibernateException("Exception while transaction. The transaction is rolled back.", t)
		}
	}

	public static ArrayList<String> doTextImport(String groupId, String importString, Date fakeDate) {
		SkatGroup group = SkatGroup.findById(groupId)
		def errorList = []
		importString = importString.toLowerCase()
		new StringReader(importString).eachLine {
			try {
				if(it.trim().length() == 0) {
					if(fakeDate != null) {
						fakeDate = fakeDate.plus(1)
					}
				} else {
					String[] parts = it.split(" ")
					Game game = new Game();
					if(fakeDate != null) {
						game.createDate = fakeDate
					}
					game.group = group
					String name = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1)
					game.player = findOrCreatePlayer(name)
					game.bid = Integer.valueOf(parts[1])
					game.hand = it.contains(" hand")
					game.won = !it.contains(" verloren")
					boolean ouvert = it.contains(" ouvert")
					boolean schneider = it.contains(" schneider")
					boolean schwarz = it.contains(" schwarz")
					boolean angesagt = it.contains(" angesagt")
					boolean spritze = it.contains(" spritze")
					boolean reh = it.contains(" reh")
					boolean hirsch = it.contains(" hirsch")
					
	
					int gameType = getGameTypeOfString(it)
					if(gameType == 23) {
						if(game.hand && ouvert) {
							gameType = 59
						} else if(game.hand) {
							gameType = 35
						} else if(ouvert) {
							gameType = 46
						}
					}
					game.gameType = gameType
	
					if(!game.isNullGame()) {
						if(it.contains(" mit ") || it.contains(" ohne ")) {
							int index = Math.max(it.indexOf(" mit ") + 5, it.indexOf(" ohne ") + 6)
							game.jacks = Integer.valueOf(it.substring(index, index + 1))
						}
						game.gameLevel = getGameLevel(game.hand, schneider, schwarz, ouvert, angesagt)
					}
					game.announcement = hirsch ? 8 : reh ? 4 : spritze ? 2 : 1
					game.save()
				}
			} catch(Exception exc) {
				log.error("while parsing line: " + it, exc)
				errorList.push(it)
			}
		}
		return errorList;
	}

	private static int getGameTypeOfString(String line) {
		if(line.contains(" schellen")) {
			return 9;
		} else if(line.contains(" rot")) {
			return 10;
		} else if(line.contains(" grün")) {
			return 11;
		}else if(line.contains(" eicheln")) {
			return 12;
		} else if(line.contains(" grand")) {
			return 24;
		} else if(line.contains(" null")) {
			return 23;
		}
		throw new IllegalArgumentException(line)
	}

	private static int getGameLevel(boolean hand, boolean schneider, boolean schwarz, boolean ouvert, boolean angesagt) {
		if(hand) {
			if(ouvert) {
				return 7
			}
			if(schwarz && angesagt) {
				return 6
			}
			if(schwarz) {
				return 5
			}
			if(schneider && angesagt) {
				return 4
			}
			if(schneider) {
				return 3
			}
			return 2
		} else {
			if(schwarz) {
				return 3
			}
			if(schneider) {
				return 2
			}
			return 1
		}
	}

	public static ArrayList<String> doJSONImport(String json) {
		def errorList = []
		JsonSlurper slurper = new JsonSlurper()
		def result = slurper.parseText(json)
		def closure = {
			for(Object jsonGame in result) {
				try {
					Game game = new Game()
					game.group = findOrCreateGroup(jsonGame.group)
					game.player = findOrCreatePlayer(jsonGame.player)
					game.bid = jsonGame.bid  != null ? jsonGame.bid : 18
					game.gameType = jsonGame.gameType != null ? jsonGame.gameType : 9
					game.hand = jsonGame.hand != null ? jsonGame.hand : false
					game.gameLevel = jsonGame.gameLevel != null ? jsonGame.gameLevel : 1
					// ramsch uses jacks as value
					game.jacks =  jsonGame.bid == null || jsonGame.bid != 0 ? (jsonGame.jacks != null ? jsonGame.jacks : 1) : (jsonGame.value  / jsonGame.gameLevel)
					game.announcement = jsonGame.announcement != null ? jsonGame.announcement : 1
					game.won = jsonGame.won != null ? jsonGame.won : true
					game.createDate = jsonGame.createDate != null ? new Date(jsonGame.createDate) : new Date()
					game.modifyDate = jsonGame.modifyDate != null ? new Date(jsonGame.modifyDate) : new Date()
					game.save()
				} catch(Exception exc) {
					Logger.getLogger(this).error("while parsing json object: " + jsonGame, exc)
					errorList.push(jsonGame)
				}
			}
		}
		this.doWithTransaction(closure)
		return errorList
	}

	public static SkatGroup findOrCreateGroup(String name) {
		SkatGroup group = SkatGroup.findByName(name)
		if(group == null) {
			group = new SkatGroup()
			group.name = name
			group.save()
		}
		return group
	}

	public static Player findOrCreatePlayer(String name) {
		Player player = Player.findByName(name)
		if(player == null) {
			player = new Player()
			player.name = name
			player.save()
		}
		return player
	}
}
