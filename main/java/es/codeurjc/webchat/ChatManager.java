package es.codeurjc.webchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatManager {

	private Map<String, Chat> chats = Collections.synchronizedMap(new HashMap<>());
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<>());

	private int maxChats;

	public ChatManager(int maxChats) {
		this.maxChats = maxChats;
	}

	public void newUser(User user) {
		synchronized(users) {
			if(users.containsKey(user.getName())){
				throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");
			} else {
				users.put(user.getName(), user);
			}
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException {
		Lock lock = new ReentrantLock(true);
		lock.lock();
		if (chats.size() == maxChats) {
			lock.unlock();
			boolean hopDetectedInTimeout = false;
			// Wait until timeout expires before throwing exception ...
			// Measure time
			long startTime = System.nanoTime();

			long elapsedTimeInMilliseconds = 0;
			do {
				// Measure time after all users have called newMessage
				long endTime = System.nanoTime();
				elapsedTimeInMilliseconds = (endTime - startTime)/1_000_000;
				lock.lock();
				if(chats.size() < maxChats) {
					hopDetectedInTimeout = true;
					lock.unlock();
					break;
				}
				lock.unlock();
				Thread.sleep(1);
			} while(elapsedTimeInMilliseconds < unit.toMillis(timeout));
			if(!hopDetectedInTimeout) {
				throw new TimeoutException("There is no enought capacity to create a new chat");
			}
		}

		synchronized(chats) {
			if(chats.containsKey(name)){
				return chats.get(name);
			} else {
				Chat newChat = new Chat(this, name);
				chats.put(name, newChat);
				synchronized(users) {
					for(User user : users.values()){
						user.newChat(newChat);
					}
					return newChat;
				}
			}
		}
	}

	public void closeChat(Chat chat) {
		synchronized(chats) {
			Chat removedChat = chats.remove(chat.getName());
			if (removedChat == null) {
				throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
						+ chat.getName() + "\'");
			}
			synchronized(users) {
				for(User user : users.values()){
					user.chatClosed(removedChat);
				}
			}
		}
	}

	public Collection<Chat> getChats() {
		synchronized(chats) {
			return Collections.unmodifiableCollection(new ArrayList<>(chats.values()));
		}
	}

	public Chat getChat(String chatName) {
		synchronized(chats) {
			return chats.get(chatName);
		}
	}

	public Collection<User> getUsers() {
		synchronized(users) {
			return Collections.unmodifiableCollection(new ArrayList<>(users.values()));
		}
	}

	public User getUser(String userName) {
		synchronized(users) {
			return users.get(userName);
		}
	}

	public void close() {}
}
