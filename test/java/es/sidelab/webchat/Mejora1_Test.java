package es.sidelab.webchat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class Mejora1_Test {
	private final int THREAD_AMOUNT = 4;
	private final int MAX_CHAT_NUMBER = 50;
	private final int CHATS_PER_USER = 5;
	private ChatManager chatManager = new ChatManager(MAX_CHAT_NUMBER);
	
	// dummy helper to create a List of Callables return a String
	public List<Callable<Boolean>> createCallableList(int size) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			callables.add(new UserCreateChats(i, chatManager, CHATS_PER_USER));
		}
		return callables;
	}
	@Test
	public void GivenAChatManagerWhenMultipleUsersAreRegisteredThenNoConcurrenceError()
			throws InterruptedException, TimeoutException {	
		// 4 hilos con 4 usuarios concurrentes		
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables = createCallableList(THREAD_AMOUNT);
			for (Callable<Boolean> callable : callables) {
				taskCompletionService.submit(callable);
			}
			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskCompletionService.take();	
				assertTrue("Task: " + Integer.toString(i)+ " should have been completed correctly, but it has not", result.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorService.shutdown();
		
		// Asegurar que al menos hay 4 usuarios en el chat manager
		int usersInManager = chatManager.getUsers().size();
		assertEquals("There should be 4 users in chat manager, but the value is "
				+ usersInManager, THREAD_AMOUNT, usersInManager);
		
		// Asegurar que al menos hay 5 chats en el chat manager
		assertEquals("There should be " + Integer.toString(CHATS_PER_USER) +
				" chats in chat manager, but the value is "
				+ chatManager.getChats().size(), CHATS_PER_USER,
				chatManager.getChats().size());

		// Asegurar que cada uno de los chats tiene todos los usuarios añadidos
		for(Chat chat: chatManager.getChats()) {
			assertEquals("There should be " + Integer.toString(THREAD_AMOUNT) +
					" users in chat " + chat.getName() + " but the value is "
					+ chat.getUsers().size(), THREAD_AMOUNT,
					chat.getUsers().size());
		}
	}
}
