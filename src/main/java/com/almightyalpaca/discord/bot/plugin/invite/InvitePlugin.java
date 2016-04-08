package com.almightyalpaca.discord.bot.plugin.invite;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.almightyalpaca.discord.bot.system.command.Command;
import com.almightyalpaca.discord.bot.system.command.CommandHandler;
import com.almightyalpaca.discord.bot.system.command.arguments.special.Rest;
import com.almightyalpaca.discord.bot.system.events.commands.CommandEvent;
import com.almightyalpaca.discord.bot.system.events.manager.EventHandler;
import com.almightyalpaca.discord.bot.system.exception.PluginLoadingException;
import com.almightyalpaca.discord.bot.system.exception.PluginUnloadingException;
import com.almightyalpaca.discord.bot.system.plugins.Plugin;
import com.almightyalpaca.discord.bot.system.plugins.PluginInfo;
import com.almightyalpaca.discord.bot.system.util.URLUtils;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.utils.InviteUtil;

public class InvitePlugin extends Plugin {

	class InviteCommand extends Command {

		public InviteCommand() {
			super("invite", "Invite me", "[invite]");
		}

		@CommandHandler(dm = false, guild = true, async = true)
		private void onCommand(final CommandEvent event, final Rest rest) {
			final Triple<Integer, Integer, Integer> result = InvitePlugin.this.handleInvite(rest.getString(), 5, TimeUnit.SECONDS);
			final MessageBuilder builder = new MessageBuilder();
			if (result.getMiddle() == 0) {
				if (result.getRight() == 0) {
					builder.appendString("Failed to join the server.");
				} else {
					builder.appendString("I'm already in that server.");
				}
			} else {
				builder.appendString("Successfully joined " + result.getMiddle() + " server" + (result.getMiddle() == 1 ? "." : "s."));
			}
			builder.send(event.getChannel());

		}
	}

	private static final PluginInfo INFO = new PluginInfo("com.almightyalpaca.discord.bot.plugin.invite", "1.0.0", "Almighty Alpaca", "Invite Plugin", "Invite me to any server.");

	private static final Pattern invitePattern = Pattern.compile("\\bhttps://(?:www\\.)?discord(?:\\.gg|app\\.com/invite)/([a-zA-Z0-9-]+)\\b");

	private static final Pattern discordmePatter = Pattern.compile("https?:\\/\\/(www\\.)?discord\\.me\\/([-a-zA-Z0-9-_]*)");

	public InvitePlugin() {
		super(InvitePlugin.INFO);
	}

	public Triple<Integer, Integer, Integer> handleInvite(String string, int timeout, TimeUnit timeUnit) {
		int count = 0;
		int alreadyIn = 0;
		final Matcher discordmeMatcher = InvitePlugin.discordmePatter.matcher(string);
		while (discordmeMatcher.find()) {
			final String match = discordmeMatcher.group(1);
			try {
				string = string.replace(match, URLUtils.expand(match));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		final AtomicInteger i = new AtomicInteger();

		final Matcher inviteMatcher = InvitePlugin.invitePattern.matcher(string);
		while (inviteMatcher.find()) {
			final InviteUtil.Invite invite = InviteUtil.resolve(inviteMatcher.group(1));
			if (invite != null) {
				count++;
				if (this.getJDA().getGuildById(invite.getGuildId()) == null) {
					InviteUtil.join(invite, this.getJDA(), g -> {
						i.incrementAndGet();
					});
				} else {
					alreadyIn++;
				}
			}
		}

		long timeoutMillis = timeUnit.toMillis(timeout) / 100;

		while (i.get() < count - alreadyIn && timeoutMillis-- >= 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		return new ImmutableTriple<Integer, Integer, Integer>(count, i.get(), alreadyIn);
	}

	@Override
	public void load() throws PluginLoadingException {
		this.registerEventHandler(this);
		this.registerCommand(new InviteCommand());
	}

	@EventHandler(async = true)
	public void onPrivateMesageReceived(final PrivateMessageReceivedEvent event) {
		if (event.getAuthor() != event.getJDA().getSelfInfo()) {
			final Triple<Integer, Integer, Integer> result = this.handleInvite(event.getMessage().getRawContent(), 5, TimeUnit.SECONDS);
			if (result.getLeft() != 0) {
				final MessageBuilder builder = new MessageBuilder();
				if (result.getMiddle() == 0) {
					if (result.getRight() == 0) {
						builder.appendString("Failed to join the server.");
					} else {
						builder.appendString("I'm already in that server.");
					}
				} else {
					builder.appendString("Successfully joined " + result.getMiddle() + " server" + (result.getMiddle() == 1 ? "." : "s."));
				}
				builder.send(event.getChannel());
			}
		}
	}

	@Override
	public void unload() throws PluginUnloadingException {}
}
