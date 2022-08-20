package love.simbot.example.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.Job;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class BatchController {
	// JobLauncher 由框架提供
	@Autowired
	JobLauncher jobLauncher;

	// Job 为刚刚配置的
	@Autowired
	Job importJob;

	//@Autowired
	//public JobParameters jobParameters;

	public String batch(String path,String kind) throws Exception{
		 JobParameters jobParameters = new JobParametersBuilder()
				.addString("path", path)
				.addString("kind", kind)
				.addString("date", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()))
				.toJobParameters();
		// 通过调用 JobLauncher 中的 run 方法启动一个批处理
		jobLauncher.run(importJob, jobParameters);
		return "成功运行批处理："+path;
	}
}
