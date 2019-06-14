package com.happyzleaf.pixelgenocide.util;

import org.spongepowered.api.scheduler.Task;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * NOTE: I lost too many hours on this, and I don't feel like I like it.
 * This is the conventional way to do things, but it's way too abstracted, just like everything from Sponge.
 *
 * NOTE 2: Copied from an old project of mine
 *
 * @author happyzleaf
 * @since 13/01/2019
 */
public class TimedTask {
	public static TimedTask.Builder builder() {
		return new Builder(Task.builder());
	}
	
	public interface EndNotifier {
		/**
		 * @return True if the task can be cancelled, false if it has to restart.
		 */
		boolean end();
	}
	
	public interface TimeBroadcaster {
		void broadcast(long remainingSeconds);
		
		int getRate(long remainingSeconds);
	}
	
	private static class Updater implements Consumer<Task> {
		private GameTime time;
		private long remainingSeconds;
		
		private EndNotifier notifier;
		private TimeBroadcaster broadcaster;
		
		private Updater(GameTime time, @Nullable EndNotifier notifier, @Nullable TimeBroadcaster broadcaster) {
			this.time = time;
			this.remainingSeconds = time.toSeconds();
			
			this.notifier = notifier;
			this.broadcaster = broadcaster;
		}
		
		@Override
		public void accept(Task task) {
			if (remainingSeconds > 0) {
				// or if (broadcaster != null) broadcaster.broadcast(remainingSeconds);
				if (broadcaster != null && remainingSeconds % broadcaster.getRate(remainingSeconds) == 0) {
					broadcaster.broadcast(remainingSeconds);
				}
				remainingSeconds--;
			} else {
				if (notifier == null || notifier.end()) {
					task.cancel();
				} else {
					remainingSeconds = time.toSeconds();
				}
			}
		}
	}
	
	@SuppressWarnings("NullableProblems") // The implementation will take care for us
	public static class Builder implements Task.Builder {
		private final Task.Builder parent;
		
		private GameTime time = null;
		private EndNotifier notifier = null;
		private TimeBroadcaster broadcaster = null;
		
		private Builder(Task.Builder parent) {
			this.parent = parent;
		}
		
		public Builder time(GameTime time) {
			checkArgument(time.toSeconds() > 1, "The time cannot be below 1 second.");
			this.time = checkNotNull(time, "time");
			return this;
		}
		
		public Builder notifier(@Nullable EndNotifier notifier) {
			this.notifier = notifier;
			return this;
		}
		
		public Builder broadcaster(@Nullable TimeBroadcaster broadcaster) {
			this.broadcaster = broadcaster;
			return this;
		}
		
		@Override
		public Builder from(Task value) {
			parent.from(value);
			if (value.getConsumer() instanceof Updater) {
				Updater u = (Updater) value.getConsumer();
				time = u.time;
				notifier = u.notifier;
				broadcaster = u.broadcaster;
				// doesn't copy Updater#remainingSeconds, but it's worth being updated only if it's actually used.
			}
			return this;
		}
		
		@Override
		public Builder reset() {
			parent.reset();
			time = null;
			notifier = null;
			broadcaster = null;
			// doesn't reset Updater#remainingSeconds, same as Builder#from(Task)
			return this;
		}
		
		@Override
		public Builder async() {
			parent.async();
			return this;
		}
		
		@Override
		public Builder execute(Consumer<Task> executor) {
			parent.execute(executor);
			return this;
		}
		
		@Override
		public Task.Builder execute(Runnable runnable) {
			parent.execute(runnable);
			return this;
		}
		
		@Override
		public Builder delay(long delay, TimeUnit unit) {
			parent.delay(delay, unit);
			return this;
		}
		
		@Override
		public Builder delayTicks(long ticks) {
			parent.delayTicks(ticks);
			return this;
		}
		
		@Override
		public Builder interval(long interval, TimeUnit unit) {
			parent.interval(interval, unit);
			return this;
		}
		
		@Override
		public Builder intervalTicks(long ticks) {
			parent.intervalTicks(ticks);
			return this;
		}
		
		@Override
		public Builder name(String name) {
			parent.name(name);
			return this;
		}
		
		@Override
		public Task submit(Object plugin) {
			checkArgument(time != null, "You must set the time limit. See TimedTask.Builder#time(GameTime)");

//			parent.delay(1, TimeUnit.SECONDS);
			parent.interval(1, TimeUnit.SECONDS);
			parent.execute(new Updater(time, notifier, broadcaster));
			return parent.submit(plugin);
		}
	}
}
