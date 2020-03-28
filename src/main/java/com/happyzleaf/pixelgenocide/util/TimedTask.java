package com.happyzleaf.pixelgenocide.util;

import com.google.common.reflect.TypeToken;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author happyzleaf
 * @since 22/03/2020
 */
public class TimedTask {
	private final Runnable runnable;
	private final Consumer<Long> broadcaster;

	private GameTime duration = null;
	private Set<Long> cache = new HashSet<>();

	private long remainingSeconds;
	private UUID task = null;

	public TimedTask(Runnable runnable, Consumer<Long> broadcaster) {
		this.broadcaster = broadcaster;
		this.runnable = runnable;
	}

	public void setDuration(GameTime duration, Info info) throws RuntimeException {
		if (task != null) {
			throw new RuntimeException("The task must be cancelled before setting a new duration.");
		}

		cache.clear();
		for (long s = duration.toSeconds(); s > 0; s--) {
			int rate = info.getRate(s);
			if (rate != 0 && s % rate == 0) {
				cache.add(s);
			}
		}

		this.duration = duration;
	}

	public void start(Object plugin) {
		cancel();

		if (duration == null) {
			throw new RuntimeException("You must provide a duration before starting the task.");
		}

		remainingSeconds = duration.toSeconds();
		task = Task.builder()
				.interval(1, TimeUnit.SECONDS)
				.execute(() -> {
					if (remainingSeconds <= 0) {
						runnable.run();

						remainingSeconds = duration.toSeconds();
					} else {
						if (cache.contains(remainingSeconds)) {
							broadcaster.accept(remainingSeconds);
						}

						remainingSeconds--;
					}
				})
				.submit(plugin).getUniqueId();
	}

	public void cancel() {
		if (task != null) {
			Sponge.getScheduler().getTaskById(task).ifPresent(Task::cancel);

			task = null;
		}
	}

	public long getRemainingSeconds() {
		return remainingSeconds;
	}

	public static class Info {
		private static final ScriptEngine ENGINE = new ScriptEngineManager(null).getEngineByName("Nashorn");

		private final String source;
		private ScriptObjectMirror script;

		public Info(String source) throws IllegalArgumentException {
			if (source == null || source.isEmpty()) {
				throw new IllegalArgumentException("The script is missing or empty.");
			}

			try {
				script = (ScriptObjectMirror) ((Compilable) ENGINE).compile("function (s) { return " + source + "; }").eval();
			} catch (ScriptException e) {
				throw new IllegalArgumentException(e);
			}

			this.source = source;
		}

		public int getRate(long s) throws IllegalArgumentException {
			Object result = script.call(null, s);
			if (!(result instanceof Number)) {
				throw new IllegalArgumentException("The timer script didn't return a proper number.");
			}

			int rate = ((Number) result).intValue();
			if (rate < 0) {
				throw new IllegalArgumentException("The timer rate cannot be negative.");
			}

			return rate;
		}
	}

	public static class InfoSerializer implements TypeSerializer<Info> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable Info obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The object is null.");
			}

			value.setValue(obj.source);
		}

		@Nullable
		@Override
		public Info deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			try {
				return new Info(value.getString());
			} catch (IllegalArgumentException e) {
				throw new ObjectMappingException(e);
			}
		}
	}
}
