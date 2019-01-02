package es.sidelab.webchat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import es.codeurjc.webchat.ChatManager;

public class UserCreateChatOnMaxChatCapacityWithTimeout implements Callable<Boolean> {
	private int thrNum;
	private ChatManager chatManager;
	private int timeoutMs;
	public UserCreateChatOnMaxChatCapacityWithTimeout(int thread_number, ChatManager chat_manager,
			int timeout_ms) {
		thrNum = thread_number;
		chatManager = chat_manager;
		timeoutMs = timeout_ms;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			chatManager.newChat("chat-thr" + Integer.toString(thrNum),
				timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
