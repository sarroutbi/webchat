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
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import es.codeurjc.webchat.ChatManager;

public class Mejora6_Test {
	private final int MAX_CHAT_NUMBER = 10;

	// dummy helper
	public List<Callable<Boolean>> createChatWhenNoPlaceCallableList(ChatManager chat_manager,
			int users_amount, int timeout_ms) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserCreateChatOnMaxChatCapacityWithTimeout(i, chat_manager, timeout_ms));
		}
		return callables;
	}

	// dummy helper
	public List<Callable<Boolean>> closeChatCallableList(ChatManager chat_manager,
			int users_amount) {
		List<Callable<Boolean>> callables = new ArrayList<>();
		for (int i = 0; i < users_amount; i++) {
			callables.add(new UserCloseChat(chat_manager, "chat-thr" + Integer.toString(i)));
		}
		return callables;
	}

	@Test
	public void GivenAChatManagerWithMaximumAmountOfChatsWhenTimeoutExpiresThenExceptionIsThrown() {
		final int THREAD_AMOUNT = 2;
		final int TIMEOUT_CHAT_CREATION_MS = 2000;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);
		// Fill chat manager with chats
		for (int counter_chat = 0; counter_chat < MAX_CHAT_NUMBER; counter_chat++) {
			try {
				chat_manager.newChat("chat" + Integer.toString(counter_chat),
						TIMEOUT_CHAT_CREATION_MS, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// N threads
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);
		try {
			List<Callable<Boolean>> callables = createChatWhenNoPlaceCallableList(chat_manager, THREAD_AMOUNT,
					TIMEOUT_CHAT_CREATION_MS);
			for (Callable<Boolean> callable : callables) {
				taskCompletionService.submit(callable);
			}
			// Wait the time and check if it went OK
			Thread.sleep(TIMEOUT_CHAT_CREATION_MS);
			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskCompletionService.take();
				assertEquals("Task: " + Integer.toString(i) +
						" should have not completed correctly, but it has", result.get(), false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorService.shutdown();
	}

	@Test
	public void GivenAChatManagerWithMaximumAmountOfChatsWhenChatIsClosedThenChatIsCreated() {
		final int THREAD_AMOUNT = 2;
		final int TIMEOUT_CHAT_CREATION_MS = 2000;
		ChatManager chat_manager = new ChatManager(MAX_CHAT_NUMBER);
		// Fill chat manager with chats
		for (int counter_chat = 0; counter_chat < MAX_CHAT_NUMBER; counter_chat++) {
			try {
				chat_manager.newChat("chat-thr" + Integer.toString(counter_chat),
						TIMEOUT_CHAT_CREATION_MS, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// N threads open a chat
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskCompletionService = new ExecutorCompletionService<Boolean>(
				executorService);

		// N threads close a chat
		ExecutorService executorCloseService = Executors.newFixedThreadPool(THREAD_AMOUNT);
		CompletionService<Boolean> taskClosureService = new ExecutorCompletionService<Boolean>(
				executorCloseService);
		try {
			List<Callable<Boolean>> callables = createChatWhenNoPlaceCallableList(chat_manager, THREAD_AMOUNT,
					TIMEOUT_CHAT_CREATION_MS);
			List<Callable<Boolean>> callablesClose = closeChatCallableList(chat_manager, THREAD_AMOUNT);

			for (Callable<Boolean> callable : callables) {
				taskCompletionService.submit(callable);
			}
			for (Callable<Boolean> callable : callablesClose) {
				taskClosureService.submit(callable);
			}
			// Wait the time and check if it went OK
			Thread.sleep(TIMEOUT_CHAT_CREATION_MS);

			for (int i = 0; i < callables.size(); i++) {
				Future<Boolean> result = taskCompletionService.take();
				assertTrue("Task: " + Integer.toString(i) +
						" should have completed correctly, but it has not", result.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		executorService.shutdown();
	}

}
