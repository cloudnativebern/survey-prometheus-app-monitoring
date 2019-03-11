package ch.puzzle.cncfmeetupdemo;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
@RequestMapping(value = "/api/clicks")
public class ClickController {

	private final Counter meetupThumbsUpCounter;
	private final Counter meetupThumbsDownCounter;
	private final AtomicInteger	meetupClicks;	

	@Autowired
	public ClickController(MeterRegistry meterRegistry) {
		this.meetupThumbsUpCounter = meterRegistry.counter("meetup.thumbs.up.count");
		this.meetupThumbsDownCounter = meterRegistry.counter("meetup.thumbs.down.count");
		this.meetupClicks = meterRegistry.gauge("meetup.thumbs.total", new AtomicInteger(0));
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value="/up")
	public void postUp() {
		meetupThumbsUpCounter.increment();
		meetupClicks.incrementAndGet();
	}
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value="/down")
	public void postDown() {
		meetupThumbsDownCounter.increment();
		meetupClicks.decrementAndGet();
	}

	@GetMapping
	public Metrics getAll() {
		return new Metrics(meetupThumbsUpCounter.count(), meetupThumbsDownCounter.count(), meetupClicks.get());
	}
	
	public class Metrics {
		
		private double thumbsUp;
		private double thumbsDown;
		private int clicks;
		public Metrics(double thumbsUp, double thumbsDown, int clicks) {
			super();
			this.thumbsUp = thumbsUp;
			this.thumbsDown = thumbsDown;
			this.clicks = clicks;
		}
		public double getThumbsUp() {
			return thumbsUp;
		}
		public double getThumbsDown() {
			return thumbsDown;
		}
		public int getClicks() {
			return clicks;
		}
	}
}
