package com.example.sidequesting.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** Store for user sessions. */
@Service
public class UserSessionStore {

  private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

  /**
   * Gets or creates a user session.
   *
   * @param userId the ID of the user
   * @return the user session
   */
  public UserSession getOrCreate(long userId) {
    return sessions.computeIfAbsent(userId, id -> new UserSession(BotState.IDLE));
  }

  /**
   * Gets an existing user session.
   *
   * @param userId the ID of the user
   * @return an optional containing the session if found
   */
  public Optional<UserSession> get(long userId) {
    return Optional.ofNullable(sessions.get(userId));
  }

  /**
   * Puts a user session into the store.
   *
   * @param userId the ID of the user
   * @param session the session to store
   */
  public void put(long userId, UserSession session) {
    sessions.put(userId, session);
  }

  /**
   * Removes a user session from the store.
   *
   * @param userId the ID of the user
   */
  public void remove(long userId) {
    sessions.remove(userId);
  }

  /**
   * Represents a user session.
   *
   * @param state the current state of the bot for the user
   * @param data the data stored in the session
   */
  public record UserSession(BotState state, Map<String, String> data) {
    /**
     * Constructs a session with a state and empty data.
     *
     * @param state the initial state
     */
    public UserSession(BotState state) {
      this(state, Map.of());
    }

    /**
     * Returns a new session with the updated state.
     *
     * @param newState the new state
     * @return a new UserSession instance
     */
    public UserSession withState(BotState newState) {
      return new UserSession(newState, data);
    }

    /**
     * Returns a new session with added or updated data.
     *
     * @param key the data key
     * @param value the data value
     * @return a new UserSession instance
     */
    public UserSession withData(String key, String value) {
      var updated = new java.util.HashMap<>(data);
      updated.put(key, value);
      return new UserSession(state, Map.copyOf(updated));
    }

    /**
     * Retrieves data from the session.
     *
     * @param key the data key
     * @return the data value or an empty string if not found
     */
    public String getData(String key) {
      return data.getOrDefault(key, "");
    }
  }

  /** Possible bot states for onboarding flow. */
  public enum BotState {
    IDLE,
    AWAITING_LANGUAGE,
    AWAITING_DIFFICULTY,
    AWAITING_LOCATION,
    AWAITING_RANGE
  }
}
