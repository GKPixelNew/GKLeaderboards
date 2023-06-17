package us.ajg0702.leaderboards.boards;

import com.google.gson.JsonObject;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.leaderboards.LeaderboardPlugin;
import us.ajg0702.leaderboards.TimeUtils;
import us.ajg0702.leaderboards.boards.keys.BoardType;
import us.ajg0702.leaderboards.cache.Cache;
import us.ajg0702.leaderboards.utils.EasyJsonObject;
import us.ajg0702.utils.common.Messages;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class StatEntry {

	public static final String BOARD_DOES_NOT_EXIST = "Board does not exist";
	public static final String AN_ERROR_OCCURRED = "An error occurred";

	private static LeaderboardPlugin plugin;
	
	final String playerName;
	final String playerDisplayName;
	String prefix;
	String suffix;

	final UUID playerID;
	
	final int position;
	final String board;

	private Cache cache;

	private final TimedType type;


	static boolean formatStringsSet = false;
	static String k = "k";
	static String m = "m";
	static String b = "b";
	static String t = "t";
	static String q = "q";
	
	double score;
	String scorePretty;
	public StatEntry(int position, String board, String prefix, String playerName, String playerDisplayName, UUID playerID, String suffix, double score, TimedType type) {
		this.playerName = playerName;
		this.playerDisplayName = playerDisplayName == null ? "" : playerDisplayName;
		this.score = score;
		this.prefix = prefix;
		this.suffix = suffix;
		this.type = type;

		this.playerID = playerID;

		this.cache = plugin.getCache();

		if(plugin != null && !formatStringsSet) {
			formatStringsSet = true;
			try {
				Messages msgs = plugin.getMessages();
				k = msgs.getString("formatted.k");
				m = msgs.getString("formatted.m");
				b = msgs.getString("formatted.b");
				t = msgs.getString("formatted.t");
				q = msgs.getString("formatted.q");
			} catch(NoClassDefFoundError ignored) {}
		}
		
		this.position = position;
		this.board = board;

		scorePretty = calcPrettyScore();
	}
	private String calcPrettyScore() {
		if(score == 0 && playerName.equals(BOARD_DOES_NOT_EXIST)) {
			return "BDNE";
		}
		if(score == 0 && playerName.equals(plugin.getMessages().getRawString("loading.text"))) {
			return "...";
		}
		Messages messages = cache.getPlugin().getMessages();
		if(!hasPlayer()) {
			if(score == -1) {
				return messages.getRawString("no-data.lb.value");
			} else if(score == -2) {
				return messages.getRawString("no-data.rel.value");
			} else {
				return plugin.getMessages().getRawString("loading.short");
			}
		}
		return plugin.getPlaceholderFormatter().toFormat(score, board);
	}

	public void changeScore(double newScore, String newPrefix, String newSuffix) {
		score = newScore;
		prefix = newPrefix;
		suffix = newSuffix;

		scorePretty = calcPrettyScore();

	}

	public boolean hasPlayer() {
		return getPlayerID() != null;
	}
	
	public String getPrefix() {
		return prefix;
	}
	public String getSuffix() {
		return suffix;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	@NotNull
	public String getPlayerDisplayName() {
		return playerDisplayName;
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public int getPosition() {
		return position;
	}
	public String getBoard() {
		return board;
	}

	public TimedType getType() {
		return type;
	}

	public double getScore() {
		return score;
	}

	public String getScoreFormatted() {
		if(score == 0 && playerName.equals(BOARD_DOES_NOT_EXIST)) {
			return "BDNE";
		}
		if(!hasPlayer()) {
			if(cache != null) {
				if(score == -1) {
					return plugin.getMessages().getRawString("no-data.lb.value");
				} else if(score == -2) {
					return plugin.getMessages().getRawString("no-data.rel.value");
				} else {
					return plugin.getMessages().getRawString("loading.short");
				}
			} else {
				return "---";
			}
		}

		return formatDouble(score);
	}

	public static String formatDouble(double d) {
		if (d < 1000L) {
			return formatNumber(d);
		}
		if (d < 1000000L) {
			return formatNumber(d/1000L)+k;
		}
		if (d < 1000000000L) {
			return formatNumber(d/1000000L)+m;
		}
		if (d < 1000000000000L) {
			return formatNumber(d/1000000000L)+b;
		}
		if (d < 1000000000000000L) {
			return formatNumber(d/1000000000000L)+t;
		}
		if (d < 1000000000000000000L) {
			return formatNumber(d/1000000000000000L)+q;
		}

		return addCommas(d);
	}

	private static String formatNumber(double d) {
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(0);
		return format.format(d);
	}
	
	public String getScorePretty() {
		return scorePretty;
	}

	public String getTime() {
		if(score == 0 && playerName.equals(BOARD_DOES_NOT_EXIST)) {
			return "BDNE";
		}
		if(!hasPlayer()) {
			if(score == -1) {
				return plugin.getMessages().getRawString("no-data.lb.value");
			} else if(score == -2) {
				return plugin.getMessages().getRawString("no-data.rel.value");
			} else {
				return plugin.getMessages().getRawString("loading.short");
			}
		}
		return TimeUtils.formatTimeSeconds(Math.round(getScore()));
	}
	
	
	public static String addCommas(double number) {
		boolean useComma = true;
		char comma = 0;
		char decimal;
		if(plugin != null) {
			String commaString = plugin.getAConfig().getString("comma");
			useComma = !commaString.isEmpty();
			if(useComma) comma = commaString.charAt(0);
			decimal = plugin.getAConfig().getString("decimal").charAt(0);
		} else {
			comma = ',';
			decimal = '.';
		}
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));
		if(useComma) {
			symbols.setGroupingSeparator(comma);
		}
		symbols.setDecimalSeparator(decimal);
		DecimalFormat df = new DecimalFormat("#,###.##", symbols);
		df.setGroupingUsed(useComma);
		return df.format(number);
	}

	/**
	 * Deprecated. Use getPlayerName instead
	 */
	@Deprecated
	public String getPlayer() {
		return playerName;
	}


	public static StatEntry boardNotFound(LeaderboardPlugin plugin, int position, String board, TimedType type) {
		return new StatEntry(position, board, "", BOARD_DOES_NOT_EXIST, BOARD_DOES_NOT_EXIST, null, "", 0, type);
	}
	public static StatEntry error(LeaderboardPlugin plugin, int position, String board, TimedType type) {
		return new StatEntry(position, board, "", AN_ERROR_OCCURRED, AN_ERROR_OCCURRED, null, "", 0, type);
	}
	public static StatEntry noData(LeaderboardPlugin plugin, int position, String board, TimedType type) {
		return new StatEntry(position, board, "", plugin.getMessages().getRawString("no-data.lb.name"), plugin.getMessages().getRawString("no-data.lb.name"), null, "", -1, type);
	}
	public static StatEntry noRelData(LeaderboardPlugin plugin, int position, String board, TimedType type) {
		return new StatEntry(position, board, "", plugin.getMessages().getRawString("no-data.rel.name"), plugin.getMessages().getRawString("no-data.rel.name"), null, "", -2, type);
	}
	public static StatEntry loading(LeaderboardPlugin plugin, int position, String board, TimedType type) {
		return new StatEntry(position, board, "", plugin.getMessages().getRawString("loading.text"), plugin.getMessages().getRawString("loading.text"), null, "", 0, type);
	}
	public static StatEntry loading(LeaderboardPlugin plugin, String board, TimedType type) {
		return new StatEntry(-2, board, "", plugin.getMessages().getRawString("loading.text"), plugin.getMessages().getRawString("loading.text"), null, "", 0, type);
	}
	public static StatEntry loading(LeaderboardPlugin plugin, BoardType boardType) {
		return new StatEntry(-2, boardType.getBoard(), "", plugin.getMessages().getRawString("loading.text"), plugin.getMessages().getRawString("loading.text"), null, "", 0, boardType.getType());
	}
	public static StatEntry loading(LeaderboardPlugin plugin, OfflinePlayer player, BoardType boardType) {
		return new StatEntry(-2, boardType.getBoard(), "", player.getName(), player.getName(), player.getUniqueId(), "", 0, boardType.getType());
	}

	@SuppressWarnings("unused")
	public JsonObject toJsonObject() {
		return new EasyJsonObject()
				.add("playerName", playerName)
				.add("playerDisplayName", playerDisplayName)
				.add("prefix", prefix)
				.add("suffix", suffix)
				.add("playerID", playerID.toString())
				.add("position", position)
				.add("board", board)
				.add("type", type.toString())
				.add("score", score)
				.getHandle();
	}

	@SuppressWarnings("unused")
	public static StatEntry fromJsonObject(LeaderboardPlugin plugin, JsonObject object) {
		return new StatEntry(
				object.get("position").getAsInt(),
				object.get("board").getAsString(),
				object.get("prefix").getAsString(),
				object.get("playerName").getAsString(),
				object.get("playerDisplayName").getAsString(),
				UUID.fromString(object.get("playerID").getAsString()),
				object.get("suffix").getAsString(),
				object.get("score").getAsDouble(),
				TimedType.valueOf(object.get("type").getAsString().toUpperCase(Locale.ROOT))
		);
	}

	public static void setPlugin(LeaderboardPlugin leaderboardPlugin) {
		plugin = leaderboardPlugin;
	}
}
