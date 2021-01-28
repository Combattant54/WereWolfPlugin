package io.github.ph1lou.werewolfplugin.scoreboards;

import fr.mrmicky.fastboard.FastBoard;
import io.github.ph1lou.werewolfapi.LoverAPI;
import io.github.ph1lou.werewolfapi.ModerationManagerAPI;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.ScoreAPI;
import io.github.ph1lou.werewolfapi.enums.ConfigsBase;
import io.github.ph1lou.werewolfapi.enums.Day;
import io.github.ph1lou.werewolfapi.enums.LoverType;
import io.github.ph1lou.werewolfapi.enums.RolesBase;
import io.github.ph1lou.werewolfapi.enums.StateGame;
import io.github.ph1lou.werewolfapi.enums.StatePlayer;
import io.github.ph1lou.werewolfapi.enums.TimersBase;
import io.github.ph1lou.werewolfapi.events.ActionBarEvent;
import io.github.ph1lou.werewolfapi.events.FinalDeathEvent;
import io.github.ph1lou.werewolfapi.events.FinalJoinEvent;
import io.github.ph1lou.werewolfapi.events.HostEvent;
import io.github.ph1lou.werewolfapi.events.InvisibleEvent;
import io.github.ph1lou.werewolfapi.events.LoversRepartitionEvent;
import io.github.ph1lou.werewolfapi.events.ModeratorEvent;
import io.github.ph1lou.werewolfapi.events.NewWereWolfEvent;
import io.github.ph1lou.werewolfapi.events.RepartitionEvent;
import io.github.ph1lou.werewolfapi.events.ResurrectionEvent;
import io.github.ph1lou.werewolfapi.events.RevealAmnesiacLoversEvent;
import io.github.ph1lou.werewolfapi.events.StopEvent;
import io.github.ph1lou.werewolfapi.events.StudLoverEvent;
import io.github.ph1lou.werewolfapi.events.UpdateEvent;
import io.github.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import io.github.ph1lou.werewolfapi.events.UpdatePlayerNameTag;
import io.github.ph1lou.werewolfapi.events.WereWolfListEvent;
import io.github.ph1lou.werewolfapi.registers.RoleRegister;
import io.github.ph1lou.werewolfapi.versions.VersionUtils;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import io.github.ph1lou.werewolfplugin.save.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class ScoreBoard implements ScoreAPI, Listener {

	private final GameManager game;
	private int group_size = 5;
	private int player = 0;
	private int timer = 0;
	private int role = 0;
	private int day;
	private String dayState;
	private final List<String> scoreboard1 = new ArrayList<>();
	private final List<String> scoreboard2 = new ArrayList<>();
	private final List<String> scoreboard3 = new ArrayList<>();
	private List<String> roles = new ArrayList<>();
	private final List<UUID> kill_score = new ArrayList<>();
	private final TabManager tabManager;

	public ScoreBoard(GameManager game) {
		this.game = game;
		this.tabManager = new TabManager(game);
	}

	public void updateScoreBoard1() {

		scoreboard1.clear();

		int i = 0;

		Lang lang = (Lang) game.getMain().getLangManager();

		while (lang.getLanguage().containsKey("werewolf.score_board.scoreboard_1." + i)) {
			String line = game.translate("werewolf.score_board.scoreboard_1." + i);
			line = line.replace("&players&", String.valueOf(player));
			line = line.replace("&roles&", String.valueOf(role));
			line = line.replace("&max&", String.valueOf(game.getConfig().getPlayerMax()));
			line = line.substring(0, Math.min(30, line.length()));
			scoreboard1.add(line);
			i++;
		}
		String line = game.translate("werewolf.score_board.game_name");
		scoreboard1.add(line.substring(0, Math.min(30, line.length())));
		line = game.translate("werewolf.score_board.name", game.getGameName());
		scoreboard1.add(line.substring(0, Math.min(30, line.length())));
	}
	
	public void updateScoreBoard2(FastBoard board) {

		UUID playerUUID = board.getPlayer().getUniqueId();
		List<String> score = new ArrayList<>(scoreboard2);
		PlayerWW playerWW = game.getPlayerWW(playerUUID);
		ModerationManagerAPI moderationManager = game.getModerationManager();
		String role;
		if (playerWW != null) {

			if (!playerWW.isState(StatePlayer.DEATH)) {

				if (!game.isState(StateGame.GAME)) {
					role = conversion(game.getConfig().getTimerValue(TimersBase.ROLE_DURATION.getKey()));
				} else role = game.translate(playerWW.getRole().getKey());
			} else role = game.translate("werewolf.score_board.death");
		} else if (moderationManager.getModerators().contains(playerUUID)) {
			role = game.translate("werewolf.commands.admin.moderator.name");
		} else if (moderationManager.getHosts().contains(playerUUID)) {
			role = game.translate("werewolf.commands.admin.host.name");
		} else role = game.translate("werewolf.score_board.spectator");

		for(int i=0;i<score.size();i++){
			score.set(i,score.get(i).replace("&role&",role));
			score.set(i,score.get(i).substring(0,Math.min(30,score.get(i).length())));
		}
		board.updateLines(score);
	}

	private void updateGlobalScoreBoard2() {

		WorldBorder wb = game.getMapManager().getWorld().getWorldBorder();
		String border_size = String.valueOf(Math.round(wb.getSize()));
		String border;
		Lang lang = (Lang) game.getMain().getLangManager();

		if (game.getConfig().getTimerValue(TimersBase.BORDER_BEGIN.getKey()) > 0) {
			border = conversion(game.getConfig().getTimerValue(TimersBase.BORDER_BEGIN.getKey()));
		} else {
			border = game.translate("werewolf.utils.on");
			if (wb.getSize() > game.getConfig().getBorderMin()) {
				border_size = border_size + " > " + game.getConfig().getBorderMin();
			}
		}

		scoreboard2.clear();

		int i = 0;
		this.day = timer / game.getConfig().getTimerValue(TimersBase.DAY_DURATION.getKey()) / 2 + 1;
		this.dayState = game.translate(game.isDay(Day.DAY) ? "werewolf.score_board.day" : "werewolf.score_board.night");

		while (lang.getLanguage().containsKey("werewolf.score_board.scoreboard_2." + i)) {
			String line = game.translate("werewolf.score_board.scoreboard_2." + i);
			line = line.replace("&timer&", conversion(timer));
			line = line.replace("&day&", String.valueOf(this.day));
			line = line.replace("&players&", String.valueOf(player));
			line = line.replace("&group&", String.valueOf(group_size));
			line = line.replace("&border&", border);
			line = line.replace("&daystate&", this.dayState);
			line = line.replace("&border_size&", border_size);
			scoreboard2.add(line);
			i++;
		}
		scoreboard2.add(game.translate("werewolf.score_board.game_name"));
		scoreboard2.add(game.translate("werewolf.score_board.name", game.getGameName()));
	}

	private void updateScoreBoardRole(){


		if (game.getConfig().getLoverCount(LoverType.LOVER.getKey()) > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("§3").append(game.getConfig().getLoverCount(LoverType.LOVER.getKey())).append("§f ").append(game.translate(LoverType.LOVER.getKey()));
			roles.add(sb.substring(0, Math.min(30, sb.length())));
		}
		if (game.getConfig().getLoverCount(LoverType.AMNESIAC_LOVER.getKey()) > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("§3").append(game.getConfig().getLoverCount(LoverType.AMNESIAC_LOVER.getKey())).append("§f ").append(game.translate(LoverType.AMNESIAC_LOVER.getKey()));
			roles.add(sb.substring(0, Math.min(30, sb.length())));
		}
		if (game.getConfig().getLoverCount(LoverType.CURSED_LOVER.getKey()) > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("§3").append(game.getConfig().getLoverCount(LoverType.CURSED_LOVER.getKey())).append("§f ").append(game.translate(LoverType.CURSED_LOVER.getKey()));
			roles.add(sb.substring(0, Math.min(30, sb.length())));
		}
		for (RoleRegister roleRegister : game.getMain().getRegisterManager().getRolesRegister()) {
			String key = roleRegister.getKey();
			if (game.getConfig().getRoleCount(key) > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("§3").append(game.getConfig().getRoleCount(key)).append("§f ").append(game.translate(roleRegister.getKey()));
				roles.add(sb.substring(0, Math.min(30, sb.length())));
			}
		}

		if (!roles.isEmpty()) {

			int inf= 6*((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())%30/5);
			int total = (int) Math.ceil(roles.size()/6f);

			if(inf<roles.size()){

				roles=roles.subList(inf,Math.min(roles.size(),inf+6));
				String up = game.translate("werewolf.score_board.composition");
				roles.add(0,up.substring(0,Math.min(30,up.length())));


				String down = game.translate("werewolf.score_board.page", inf / 6 + 1, total);

				roles.add(roles.size(), down.substring(0, Math.min(30, down.length())));

			} else roles.clear();
		}
	}

	@Override
	public void getKillCounter() {

		for (PlayerWW playerWW1 : game.getPlayerWW()) {
			int i = 0;
			while (i < kill_score.size() && playerWW1.getNbKill() <
					Objects.requireNonNull(game.getPlayerWW(kill_score.get(i))).getNbKill()) {
				i++;
			}
			kill_score.add(i, playerWW1.getUUID());
		}

		scoreboard3.add(game.translate("werewolf.score_board.score")
				.substring(0, Math.min(30, game.translate("werewolf.score_board.score").length())));

		for (int i = 0; i < Math.min(game.getPlayerWW().size(), 10); i++) {

			PlayerWW playerWW1 = game.getPlayerWW(kill_score.get(i));

			if (playerWW1 != null) {
				scoreboard3.add(playerWW1.getName() + "§3 " +
						playerWW1.getNbKill());
			}

		}
		String line = game.translate("werewolf.score_board.game_name");
		scoreboard3.add(line.substring(0, Math.min(30, line.length())));
		line = game.translate("werewolf.score_board.name", game.getGameName());
		scoreboard3.add(line.substring(0, Math.min(30, line.length())));

		Bukkit.getPluginManager().callEvent(new UpdateEvent());
	}

	public int midDistance(Player player) {

		World world = player.getWorld();
		Location location = player.getLocation();
		location.setY(world.getSpawnLocation().getY());
		int distance = 0;

		try {
			distance = (int) location.distance(world.getSpawnLocation());
		} catch (Exception ignored) {
		}


		return distance / 300 * 300;
	}


	@EventHandler(priority = EventPriority.LOW)
	public void onActionBarEvent(ActionBarEvent event) {

		if (game.isState(StateGame.LOBBY)) return;

		if (game.isState(StateGame.TRANSPORTATION)) return;

		Player player = Bukkit.getPlayer(event.getPlayerUUID());

		if (player == null) return;

		int d = midDistance(player);
		event.setActionBar(event.getActionBar() + game.translate("werewolf.action_bar.in_game",
				d, d + 300,
				(int) Math.floor(player.getLocation().getY())));
	}


	@EventHandler
	public void updateBoard(UpdateEvent event) {

		if (Bukkit.getOnlinePlayers().size() == 0) return;

		roles.clear();

		if (!game.getConfig().isConfigActive(ConfigsBase.HIDE_COMPOSITION.getKey())
				&& TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) % 60 >= 30) {
			updateScoreBoardRole();
		}

		if (roles.isEmpty()) {
			if (game.isState(StateGame.LOBBY)) {
				updateScoreBoard1();
			} else updateGlobalScoreBoard2();
		}

		String bot = "";

		if (game.isState(StateGame.START) || game.isState(StateGame.GAME)) {
			bot = game.translate("werewolf.tab.timer", conversion(timer), day, dayState);
		}

		bot += game.translate("werewolf.tab.bot");

		for (FastBoard board : game.getBoards().values()) {

			VersionUtils.getVersionUtils().sendTabTitle(board.getPlayer(),
					game.translate("werewolf.tab.top"),
					bot);

			if (game.isState(StateGame.END)) {
				board.updateLines(scoreboard3);
			} else if (!roles.isEmpty()) {
				board.updateLines(roles);
			} else if (game.isState(StateGame.LOBBY)) {
				board.updateLines(scoreboard1);
			} else {
				updateScoreBoard2(board);
			}
		}

	}

	@Override
	public String updateArrow(Player player, Location target) {

		Location location = player.getLocation();
		String arrow;
		location.setY(target.getY());
		Vector dirToMiddle = target.toVector().subtract(player.getEyeLocation().toVector()).normalize();

		int distance = 0;
		try {
			distance = (int) Math.round(target.distance(location));
		} catch (Exception ignored) {
		}

		Vector playerDirection = player.getEyeLocation().getDirection();
		double angle = dirToMiddle.angle(playerDirection);
		double det = dirToMiddle.getX() * playerDirection.getZ() - dirToMiddle.getZ() * playerDirection.getX();

		angle = angle * Math.signum(det);

		if (angle > -Math.PI / 8 && angle < Math.PI / 8) {
			arrow = "↑";
		} else if (angle > -3 * Math.PI / 8 && angle < -Math.PI / 8) {
			arrow = "↗";
		} else if (angle<3*Math.PI/8 && angle>Math.PI/8) {
			arrow = "↖";
		} else if (angle>3*Math.PI/8 && angle<5*Math.PI/8) {
			arrow="←";
		} else if (angle<-3*Math.PI/8 && angle>-5*Math.PI/8) {
			arrow = "→";
		} else if (angle<-5*Math.PI/8 && angle>-7*Math.PI/8) {
			arrow = "↘";
		} else if (angle > 5 * Math.PI / 8 && angle < 7 * Math.PI / 8) {
			arrow = "↙";
		} else arrow = "↓";

		return distance + " §l" + arrow;
	}

	@Override
	public String conversion(int timer) {

		String value;
		float sign = Math.signum(timer);
		timer = Math.abs(timer);

		if (timer % 60 > 9) {
			value = timer % 60 + "s";
		} else value = "0" + timer % 60 + "s";

		if(timer/3600>0) {

			if(timer%3600/60>9) {
				value = timer / 3600 + "h" + timer % 3600 / 60 + "m" + value;
			} else value = timer / 3600 + "h0" + timer % 3600 / 60 + "m" + value;
		} else if (timer / 60 > 0) {
			value = timer / 60 + "m" + value;
		}
		if (sign < 0) value = "-" + value;

		return value;
	}

	@EventHandler
	public void onNameTagUpdate(UpdateNameTagEvent event) {
		event.getPlayers().forEach(tabManager::updatePlayerOthersAndHimself);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		tabManager.unregisterPlayer(event.getPlayer());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		tabManager.registerPlayer(event.getPlayer());
	}

	@EventHandler
	public void onStop(StopEvent event) {
		Bukkit.getOnlinePlayers()
				.forEach(tabManager::registerPlayer);
	}

	@EventHandler
	public void onModeratorUpdate(ModeratorEvent event) {

		Player player = Bukkit.getPlayer(event.getPlayerUUID());

		if (player == null) return;

		tabManager.updatePlayerOthersAndHimself(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onUpdate(UpdatePlayerNameTag event) {

		StringBuilder sb = new StringBuilder(event.getSuffix());

		PlayerWW playerWW = game.getPlayerWW(event.getPlayerUUID());

		if (playerWW == null) {
			return;
		}

		if (playerWW.isState(StatePlayer.DEATH)) {
			sb.append(" ").append(game.translate("werewolf.score_board.death"));
			event.setSuffix(sb.toString());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onNewWereWolf(NewWereWolfEvent event) {

		Player player = Bukkit.getPlayer(event.getPlayerWW().getUUID());

		if (player == null) return;

		tabManager.updatePlayerOthersAndHimself(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWereWolfList(WereWolfListEvent event) {
		tabManager.updatePlayers();
	}

	@EventHandler
	public void onFinalJoinEvent(FinalJoinEvent event) {
		tabManager.updatePlayerForOthers(event.getPlayerWW().getUUID());
	}

	@EventHandler
	public void onRevive(ResurrectionEvent event) {
		tabManager.updatePlayerForOthers(event.getPlayerWW().getUUID());
	}

	@EventHandler
	public void onFinalDeath(FinalDeathEvent event) {
		tabManager.updatePlayerForOthers(event.getPlayerWW().getUUID());
	}

	@EventHandler
	public void onHostUpdate(HostEvent event) {
		tabManager.updatePlayerForOthers(event.getPlayerUUID());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInvisible(InvisibleEvent event) {
		tabManager.updatePlayerForOthers(event.getPlayerWW().getUUID());
	}


	@EventHandler(priority = EventPriority.MONITOR)
	public void onRepartition(RepartitionEvent event) {
		for (UUID uuid : game.getModerationManager().getModerators()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				tabManager.updatePlayerScoreBoard(player);
			}
		}
	}


	@EventHandler(priority = EventPriority.MONITOR)
	public void onAmnesiacReveal(RevealAmnesiacLoversEvent event) {

		for (UUID uuid : game.getModerationManager().getModerators()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				for (PlayerWW playerWW : event.getPlayerWWS()) {
					tabManager.updatePlayerScoreBoard(player, Collections.singletonList(playerWW.getUUID()));
				}

			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLoverStudRepartition(StudLoverEvent event) {
		for (UUID uuid : game.getModerationManager().getModerators()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				tabManager.updatePlayerScoreBoard(player, Collections.singletonList(event.getPlayerWW().getUUID()));
				tabManager.updatePlayerScoreBoard(player, Collections.singletonList(event.getTargetWW().getUUID()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(FinalDeathEvent event) {

		Player player = Bukkit.getPlayer(event.getPlayerWW().getUUID());

		if (player == null) return;

		tabManager.updatePlayerOthersAndHimself(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLoverRepartition(LoversRepartitionEvent event) {
		for (UUID uuid : game.getModerationManager().getModerators()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				for (LoverAPI loverAPI : game.getLoversManager().getLovers()) {
					if (!loverAPI.isKey(RolesBase.AMNESIAC_WEREWOLF.getKey())) {
						tabManager.updatePlayerScoreBoard(player, loverAPI.getLovers()
								.stream()
								.map(PlayerWW::getUUID)
								.collect(Collectors.toList()));
					}
				}
			}
		}
	}

	@Override
	public int getRole() {
		return role;
	}

	@Override
	public void setRole(int role) {
		this.role = role;
	}

	@Override
	public void addTimer() {
		this.timer++;
	}

	@Override
	public int getPlayerSize() {
		return player;
	}

	@Override
	public void removePlayerSize() {
		this.player = this.player - 1;
	}

	@Override
	public void addPlayerSize() {
		this.player = this.player + 1;
	}

	@Override
	public int getTimer() {
		return timer;
	}

	@Override
	public int getGroup() {
		return this.group_size;
	}

	@Override
	public void setGroup(int group) {
		this.group_size = group;
	}

/*
	public char getArrowChar(Player p, Location mate) {

		Location ploc = p.getLocation();
		ploc.setY(mate.getY());
		p.setCompassTarget(mate);
		Vector v = p.getCompassTarget().subtract(ploc).toVector().normalize();

		Vector d = ploc.getDirection();

		double a = Math.toDegrees(Math.atan2(d.getX(), d.getZ()));
		a -= Math.toDegrees(Math.atan2(v.getX(), v.getZ()));
		a = ((int) (a + 22.5D) % 360);

		if (a < 0.0) a += 360.0;

		return "↑↗→↘↓↙←↖".charAt((int) a / 45);

	}
*/
}