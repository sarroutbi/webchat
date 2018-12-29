package es.sidelab.webchat;

import java.util.concurrent.Callable;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class UserDeleteChat implements Callable<Boolean>{
	private int chatAmount;
	private ChatManager chatManager;
	UserDeleteChat(ChatManager chat_manager, int chat_amount) {
		chatManager = chat_manager;
		chatAmount = chat_amount;
	}

	public Boolean call() {
		for (int chat = 0; chat < chatAmount; chat++) {
			try {
				String chat_name = "chat" + Integer.toString(chat);
				Chat currentChat = chatManager.getChat(chat_name);
				// Chat can have been closed by other user ...
				if(currentChat != null) {
					chatManager.closeChat(currentChat);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
