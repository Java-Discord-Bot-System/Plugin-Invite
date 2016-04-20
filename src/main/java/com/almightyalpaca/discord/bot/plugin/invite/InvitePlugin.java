package com.almightyalpaca.discord.bot.plugin.invite;

import com.almightyalpaca.discord.bot.system.command.Command;
import com.almightyalpaca.discord.bot.system.command.CommandHandler;
import com.almightyalpaca.discord.bot.system.config.Config;
import com.almightyalpaca.discord.bot.system.events.commands.CommandEvent;
import com.almightyalpaca.discord.bot.system.exception.PluginLoadingException;
import com.almightyalpaca.discord.bot.system.exception.PluginUnloadingException;
import com.almightyalpaca.discord.bot.system.plugins.Plugin;
import com.almightyalpaca.discord.bot.system.plugins.PluginInfo;

import net.dv8tion.jda.MessageBuilder;

public class InvitePlugin extends Plugin {

	class InviteCommand extends Command {

		public InviteCommand() {
			super("invite", "Invite me", "[invite]");
		}

		@CommandHandler(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event) {
			final MessageBuilder builder = new MessageBuilder();
			builder.appendString("This bot uses OAuth. You can invite it usong the following link:").newLine();
			builder.appendString("https://discordapp.com/oauth2/authorize?client_id=" + InvitePlugin.this.config.getString("appid") + "&scope=bot&permissions=-1");
			builder.send(event.getChannel());
		}
	}

	private static final PluginInfo	INFO	= new PluginInfo("com.almightyalpaca.discord.bot.plugin.invite", "1.0.0", "Almighty Alpaca", "Invite Plugin", "Invite me to any server.");
	private Config					config;

	public InvitePlugin() {
		super(InvitePlugin.INFO);
	}

	@Override
	public void load() throws PluginLoadingException {
		this.config = this.getSharedConfig("discord");
		if (this.config.getString("appid", "Bot's application id") == "Bot's application id") {
			throw new PluginLoadingException("Pls add the bot's application id to the config");
		}
		this.registerCommand(new InviteCommand());
	}

	@Override
	public void unload() throws PluginUnloadingException {}
}
