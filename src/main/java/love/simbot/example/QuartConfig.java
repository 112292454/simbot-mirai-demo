package love.simbot.example;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartConfig {
	@Bean
	public JobDetail teatQuartzDetail(){
		return JobBuilder.newJob(QuartzTask.class).withIdentity("quartztask").storeDurably().build();
	}
	@Bean
	public Trigger testQuartzTrigger(){
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInSeconds(1)  //设置时间周期单位秒
				.repeatForever();
		return TriggerBuilder.newTrigger().forJob(teatQuartzDetail())
				.withIdentity("quartztask")
				.withSchedule(scheduleBuilder)
				.build();
	}
}
