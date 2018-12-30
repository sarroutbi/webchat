package es.sidelab.webchat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class UserCreateChatWithMultipleUsersAndOneIsRemoved implements Callable<Boolean>{
	private int usersInChat;
	private ChatManager chatManager;
	private boolean testOk = true;
	UserCreateChatWithMultipleUsersAndOneIsRemoved(ChatManager chat_manager,
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
			try {
				TestUser registeredUser = new TestUser("user" + Integer.toString(j)) {
					@Override
					public void userExitedFromChat(Chat chat, User user) {
						if(!user.getName().equals("user0")) {
							testOk = false;
						}
					}
				};
				uniqueChat.addUser(registeredUser);
			} catch (Exception e2) {
				e2.printStackTrace();
				return false;
			}
		}
		User firstUser = uniqueChat.getUser("user0");
		try {
			uniqueChat.removeUser(firstUser);
		} catch (Exception e3) {
			e3.printStackTrace();
			return false;
		}
		return testOk;
	}

}
