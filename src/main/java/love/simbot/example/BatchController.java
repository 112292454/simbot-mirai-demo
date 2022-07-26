package love.simbot.example;

import love.forte.common.ioc.annotation.Depend;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.Job;

public class BatchController {
	// JobLauncher 由框架提供
	@Autowired
	@Depend
	JobLauncher jobLauncher;

	// Job 为刚刚配置的
	@Autowired
	@Depend
	Job importJob;
	@Autowired
	@Depend
	JobParameters jobParameters;

	public String batch(String path,String kind) throws Exception{
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("path", path)
				.addString("kind", kind)
				.toJobParameters();
		// 通过调用 JobLauncher 中的 run 方法启动一个批处理
		jobLauncher.run(importJob, jobParameters);
		return "成功运行批处理："+path;
	}
}
