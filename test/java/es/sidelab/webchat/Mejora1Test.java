package es.sidelab.webchat;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora1Test {
	private final int THREAD_AMOUNT = 4;
	private final int CHATS_PER_USER = 5;
	private final int MAX_CHAT_NUMBER = 50;
	private ChatManager chatManager = new ChatManager(MAX_CHAT_NUMBER);

	public void WorkerThread(int thread_number){
		chatManager.newUser(new TestUser("user" + Integer.toString(thread_number)) {
			public void newChat(Chat chat) {
				// TODO: Check what to do with chat 
				// chatName[0] = chat.getName();
			}
		});
	}

	@Test
	public void GivenAChatManagerWhenMultipleUsersAreRegisteredThenNoConcurrenceError()
			throws InterruptedException, TimeoutException {		

		// 4 hilos con 4 usuarios concurrentes		
		ExecutorService exec = Executors.newFixedThreadPool(THREAD_AMOUNT);
		for(int i = 0; i <= THREAD_AMOUNT; i++) {
			final int thr_num = i;
			// Crear hilo basado en Completion Service, y por cada hilo:
			CompletionService<Void> service = new ExecutorCompletionService<Void>(exec);
			service.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					// 1 - Crear objeto TestUser, registrándolo en el ChatManager
					TestUser chatUser = new TestUser("user" + Integer.toString(thr_num)) {
						public void newChat(Chat chat) {
							// TODO: Check what to do with chat 
							// chatName[0] = chat.getName();
						}
					};
					chatManager.newUser(chatUser);
					// 2 - Repetir 5 veces:
					for (int j = 0; j < CHATS_PER_USER; j++) {
						//     - Crear chat de nombre "chat" + iteración y registrar usuario en chat
						System.out.println("-- User: " + Integer.toString(thr_num) + ", Creating new chat:" + "chat" + Integer.toString(j) + " --");
						Chat currentChat = chatManager.newChat("chat" + Integer.toString(j), 5, TimeUnit.SECONDS);
						currentChat.addUser(chatUser);
						//     - Mostrar por pantalla todos los usuarios de ese chat
						for(User u : currentChat.getUsers()) {
							System.out.println("-- User: " + Integer.toString(thr_num) + ", Chat:" + currentChat.getName() + " chat:" + u.getName() + " --");
						}
					}
					return null;
				}
			});
		}
		
		// Asegurar que al menos hay 4 usuarios en el chat manager
		int usersInManager = chatManager.getUsers().size();
		assertEquals("There should be 4 users in chat manager, but the value is "
				+ usersInManager, THREAD_AMOUNT, usersInManager);
		
		// Asegurar que al menos hay 5 chats en el chat manager
		assertEquals("There should be 5 chats in chat manager, but the value is "
				+ chatManager.getChats().size(), CHATS_PER_USER,
				chatManager.getChats().size());

		// Asegurar que existen 4 usuarios en cada chat
		for(Chat chat : chatManager.getChats()) {
			assertEquals("There should be 4 users in chat " + chat.getName() + ", but the value is "
						  + chat.getUsers().size(), THREAD_AMOUNT, chat.getUsers().size());
		}
	}
}
