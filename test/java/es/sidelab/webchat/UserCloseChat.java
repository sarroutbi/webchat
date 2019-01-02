package es.sidelab.webchat;

import java.util.concurrent.Callable;


import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class UserCloseChat implements Callable<Boolean> {
	private ChatManager chatManager;
	private String chatName;
	public UserCloseChat(ChatManager chat_manager,
			String chat_name) {
		chatManager = chat_manager;
		chatName = chat_name;
	}
	@Override
	public Boolean call() throws Exception {
		try {
			synchronized (this) {
				Chat chatToClose = chatManager.getChat(chatName);
				if(null != chatToClose) {
					chatManager.closeChat(chatToClose);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
