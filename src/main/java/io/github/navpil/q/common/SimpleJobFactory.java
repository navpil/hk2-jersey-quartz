package io.github.navpil.q.common;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleJobFactory implements JobFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleJobFactory.class);
    private final Map<String, Supplier<? extends Job>> factories = new HashMap<>();

    public <T extends Job, J extends T> void register(Class<T> clazz, Supplier<J> supplier) {
        factories.put(clazz.getName(), supplier);
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            String className = jobClass.getName();
            LOG.debug("Producing instance of Job '" + jobDetail.getKey() + "', class=" + className);

            if (factories.containsKey(className)) {
                return factories.get(className).get();
            }

            return jobClass.newInstance();

        } catch (Exception e) {
            SchedulerException se = new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "'", e);
            throw se;
        }

    }

}