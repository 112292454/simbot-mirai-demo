package love.simbot.example;

import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.timer.Cron;
import love.forte.simbot.timer.EnableTimeTask;
import org.quartz.JobExecutionContext;


import java.util.Date;

@Beans
@EnableTimeTask
public class BotQuartzTask {
	@Depend
	MyProduce myProduce;

	@Cron(value = "0 * * * * ?")
	public void task1(){
		System.out.println("当前一分钟内发言过的群数量："+myProduce.sendedTimes.size());
		myProduce.sendedTimes.clear();
		myProduce.warned.clear();
		myProduce.min=System.currentTimeMillis()/60000;
		System.out.println("quartz task     " + new Date());
	}

	@Cron(value = "0 0 0/4 * * ?")
	public void task2(){
		myProduce.localPicSended.clear();
	}

}
