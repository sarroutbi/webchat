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

import org.junit.Test;

import es.codeurjc.webchat.ChatManager;

public class Mejora5_Test {
	private final int MAX_CHAT_NUMBER = 50;

	// dummy helper
	public List<Callable<Boolean>> createChatCallableList(ChatManager chat_manager,
			int users_amount, int chat_amount) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserCreateChats(i, chat_manager, chat_amount));
		}
		return callables;
	}

	// dummy helper
	public List<Callable<Boolean>> removeChatCallableList(ChatManager chat_manager,
			int users_amount, int chat_amount) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserDeleteChat(chat_manager, chat_amount));
		}
		return callables;
	}

	@Test
	public void GivenAChatManagerWhenChatIsCreatedThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int CHATS_PER_USER = 2;
		ChatManager chatManager = new ChatManager(MAX_CHAT_NUMBER);

		// 2 hilos para crear 2 chats
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables = createChatCallableList(chatManager, THREAD_AMOUNT, CHATS_PER_USER);
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

		// Asegurar que al menos hay 2 usuarios en el chat manager
		int usersInManager = chatManager.getUsers().size();
		assertEquals("There should be " + THREAD_AMOUNT + " users in chat manager, but the value is "
				+ usersInManager, THREAD_AMOUNT, usersInManager);

		// Asegurar que al menos hay 2 chats en el chat manager
		assertEquals("There should be " + Integer.toString(CHATS_PER_USER) +
				" chats in chat manager, but the value is "
				+ chatManager.getChats().size(), CHATS_PER_USER,
				chatManager.getChats().size());
	}

	@Test
	public void GivenAChatManagerWhenExistingChatIsRemovedThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int CHATS_PER_USER = 2;
		ChatManager chatManager = new ChatManager(MAX_CHAT_NUMBER);

		// 2 hilos para crear 2 chats
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables = createChatCallableList(chatManager, THREAD_AMOUNT, CHATS_PER_USER);
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

		ExecutorService executorDeletionService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskDeletionService = new ExecutorCompletionService<Boolean>(
				executorDeletionService);
		try {
			List<Callable<Boolean>> callables = removeChatCallableList(chatManager, THREAD_AMOUNT, CHATS_PER_USER);
			for (Callable<Boolean> callable : callables) {
				taskDeletionService.submit(callable);
			}
			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskDeletionService.take();
				assertTrue("Task: " + Integer.toString(i)+ " should have been completed correctly, but it has not", result.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorDeletionService.shutdown();

		// Asegurar que no quedan chats en el chat manager
		assertEquals("There should not be chats in chat manager, but the value is " +
				chatManager.getChats().size(),
				chatManager.getChats().size(), 0);
	}

	@Test
	public void GivenAnExistingChatWhenUserIsRegisteredThenNoErrorOccursAndRestOfUsersAreNotified() {

	}

	@Test
	public void GivenAnExistingChatWhenUserExitsThenNoErrorOccursAndRestOfUsersAreNotified() {

	}

	@Test
	public void GivenAnExistingChatWhenUserSendsMessageThenNoErrorOccursAndRestOfUsersAreNotified() {

	}

}
