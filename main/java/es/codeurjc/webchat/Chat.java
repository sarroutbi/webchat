package es.codeurjc.webchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Chat {

	private String name;
	
	// Estructura de datos original:
	// private Map<String, User> users = new HashMap<>();
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<>());

	private ChatManager chatManager;

	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		synchronized(users) {
			users.put(user.getName(), user);
			for(User u : users.values()){
				if (u != user) {
					u.newUserInChat(this, user);
				}
			}
		}
	}

	public void removeUser(User user) {
		synchronized(users) {
			users.remove(user.getName());
			for(User u : users.values()){
				u.userExitedFromChat(this, user);
			}
		}
	}

	public Collection<User> getUsers() {
		synchronized(users) {
			Collection<User> collection = Collections.unmodifiableCollection(new ArrayList<>(users.values()));
			return Collections.synchronizedCollection(collection);
		}
	}

	public User getUser(String name) {
		synchronized(users) {
			return users.get(name);
		}
	}

	public void sendMessage(User user, String message) {
		synchronized(users) {
			for(User u : users.values()){
				u.newMessage(this, user, message);
			}
		}
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
