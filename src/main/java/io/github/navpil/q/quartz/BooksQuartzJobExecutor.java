package io.github.navpil.q.quartz;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.glassfish.hk2.api.Immediate;
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

import java.util.Properties;

@Immediate
public class BooksQuartzJobExecutor {

    private Scheduler scheduler;
    private final JobFactory jobFactory;

    @Inject
    public BooksQuartzJobExecutor(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    @PostConstruct
    public void start() {
        try {
            // Grab the Scheduler instance from the Factory
            // By initializing factory manually and not by quartz.properties we may
            //  have several HK2/Jersey/Quartz contexts in the same application
            Properties props = new Properties();
            props.put("org.quartz.scheduler.instanceName", "LastUserQuartzJobExecutor");
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
                    .withIdentity("job1", "group1")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(2)
                            .repeatForever())
                    .build();

            //An alternative way to create the same trigger
            CronTriggerImpl cronTrigger = new CronTriggerImpl();
            cronTrigger.setName("every-other-second-trigger");
            cronTrigger.setGroup("group1");
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
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

}
