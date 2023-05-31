package io.github.navpil.q.quartz;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BooksQuartzJobExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(BooksQuartzJobExecutor.class);
    private Scheduler scheduler;
    private final JobFactory jobFactory;

    /**
     * These two booleans are only used because this is test project code
     * In real code one would either manually call .start() or rely on @PostConstruct, not both
     */
    private final AtomicBoolean startCalled = new AtomicBoolean(false);
    /**
     * These two booleans are only used because this is test project code
     * In real code one would either manually call .shutdown() or rely on @PreDestroy, not both
     */
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    @Inject
    public BooksQuartzJobExecutor(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    @PostConstruct
    public void start() {
        if (startCalled.getAndSet(true)) {
            LOG.info("Service was already started by other means");
            return;
        }
        //We have to use prefix for names, because we start several Quartz jobs in the same JVM
        String prefix = UUID.randomUUID() + ".";
        LOG.info("Starting with prefix " + prefix);
        try {
            // Grab the Scheduler instance from the Factory
            // By initializing factory manually and not by quartz.properties we may
            //  have several HK2/Jersey/Quartz contexts in the same application
            Properties props = new Properties();
            props.put("org.quartz.scheduler.instanceName", prefix + "LastUserQuartzJobExecutor");
            props.put("org.quartz.threadPool.threadCount", "3");
            props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
            StdSchedulerFactory factory = new StdSchedulerFactory(props);
            scheduler = factory.getScheduler();

            // and start it off
            scheduler.start();

            if (jobFactory != null) {
                scheduler.setJobFactory(jobFactory);
            }

            // define the job and tie it to our HelloJob class
            JobDetail job = JobBuilder.newJob(UpdateBookServiceJob.class)
                    .withIdentity(prefix + "job1", prefix + "group1")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(prefix + "trigger1", prefix + "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(2)
                            .repeatForever())
                    .build();

            //An alternative way to create the same trigger
            CronTriggerImpl cronTrigger = new CronTriggerImpl();
            cronTrigger.setName(prefix + "every-other-second-trigger");
            cronTrigger.setGroup(prefix + "group1");
            swallow(() -> cronTrigger.setCronExpression("0/2 * * * * ? *"));
            //... which is here only for demonstration, since it's not used

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    interface ExceptionalRunnable {
        void run() throws Exception;
    }

    private void swallow(ExceptionalRunnable r) {
        try {
            r.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (shutdownCalled.getAndSet(true)) {
            LOG.info("Service was already stopped by other means");
            return;
        }
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

}
