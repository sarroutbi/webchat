package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora4_Test {
	private final int MAX_CHATS = 10;
	private final int MAX_USERS = 4;
	private CountDownLatch latch = new CountDownLatch(MAX_USERS-1);

	@Test
	public void GivenAChatWhenUserLastOneSecInPrintingAMessageThenLessThanOneSecondPerUserIsSpentDueToParallelism()
			throws InterruptedException, TimeoutException {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(MAX_CHATS);
		ArrayList<User> userList = new ArrayList<User>(MAX_USERS);

		for (int i = 0; i < MAX_USERS; i++) {
			String userDesc = "user" + Integer.toString(i);
			userList.add(new TestUser(userDesc) {
				// Implement new message to user
				@Override
				public void newMessage(Chat chat, User user, String message) {
					System.out.println("User:" + userDesc + ", new message '" + message + "' from user " + user.getName()
							+ " in chat " + chat.getName());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					latch.countDown();
				}
			});
			chatManager.newUser(userList.get(i));
		};

		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		for (int i = 0; i < MAX_USERS; i++) {
			chat.addUser(userList.get(i));
		}

		// Measure time
		long startTime = System.nanoTime();
		chat.sendMessage(userList.get(0), "Test message");
		// Wait all users receive message
		latch.await();

		// Measure time after all users have called newMessage
		long endTime = System.nanoTime();
		long elapsedTimeInMilliseconds = (endTime - startTime)/1000000;
		assertTrue("Time elapsed was bigger that expected" + Long.toString(elapsedTimeInMilliseconds),
				elapsedTimeInMilliseconds < 3000);
	}
}
