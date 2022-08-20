package love.simbot.example.quartz;

import love.simbot.example.service.MyProduce;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class GroupPicTask extends QuartzJobBean {
	@Autowired
	MyProduce myProduce;

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		myProduce.localPicSended.clear();
	}
}
