package es.sidelab.webchat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;


public class UserCreateChats implements Callable<Boolean> {
	private int thrNum;
	private int chatsPerUser;
	private ChatManager chatManager;
	UserCreateChats(int thread_number, ChatManager chat_manager,
			int chats_per_user) {
		thrNum = thread_number;
		chatManager = chat_manager;
		chatsPerUser = chats_per_user;
	}

	public Boolean call() {
		// 1 - Crear usuario y darlo de alta en el chat manager
		TestUser chatUser = new TestUser("user" + Integer.toString(thrNum)) {
			public void newChat(Chat chat) {
			}
		};
		chatManager.newUser(chatUser);
		// 2 - Repetir 5 veces:
		for (int j = 0; j < chatsPerUser; j++) {
			//     - Crear chat de nombre "chat" + iteración y registrar usuario en chat
			try {
				Chat currentChat = chatManager.newChat("chat" + Integer.toString(j), 5, TimeUnit.SECONDS);
				currentChat.addUser(chatUser);
				//     - Mostrar por pantalla todos los usuarios de ese chat
				for(User u : currentChat.getUsers()) {
					System.out.println("-- Thread:" + Integer.toString(thrNum) + ", Chat:" + currentChat.getName() + " User in chat:" + u.getName() + " --");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
