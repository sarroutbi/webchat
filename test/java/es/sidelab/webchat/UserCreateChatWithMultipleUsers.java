package es.sidelab.webchat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class UserCreateChatWithMultipleUsers implements Callable<Boolean>{
	private int usersInChat;
	private ChatManager chatManager;
	UserCreateChatWithMultipleUsers(ChatManager chat_manager,
			int users_in_chat) {
		chatManager = chat_manager;
		usersInChat = users_in_chat;
	}

	public Boolean call() {
		// 1 - Crear usuario y darlo de alta en el chat manager
		TestUser chatUser = new TestUser("user" + Integer.toString((int)(Math.random() * 1000))) {
			public void newChat(Chat chat) {
			}
		};
		chatManager.newUser(chatUser);
		Chat uniqueChat;
		try {
			uniqueChat = chatManager.newChat("chat" + Integer.toString((int)(Math.random() * 1000)), 5, TimeUnit.SECONDS);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		// 2 - Crear N usuarios y registrarlos en el chat
		for (int j = 0; j < usersInChat; j++) {
			//     - Crear chat de nombre "chat" + iteración y registrar usuario en chat
			try {
				TestUser registeredUser = new TestUser("user" + Integer.toString(j)) {
					@Override
					public void newUserInChat(Chat chat, User user) {
						System.out.println("New user " + user.getName() + " in chat " + chat.getName());
					}
				};
				uniqueChat.addUser(registeredUser);
			} catch (Exception e2) {
				e2.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
