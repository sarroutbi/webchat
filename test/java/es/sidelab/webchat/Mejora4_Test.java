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
	private int expectedMessage = 1;

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

	@Test
	public void GivenAChatWithTwoUsersWhenUserSendsMessagesThenTheyAreReceivedInCorrectOrder()
			throws InterruptedException, TimeoutException {

		final int ORDER_TEST_USERS = 2;
		final int MAX_MESSAGES = 5;

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(MAX_CHATS);
		ArrayList<User> userList = new ArrayList<User>(2);

		for (int i = 0; i < ORDER_TEST_USERS; i++) {
			String userDesc = "user" + Integer.toString(i);
			userList.add(new TestUser(userDesc) {
				// Implement new message to user
				@Override
				public void newMessage(Chat chat, User user, String message) {
					System.out.println("User:" + userDesc + ", new message '" + message + "' from user " + user.getName()
							+ " in chat " + chat.getName());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(!message.equals(Integer.toString(expectedMessage))) {
						// Notify to exchanger
						System.out.println(" ---------------- ERROR ----------------");
						System.out.println(" msg: " + message + " expected:" + expectedMessage);
					}
					expectedMessage++;
				}
			});
			chatManager.newUser(userList.get(i));
		};

		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		for (int i = 0; i < ORDER_TEST_USERS; i++) {
			chat.addUser(userList.get(i));
		}

		// Send N messages
		for (int i = 1; i <= MAX_MESSAGES; i++) {
			userList.get(1).newMessage(chat, userList.get(0), Integer.toString(i));
		}

		// Check Exchanger to assertTrue

	}
}
