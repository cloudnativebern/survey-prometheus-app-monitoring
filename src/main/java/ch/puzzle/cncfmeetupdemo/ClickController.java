package ch.puzzle.cncfmeetupdemo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

@RestController
@RequestMapping(value = "/api/clicks")
public class ClickController {

	private final Counter meetupThumbsUpCounter;
	private final Counter meetupThumbsDownCounter;
	private final Counter meetupClicks;
	
	private final Map<String, AtomicInteger> gauges;
	private final MeterRegistry meterRegistry;
	

	@Autowired
	public ClickController(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		gauges = new HashMap<>();
//		gauges.put("serverless", meterRegistry.gauge("meetup.thumbs", Arrays.asList(Tag.of("thumb", "serverless")), new AtomicInteger(0)));
//		gauges.put("security", meterRegistry.gauge("meetup.thumbs", Arrays.asList(Tag.of("thumb", "secuirty")), new AtomicInteger(0)));
		
		this.meetupThumbsUpCounter = meterRegistry.counter("meetup.thumbs.up.count");
		this.meetupThumbsDownCounter = meterRegistry.counter("meetup.thumbs.down.count");
		this.meetupClicks = meterRegistry.counter("meetup.thumbs.total");
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value="/up/{voter}")
	public void postUp(@PathVariable("voter") String voter) {
		if(!gauges.containsKey(voter)) {
			gauges.put(voter, meterRegistry.gauge("meetup.thumbs", Arrays.asList(Tag.of("thumb", "serverless")), new AtomicInteger(0)));
		}
		gauges.get(voter).incrementAndGet();
		
		meetupThumbsUpCounter.increment();
		meetupClicks.increment();
	}
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value="/down/{voter}")
	public void postDown(@PathVariable("voter") String voter) {
		if(!gauges.containsKey(voter)) {
			gauges.put(voter, meterRegistry.gauge("meetup.thumbs", Arrays.asList(Tag.of("thumb", "serverless")), new AtomicInteger(0)));
		}
		
		gauges.get(voter).decrementAndGet();

		meetupThumbsDownCounter.increment();
		meetupClicks.increment();
	}

	@GetMapping
	public Metrics getAll() {
		return new Metrics(meetupThumbsUpCounter.count(), meetupThumbsDownCounter.count(), meetupClicks.count(), gauges);
	}
	
	public class Metrics {
		
		private double thumbsUp;
		private double thumbsDown;
		private double clicks;
		private Map<String, AtomicInteger> gauges;
		public Metrics(double thumbsUp, double thumbsDown, double clicks, Map<String, AtomicInteger> gauges) {
			super();
			this.thumbsUp = thumbsUp;
			this.thumbsDown = thumbsDown;
			this.clicks = clicks;
			this.gauges = gauges;
		}
		public double getThumbsUp() {
			return thumbsUp;
		}
		public double getThumbsDown() {
			return thumbsDown;
		}
		public double getClicks() {
			return clicks;
		}
		public Map<String, AtomicInteger> getGauges() {
			return gauges;
		}
	}
}
