package io.github.navpil.q.common;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.glassfish.hk2.api.ServiceLocator;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HK2JobFactory implements JobFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HK2JobFactory.class);
    private final ServiceLocator serviceLocator;

    @Inject
    public HK2JobFactory(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            LOG.debug("Producing instance of Job '" + jobDetail.getKey() + "', class=" + jobClass.getName());

            Job job = serviceLocator.getService(jobClass);
            if (job == null) {
                LOG.debug("Unable to instantiate job via ServiceLocator, returning unmanaged instance.");
                return jobClass.newInstance();
            }
            return job;

        } catch (Exception e) {
            SchedulerException se = new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "'", e);
            return jobExecutionContext -> {};
        }

    }

}