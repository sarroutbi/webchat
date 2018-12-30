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

import es.codeurjc.webchat.Chat;
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
			int users_amount) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserDeleteChat(chat_manager));
		}
		return callables;
	}

	// dummy helper
	public List<Callable<Boolean>> createUniqueChatCallableList(ChatManager chat_manager,
			int users_amount, int users_in_chat) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserCreateChatWithMultipleUsers(chat_manager, users_in_chat));
		}
		return callables;
	}

	// dummy helper
	public List<Callable<Boolean>> createUniqueChatAndRemoveOneUserCallableList(ChatManager chat_manager,
			int users_amount, int users_in_chat) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserCreateChatWithMultipleUsersAndOneIsRemoved(chat_manager, users_in_chat));
		}
		return callables;
	}

	// dummy helper
	public List<Callable<Boolean>> createUniqueChatWithMultipleUsersAndSendMessage(ChatManager chat_manager,
			int users_amount, int users_per_chat) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserCreateChatWithMultipleUsersAndSendsMessage(chat_manager, users_per_chat));
		}
		return callables;
	}

	@Test
	public void GivenAChatManagerWhenChatIsCreatedThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int CHATS_PER_USER = 2;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);

		// 2 hilos para crear 2 chats
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables = createChatCallableList(chat_manager, THREAD_AMOUNT, CHATS_PER_USER);
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
		int usersInManager = chat_manager.getUsers().size();
		assertEquals("There should be " + THREAD_AMOUNT + " users in chat manager, but the value is "
				+ usersInManager, THREAD_AMOUNT, usersInManager);

		// Asegurar que al menos hay 2 chats en el chat manager
		assertEquals("There should be " + Integer.toString(CHATS_PER_USER) +
				" chats in chat manager, but the value is "
				+ chat_manager.getChats().size(), CHATS_PER_USER,
				chat_manager.getChats().size());
	}

	@Test
	public void GivenAChatManagerWhenExistingChatIsRemovedThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int CHATS_PER_USER = 2;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);

		// 2 hilos para crear 2 chats
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables = createChatCallableList(chat_manager, THREAD_AMOUNT, CHATS_PER_USER);
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
			List<Callable<Boolean>> callables = removeChatCallableList(chat_manager, THREAD_AMOUNT);
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

		// Asegurar que quedan los chats correspondientes en el chat manager
		assertEquals("There should be chats in chat manager, but the value is " +
				chat_manager.getChats().size(),
				chat_manager.getChats().size(), CHATS_PER_USER-1);
	}

	@Test
	public void GivenAnExistingChatWhenUserIsRegisteredThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int USERS_PER_CHAT = 5;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);

		// 2 hilos para crear 1 chat por hilo
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables =
					createUniqueChatCallableList(chat_manager, THREAD_AMOUNT, USERS_PER_CHAT);
			for (Callable<Boolean> callable : callables) {
				taskCompletionService.submit(callable);
			}
			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskCompletionService.take();
				assertTrue("Task: " + Integer.toString(i) +
						" should have been completed correctly, but it has not", result.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorService.shutdown();


		assertEquals("There should be chats in chat manager, but the value is " +
				chat_manager.getChats().size(),
				chat_manager.getChats().size(), THREAD_AMOUNT);

		for(Chat chat : chat_manager.getChats()) {
			assertEquals("There should be " + Integer.toString(USERS_PER_CHAT) +
					" users in chat " + chat.getName() + ", but the value is " +
					chat.getUsers().size(),
					chat.getUsers().size(), USERS_PER_CHAT);
		}
	}

	@Test
	public void GivenAnExistingChatWhenUserExitsThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int USERS_PER_CHAT = 3;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);

		// Crear 1 chat y eliminar un usuario
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables =
					createUniqueChatAndRemoveOneUserCallableList(chat_manager, THREAD_AMOUNT, USERS_PER_CHAT);
			for (Callable<Boolean> callable : callables) {
				taskCompletionService.submit(callable);
			}
			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskCompletionService.take();
				assertTrue("Task: " + Integer.toString(i) +
						" should have been completed correctly, but it has not", result.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorService.shutdown();

		// Asegurar que hay un usuario menos por chat
		for(Chat chat : chat_manager.getChats()) {
			assertEquals("There should be " + Integer.toString(USERS_PER_CHAT-1) +
					" users in chat " + chat.getName() + ", but the value is " +
					chat.getUsers().size(),
					chat.getUsers().size(), USERS_PER_CHAT-1);
		}

	}

	@Test
	public void GivenAnExistingChatWhenUserSendsMessageThenNoErrorOccurs() {
		final int THREAD_AMOUNT = 2;
		final int USERS_PER_CHAT = 3;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);

		// Crear 1 chat con varios usuarios y chequear que el envío de
		// un mensaje llega a todos ellos
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables =
					createUniqueChatWithMultipleUsersAndSendMessage(chat_manager, THREAD_AMOUNT, USERS_PER_CHAT);
			for (Callable<Boolean> callable : callables) {
				taskCompletionService.submit(callable);
			}
			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskCompletionService.take();
				assertTrue("Task: " + Integer.toString(i) +
						" should have been completed correctly, but it has not", result.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorService.shutdown();

		// Las propias tareas redefinidas en los usuarios chequean el mensaje
		// Únicamente se chequea que el chat_manager tiene los chats y usuarios esperados
		assertEquals("There should be chats in chat manager, but the value is " +
				chat_manager.getChats().size(),
				chat_manager.getChats().size(), THREAD_AMOUNT);
		for(Chat chat : chat_manager.getChats()) {
			assertEquals("There should be " + Integer.toString(USERS_PER_CHAT) +
					" users in chat " + chat.getName() + ", but the value is " +
					chat.getUsers().size(),
					chat.getUsers().size(), USERS_PER_CHAT);
		}


	}

}
