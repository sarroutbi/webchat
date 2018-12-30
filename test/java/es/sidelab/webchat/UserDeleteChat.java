package es.sidelab.webchat;

import java.util.concurrent.Callable;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class UserDeleteChat implements Callable<Boolean>{
	private ChatManager chatManager;
	UserDeleteChat(ChatManager chat_manager) {
		chatManager = chat_manager;
	}

	public Boolean call() {
		try {
			Chat currentChat = chatManager.getChat("chat0");
			// Chat can have been closed by other thread ...
			if(currentChat != null) {
				chatManager.closeChat(currentChat);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
