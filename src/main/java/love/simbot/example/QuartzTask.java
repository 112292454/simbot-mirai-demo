package love.simbot.example;

import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.timer.Cron;
import love.forte.simbot.timer.EnableTimeTask;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;

@Beans
@EnableTimeTask
public class QuartzTask extends QuartzJobBean {
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		System.out.println("quartz task" + new Date());
	}

	@Cron(value = "0/1 * * * * ?")
	public void task1(){
		System.out.println("quartz task" + new Date());
	}
}
