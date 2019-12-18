package com.colcamex.www.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import com.colcamex.www.bean.ControllerBean;

import static java.util.concurrent.TimeUnit.*;

/**
 * This class starts and controls two threads: 
 * 	ExcellerisController
 * 	ScheduledExecuterService
 * 
 * It's best to ensure that these threads are destroyed when 
 * not in use.
 * 
 * @author dennis warren
 *
 */
public class PollTimer {
	
	// available services 
	
	public static final int DEFAULT_POLL_INTERVAL = 7200;
	
	// polling frequencies
	private static final Integer INITIAL_DELAY = (60*30); // download after 10 minutes of start.
	private static final int TIME_CHECK_INTERVAL = 60; //seconds
	
	private static Logger logger = Logger.getLogger(PollTimer.class);

	private static Date lastRun;
	private static Date nextRun;
	private static int lastMethod;
	private static Calendar[] pollTimes;
	private static int pollFrequency;
	private static boolean hostStatus;

    private static ScheduledExecutorService scheduler;
    
	public static synchronized ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public static void setScheduler(ScheduledExecutorService scheduler) {
		PollTimer.scheduler = scheduler;
	}

	public static boolean isHostStatus() {
		return hostStatus;
	}

	public static void setHostStatus(boolean hostStatus) {
		PollTimer.hostStatus = hostStatus;
	}

	public static synchronized Date getLastRun() {
		return lastRun;
	}

	public static void setLastRun(Date lastRun) {
		PollTimer.lastRun = lastRun;
	}

	public static synchronized Date getNextRun() {
		return nextRun;
	}

	private static void setNextRun(Date timeStamp) {
		
		if(timeStamp != null) {

			Calendar timestamp = PollTimer.getTimestamp(timeStamp);
			
			int lastmethod = getLastMethod();

			if(lastmethod == ControllerBean.FREQUENCY) {
				timestamp.add(Calendar.SECOND, getPollFrequency());
				nextRun = timestamp.getTime();

			}
			
			if(lastmethod == ControllerBean.TIME_OF_DAY) {
				
				Calendar[] times = getPollTimes();
				Calendar lastTime = timestamp;

				for(int i = 0; times.length > i; i++) {
					
					Calendar time = times[i];

					if(time != null) {
						// if its after the current time
						if(time.after(timestamp)) { 
							if(time.before(lastTime)) {
								nextRun = time.getTime();
							}				
						}
					}
					
					lastTime = time;		
				}
	
			}
		} else {
			nextRun = null;
		}
		
		logger.debug("Next run is at: "+nextRun);
		
	}

	public static synchronized int getLastMethod() {
		return PollTimer.lastMethod;
	}

	private static void setLastMethod(int lastMethod) {
		PollTimer.lastMethod = lastMethod;
	}

	/**
	 * Returns the current run status of this scheduler.
	 * @return true if running.
	 */
	public static synchronized boolean isRunning() {

		if(scheduler != null) {
			return (! scheduler.isShutdown());
		}  
	
		return false;
	}

	public static synchronized Calendar[] getPollTimes() {
		return pollTimes;
	}

	public static void setPollTimes(Calendar[] pollTimes) {
		PollTimer.pollTimes = pollTimes;
	}

	public static synchronized int getPollFrequency() {
		return PollTimer.pollFrequency;
	}

	public static void setPollFrequency(int pollFrequency) {
		PollTimer.pollFrequency = pollFrequency;
	}

	/**
	 * Start a service
	 * 
	 * @param method
	 * @param interval
	 */
	public static void start(ServiceExecuter services, int method, Object interval) {		
		
		logger.info("Poll Setting: " + method);		
		logger.info("Poll interval(s): " + interval);
		
		if(method == ControllerBean.FREQUENCY) {
			// run timer as frequency.
			PollTimer.frequencyPoll(services, (Integer) interval);
		}
		
		if(method == ControllerBean.TIME_OF_DAY) {
			PollTimer.setPollTimes((Calendar[]) interval);
			// run timer as time of day.
			PollTimer.timeofdayPoll(services, (Calendar[]) interval);
		}
		
		if(PollTimer.isRunning()) {
			Date startTime = new Date(System.currentTimeMillis());
			logger.info("Poll timer successfully started on " + startTime);
		}
	
	}

	/**
	 * If the scheduler is running this will shut it down.
	 */
	public static List<Runnable> stop() {
		
		logger.info("Poll timer stopped by user on " + new Date());
		return scheduler.shutdownNow();						
	}

    /**
     * Set this scheduler to run download service at a set 
     * interval of time.
     * 
     * @param frequency
     */
	private static void frequencyPoll(ServiceExecuter services, int frequency) {

		if(scheduler != null) {
			scheduler.shutdownNow();
		}

		scheduler = Executors.newScheduledThreadPool(2);

        if(frequency > 0) {       	        	
			scheduler.scheduleWithFixedDelay(
					services, // action to trigger. 
					frequency, //INITIAL_DELAY,
					frequency, // trigger delay period
					SECONDS
			);
			
        }
	}
	
	/**
	 * Poll the server with a selection of specific times.
	 */
	private static synchronized void timeofdayPoll(ServiceExecuter services, Calendar[] interval) {
		
		//find the first run time from now
		Calendar now = getTimestamp();
		Calendar firstrun = now;
		
		if(interval.length > 0) {

			for (int i = 0; interval.length > i; i++) {
				
				Calendar time = interval[i];

				if(time != null) {
					if( time.after(now) ) {
						if(firstrun.before(time)) {
							firstrun = time;
							logger.info("Setting first run time to: "+time.getTime());
						}
					}
				}
			}
			
			// if no times qualified then pick the lowest time and add 24 hours.
			if(firstrun.equals(now)) {
				for (int i = 0; interval.length > i; i++) {
					
					Calendar time = interval[i];
					if(time.before(firstrun)) {
												
						firstrun = time;		
					}
				
				}
				firstrun.add(Calendar.DATE, 1);
			}
		}
		
        PollTimer.nextRun = firstrun.getTime();
		
		if(scheduler != null) {
			scheduler.shutdownNow();
		}
		
		scheduler = Executors.newScheduledThreadPool(1);
		Runnable checkTime = new CheckTime(services, interval);
		
		scheduler.scheduleWithFixedDelay(
			checkTime,
			INITIAL_DELAY,
			TIME_CHECK_INTERVAL,
			SECONDS				
		);

	}
	
	/**
	 * Checks the time array at a predetermined interval of time 
	 * if any of the times match then the down-loader is triggered.
	 * @author dennis warren
	 *
	 */
	private static final class CheckTime implements Runnable {

		private Calendar[] interval;
		private ServiceExecuter services;

		public CheckTime(ServiceExecuter services, Calendar[] interval) {
			this.interval = interval;
			this.services = services;
		}

		public synchronized void run() {
			Thread thread = Thread.currentThread();
			logger.debug("Running CheckTime [" + thread.getName() + " (" + thread.getId() + ")]");	

			Calendar now = PollTimer.getTimestamp();

			logger.debug("Checking current time against "+interval.length+" time choices.");

			for (int i = 0; interval.length > i; i++) {
				Calendar time = interval[i];

				if (time != null){
					
					if(time.isSet(Calendar.HOUR_OF_DAY)
							&&(time.isSet(Calendar.MINUTE)) ){
							
						logger.debug("Comparing time now: " + 
								now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + 
								" versus required time: " + 
								time.get(Calendar.HOUR_OF_DAY) + ":" + time.get(Calendar.MINUTE)
						);
	
						if ((time.get(Calendar.HOUR_OF_DAY) == now
								.get(Calendar.HOUR_OF_DAY))
								&& (time.get(Calendar.MINUTE) ==
								now.get(Calendar.MINUTE)) )
						{
							if(services != null) {
								services.run();
							}

							logger.debug("Time match found deploying downloader.");
						}

					}
				}
			}
			logger.debug("Time match not found.");
		}
	}
	
	public static Calendar getTimestamp(Date time) {
		Calendar now = getTimestamp();
		now.setTime(time);
		return now;
	}
	
	public static Calendar getTimestamp() {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		return now;
	}
	
	
}
